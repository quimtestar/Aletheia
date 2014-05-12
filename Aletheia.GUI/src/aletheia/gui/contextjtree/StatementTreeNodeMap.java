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
package aletheia.gui.contextjtree;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.AbstractReadOnlyMap;
import aletheia.utilities.collections.SoftCacheWithCleanerMap;

public class StatementTreeNodeMap extends AbstractReadOnlyMap<Statement, StatementTreeNode>
{
	private final SoftCacheWithCleanerMap<Statement, StatementTreeNode> map;
	private final ContextTreeModel model;

	private class CacheListener implements SoftCacheWithCleanerMap.Listener<Statement>
	{

		@Override
		public void keyCleaned(Statement statement)
		{
			statement.removeStateListener(model.getStatementListener());
			statement.removeAuthorityStateListener(model.getStatementListener());
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.removeNomenclatorListener(model.getStatementListener());
				ctx.removeLocalStateListener(model.getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.removeRootNomenclatorListener(model.getStatementListener());
				}
			}
		}

	}

	public StatementTreeNodeMap(ContextTreeModel model)
	{
		this.map = new SoftCacheWithCleanerMap<Statement, StatementTreeNode>();
		this.map.addListener(new CacheListener());
		this.model = model;
	}

	@Override
	public StatementTreeNode get(Object o)
	{
		if (o instanceof Statement)
			return get((Statement) o);
		else
			return null;
	}

	public boolean cached(Statement statement)
	{
		return map.containsKey(statement);
	}

	public StatementTreeNode get(Statement statement)
	{
		synchronized (map)
		{
			StatementTreeNode node = map.get(statement);
			if (node == null)
			{
				if (statement instanceof Context)
					node = new ContextTreeNode(model, (Context) statement);
				else
					node = new StatementTreeNode(model, statement);
				map.put(statement, node);
				statement.addStateListener(model.getStatementListener());
				statement.addAuthorityStateListener(model.getStatementListener());
				if (statement instanceof Context)
				{
					Context ctx = (Context) statement;
					ctx.addNomenclatorListener(model.getStatementListener());
					ctx.addLocalStateListener(model.getStatementListener());
					if (ctx instanceof RootContext)
					{
						RootContext rootCtx = (RootContext) ctx;
						rootCtx.addRootNomenclatorListener(model.getStatementListener());
					}
				}
			}
			return node;
		}
	}

	@Override
	public boolean containsKey(Object key)
	{
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Map.Entry<Statement, StatementTreeNode>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public Set<Statement> keySet()
	{
		return map.keySet();
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public Collection<StatementTreeNode> values()
	{
		return map.values();
	}

	public StatementTreeNode deleteStatement(Statement statement)
	{
		StatementTreeNode node = map.remove(statement);
		statement.removeStateListener(model.getStatementListener());
		statement.removeAuthorityStateListener(model.getStatementListener());
		if (statement instanceof Context)
		{
			Context ctx = (Context) statement;
			ctx.removeNomenclatorListener(model.getStatementListener());
			ctx.removeLocalStateListener(model.getStatementListener());
			if (ctx instanceof RootContext)
			{
				RootContext rootCtx = (RootContext) ctx;
				rootCtx.removeRootNomenclatorListener(model.getStatementListener());
			}
		}
		return node;
	}

}
