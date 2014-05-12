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

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import aletheia.model.authority.DelegateAuthorizer;

public class DelegateTreeModelLeafNode extends DelegateTreeModelNode
{
	private final DelegateAuthorizer delegateAuthorizer;

	public DelegateTreeModelLeafNode(DelegateTreeModel model, DelegateTreeModelBranchNode parent, DelegateAuthorizer delegateAuthorizer)
	{
		super(model, parent);
		this.delegateAuthorizer = delegateAuthorizer;
	}

	protected DelegateAuthorizer getDelegateAuthorizer()
	{
		return delegateAuthorizer;
	}

	@Override
	public int getChildCount()
	{
		return 0;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return -1;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

	@Override
	public boolean isLeaf()
	{
		return true;
	}

	@Override
	public DelegateTreeModelNode getChildAt(int childIndex)
	{
		return null;
	}

	@Override
	public Enumeration<DelegateTreeModelNode> children()
	{
		return null;
	}

	@Override
	protected DelegateTreeModelLeafNodeRenderer buildRenderer(DelegateTreeJTree delegateTreeJTree)
	{
		return new DelegateTreeModelLeafNodeRenderer(delegateTreeJTree, this);
	}

}
