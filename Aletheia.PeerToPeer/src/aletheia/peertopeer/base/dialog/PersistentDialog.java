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
package aletheia.peertopeer.base.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.peertopeer.PeerToPeerConnectionLock.LockTimeoutException;
import aletheia.peertopeer.PeerToPeerNodeProperties;
import aletheia.peertopeer.base.message.LockInitMessage;
import aletheia.peertopeer.base.message.LockRequestMessage;
import aletheia.peertopeer.base.message.LockResponseMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.base.protocol.PersistentMessageProtocol;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;

public abstract class PersistentDialog extends Dialog
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final static float lockConnectionTimeOutBase = 5f; // in secs
	private final static float lockConnectionTimeoutExtra = 1f; // in secs
	private final static float lockMessageTimeOut = PeerToPeerNodeProperties.instance.isDebug() ? 0f : 10f; // in secs
	private final static Random random = new Random();

	private final Transaction transaction;
	private final PersistentMessageProtocol persistentMessageProtocol;

	public PersistentDialog(Phase phase)
	{
		super(phase);
		this.transaction = getPersistenceManager().beginTransaction();
		try
		{
			this.persistentMessageProtocol = new PersistentMessageProtocol(getProtocolVersion(), getPersistenceManager(), this.transaction);
		}
		catch (Throwable t)
		{
			transaction.abort();
			throw t;
		}
	}

	protected Transaction getTransaction()
	{
		return transaction;
	}

	private void maleLockSequence() throws InterruptedException, IOException, ProtocolException
	{
		setRemainingTime(0);
		recvMessage(LockInitMessage.class);
		setRemainingTime((long) (lockMessageTimeOut * 1000));
		while (true)
		{
			getPeerToPeerNode().getPersistentConnectionLock().lock(getPeerToPeerConnection());
			logger.debug("MALE: locked");
			logger.trace("sending LockRequestMessage");
			sendMessage(new LockRequestMessage());
			setRemainingTime((long) (lockMessageTimeOut * 1000));
			logger.trace("receiving LockResponseMessage");
			LockResponseMessage lockResponseMessage = recvMessage(LockResponseMessage.class);
			if (lockResponseMessage.getResponse() == LockResponseMessage.Response.ACKNOWLEDGE)
				break;
			getPeerToPeerNode().getPersistentConnectionLock().unlock(getPeerToPeerConnection());
			logger.debug("MALE: unlocked");
		}
		logger.debug("MALE: sync");
	}

	private void femaleLockSequence() throws IOException, ProtocolException, InterruptedException
	{
		setRemainingTime(0);
		sendMessage(new LockInitMessage());
		while (true)
		{
			logger.trace("receiving LockRequestMessage");
			recvMessage(LockRequestMessage.class);
			try
			{
				logger.debug("FEMALE: locking");
				getPeerToPeerNode().getPersistentConnectionLock().lock(getPeerToPeerConnection(),
						(long) ((lockConnectionTimeOutBase + random.nextFloat() * lockConnectionTimeoutExtra) * 1000));
				logger.debug("FEMALE: acknowledging");
				sendMessage(new LockResponseMessage(LockResponseMessage.Response.ACKNOWLEDGE));
				break;
			}
			catch (LockTimeoutException e)
			{
				logger.debug("FEMALE: refusing");
				sendMessage(new LockResponseMessage(LockResponseMessage.Response.REFUSE));
			}
		}
		logger.debug("FEMALE: sync");
	}

	private void lockSequence() throws InterruptedException, IOException, ProtocolException
	{
		switch (getGender())
		{
		case MALE:
		{
			maleLockSequence();
			break;
		}
		case FEMALE:
		{
			femaleLockSequence();
			break;
		}
		default:
			throw new Error();
		}
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		try
		{
			lockSequence();
			super.run();
			transaction.commit();
		}
		catch (DialogStreamException e)
		{
			transaction.commit();
			throw e;
		}
		catch (Exception e)
		{
			logger.warn("Aborting transaction!!!!");
			throw e;
		}
		finally
		{
			transaction.abort();
			getPeerToPeerNode().getPersistentConnectionLock().unlock(getPeerToPeerConnection());
			getSender().shutdown();
		}
	}

	@Override
	protected PersistentMessageProtocol getMessageProtocol()
	{
		return persistentMessageProtocol;
	}

	protected List<Statement> responseStatementListDependencySorted(Transaction transaction, Collection<UUID> uuids)
	{
		Set<Statement> pending = new HashSet<Statement>();
		for (UUID uuid : uuids)
			pending.add(getPersistenceManager().getStatement(transaction, uuid));
		List<Statement> list = new ArrayList<Statement>();
		while (!pending.isEmpty())
		{
			Stack<Statement> stackin = new Stack<Statement>();
			Stack<Statement> stackout = new Stack<Statement>();
			stackin.push(pending.iterator().next());
			while (!stackin.isEmpty())
			{
				Statement st = stackin.pop();
				stackout.push(st);
				for (Statement dep : st.dependencies(transaction))
					if (pending.contains(dep))
						stackin.push(dep);
				if (!(st instanceof RootContext))
				{
					Context ctx = st.getContext(transaction);
					if (pending.contains(ctx))
						stackin.push(ctx);
				}
			}
			while (!stackout.isEmpty())
			{
				Statement st = stackout.pop();
				if (pending.contains(st))
				{
					list.add(st);
					pending.remove(st);
				}
			}
		}
		return list;
	}

}
