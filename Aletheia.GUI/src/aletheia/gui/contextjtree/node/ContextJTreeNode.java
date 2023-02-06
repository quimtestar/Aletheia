/*******************************************************************************
 * Copyright (c) 2015, 2016 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.contextjtree.node;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Stack;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.Logger;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.ReverseList;

public abstract class ContextJTreeNode implements TreeNode
{
	private static final Logger logger = LoggerManager.instance.logger();

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
		Optional.ofNullable(rendererRef).ifPresent(Reference::clear);
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
			{
				logger.trace("Couldn't build renderer for node: " + this);
				renderer = new EmptyContextJTreeNodeRenderer(contextJTree);
				getModel().nodeStructureChangedDegenerateCheck(getParent());
			}
			else
				rendererRef = new SoftReference<>(renderer);
		}
		return renderer;
	}

	protected synchronized ContextJTreeNodeRenderer getRenderer()
	{
		return Optional.ofNullable(rendererRef).map(Reference::get).orElse(null);
	}

	protected abstract ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree);

	public TreePath path()
	{
		Stack<ContextJTreeNode> stack = new Stack<>();
		ContextJTreeNode node = this;
		while (node != null)
		{
			stack.push(node);
			node = node.getParent();
		}
		return new TreePath(new ReverseList<>(stack).toArray());
	}

	@Override
	public String toString()
	{
		return "[" + Integer.toHexString(hashCode()) + "]";
	}

}
