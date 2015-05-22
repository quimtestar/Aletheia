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

import java.util.HashMap;
import java.util.Map;
import aletheia.gui.contextjtree.node.ContextTreeNode;
import aletheia.gui.contextjtree.node.GroupStatementSorterTreeNode;
import aletheia.gui.contextjtree.node.StatementSorterTreeNode;
import aletheia.gui.contextjtree.node.StatementTreeNode;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.SingletonSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;

public class StatementSorterTreeNodeMap extends GenericTreeNodeMap<Sorter, StatementSorterTreeNode>
{
	private final Map<Statement, StatementTreeNode> statementTreeNodeMap;

	public StatementSorterTreeNodeMap(ContextTreeModel model)
	{
		super(model);
		this.statementTreeNodeMap = new HashMap<Statement, StatementTreeNode>();
	}

	@Override
	protected synchronized StatementSorterTreeNode buildNode(Sorter sorter)
	{
		if (sorter instanceof GroupSorter)
			return new GroupStatementSorterTreeNode(getModel(), (GroupSorter<?>) sorter);
		else if (sorter instanceof SingletonSorter)
		{
			SingletonSorter singletonSorter = (SingletonSorter) sorter;
			Statement statement = singletonSorter.getStatement();
			statement.addStateListener(getModel().getStatementListener());
			statement.addAuthorityStateListener(getModel().getStatementListener());
			StatementTreeNode node;
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.addNomenclatorListener(getModel().getStatementListener());
				ctx.addLocalStateListener(getModel().getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.addRootNomenclatorListener(getModel().getStatementListener());
				}
				node = new ContextTreeNode(getModel(), singletonSorter);
			}
			else
				node = new StatementTreeNode(getModel(), singletonSorter);
			statementTreeNodeMap.put(statement, node);
			return node;
		}
		else
			throw new Error();
	}

	@Override
	protected synchronized void keyRemoved(Sorter sorter)
	{
		if (sorter instanceof SingletonSorter)
		{
			Statement statement = ((SingletonSorter) sorter).getStatement();
			statement.removeStateListener(getModel().getStatementListener());
			statement.removeAuthorityStateListener(getModel().getStatementListener());
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.removeNomenclatorListener(getModel().getStatementListener());
				ctx.removeLocalStateListener(getModel().getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.removeRootNomenclatorListener(getModel().getStatementListener());
				}
			}
			statementTreeNodeMap.remove(statement);
		}
	}

	public synchronized StatementTreeNode getStatementTreeNode(Statement statement)
	{
		return statementTreeNodeMap.get(statement);
	}

	public synchronized StatementTreeNode removeStatement(Statement statement)
	{
		StatementTreeNode node = statementTreeNodeMap.get(statement);
		if (node != null)
			removeKey(node.getSorter());
		return node;
	}

	public synchronized boolean cachedStatement(Statement statement)
	{
		return statementTreeNodeMap.containsKey(statement);
	}

}
