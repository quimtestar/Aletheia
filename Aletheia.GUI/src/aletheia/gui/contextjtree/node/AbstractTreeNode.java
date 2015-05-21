/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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

import java.lang.ref.SoftReference;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.EmptyContextJTreeNodeRenderer;

public abstract class AbstractTreeNode implements MyTreeNode
{

	private SoftReference<ContextJTreeNodeRenderer> rendererRef;

	public AbstractTreeNode()
	{
		super();
		this.rendererRef = null;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (node instanceof AbstractTreeNode)
			return getIndex((AbstractTreeNode) node);
		else
			return -1;
	}

	@Override
	public abstract BranchTreeNode getParent();

	@Override
	public Enumeration<AbstractTreeNode> children()
	{
		return null;
	}

	@Override
	public AbstractTreeNode getChildAt(int childIndex)
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

	public int getIndex(AbstractTreeNode node)
	{
		return -1;
	}

	@Override
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

	protected abstract ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree);

	@Override
	public TreePath path()
	{
		return getParent().path().pathByAddingChild(this);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

}
