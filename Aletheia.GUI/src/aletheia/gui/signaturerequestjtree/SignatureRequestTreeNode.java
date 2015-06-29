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
package aletheia.gui.signaturerequestjtree;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionComparator;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CombinedCollection;
import aletheia.utilities.collections.ReverseList;

public abstract class SignatureRequestTreeNode implements TreeNode
{
	private final SignatureRequestTreeModel signatureRequestTreeModel;
	private final SignatureRequestTreeNode parent;

	private SoftReference<List<? extends SignatureRequestTreeNode>> childNodeListRef;
	private SoftReference<SignatureRequestTreeNodeRenderer> rendererRef;

	public SignatureRequestTreeNode(SignatureRequestTreeModel signatureRequestTreeModel, SignatureRequestTreeNode parent)
	{
		this.signatureRequestTreeModel = signatureRequestTreeModel;
		this.parent = parent;
		this.childNodeListRef = null;
		this.rendererRef = null;
	}

	protected SignatureRequestTreeModel getModel()
	{
		return signatureRequestTreeModel;
	}

	@Override
	public SignatureRequestTreeNode getParent()
	{
		return parent;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return signatureRequestTreeModel.getPersistenceManager();
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return childNodeList().isEmpty();
	}

	protected abstract Collection<? extends SignatureRequestTreeNode> childNodeCollection(Transaction transaction);

	protected synchronized List<? extends SignatureRequestTreeNode> childNodeList()
	{
		List<? extends SignatureRequestTreeNode> childNodeList = null;
		if (childNodeListRef != null)
			childNodeList = childNodeListRef.get();
		if (childNodeList == null)
		{
			Transaction transaction = getModel().beginTransaction();
			try
			{
				childNodeList = new BufferedList<>(childNodeCollection(transaction));
			}
			finally
			{
				transaction.abort();
			}
			childNodeListRef = new SoftReference<List<? extends SignatureRequestTreeNode>>(childNodeList);
		}
		return childNodeList;
	}

	@Override
	public SignatureRequestTreeNode getChildAt(int childIndex)
	{
		return childNodeList().get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return childNodeList().size();
	}

	protected int getIndex(SignatureRequestTreeNode node)
	{
		return childNodeList().indexOf(node);
	}

	@Override
	public Enumeration<? extends SignatureRequestTreeNode> children()
	{
		final Iterator<? extends SignatureRequestTreeNode> iterator = childNodeList().iterator();
		return new Enumeration<SignatureRequestTreeNode>()
		{

			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public SignatureRequestTreeNode nextElement()
			{
				return iterator.next();
			}
		};
	}

	public synchronized void rebuild()
	{
		rebuildChildNodeList();
	}

	protected synchronized void rebuildChildNodeList()
	{
		childNodeListRef = null;
	}

	protected boolean isCachedChild(SignatureRequestTreeNode node)
	{
		if (childNodeListRef == null)
			return false;
		List<? extends SignatureRequestTreeNode> childNodeList = childNodeListRef.get();
		if (childNodeList == null)
			return false;
		return childNodeList.contains(node);
	}

	protected static class StatementComparator implements Comparator<Statement>
	{
		private final Transaction transaction;

		public StatementComparator(Transaction transaction)
		{
			this.transaction = transaction;
		}

		@Override
		public int compare(Statement st1, Statement st2)
		{
			Identifier id1 = st1.identifier(transaction);
			Identifier id2 = st2.identifier(transaction);
			int c = Boolean.compare(id1 == null, id2 == null);
			if (c != 0)
				return c;
			if ((id1 == null) || (id2 == null))
			{
				c = st1.getUuid().compareTo(st2.getUuid());
				if (c != 0)
					return c;
			}
			else
			{
				c = id1.compareTo(id2);
				if (c != 0)
					return c;
			}
			return 0;
		}

	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (node instanceof SignatureRequestTreeNode)
			return getIndex((SignatureRequestTreeNode) node);
		else
			return -1;
	}

	protected abstract SignatureRequestTreeNodeRenderer buildRenderer(SignatureRequestJTree signatureRequestJTree);

	public synchronized SignatureRequestTreeNodeRenderer renderer(SignatureRequestJTree signatureRequestJTree)
	{
		SignatureRequestTreeNodeRenderer renderer = null;
		if (rendererRef != null)
			renderer = rendererRef.get();
		if ((renderer == null) || (renderer.getPersistentJTree() != signatureRequestJTree))
		{
			renderer = buildRenderer(signatureRequestJTree);
			rendererRef = new SoftReference<SignatureRequestTreeNodeRenderer>(renderer);
		}
		return renderer;
	}

	public void clearRenderer()
	{
		rendererRef = null;
	}

	protected TreePath path()
	{
		SignatureRequestTreeNode node = this;
		List<SignatureRequestTreeNode> nodes = new ArrayList<SignatureRequestTreeNode>();
		while (true)
		{
			nodes.add(node);
			if (node instanceof RootSignatureRequestTreeNode)
				break;
			node = node.getParent();
		}
		return new TreePath(new ReverseList<>(nodes).toArray(new SignatureRequestTreeNode[0]));
	}

	protected Context getContext()
	{
		return getParent().getContext();
	}

	protected ActualContextSignatureRequestTreeNode makeActualContextNode(Context context)
	{
		return getModel().makeActualContextNode(this, context);
	}

	protected VirtualContextSignatureRequestTreeNode makeVirtualContextNode(UUID contextUuid)
	{
		return getModel().makeVirtualContextNode(this, contextUuid);
	}

	protected Collection<ContextSignatureRequestTreeNode> childContextNodeCollection(final Transaction transaction, Collection<UUID> subContextUuidsCollection)
	{
		List<ActualContextSignatureRequestTreeNode> actualContextNodes = new ArrayList<ActualContextSignatureRequestTreeNode>();
		List<VirtualContextSignatureRequestTreeNode> virtualContextNodes = new ArrayList<VirtualContextSignatureRequestTreeNode>();
		for (UUID uuid : subContextUuidsCollection)
		{
			Context context = getPersistenceManager().getContext(transaction, uuid);
			if (context == null)
				virtualContextNodes.add(makeVirtualContextNode(uuid));
			else
				actualContextNodes.add(makeActualContextNode(context));
		}

		Collections.sort(actualContextNodes,
				new BijectionComparator<Statement, ActualContextSignatureRequestTreeNode>(new Bijection<Statement, ActualContextSignatureRequestTreeNode>()
				{

					@Override
					public ActualContextSignatureRequestTreeNode forward(Statement statement)
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public Statement backward(ActualContextSignatureRequestTreeNode node)
					{
						return node.getContext();
					}
				}, new StatementComparator(transaction)));
		return new CombinedCollection<ContextSignatureRequestTreeNode>(new AdaptedCollection<ContextSignatureRequestTreeNode>(actualContextNodes),
				new AdaptedCollection<ContextSignatureRequestTreeNode>(virtualContextNodes));

	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + signatureRequestTreeModel.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignatureRequestTreeNode other = (SignatureRequestTreeNode) obj;
		if (parent == null)
		{
			if (other.parent != null)
				return false;
		}
		else if (!parent.equals(other.parent))
			return false;
		if (!signatureRequestTreeModel.equals(other.signatureRequestTreeModel))
			return false;
		return true;
	}

}
