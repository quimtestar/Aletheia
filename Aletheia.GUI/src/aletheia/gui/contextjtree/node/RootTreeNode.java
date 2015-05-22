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

import javax.swing.tree.TreePath;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class RootTreeNode extends AbstractTreeNode implements BranchTreeNode
{
	private final ContextTreeModel model;
	private BranchNodeStatementListManager<RootContext> rootContextListManager;

	public RootTreeNode(ContextTreeModel model)
	{
		super();
		this.model = model;
		this.rootContextListManager = null;
	}

	public ContextTreeModel getModel()
	{
		return model;
	}

	private BranchNodeStatementListManager<RootContext> newRootContextListManager()
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return new BranchNodeStatementListManager<RootContext>(getModel().getPersistenceManager().sortedRootContexts(transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized BranchNodeStatementListManager<RootContext> getRootContextListManager()
	{
		if (rootContextListManager == null)
			rootContextListManager = newRootContextListManager();
		return rootContextListManager;
	}

	private List<RootContext> getRootContextList()
	{
		return getRootContextListManager().getStatementList();
	}

	@Override
	public synchronized Changes changeStatementList()
	{
		if (rootContextListManager == null)
			return new Changes();
		List<RootContext> oldStatementList = rootContextListManager.getStatementList();
		rootContextListManager = newRootContextListManager();
		List<RootContext> newStatementList = rootContextListManager.getStatementList();
		return new Changes(oldStatementList, newStatementList);
	}

	@Override
	public synchronized boolean checkStatementInsert(Statement statement)
	{
		if (rootContextListManager == null)
			return false;
		return rootContextListManager.checkStatementInsert(statement);
	}

	@Override
	public synchronized boolean checkStatementRemove(Statement statement)
	{
		if (rootContextListManager == null)
			return false;
		return rootContextListManager.checkStatementRemove(statement);
	}

	public List<RootContext> rootContextList()
	{
		return Collections.unmodifiableList(getRootContextList());
	}

	@Override
	public Enumeration<AbstractTreeNode> children()
	{
		final Iterator<RootContext> iterator = getRootContextList().iterator();

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
		List<RootContext> list = getRootContextList();
		if (childIndex < list.size())
			node = getModel().nodeMap().get(list.get(childIndex));
		else
			node = new EmptyTreeNode(this);
		return node;
	}

	@Override
	public int getChildCount()
	{
		return getRootContextList().size();
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
		return getRootContextList().indexOf(node.getStatement());
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
