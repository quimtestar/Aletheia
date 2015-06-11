package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ConsequentContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.model.statement.Context;

public class ConsequentContextJTreeNode extends ContextJTreeNode
{
	private final ContextSorterContextJTreeNode parent;

	public ConsequentContextJTreeNode(ContextJTreeModel model, ContextSorterContextJTreeNode parent)
	{
		super(model);
		this.parent = parent;
	}

	@Override
	public ContextSorterContextJTreeNode getParent()
	{
		return parent;
	}

	public Context getContext()
	{
		return getParent().getContext();
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new ConsequentContextJTreeNodeRenderer(contextJTree, parent.getContext());
	}

	@Override
	public String toString()
	{
		return super.toString() + "[Consequent: " + getContext().label() + "]";
	}

}
