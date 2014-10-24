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
import java.util.Random;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;

public class ConjugalNetworkJoinThread extends Thread
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final static int connectionIntervalBase = 600 * 1000;

	private final MalePeerToPeerNode peerToPeerNode;
	private final Random random;

	private boolean shutdown;

	public ConjugalNetworkJoinThread(MalePeerToPeerNode peerToPeerNode)
	{
		super("ConjugalNetworkJoinThread " + peerToPeerNode.getNodeUuid());
		setDaemon(true);
		this.peerToPeerNode = peerToPeerNode;
		this.random = new Random();

		this.shutdown = false;
	}

	public MalePeerToPeerNode getPeerToPeerNode()
	{
		return peerToPeerNode;
	}

	private double networkSizeEstimation()
	{
		return peerToPeerNode.networkSizeEstimation();
	}

	private long randomInterval()
	{
		return (long) (networkSizeEstimation() * connectionIntervalBase * -Math.log(1 - random.nextDouble()));
	}

	@Override
	public void run()
	{
		while (!shutdown)
		{
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
			if (shutdown)
				break;
			logger.debug("Joining");
			try
			{
				peerToPeerNode.networkJoin(null);
			}
			catch (ConnectException | InterruptedException | IOException e)
			{
				logger.error("Exception caught", e);
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
