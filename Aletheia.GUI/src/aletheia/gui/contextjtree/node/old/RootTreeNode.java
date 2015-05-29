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

import javax.swing.tree.TreePath;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.old.GroupSorter;
import aletheia.gui.contextjtree.sorter.old.RootContextRootGroupSorter;
import aletheia.gui.contextjtree.sorter.old.Sorter;
import aletheia.gui.contextjtree.sorter.old.StatementRootGroupSorter;
import aletheia.gui.contextjtree.sorter.old.StatementSorter;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

public class RootTreeNode extends AbstractTreeNode implements BranchTreeNode
{
	private final ContextJTreeModel model;
	private BranchNodeSorterListManager<RootContext> sorterListManager;

	public RootTreeNode(ContextJTreeModel model)
	{
		super();
		this.model = model;
		this.sorterListManager = null;
	}

	public ContextJTreeModel getModel()
	{
		return model;
	}

	private BranchNodeSorterListManager<RootContext> newRootContextListManager()
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return new BranchNodeSorterListManager<RootContext>(new RootContextRootGroupSorter(getModel().getPersistenceManager(),transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized BranchNodeSorterListManager<RootContext> getSorterListManager()
	{
		if (sorterListManager == null)
			sorterListManager = newRootContextListManager();
		return sorterListManager;
	}

	private BufferedList<Sorter> getSorterList()
	{
		return getSorterListManager().getSorterList();
	}

	@Override
	public synchronized Changes changeSorterList()
	{
		if (sorterListManager == null)
			return new Changes();
		List<Sorter> oldSorterList = sorterListManager.getSorterList();
		sorterListManager = newRootContextListManager();
		List<Sorter> newSorterList = sorterListManager.getSorterList();
		return new Changes(oldSorterList, newSorterList);
	}

	@Override
	public synchronized boolean checkStatementInsert(Statement statement)
	{
		if (sorterListManager == null)
			return false;
		return sorterListManager.checkStatementInsert(statement);
	}

	@Override
	public synchronized boolean checkStatementRemove(Statement statement)
	{
		if (sorterListManager == null)
			return false;
		return sorterListManager.checkStatementRemove(statement);
	}
	
	public synchronized BranchTreeNode findStatementInsertNode(Statement statement)
	{
		if (sorterListManager==null)
			return null;
		Sorter sorter=sorterListManager.findSorter(statement);
		if (sorter instanceof StatementSorter)
			return null;
		else if (sorter instanceof GroupSorter)
		{
			GroupSorterTreeNode node=(GroupSorterTreeNode) getModel().nodeMap().get(sorter);
			return node.findStatementInsertNode(statement);
		}
		else
			throw new Error();
	}


	public List<Sorter> rootContextList()
	{
		return Collections.unmodifiableList(getSorterList());
	}

	@Override
	public Enumeration<AbstractTreeNode> children()
	{
		final Iterator<Sorter> iterator = getSorterList().iterator();

		return new Enumeration<AbstractTreeNode>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public AbstractTreeNode nextElement()
			{
				if (!iterator.hasNext())
					throw new NoSuchElementException();
				return getModel().nodeMap().get(iterator.next());
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
		else
			node = new EmptyTreeNode(this);
		return node;
	}

	@Override
	public int getChildCount()
	{
		return getSorterList().size();
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
		else
			return -1;

	}

	public int getIndex(StatementTreeNode node)
	{
		return getSorterList().indexOf(node.getStatement());
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	@Override
	public TreePath path()
	{
		return new TreePath(this);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public BranchTreeNode getParent()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new EmptyContextJTreeNodeRenderer(contextJTree);
	}

	@Override
	public String toString()
	{
		return "RootTreeNode";
	}

}
