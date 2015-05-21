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
package aletheia.gui.contextjtree.node;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import aletheia.gui.contextjtree.BranchNodeStatementSorterListManager;
import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.statementsorter.ContextGroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.GroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.SingletonStatementSorter;
import aletheia.gui.contextjtree.statementsorter.StatementSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextTreeNode extends StatementTreeNode implements BranchTreeNode
{
	private BranchNodeStatementSorterListManager<Statement> statementSorterListManager;
	private final ConsequentTreeNode consequentTreeNode;

	public ContextTreeNode(ContextTreeModel model, SingletonStatementSorter<?> singletonStatementSorter)
	{
		super(model, singletonStatementSorter);
		if (!(singletonStatementSorter.getStatement() instanceof Context))
			throw new IllegalArgumentException();
		this.statementSorterListManager = null;
		this.consequentTreeNode = new ConsequentTreeNode(model, getContext());
	}

	private BranchNodeStatementSorterListManager<Statement> newStatementSorterListManager()
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return new BranchNodeStatementSorterListManager<Statement>(new ContextGroupStatementSorter(transaction,getContext()));
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized BranchNodeStatementSorterListManager<Statement> getStatementSorterListManager()
	{
		if (statementSorterListManager == null)
			statementSorterListManager = newStatementSorterListManager();
		return statementSorterListManager;
	}

	private List<StatementSorter<Statement>> getStatementSorterList()
	{
		return getStatementSorterListManager().getStatementSorterList();
	}

	@Override
	public synchronized Changes changeStatementList()
	{
		if (statementSorterListManager == null)
			return new Changes();
		List<StatementSorter<Statement>> oldStatementSorterList = statementSorterListManager.getStatementSorterList();
		statementSorterListManager = newStatementSorterListManager();
		List<StatementSorter<Statement>> newStatementSorterList = statementSorterListManager.getStatementSorterList();
		return new Changes(oldStatementSorterList, newStatementSorterList);
	}

	@Override
	public synchronized boolean checkStatementInsert(Statement statement)
	{
		if (statementSorterListManager == null)
			return false;
		return statementSorterListManager.checkStatementInsert(statement);
	}

	@Override
	public synchronized boolean checkStatementRemove(Statement statement)
	{
		if (statementSorterListManager == null)
			return false;
		return statementSorterListManager.checkStatementRemove(statement);
	}

	public Context getContext()
	{
		return (Context) getStatement();
	}

	public ConsequentTreeNode getConsequentTreeNode()
	{
		return consequentTreeNode;
	}

	public List<StatementSorter<Statement>> statementSorterList()
	{
		return Collections.unmodifiableList(getStatementSorterList());
	}

	@Override
	public Enumeration<AbstractTreeNode> children()
	{
		final Iterator<StatementSorter<Statement>> iterator = getStatementSorterList().iterator();

		return new Enumeration<AbstractTreeNode>()
		{
			boolean atEnd = false;

			@Override
			public boolean hasMoreElements()
			{
				return !atEnd;
			}

			@Override
			public AbstractTreeNode nextElement()
			{
				if (iterator.hasNext())
					return getModel().nodeMap().get(iterator.next());
				else
				{
					if (atEnd)
						throw new NoSuchElementException();
					atEnd = true;
					return consequentTreeNode;
				}
			}
		};
	}

	@Override
	public AbstractTreeNode getChildAt(int childIndex)
	{
		AbstractTreeNode node;
		List<StatementSorter<Statement>> list = getStatementSorterList();
		if (childIndex < list.size())
			node = getModel().nodeMap().get(list.get(childIndex));
		else if (childIndex == getStatementSorterList().size())
			node = consequentTreeNode;
		else
			node = new EmptyTreeNode(this);
		return node;
	}

	@Override
	public int getChildCount()
	{
		return getStatementSorterList().size() + 1;
	}

	@Override
	public int getIndex(AbstractTreeNode node)
	{
		if (node instanceof StatementTreeNode)
		{
			StatementTreeNode stNode = (StatementTreeNode) node;
			int i = getIndex(stNode);
			return i;
		}
		else if (node instanceof ConsequentTreeNode)
			return getIndex((ConsequentTreeNode) node);
		else
			return -1;

	}

	public int getIndex(StatementTreeNode node)
	{
		return getStatementSorterList().indexOf(node.getStatement()); //TODO
	}

	public int getIndex(ConsequentTreeNode node)
	{
		if (consequentTreeNode.equals(node))
			return getStatementSorterList().size();
		else
			return -1;
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

}
