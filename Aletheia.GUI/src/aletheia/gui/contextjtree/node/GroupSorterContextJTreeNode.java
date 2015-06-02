package aletheia.gui.contextjtree.node;

import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.GroupSorterContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.SorterDependencyFilter;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionIterator;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.IteratorEnumeration;

public abstract class GroupSorterContextJTreeNode<S extends Statement> extends SorterContextJTreeNode
{
	private BufferedList<Sorter> sorterList;

	public GroupSorterContextJTreeNode(ContextJTreeModel model, GroupSorter<S> sorter)
	{
		super(model, sorter);
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
				sorterList = new BufferedList<Sorter>(new SorterDependencyFilter<Sorter>(getSorter().iterable(transaction),transaction));
			}
			finally
			{
				transaction.abort();
			}
		}
		return sorterList;
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		return getModel().nodeMap().get(getSorterList().get(childIndex));
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
						return getModel().nodeMap().get(sorter);
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

}
