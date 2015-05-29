package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ConsequentContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.model.statement.Statement;

public class ConsequentContextJTreeNode extends ContextJTreeNode
{
	private final ContextContextJTreeNode parent;

	public ConsequentContextJTreeNode(ContextJTreeModel model, ContextContextJTreeNode parent)
	{
		super(model);
		this.parent=parent;
	}

	@Override
	public ContextContextJTreeNode getParent()
	{
		return parent;
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new ConsequentContextJTreeNodeRenderer(contextJTree, parent.getContext());
	}

}
