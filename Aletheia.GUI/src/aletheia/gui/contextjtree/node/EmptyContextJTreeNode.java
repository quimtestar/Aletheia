package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.model.statement.Statement;

public class EmptyContextJTreeNode extends ContextJTreeNode
{
	private final GroupSorterContextJTreeNode<? extends Statement> parent;

	public EmptyContextJTreeNode(ContextJTreeModel model, GroupSorterContextJTreeNode<? extends Statement> parent)
	{
		super(model);
		this.parent = parent;
	}

	@Override
	public GroupSorterContextJTreeNode<? extends Statement> getParent()
	{
		return parent;
	}

	@Override
	protected EmptyContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new EmptyContextJTreeNodeRenderer(contextJTree);
	}

	@Override
	protected synchronized EmptyContextJTreeNodeRenderer getRenderer()
	{
		return (EmptyContextJTreeNodeRenderer) super.getRenderer();
	}

	@Override
	public String toString()
	{
		return super.toString() + "[Empty]";
	}

}
