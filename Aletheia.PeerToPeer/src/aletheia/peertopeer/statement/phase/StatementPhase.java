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
package aletheia.peertopeer.statement.phase;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Context;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.RootPhase;
import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.peertopeer.statement.PendingPersistentDataChanges;
import aletheia.peertopeer.statement.RemoteSubscription;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;

public class StatementPhase extends SubRootPhase
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final InitialStatementPhase initialStatementPhase;
	private final LoopStatementPhase loopStatementPhase;

	private final RemoteSubscription remoteSubscription;
	private final PendingPersistentDataChanges pendingPersistentDataChanges;

	public StatementPhase(RootPhase rootPhase, UUID peerNodeUuid) throws IOException
	{
		super(rootPhase, peerNodeUuid);
		this.initialStatementPhase = new InitialStatementPhase(this);
		this.loopStatementPhase = new LoopStatementPhase(this);
		this.remoteSubscription = new RemoteSubscription(getPersistenceManager());
		this.pendingPersistentDataChanges = new PendingPersistentDataChanges();
	}

	protected InitialStatementPhase getInitialStatementPhase()
	{
		return initialStatementPhase;
	}

	protected LoopStatementPhase getLoopStatementPhase()
	{
		return loopStatementPhase;
	}

	public RemoteSubscription getRemoteSubscription()
	{
		return remoteSubscription;
	}

	public PendingPersistentDataChanges getPendingPersistentDataChanges()
	{
		return pendingPersistentDataChanges;
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug("Entering statement phase");
		try
		{
			initialStatementPhase.run();
			loopStatementPhase.run();
		}
		finally
		{
			contextStateUnlistenAll();
			logger.debug("Exiting statement phase");
		}
	}

	public void contextStateListenTo(Transaction transaction, Context context)
	{
		getLoopStatementPhase().contextStateListenTo(transaction, context);
	}

	public void contextStateUnlisten(Transaction transaction, Context context)
	{
		getLoopStatementPhase().contextStateUnlisten(transaction, context);
	}

	public void contextStateUnlistenAll()
	{
		getLoopStatementPhase().contextStateUnlistenAll();
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		initialStatementPhase.shutdown(fast);
		loopStatementPhase.shutdown(fast);
	}

	public boolean isOpen()
	{
		return loopStatementPhase.isOpen();
	}

}
