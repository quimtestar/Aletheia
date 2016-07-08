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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedList;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CombinedList;

public abstract class DelegateTreeModelBranchNode extends DelegateTreeModelNode
{
	private final DelegateTreeNode delegateTreeNode;

	private SoftReference<List<DelegateTreeSubNode>> subNodeListRef;
	private SoftReference<List<DelegateAuthorizer>> delegateAuthorizerListRef;

	public DelegateTreeModelBranchNode(DelegateTreeModel model, DelegateTreeModelBranchNode parent, DelegateTreeNode delegateTreeNode)
	{
		super(model, parent);
		this.delegateTreeNode = delegateTreeNode;
		this.subNodeListRef = null;
		this.delegateAuthorizerListRef = null;
	}

	public DelegateTreeNode getDelegateTreeNode()
	{
		return delegateTreeNode;
	}

	private synchronized List<DelegateTreeSubNode> getSubNodeList()
	{
		List<DelegateTreeSubNode> subNodeList = null;
		if (subNodeListRef != null)
			subNodeList = subNodeListRef.get();
		if (subNodeList == null)
		{
			Transaction transaction = getModel().beginTransaction();
			try
			{
				subNodeList = new BufferedList<>(delegateTreeNode.localDelegateTreeSubNodeMap(transaction).values());
				subNodeListRef = new SoftReference<>(subNodeList);
			}
			finally
			{
				transaction.abort();
			}
		}
		return subNodeList;
	}

	public synchronized void rebuildSubNodeList()
	{
		subNodeListRef = null;
	}

	private synchronized List<DelegateAuthorizer> getDelegateAuthorizerList()
	{
		List<DelegateAuthorizer> delegateAuthorizerList = null;
		if (delegateAuthorizerListRef != null)
			delegateAuthorizerList = delegateAuthorizerListRef.get();
		if (delegateAuthorizerList == null)
		{
			final Transaction transaction = getModel().beginTransaction();
			try
			{
				delegateAuthorizerList = new BufferedList<>(delegateTreeNode.localDelegateAuthorizerMap(transaction).values());
				Collections.sort(delegateAuthorizerList, new Comparator<DelegateAuthorizer>()
				{

					@Override
					public int compare(DelegateAuthorizer a1, DelegateAuthorizer a2)
					{
						return a1.getDelegate(transaction).getNick().compareTo(a2.getDelegate(transaction).getNick());
					}
				});
				delegateAuthorizerListRef = new SoftReference<>(delegateAuthorizerList);
			}
			finally
			{
				transaction.abort();
			}
		}
		return delegateAuthorizerList;

	}

	public synchronized void rebuildDelegateAuthorizerList()
	{
		delegateAuthorizerListRef = null;
	}

	private List<DelegateTreeModelBranchSubNode> getBranchSubNodeList()
	{
		return new BijectionList<>(new Bijection<DelegateTreeSubNode, DelegateTreeModelBranchSubNode>()
		{

			@Override
			public DelegateTreeModelBranchSubNode forward(DelegateTreeSubNode delegateTreeSubNode)
			{
				return new DelegateTreeModelBranchSubNode(getModel(), DelegateTreeModelBranchNode.this, delegateTreeSubNode);
			}

			@Override
			public DelegateTreeSubNode backward(DelegateTreeModelBranchSubNode delegateTreeModelBranchSubNode)
			{
				return delegateTreeModelBranchSubNode.getDelegateTreeNode();
			}
		}, getSubNodeList());
	}

	private List<DelegateTreeModelLeafNode> getLeafNodeList()
	{
		return new BijectionList<>(new Bijection<DelegateAuthorizer, DelegateTreeModelLeafNode>()
		{

			@Override
			public DelegateTreeModelLeafNode forward(DelegateAuthorizer delegateAuthorizer)
			{
				return new DelegateTreeModelLeafNode(getModel(), DelegateTreeModelBranchNode.this, delegateAuthorizer);
			}

			@Override
			public DelegateAuthorizer backward(DelegateTreeModelLeafNode delegateTreeModelLeafNode)
			{
				return delegateTreeModelLeafNode.getDelegateAuthorizer();
			}

		}, getDelegateAuthorizerList());
	}

	private List<DelegateTreeModelNode> getChildList()
	{
		return new CombinedList<>(new AdaptedList<DelegateTreeModelNode>(getBranchSubNodeList()), new AdaptedList<DelegateTreeModelNode>(getLeafNodeList()));
	}

	@Override
	public DelegateTreeModelNode getChildAt(int i)
	{
		return getChildList().get(i);
	}

	@Override
	public int getChildCount()
	{
		return getChildList().size();
	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (node instanceof DelegateTreeModelBranchSubNode)
			return getBranchSubNodeList().indexOf(node);
		else if (node instanceof DelegateTreeModelLeafNode)
			return getBranchSubNodeList().size() + getLeafNodeList().indexOf(node);
		else
			return -1;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	@Override
	public Enumeration<DelegateTreeModelNode> children()
	{
		final List<DelegateTreeModelNode> childList = getChildList();
		final Iterator<DelegateTreeModelNode> iterator = childList.iterator();
		return new Enumeration<DelegateTreeModelNode>()
		{
			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public DelegateTreeModelNode nextElement()
			{
				return iterator.next();
			}
		};
	}

}
