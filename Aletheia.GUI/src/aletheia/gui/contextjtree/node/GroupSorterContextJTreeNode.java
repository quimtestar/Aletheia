/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.GroupSorterContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.SorterDependencyFilter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.ListChanges;
import aletheia.utilities.collections.AdaptedIterator;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionIterator;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.IteratorEnumeration;

public abstract class GroupSorterContextJTreeNode<S extends Statement> extends SorterContextJTreeNode
{
	private class SorterListManager
	{
		private final BufferedList<Sorter> sorterList;
		private final boolean degenerate;
		private final Map<UUID, Integer> uuidIndexes;

		public SorterListManager()
		{
			Transaction transaction = getModel().beginTransaction();
			try
			{
				sorterList = new BufferedList<>(new SorterDependencyFilter<>(getSorter().iterable(transaction), transaction));
				degenerate = getSorter().degenerate(transaction);
				uuidIndexes = new HashMap<>();
				for (ListIterator<Sorter> iterator = sorterList.listIterator(); iterator.hasNext();)
				{
					int i = iterator.nextIndex();
					Sorter sorter = iterator.next();
					if (sorter instanceof StatementSorter)
					{
						Integer old = uuidIndexes.put(((StatementSorter) sorter).getStatement().getUuid(), i);
						if (old != null)
							throw new IllegalArgumentException();
					}
				}

			}
			finally
			{
				transaction.abort();
			}
		}

		public BufferedList<Sorter> getSorterList()
		{
			return sorterList;
		}

		public boolean isDegenerate()
		{
			return degenerate;
		}

		@SuppressWarnings("unused")
		public Map<UUID, Integer> getUuidIndexes()
		{
			return uuidIndexes;
		}

		public synchronized boolean checkStatementInsert(Statement statement)
		{
			Integer index = uuidIndexes.get(statement.getUuid());
			if (index == null)
				return false;
			StatementSorter sorter = (StatementSorter) sorterList.get(index);
			Statement statement_ = sorter.getStatement();
			if ((statement_.getIdentifier() == null) != (statement.getIdentifier() == null))
				return false;
			if ((statement_.getIdentifier() != null && !statement_.getIdentifier().equals(statement.getIdentifier())))
				return false;
			return true;
		}

		public synchronized boolean checkStatementRemove(Statement statement)
		{
			return !uuidIndexes.containsKey(statement.getUuid());
		}

	}

	private SorterListManager sorterListManager;

	public GroupSorterContextJTreeNode(ContextJTreeModel model, GroupSorter<S> sorter)
	{
		super(model, sorter);
		sorterListManager = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GroupSorter<S> getSorter()
	{
		return (GroupSorter<S>) super.getSorter();
	}

	private synchronized SorterListManager getSorterListManager()
	{
		return sorterListManager;
	}

	public boolean computedSorterList()
	{
		return sorterListManager != null;
	}

	private synchronized SorterListManager obtainSorterListManager()
	{
		if (sorterListManager == null)
			sorterListManager = new SorterListManager();
		return sorterListManager;
	}

	private synchronized void clearSorterListManager()
	{
		sorterListManager = null;
	}

	private synchronized List<Sorter> obtainSorterList()
	{
		return obtainSorterListManager().getSorterList();
	}

	public synchronized List<Sorter> getSorterList()
	{
		SorterListManager manager = getSorterListManager();
		if (manager == null)
			return null;
		return manager.getSorterList();
	}

	public synchronized boolean isDegenerate()
	{
		SorterListManager manager = getSorterListManager();
		return manager == null || manager.isDegenerate();
	}

	public synchronized boolean checkStatementInsert(Statement statement)
	{
		SorterListManager manager = getSorterListManager();
		if (manager == null)
			return true;
		return manager.checkStatementInsert(statement);
	}

	public synchronized boolean checkStatementRemove(Statement statement)
	{
		SorterListManager manager = getSorterListManager();
		if (manager == null)
			return false;
		return manager.checkStatementRemove(statement);
	}

	@Override
	public Sorter getNodeMapSorter()
	{
		return getSorter();
	}

	protected boolean isCached()
	{
		return getModel().getNodeMap().isCached(getNodeMapSorter());
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		if (!isCached())
			return new EmptyContextJTreeNode(getModel(), this);
		return getModel().getNodeMap().get(obtainSorterList().get(childIndex));
	}

	@Override
	public int getChildCount()
	{
		return obtainSorterList().size();
	}

	@Override
	public int getIndex(ContextJTreeNode node)
	{
		if (node instanceof SorterContextJTreeNode)
			return obtainSorterList().indexOf(((SorterContextJTreeNode) node).getSorter());
		else
			return super.getIndex(node);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	public Iterator<? extends ContextJTreeNode> childrenIterator()
	{
		if (!isCached())
			return Collections.emptyIterator();
		return new BijectionIterator<>(new Bijection<Sorter, SorterContextJTreeNode>()
		{

			@Override
			public SorterContextJTreeNode forward(Sorter sorter)
			{
				return getModel().getNodeMap().get(sorter);
			}

			@Override
			public Sorter backward(SorterContextJTreeNode sorterContextJTreeNode)
			{
				return sorterContextJTreeNode.getSorter();
			}
		}, obtainSorterList().iterator());
	}

	public Iterable<? extends ContextJTreeNode> childrenIterable()
	{
		return new Iterable<ContextJTreeNode>()
		{

			@Override
			public Iterator<ContextJTreeNode> iterator()
			{
				return new AdaptedIterator<>(childrenIterator());
			}
		};
	}

	@Override
	public Enumeration<ContextJTreeNode> children()
	{
		return new IteratorEnumeration<>(childrenIterator());
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new GroupSorterContextJTreeNodeRenderer(contextJTree, this);
	}

	public synchronized ListChanges<Sorter> changeSorterList()
	{
		List<Sorter> oldSorterList = getSorterList();
		if (oldSorterList == null)
			return null;
		clearSorterListManager();
		List<Sorter> newSorterList = obtainSorterList();
		return new ListChanges<>(oldSorterList, newSorterList);
	}

	@Override
	protected synchronized ContextJTreeNodeRenderer getRenderer()
	{
		return super.getRenderer();
	}

	public void setExpanded(boolean expanded)
	{
		ContextJTreeNodeRenderer renderer = getRenderer();
		if (renderer instanceof GroupSorterContextJTreeNodeRenderer)
			((GroupSorterContextJTreeNodeRenderer) renderer).setExpanded(expanded);
	}

}
