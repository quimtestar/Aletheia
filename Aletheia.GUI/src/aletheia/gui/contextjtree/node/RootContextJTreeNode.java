package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.RootGroupSorter;

public class RootContextJTreeNode extends RootContextGroupSorterContextJTreeNode
{

	public RootContextJTreeNode(ContextJTreeModel model)
	{
		super(model, new RootGroupSorter(model.getPersistenceManager()));
	}

	@Override
	public RootGroupSorter getSorter()
	{
		return (RootGroupSorter) super.getSorter();
	}

	@Override
	protected EmptyContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new EmptyContextJTreeNodeRenderer(contextJTree);
	}

	@Override
	public RootContextJTreeNode getParent()
	{
		return null;
	}

}
