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
package aletheia.prooffinder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CacheWithCleanerMap;
import aletheia.utilities.collections.CombinedCollection;
import aletheia.utilities.collections.WeakCacheWithCleanerMap;

public class StatementCacheTree implements ContextWatcher.Listener, CacheWithCleanerMap.Listener<Context>
{
	public interface Listener
	{
		public void newProvedStatement(Context context, Statement statement);

		public void disProvedStatement(Context context, Statement statement);
	}

	private final PersistenceManager persistenceManager;
	private final ContextWatcher contextWatcher;
	private final CacheWithCleanerMap<Context, Set<Statement>> localStatementsCacheMap;
	private final Set<Listener> listeners;

	public StatementCacheTree(PersistenceManager persistenceManager, ContextWatcher contextWatcher)
	{
		this.persistenceManager = persistenceManager;
		this.contextWatcher = contextWatcher;
		this.localStatementsCacheMap = new WeakCacheWithCleanerMap<Context, Set<Statement>>();
		this.listeners = Collections.synchronizedSet(new HashSet<Listener>());
		this.contextWatcher.addListener(this);
		this.localStatementsCacheMap.addListener(this);
	}

	public void addListener(Listener l)
	{
		listeners.add(l);
	}

	public void removeListener(Listener l)
	{
		listeners.remove(l);
	}

	public void shutdown()
	{
		contextWatcher.removeListener(this);
	}

	public void clear()
	{
		localStatementsCacheMap.clear();
	}

	public Collection<Statement> getLocalStatementCollection(Context ctx)
	{
		Set<Statement> set = localStatementsCacheMap.get(ctx);
		if (set == null)
		{
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				set = Collections.synchronizedSet(new HashSet<Statement>());
				for (Statement st : ctx.localStatements(transaction).values())
				{
					if (st.isProved())
						set.add(st);
				}
				localStatementsCacheMap.put(ctx, set);
				contextWatcher.watchContext(transaction, ctx);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}
		return set;

	}

	public Collection<Statement> getStatementCollection(Context ctx)
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			Stack<Context> stack = new Stack<Context>();
			while (!(ctx instanceof RootContext))
			{
				stack.push(ctx);
				ctx = ctx.getContext(transaction);
			}
			Collection<Statement> col = getLocalStatementCollection(ctx);
			while (!stack.isEmpty())
				col = new CombinedCollection<Statement>(getLocalStatementCollection(stack.pop()), col);
			transaction.commit();
			return col;
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public void newProvedStatement(Context context, Statement statement)
	{
		Set<Statement> set = localStatementsCacheMap.get(context);
		if (set != null)
		{
			set.add(statement);
			synchronized (listeners)
			{
				for (Listener l : listeners)
					l.newProvedStatement(context, statement);
			}
		}
	}

	@Override
	public void disProvedStatement(Context context, Statement statement)
	{
		Set<Statement> set = localStatementsCacheMap.get(context);
		if (set != null)
		{
			set.remove(statement);
			synchronized (listeners)
			{
				for (Listener l : listeners)
					l.disProvedStatement(context, statement);
			}
		}
	}

	@Override
	public void keyCleaned(Context ctx)
	{
		contextWatcher.unwatchContext(ctx);
	}

}
