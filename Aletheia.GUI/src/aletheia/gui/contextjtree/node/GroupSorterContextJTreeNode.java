package aletheia.gui.contextjtree.node;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.TreeNode;

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
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionIterator;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.IteratorEnumeration;

public abstract class GroupSorterContextJTreeNode<S extends Statement> extends SorterContextJTreeNode
{
	private BufferedList<Sorter> sorterList;
	private boolean degenerate;
	private Map<UUID, Integer> uuidIndexes;

	public GroupSorterContextJTreeNode(ContextJTreeModel model, GroupSorter<S> sorter)
	{
		super(model, sorter);
		this.degenerate = false;
		this.sorterList = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GroupSorter<S> getSorter()
	{
		return (GroupSorter<S>) super.getSorter();
	}

	private synchronized List<Sorter> getSorterList()
	{
		if (sorterList == null)
		{
			Transaction transaction = getModel().beginTransaction();
			try
			{
				sorterList = new BufferedList<Sorter>(new SorterDependencyFilter<Sorter>(getSorter().iterable(transaction), transaction));
				degenerate = getSorter().degenerate(transaction);
				uuidIndexes = new HashMap<UUID, Integer>();
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
		return sorterList;
	}

	public synchronized boolean isDegenerate()
	{
		return degenerate;
	}

	public synchronized boolean checkStatementInsert(Statement statement)
	{
		if (uuidIndexes == null)
			return true;
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
		if (uuidIndexes == null)
			return true;
		return !uuidIndexes.containsKey(statement.getUuid());
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		return getModel().getNodeMap().get(getSorterList().get(childIndex));
	}

	@Override
	public int getChildCount()
	{
		return getSorterList().size();
	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (!(node instanceof SorterContextJTreeNode))
			return -1;
		return getSorterList().indexOf(node);
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

	@Override
	public Enumeration<? extends ContextJTreeNode> children()
	{
		return new IteratorEnumeration<SorterContextJTreeNode>(new BijectionIterator<Sorter, SorterContextJTreeNode>(
				new Bijection<Sorter, SorterContextJTreeNode>()
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
				}, getSorterList().iterator()));
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new GroupSorterContextJTreeNodeRenderer(contextJTree, getSorter());
	}

	public static class Changes
	{
		private final int[] removedIndexes;
		private final Object[] removedObjects;
		private final int[] insertedIndexes;
		private final Object[] insertedObjects;

		protected Changes(List<?> oldList, List<?> newList)
		{
			int oldI = 0;
			int newI = 0;
			ArrayList<Integer> removedIndexes = new ArrayList<Integer>(oldList.size());
			ArrayList<Integer> insertedIndexes = new ArrayList<Integer>(newList.size());
			while (oldI < oldList.size() && newI < newList.size())
			{
				Object oldSt = oldList.get(oldI);
				Object newSt = newList.get(newI);
				while (oldSt.equals(newSt))
				{
					oldI++;
					newI++;
					if (oldI >= oldList.size() || newI >= newList.size())
						break;
					oldSt = oldList.get(oldI);
					newSt = newList.get(newI);
				}
				if (oldI >= oldList.size() || newI >= newList.size())
					break;
				int oldJ = oldI;
				int newJ = newI;
				Object oldSt_ = oldList.get(oldJ);
				Object newSt_ = newList.get(newJ);
				Map<Object, Integer> oldMap = new HashMap<Object, Integer>();
				oldMap.put(oldSt, oldI);
				Map<Object, Integer> newMap = new HashMap<Object, Integer>();
				newMap.put(newSt, newI);
				while (!oldMap.containsKey(newSt_) && !newMap.containsKey(oldSt_))
				{
					oldMap.put(oldSt_, oldJ);
					newMap.put(newSt_, newJ);
					oldJ++;
					newJ++;
					if (oldJ >= oldList.size() || newJ >= newList.size())
						break;
					oldSt_ = oldList.get(oldJ);
					newSt_ = newList.get(newJ);
				}
				Integer newK = newMap.get(oldSt_);
				if (newJ >= newList.size() || newK != null)
				{
					while (oldI < oldJ)
						removedIndexes.add(oldI++);
					if (newK != null)
						while (newI < newK)
							insertedIndexes.add(newI++);
				}
				Integer oldK = oldMap.get(newSt_);
				if (oldJ >= oldList.size() || oldK != null)
				{
					while (newI < newJ)
						insertedIndexes.add(newI++);
					if (oldK != null)
						while (oldI < oldK)
							removedIndexes.add(oldI++);
				}
			}
			while (oldI < oldList.size())
				removedIndexes.add(oldI++);
			while (newI < newList.size())
				insertedIndexes.add(newI++);
			this.removedIndexes = new int[removedIndexes.size()];
			this.removedObjects = new Object[removedIndexes.size()];
			{
				int i = 0;
				for (int idx : removedIndexes)
				{
					this.removedIndexes[i] = idx;
					this.removedObjects[i] = oldList.get(idx);
					i++;
				}
			}
			this.insertedIndexes = new int[insertedIndexes.size()];
			this.insertedObjects = new Object[insertedIndexes.size()];
			{
				int i = 0;
				for (int idx : insertedIndexes)
				{
					this.insertedIndexes[i] = idx;
					this.insertedObjects[i] = newList.get(idx);
					i++;
				}
			}
		}

		protected Changes()
		{
			this.removedIndexes = new int[0];
			this.removedObjects = new Object[0];
			this.insertedIndexes = new int[0];
			this.insertedObjects = new Object[0];
		}

		public int[] getRemovedIndexes()
		{
			return removedIndexes;
		}

		public Object[] getRemovedObjects()
		{
			return removedObjects;
		}

		public int[] getInsertedIndexes()
		{
			return insertedIndexes;
		}

		public Object[] getInsertedObjects()
		{
			return insertedObjects;
		}

	}

	public synchronized Changes changeSorterList()
	{
		if (sorterList == null)
			return new Changes();
		List<Sorter> oldSorterList = sorterList;
		sorterList = null;
		List<Sorter> newSorterList = getSorterList();
		return new Changes(oldSorterList, newSorterList);
	}

}
