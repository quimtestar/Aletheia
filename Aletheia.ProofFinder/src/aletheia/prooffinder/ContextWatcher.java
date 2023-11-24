/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
package aletheia.prooffinder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.StateListener;
import aletheia.persistence.Transaction;

public class ContextWatcher implements StateListener
{
	public interface Listener
	{
		public void newProvedStatement(Context context, Statement statement);

		public void disProvedStatement(Context context, Statement statement);
	}

	private abstract class QueueElement
	{
		public final Transaction transaction;

		public QueueElement(Transaction transaction)
		{
			super();
			this.transaction = transaction;
		}

	}

	private abstract class ProvedStatementQueueElement extends QueueElement
	{
		public final Context context;
		public final Statement statement;

		public ProvedStatementQueueElement(Transaction transaction, Context context, Statement statement)
		{
			super(transaction);
			this.context = context;
			this.statement = statement;
		}
	}

	private class NewProvedStatementQueueElement extends ProvedStatementQueueElement
	{

		public NewProvedStatementQueueElement(Transaction transaction, Context context, Statement statement)
		{
			super(transaction, context, statement);
		}

	}

	private class DisProvedStatementQueueElement extends ProvedStatementQueueElement
	{

		public DisProvedStatementQueueElement(Transaction transaction, Context context, Statement statement)
		{
			super(transaction, context, statement);
		}

	}

	private final Set<Context> watching;
	private final Map<Context, Set<Statement>> statementMap;
	private final Set<Listener> listeners;
	private final BlockingQueue<QueueElement> queue;
	private QueueProcessorThread queueProcessorThread;
	private boolean halt;

	public ContextWatcher()
	{
		this.watching = new HashSet<>();
		this.statementMap = new HashMap<>();
		this.listeners = Collections.synchronizedSet(new HashSet<>());
		this.queue = new LinkedBlockingQueue<>();
		this.queueProcessorThread = null;
		this.halt = false;
	}

	public void watchContext(Transaction transaction, Context context)
	{
		if (watching.add(context))
		{
			context.addStateListener(this);
			Set<Statement> set = new HashSet<>();
			for (Statement st : context.statements(transaction).values())
			{
				st.addStateListener(this);
				set.add(st);
			}
			statementMap.put(context, set);
		}
	}

	public void unwatchContext(Context context)
	{
		if (watching.remove(context))
		{
			context.removeStateListener(this);
			Set<Statement> set = statementMap.remove(context);
			if (set != null)
			{
				for (Statement st : set)
				{
					if (!watching.contains(st))
						st.removeStateListener(this);
				}
			}
		}
	}

	public void unwatchAll()
	{
		for (Context context : watching)
		{
			context.removeStateListener(this);
			for (Statement st : statementMap.remove(context))
				st.removeStateListener(this);
		}
		watching.clear();
	}

	public void shutdown()
	{
		unwatchAll();
		halt = true;
		synchronized (queue)
		{
			if (queueProcessorThread != null)
			{
				queueProcessorThread.interrupt();
				try
				{
					queueProcessorThread.join();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void addListener(Listener l)
	{
		listeners.add(l);
	}

	public void removeListener(Listener l)
	{
		listeners.remove(l);
	}

	private void queuePut(QueueElement queueElement) throws InterruptedException
	{
		synchronized (queue)
		{
			queue.put(queueElement);
			if (queueProcessorThread == null)
			{
				queueProcessorThread = new QueueProcessorThread();
				queueProcessorThread.start();
			}
		}
	}

	private boolean queueIsEmpty()
	{
		synchronized (queue)
		{
			if (queue.isEmpty())
			{
				queueProcessorThread = null;
				return true;
			}
			else
				return false;
		}
	}

	private void newProvedStatement(Transaction transaction, Context context, Statement statement) throws InterruptedException
	{
		queuePut(new NewProvedStatementQueueElement(transaction, context, statement));
	}

	private void disProvedStatement(Transaction transaction, Context context, Statement statement) throws InterruptedException
	{
		queuePut(new DisProvedStatementQueueElement(transaction, context, statement));
	}

	@Override
	public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
	{
		if (!(statement instanceof RootContext))
		{
			Context ctx = statement.getContext(transaction);
			if (watching.contains(ctx))
			{
				try
				{
					if (proved)
						newProvedStatement(transaction, ctx, statement);
					else
						disProvedStatement(transaction, ctx, statement);
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void statementAddedToContext(Transaction transaction, Context context, Statement statement)
	{
		statement.addStateListener(this);
		if (statement.isProved())
		{
			try
			{
				newProvedStatement(transaction, context, statement);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier)
	{
		statement.removeStateListener(this);
		try
		{
			disProvedStatement(transaction, context, statement);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	private class QueueProcessorThread extends Thread
	{
		public QueueProcessorThread()
		{
			super("ContextWatcher.QueueProcessorThread");
		}

		@Override
		public void run()
		{
			while (!halt && !queueIsEmpty())
			{
				try
				{
					QueueElement e = queue.take();
					e.transaction.waitForClose();
					if (e instanceof ProvedStatementQueueElement)
					{
						ProvedStatementQueueElement e_ = (ProvedStatementQueueElement) e;
						if (e_ instanceof NewProvedStatementQueueElement)
						{
							synchronized (listeners)
							{
								for (Listener l : listeners)
									l.newProvedStatement(e_.context, e_.statement);
							}
						}
						else if (e_ instanceof DisProvedStatementQueueElement)
						{
							synchronized (listeners)
							{
								for (Listener l : listeners)
									l.disProvedStatement(e_.context, e_.statement);
							}
						}
						else
							throw new RuntimeException();
					}
					else
						throw new RuntimeException();
				}
				catch (InterruptedException e)
				{

				}
			}
		}

	}

	@Override
	public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
	{
	}

	@Override
	public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
	{
	}

}
