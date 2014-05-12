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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextTreeNode extends StatementTreeNode implements BranchTreeNode
{
	private BranchNodeStatementListManager<Statement> statementListManager;
	private final ConsequentTreeNode consequentTreeNode;

	public ContextTreeNode(ContextTreeModel model, Context context)
	{
		super(model, context);
		this.statementListManager = null;
		this.consequentTreeNode = new ConsequentTreeNode(model, context);
	}

	private BranchNodeStatementListManager<Statement> newStatementListManager()
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return new BranchNodeStatementListManager<Statement>(getContext().localDependencySortedStatements(transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized BranchNodeStatementListManager<Statement> getStatementListManager()
	{
		if (statementListManager == null)
			statementListManager = newStatementListManager();
		return statementListManager;
	}

	private List<Statement> getStatementList()
	{
		return getStatementListManager().getStatementList();
	}

	@Override
	public synchronized Changes changeStatementList()
	{
		if (statementListManager == null)
			return new Changes();
		List<Statement> oldStatementList = statementListManager.getStatementList();
		statementListManager = newStatementListManager();
		List<Statement> newStatementList = statementListManager.getStatementList();
		return new Changes(oldStatementList, newStatementList);
	}

	@Override
	public synchronized boolean checkStatementInsert(Statement statement)
	{
		if (statementListManager == null)
			return false;
		return statementListManager.checkStatementInsert(statement);
	}

	@Override
	public synchronized boolean checkStatementRemove(Statement statement)
	{
		if (statementListManager == null)
			return false;
		return statementListManager.checkStatementRemove(statement);
	}

	public Context getContext()
	{
		return (Context) getStatement();
	}

	public ConsequentTreeNode getConsequentTreeNode()
	{
		return consequentTreeNode;
	}

	public List<Statement> statementList()
	{
		return Collections.unmodifiableList(getStatementList());
	}

	@Override
	public Enumeration<AbstractTreeNode> children()
	{
		final Iterator<Statement> iterator = getStatementList().iterator();

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
		List<Statement> list = getStatementList();
		if (childIndex < list.size())
			node = getModel().nodeMap().get(list.get(childIndex));
		else if (childIndex == getStatementList().size())
			node = consequentTreeNode;
		else
			node = new EmptyTreeNode(this);
		return node;
	}

	@Override
	public int getChildCount()
	{
		return getStatementList().size() + 1;
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
		return getStatementList().indexOf(node.getStatement());
	}

	public int getIndex(ConsequentTreeNode node)
	{
		if (consequentTreeNode.equals(node))
			return getStatementList().size();
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
