/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.persistence;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import aletheia.utilities.MiscUtilities;

/**
 * The equivalent of the relational database transactions for the persistence
 * environment (if the persistence environment is implemented with a relational
 * database, it might be implemented with a database transaction, but not
 * necessarily). Every operation on the persistence environment receives a
 * transaction that can ultimately committed or aborted. Persistence changes are
 * not effective until the transaction is committed, and the data access might
 * be locked to other transactions to keep consistency.
 *
 */
public abstract class Transaction
{
	public interface Hook
	{
		public void run(Transaction closedTransaction);
	}

	private final PersistenceManager persistenceManager;
	private final List<StackTraceElement> stackTraceList;
	private boolean open;
	private boolean commited;
	private Collection<Hook> commitHooks;
	private Collection<Hook> closeHooks;

	/**
	 * Creates a new transaction and registers it on the persistence manager as
	 * a pending transaction.
	 *
	 * @param persistenceManager
	 *            The persistence manager associated to this transaction.
	 * @param auto
	 *            True if it's an auto-transaction. An auto-transaction is a
	 *            transaction that is automatically committed after each single
	 *            persistent operation.
	 */
	protected Transaction(PersistenceManager persistenceManager)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.stackTraceList = persistenceManager.isDebug() ? MiscUtilities.stackTraceList(1) : null;
		this.open = true;
		this.commited = false;
		this.commitHooks = null;
		this.closeHooks = null;
	}

	/**
	 * The persistence manager.
	 *
	 * @return The persistence manager.
	 */
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public List<StackTraceElement> getStackTraceList()
	{
		return stackTraceList;
	}

	/**
	 * A transaction is open if it hasn't been committed or aborted and can
	 * still be used to operate on the persistence environment.
	 *
	 * @return Is this transaction open?
	 */
	public boolean isOpen()
	{
		return open;
	}

	/**
	 * The transaction has been committed.
	 *
	 * @return Has this transaction been committed?
	 */
	public boolean isCommited()
	{
		return commited;
	}

	/**
	 * Commits this transaction and unregisters it as pending.
	 */
	public synchronized void commit()
	{
		open = false;
		commited = true;
		if (commitHooks != null)
		{
			for (Hook hook : commitHooks)
				hook.run(this);
		}
		if (closeHooks != null)
		{
			for (Hook hook : closeHooks)
				hook.run(this);
		}
		notifyAll();
	}

	/**
	 * If this transaction is open, aborts this transaction and unregisters it
	 * as pending. If this transaction is not open, does nothing.
	 */
	public synchronized void abort()
	{
		if (open)
		{
			open = false;
			commited = false;
			if (closeHooks != null)
			{
				for (Hook hook : closeHooks)
					hook.run(this);
			}
			notifyAll();
		}
	}

	/**
	 * Locks the current thread until this transaction is closed (either
	 * committed or aborted):
	 *
	 * @throws InterruptedException
	 */
	public synchronized void waitForClose() throws InterruptedException
	{
		while (open)
			wait();
	}

	/**
	 * Locks the current Locks the current thread until this transaction is
	 * closed (either committed or aborted) or the some time out is reached.
	 *
	 * @param timeOut
	 *            The time out in milliseconds.
	 * @throws InterruptedException
	 */
	public synchronized void waitForClose(long timeOut) throws InterruptedException
	{
		if (timeOut <= 0)
			waitForClose();
		else
		{
			long t0 = System.currentTimeMillis();
			long t1 = t0;
			while (open && (t1 - t0 < timeOut))
			{
				wait(timeOut - (t1 - t0));
				t1 = System.currentTimeMillis();
			}
		}
	}

	/**
	 * Delays the execution of a {@link Hook} object until this transaction is
	 * closed somehow.
	 *
	 * @param hook
	 *            The hook to execute.
	 */
	public synchronized void runWhenCommit(Hook hook)
	{
		if (isCommited())
			hook.run(this);
		else
		{
			if (commitHooks == null)
				commitHooks = new LinkedList<>();
			commitHooks.add(hook);
		}
	}

	public synchronized void runWhenClose(Hook hook)
	{
		if (!isOpen())
			hook.run(this);
		else
		{
			if (closeHooks == null)
				closeHooks = new LinkedList<>();
			closeHooks.add(hook);
		}
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

}
