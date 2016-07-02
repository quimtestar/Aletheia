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
package aletheia.gui.delegatejtree;

import java.lang.ref.SoftReference;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public abstract class DelegateTreeModelNode implements TreeNode
{
	private final DelegateTreeModel model;
	private final DelegateTreeModelBranchNode parent;

	private SoftReference<DelegateTreeModelNodeRenderer> rendererRef;

	public DelegateTreeModelNode(DelegateTreeModel model, DelegateTreeModelBranchNode parent)
	{
		this.model = model;
		this.parent = parent;
		this.rendererRef = null;
	}

	protected DelegateTreeModel getModel()
	{
		return model;
	}

	@Override
	public DelegateTreeModelBranchNode getParent()
	{
		return parent;
	}

	@Override
	public abstract DelegateTreeModelNode getChildAt(int childIndex);

	@Override
	public abstract Enumeration<DelegateTreeModelNode> children();

	protected abstract DelegateTreeModelNodeRenderer buildRenderer(DelegateTreeJTree delegateTreeJTree);

	public DelegateTreeModelNodeRenderer renderer(DelegateTreeJTree delegateTreeJTree)
	{
		DelegateTreeModelNodeRenderer renderer = null;
		if (rendererRef != null)
			renderer = rendererRef.get();
		if ((renderer == null) || (renderer.getPersistentJTree() != delegateTreeJTree))
		{
			renderer = buildRenderer(delegateTreeJTree);
			rendererRef = new SoftReference<>(renderer);
		}
		return renderer;
	}

	public TreePath path()
	{
		return getParent().path().pathByAddingChild(this);
	}

}
