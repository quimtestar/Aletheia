package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.GroupSorter;
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

	@SuppressWarnings("unchecked")
	@Override
	public GroupSorterContextJTreeNode<? extends Statement> getParent()
	{
		GroupSorter<? extends Statement> group = sorter.getGroup();
		if (group == null)
			return null;
		return (GroupSorterContextJTreeNode<? extends Statement>) getModel().nodeMap().get(group);
	}

}
