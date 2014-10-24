/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.peertopeer.Hook;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.peertopeer.HookList;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;

public class HookNetworkJoinThread extends Thread
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final static double factor = 0.8;
	private final static int connectionIntervalBase = 600 * 1000;
	private final static int failedConnectionAttemptsCut = 14;

	private final FemalePeerToPeerNode peerToPeerNode;
	private final Random random;

	private boolean shutdown;

	public HookNetworkJoinThread(FemalePeerToPeerNode peerToPeerNode)
	{
		super("HookNetworkJoinThread " + peerToPeerNode.getNodeUuid());
		setDaemon(true);
		this.peerToPeerNode = peerToPeerNode;
		this.random = new Random();
		cutHookListTail();

		this.shutdown = false;
	}

	public FemalePeerToPeerNode getPeerToPeerNode()
	{
		return peerToPeerNode;
	}

	private PersistenceManager getPersistenceManager()
	{
		return peerToPeerNode.getPersistenceManager();
	}

	private void cutHookListTail(Transaction transaction)
	{
		HookList hookList = getPersistenceManager().hookList(transaction);
		for (Hook hook : hookList.tail(Hook.computePriority(new Date(), failedConnectionAttemptsCut)))
			hook.delete(transaction);
	}

	private void cutHookListTail()
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			cutHookListTail(transaction);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}

	}

	private int randomIndex(int n)
	{
		return (int) (Math.log(1 - (1 - Math.pow(factor, n)) * random.nextDouble()) / Math.log(factor));
	}

	private int randomIndex()
	{
		return (int) (Math.log(1 - random.nextDouble()) / Math.log(factor));
	}

	private Hook randomHook(HookList hookList)
	{
		int i = randomIndex();
		while (true)
		{
			Hook hook = hookList.getNullOverflow(i);
			if (hook != null)
				return hook;
			if (i == 0)
				return null;
			i = randomIndex(i);
		}
	}

	private Hook randomHook()
	{
		/*
		 * This loop is a workaround. In seems that the Berkeley DB gets stuck into a dead lock when simulaneously
		 * inserting two hooks and trying to obtain a hook with the cursor-based iterative method
		 * HookList.getNullOverflow(int i). One of the inserts succeeds and the two other operations get stuck.
		 *
		 * I am suspecting that it is a Berkeley DB bug. I should check if this behaviour is still happening when
		 * upgrading to future versions and/or investigate this alleged bug further and comunicate it to the Oracle
		 * people. Tested so far with {je-5.0.97,  je-5.0.103}
		 *
		 * See Test.prova373() to generate the condition .
		 *
		 */
		while (true)
		{
			Transaction transaction = getPersistenceManager().beginTransaction(1000);
			try
			{
				HookList hookList = getPersistenceManager().hookList(transaction);
				return randomHook(hookList);
			}
			catch (PersistenceLockTimeoutException e)
			{
			}
			finally
			{
				transaction.abort();
			}
		}
	}

	private double networkSizeEstimation()
	{
		return peerToPeerNode.networkSizeEstimation();
	}

	private long randomInterval()
	{
		return (long) (networkSizeEstimation() * connectionIntervalBase * -Math.log(1 - random.nextDouble()));
	}

	private void failedConnection(Hook hook)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			hook.failedConnection(transaction);
			cutHookListTail(transaction);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public void run()
	{
		while (!shutdown)
		{
			Hook hook = randomHook();
			if (hook == null)
				logger.warn("No hooks in the database. Must join manually.");
			else
			{
				Set<InetSocketAddress> testedAddresses = new HashSet<InetSocketAddress>();
				int collision = 0;
				while (collision < 5)
				{
					InetSocketAddress address = hook.getInetSocketAddress();
					if (testedAddresses.add(address))
					{
						try
						{
							logger.debug("Joining to address: " + address);
							peerToPeerNode.networkJoin(address);
							break;
						}
						catch (ConnectException | IOException | InterruptedException e)
						{
							logger.warn("Couldn't join to " + address + " - " + e.getMessage(), e);
							failedConnection(hook);
							hook = randomHook();
							if (hook == null)
								break;
						}
					}
					else
					{
						hook = randomHook();
						if (hook == null)
							break;
						collision++;
					}
				}
			}
			synchronized (this)
			{
				if (shutdown)
					break;
				try
				{
					long interval = randomInterval();
					logger.debug("Sleeping for " + ((float) interval) / 1000 + " seconds");
					wait(interval);
				}
				catch (InterruptedException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public synchronized void shutdown() throws InterruptedException
	{
		shutdown = true;
		notifyAll();
		join();
	}

}
