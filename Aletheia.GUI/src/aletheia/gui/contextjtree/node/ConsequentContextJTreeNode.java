package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ConsequentContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;

public class ConsequentContextJTreeNode extends ContextJTreeNode
{
	private final ContextGroupSorterContextJTreeNode parent;

	public ConsequentContextJTreeNode(ContextJTreeModel model, ContextGroupSorterContextJTreeNode parent)
	{
		super(model);
		this.parent = parent;
	}

	@Override
	public ContextGroupSorterContextJTreeNode getParent()
	{
		return parent;
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new ConsequentContextJTreeNodeRenderer(contextJTree, parent.getContext());
	}

}
