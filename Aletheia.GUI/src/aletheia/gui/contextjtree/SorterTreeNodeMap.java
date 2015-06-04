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
import java.util.Stack;

import aletheia.gui.contextjtree.node.ContextSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.RootContextGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.sorter.ContextSorter;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootContextGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.StatementGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class SorterTreeNodeMap extends GenericTreeNodeMap<Sorter, SorterContextJTreeNode>
{
	private final ContextJTreeModel model;
	private final Map<Statement, StatementContextJTreeNode> byStatementMap;

	public SorterTreeNodeMap(ContextJTreeModel model)
	{
		super();
		this.model = model;
		this.byStatementMap = new HashMap<Statement, StatementContextJTreeNode>();
	}

	public ContextJTreeModel getModel()
	{
		return model;
	}

	@Override
	protected synchronized SorterContextJTreeNode buildNode(Sorter sorter)
	{
		if (sorter instanceof GroupSorter)
		{
			if (sorter instanceof StatementGroupSorter)
				return new StatementGroupSorterContextJTreeNode(getModel(), (StatementGroupSorter) sorter);
			else if (sorter instanceof RootContextGroupSorter)
				return new RootContextGroupSorterContextJTreeNode(getModel(), (RootContextGroupSorter) sorter);
			else
				throw new Error();
		}
		else if (sorter instanceof StatementSorter)
		{
			StatementSorter statementSorter = (StatementSorter) sorter;
			SorterContextJTreeNode node;
			Statement statement = statementSorter.getStatement();
			statement.addStateListener(getModel().getStatementListener());
			statement.addAuthorityStateListener(getModel().getStatementListener());
			if (statementSorter instanceof ContextSorter)
			{
				Context ctx = (Context) statement;
				ctx.addNomenclatorListener(getModel().getStatementListener());
				ctx.addLocalStateListener(getModel().getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.addRootNomenclatorListener(getModel().getStatementListener());
				}
				node = new ContextSorterContextJTreeNode(getModel(), (ContextSorter) statementSorter);
			}
			else
				node = new StatementSorterContextJTreeNode(getModel(), statementSorter);
			byStatementMap.put(statement, (StatementContextJTreeNode) node);
			return node;
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
			byStatementMap.remove(statement);
		}
	}

	public synchronized StatementContextJTreeNode getByStatement(Statement statement)
	{
		StatementContextJTreeNode node = byStatementMap.get(statement);
		if (node != null)
			return node;
		Transaction transaction = getModel().beginTransaction();
		try
		{
			Stack<Statement> stack = new Stack<Statement>();
			node = null;
			while (node == null)
			{
				stack.push(statement);
				if (statement instanceof RootContext)
					break;
				statement = statement.getContext(transaction);
				node = byStatementMap.get(statement);
			}
			while (!stack.isEmpty())
			{
				statement = stack.pop();
				StatementSorter sorter;
				if (statement instanceof RootContext)
					sorter = getModel().getRootTreeNode().getSorter().getByStatementDeep(transaction, statement);
				else
					sorter = ((ContextSorterContextJTreeNode) node).getSorter().getByStatementDeep(transaction, statement);
				if (sorter == null)
					return null;
				node = (StatementContextJTreeNode) get(sorter);
			}
			return node;
		}
		finally
		{
			transaction.abort();
		}
	}

	public synchronized StatementContextJTreeNode removeByStatement(Statement statement)
	{
		StatementContextJTreeNode node = byStatementMap.get(statement);
		if (node == null)
			return null;
		return (StatementContextJTreeNode) removeKey(node.getSorter());
	}

}
