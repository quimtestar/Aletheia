package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.RootContextGroupSorter;
import aletheia.model.statement.RootContext;

public class RootContextGroupSorterContextJTreeNode extends GroupSorterContextJTreeNode<RootContext>
{

	public RootContextGroupSorterContextJTreeNode(ContextJTreeModel model, RootContextGroupSorter sorter)
	{
		super(model, sorter);
	}

	@Override
	public RootContextGroupSorter getSorter()
	{
		return (RootContextGroupSorter) super.getSorter();
	}

}
