package aletheia.gui.contextjtree.node;

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
import aletheia.utilities.ListChanges;
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

	public synchronized ListChanges<Sorter> changeSorterList()
	{
		if (sorterList == null)
			return new ListChanges<Sorter>();
		List<Sorter> oldSorterList = sorterList;
		sorterList = null;
		List<Sorter> newSorterList = getSorterList();
		return new ListChanges<Sorter>(oldSorterList, newSorterList);
	}

}
