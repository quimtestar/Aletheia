package aletheia.gui.contextjtree.node;

import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.Stack;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.ReverseList;

public abstract class ContextJTreeNode implements TreeNode
{
	private final ContextJTreeModel model;

	private SoftReference<ContextJTreeNodeRenderer> rendererRef;

	public ContextJTreeNode(ContextJTreeModel model)
	{
		this.model = model;
		this.rendererRef = null;
	}

	public ContextJTreeModel getModel()
	{
		return model;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (node instanceof ContextJTreeNode)
			return getIndex((ContextJTreeNode) node);
		else
			return -1;
	}

	@Override
	public abstract GroupSorterContextJTreeNode<? extends Statement> getParent();

	@Override
	public Enumeration<? extends ContextJTreeNode> children()
	{
		return null;
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		return null;
	}

	@Override
	public int getChildCount()
	{
		return 0;
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	public int getIndex(ContextJTreeNode node)
	{
		return -1;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

	public synchronized void cleanRenderer()
	{
		rendererRef = null;
	}

	public synchronized ContextJTreeNodeRenderer renderer(ContextJTree contextJTree)
	{
		ContextJTreeNodeRenderer renderer = null;
		if (rendererRef != null)
			renderer = rendererRef.get();
		if ((renderer == null) || (renderer.getContextJTree() != contextJTree))
		{
			renderer = buildRenderer(contextJTree);
			if (renderer == null)
				renderer = new EmptyContextJTreeNodeRenderer(contextJTree);
			else
				rendererRef = new SoftReference<ContextJTreeNodeRenderer>(renderer);
		}
		return renderer;
	}

	protected synchronized ContextJTreeNodeRenderer getRenderer()
	{
		if (rendererRef != null)
			return rendererRef.get();
		else
			return null;
	}

	protected abstract ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree);

	public TreePath path()
	{
		Stack<ContextJTreeNode> stack = new Stack<ContextJTreeNode>();
		ContextJTreeNode node = this;
		while (node != null)
		{
			stack.push(node);
			node = node.getParent();
		}
		return new TreePath(new ReverseList<ContextJTreeNode>(stack).toArray());
	}

	@Override
	public String toString()
	{
		return "[" + Integer.toHexString(hashCode()) + "]";
	}

}
