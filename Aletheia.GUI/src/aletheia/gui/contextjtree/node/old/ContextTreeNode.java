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
package aletheia.gui.contextjtree.node.old;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.StatementRootGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextTreeNode extends StatementTreeNode implements BranchTreeNode
{
	private BranchNodeSorterListManager<Statement> sorterListManager;
	private final ConsequentTreeNode consequentTreeNode;

	public ContextTreeNode(ContextJTreeModel model, StatementSorter singletonSorter)
	{
		super(model, singletonSorter);
		if (!(singletonSorter.getStatement() instanceof Context))
			throw new IllegalArgumentException();
		this.sorterListManager = null;
		this.consequentTreeNode = new ConsequentTreeNode(model, getContext());
	}

	private BranchNodeSorterListManager<Statement> newSorterListManager()
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return new BranchNodeSorterListManager<Statement>(new StatementRootGroupSorter(transaction, getContext()));
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized BranchNodeSorterListManager<Statement> getSorterListManager()
	{
		if (sorterListManager == null)
			sorterListManager = newSorterListManager();
		return sorterListManager;
	}

	private List<Sorter> getSorterList()
	{
		return getSorterListManager().getSorterList();
	}

	@Override
	public synchronized Changes changeSorterList()
	{
		if (sorterListManager == null)
			return new Changes();
		List<Sorter> oldSorterList = sorterListManager.getSorterList();
		sorterListManager = newSorterListManager();
		List<Sorter> newSorterList = sorterListManager.getSorterList();
		return new Changes(oldSorterList, newSorterList);
	}

	//TODO remove
	@Deprecated
	@Override
	public synchronized boolean checkStatementInsert(Statement statement)
	{
		if (sorterListManager == null)
			return false;
		return sorterListManager.checkStatementInsert(statement);
	}
	
	//TODO remove
	@Deprecated
	@Override
	public synchronized boolean checkStatementRemove(Statement statement)
	{
		if (sorterListManager == null)
			return false;
		return sorterListManager.checkStatementRemove(statement);
	}


	
	public synchronized BranchTreeNode findStatementParentNode(Statement statement)
	{
		if (sorterListManager==null)
			return null;
		Sorter sorter=sorterListManager.findSorter(statement);
		if (sorter==null || sorter instanceof StatementSorter)
			return this;
		else if (sorter instanceof GroupSorter)
		{
			GroupSorterTreeNode node=(GroupSorterTreeNode) getModel().nodeMap().get(sorter);
			return node.findStatementParentNode(statement);
		}
		else
			throw new Error();
	}
	
	public synchronized BranchTreeNode findStatementParentNode(Identifier identifier)
	{
		if (sorterListManager==null)
			return null;
		Sorter sorter=sorterListManager.findSorter(identifier);
		if (sorter==null || sorter instanceof StatementSorter)
			return this;
		else if (sorter instanceof GroupSorter)
		{
			GroupSorterTreeNode node=(GroupSorterTreeNode) getModel().nodeMap().get(sorter);
			return node.findStatementParentNode(identifier);
		}
		else
			throw new Error();
	}


	public synchronized boolean hasStatement(Statement statement)
	{
		if (sorterListManager==null)
			return false;
		return sorterListManager.findSingletonSorter(statement)!=null;
	}
	
	public synchronized boolean hasStatement(Identifier identifier)
	{
		if (sorterListManager==null)
			return false;
		return sorterListManager.findSingletonSorter(statement)!=null;
	}

	
	public synchronized BranchTreeNode findStatementInsertNode(Statement statement)
	{
		BranchTreeNode pNode=findStatementParentNode(statement);
		if (pNode==null)
			return null;
		if (pNode.hasStatement(statement))
			return null;
		return pNode;
	}
	
	public synchronized BranchTreeNode findStatementDeleteNode(Statement statement)
	{
		BranchTreeNode pNode=findStatementParentNode(statement);
		if (pNode==null)
			return null;
		if (!pNode.hasStatement(statement))
			return null;
		return pNode;
	}
	
	public synchronized BranchTreeNode findStatementDeleteNode(Identifier identifier)
	{
		BranchTreeNode pNode=findStatementParentNode(identifier);
		if (pNode==null)
			return null;
		if (!pNode.hasStatement(identifier))
			return null;
		return pNode;
	}




	public Context getContext()
	{
		return (Context) getStatement();
	}

	public ConsequentTreeNode getConsequentTreeNode()
	{
		return consequentTreeNode;
	}

	public List<Sorter> sorterList()
	{
		return Collections.unmodifiableList(getSorterList());
	}

	@Override
	public Enumeration<AbstractTreeNode> children()
	{
		final Iterator<Sorter> iterator = getSorterList().iterator();

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
		List<Sorter> list = getSorterList();
		if (childIndex < list.size())
			node = getModel().nodeMap().get(list.get(childIndex));
		else if (childIndex == getSorterList().size())
			node = consequentTreeNode;
		else
			node = new EmptyTreeNode(this);
		return node;
	}

	@Override
	public int getChildCount()
	{
		return getSorterList().size() + 1;
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

	public int getIndex(SorterTreeNode node)
	{
		return getSorterList().indexOf(node.getSorter());
	}

	public int getIndex(ConsequentTreeNode node)
	{
		if (consequentTreeNode.equals(node))
			return getSorterList().size();
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
