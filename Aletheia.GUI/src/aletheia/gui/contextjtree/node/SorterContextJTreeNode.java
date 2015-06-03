package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.ContextGroupSorter;
import aletheia.gui.contextjtree.sorter.RootGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Statement;

public abstract class SorterContextJTreeNode extends ContextJTreeNode
{
	private final Sorter sorter;

	public SorterContextJTreeNode(ContextJTreeModel model, Sorter sorter)
	{
		super(model);
		this.sorter = sorter;
	}

	public Sorter getSorter()
	{
		return sorter;
	}

	protected Sorter parentSorter()
	{
		return getSorter().getGroup();
	}

	@SuppressWarnings("unchecked")
	@Override
	public GroupSorterContextJTreeNode<? extends Statement> getParent()
	{
		Sorter parentSorter = parentSorter();
		if (parentSorter instanceof RootGroupSorter)
			return getModel().getRootTreeNode();
		if (parentSorter instanceof ContextGroupSorter)
			parentSorter = ((ContextGroupSorter) parentSorter).getContextSorter();
		return (GroupSorterContextJTreeNode<? extends Statement>) getModel().getNodeMap().get(parentSorter);
	}

}
