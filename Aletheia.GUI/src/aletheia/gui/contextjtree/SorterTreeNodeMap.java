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

import aletheia.gui.contextjtree.node.ContextContextJTreeNode;
import aletheia.gui.contextjtree.node.GroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.StatementRootGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;

public class SorterTreeNodeMap extends GenericTreeNodeMap<Sorter, SorterContextJTreeNode>
{
	public SorterTreeNodeMap(ContextJTreeModel model)
	{
		super(model);
	}

	@Override
	protected synchronized SorterContextJTreeNode buildNode(Sorter sorter)
	{
		if (sorter instanceof GroupSorter)
		{
			if (sorter instanceof StatementRootGroupSorter)
			{
				Context context=((StatementRootGroupSorter) sorter).getContext();
				
			}
		}
			return new GroupSorterContextJTreeNode<Statement>(getModel(), (GroupSorter<?>) sorter);
		else if (sorter instanceof StatementSorter)
		{
			StatementSorter singletonSorter = (StatementSorter) sorter;
			Statement statement = singletonSorter.getStatement();
			statement.addStateListener(getModel().getStatementListener());
			statement.addAuthorityStateListener(getModel().getStatementListener());
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
				return new ContextContextJTreeNode(getModel(), new StatementRootGroupSorter(, ctx));
			}
			else
				return new StatementContextJTreeNode(getModel(), singletonSorter);
			
		}
		else
			throw new Error();
	}

	@Override
	protected synchronized void keyRemoved(Sorter sorter)
	{
		if (sorter instanceof StatementSorter)
		{
			Statement statement = ((StatementSorter) sorter).getStatement();
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
