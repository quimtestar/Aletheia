/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import aletheia.gui.common.PersistentTreeModel;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Namespace;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class DelegateTreeModel extends PersistentTreeModel
{
	private final StatementAuthority statementAuthority;

	private class StateListener implements StatementAuthority.StateListener
	{

		@Override
		public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
		{
		}

		@Override
		public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
		}

		@Override
		public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
		{
		}

		@Override
		public void signatureAdded(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
		}

		@Override
		public void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority, StatementAuthoritySignature statementAuthoritySignature)
		{
		}

		@Override
		public void delegateTreeChanged(Transaction transaction, StatementAuthority statementAuthority)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					synchronized (delegateTreeModelListeners)
					{
						for (DelegateTreeJTree.TreeModelListener l : delegateTreeModelListeners)
							l.preTreeStructureChanged();
					}
					rebuildDelegateTreeRootNode();
					final Collection<TreeModelListener> listeners = getListeners();
					if (!listeners.isEmpty())
					{
						final TreeModelEvent e = new TreeModelEvent(DelegateTreeModel.this, getRoot().path());
						synchronized (listeners)
						{
							for (TreeModelListener l : listeners)
								persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(l, e);
						}
					}
					synchronized (delegateTreeModelListeners)
					{
						for (DelegateTreeJTree.TreeModelListener l : delegateTreeModelListeners)
							l.postTreeStructureChanged();
					}

				}
			});
		}

		@Override
		public void successorEntriesChanged(Transaction transaction, StatementAuthority statementAuthority)
		{
		}

		@Override
		public void delegateAuthorizerChanged(Transaction transaction, StatementAuthority statementAuthority, Namespace prefix, Person delegate,
				Signatory authorizer)
		{
		}

	}

	private final StateListener stateListener;

	private class VirtualRootTreeNode extends DelegateTreeModelNode
	{

		private VirtualRootTreeNode()
		{
			super(DelegateTreeModel.this, null);
		}

		@Override
		public Enumeration<DelegateTreeModelNode> children()
		{
			return new Enumeration<>()
			{

				boolean hasNext = true;

				@Override
				public boolean hasMoreElements()
				{
					return hasNext;
				}

				@Override
				public DelegateTreeModelBranchRootNode nextElement()
				{
					if (hasNext)
						return getDelegateTreeModelBranchRootNode();
					else
						throw new NoSuchElementException();
				}

			};
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public DelegateTreeModelBranchRootNode getChildAt(int childIndex)
		{
			if (childIndex == 0)
				return getDelegateTreeModelBranchRootNode();
			else
				return null;
		}

		@Override
		public int getChildCount()
		{
			return getDelegateTreeModelBranchRootNode() == null ? 0 : 1;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			if (node.equals(getRoot()))
				return 0;
			else
				return -1;
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

		@Override
		protected DelegateTreeModelNodeRenderer buildRenderer(DelegateTreeJTree delegateTreeJTree)
		{
			return new EmptyDelegateTreeModelNodeRenderer(delegateTreeJTree, this);
		}

		@Override
		public TreePath path()
		{
			return new TreePath(this);
		}

	}

	private final VirtualRootTreeNode virtualRootTreeNode;

	private final Set<DelegateTreeJTree.TreeModelListener> delegateTreeModelListeners;

	private SoftReference<DelegateTreeRootNode> delegateTreeRootNodeRef;

	public DelegateTreeModel(PersistenceManager persistenceManager, StatementAuthority statementAuthority)
	{
		super(persistenceManager);
		this.statementAuthority = statementAuthority;
		this.stateListener = new StateListener();
		statementAuthority.addStateListener(stateListener);
		this.virtualRootTreeNode = new VirtualRootTreeNode();
		this.delegateTreeModelListeners = Collections.synchronizedSet(new HashSet<DelegateTreeJTree.TreeModelListener>());
		this.delegateTreeRootNodeRef = null;
	}

	protected StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	private synchronized DelegateTreeRootNode getDelegateTreeRootNode()
	{
		DelegateTreeRootNode delegateTreeRootNode = null;
		if (delegateTreeRootNodeRef != null)
			delegateTreeRootNode = delegateTreeRootNodeRef.get();
		if (delegateTreeRootNode == null)
		{
			Transaction transaction = beginTransaction();
			try
			{
				delegateTreeRootNode = statementAuthority.getDelegateTreeRootNode(transaction);
			}
			finally
			{
				transaction.abort();
			}
		}
		return delegateTreeRootNode;
	}

	public synchronized void rebuildDelegateTreeRootNode()
	{
		delegateTreeRootNodeRef = null;
	}

	protected DelegateTreeModelBranchRootNode getDelegateTreeModelBranchRootNode()
	{
		DelegateTreeRootNode delegateTreeRootNode = getDelegateTreeRootNode();
		if (delegateTreeRootNode == null)
			return null;
		return new DelegateTreeModelBranchRootNode(this, delegateTreeRootNode);
	}

	@Override
	public synchronized VirtualRootTreeNode getRoot()
	{
		return virtualRootTreeNode;
	}

	@Override
	public synchronized TreeNode getChild(Object parent, int index)
	{
		if (parent instanceof DelegateTreeModelNode)
			return ((DelegateTreeModelNode) parent).getChildAt(index);
		else
			return null;
	}

	@Override
	public synchronized int getChildCount(Object parent)
	{
		if (parent instanceof DelegateTreeModelNode)
			return ((DelegateTreeModelNode) parent).getChildCount();
		else
			return 0;
	}

	@Override
	public synchronized boolean isLeaf(Object node)
	{
		if (node instanceof DelegateTreeModelNode)
			return ((DelegateTreeModelNode) node).isLeaf();
		else
			return false;
	}

	@Override
	public synchronized int getIndexOfChild(Object parent, Object child)
	{
		if ((parent instanceof DelegateTreeModelNode) && (child instanceof DelegateTreeModelNode))
			return ((DelegateTreeModelNode) parent).getIndex((DelegateTreeModelNode) child);
		else
			return -1;

	}

	@Override
	public void cleanRenderers()
	{
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	public void shutdown()
	{
		statementAuthority.removeStateListener(stateListener);
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener)
	{
		super.addTreeModelListener(listener);
		if (listener instanceof DelegateTreeJTree.TreeModelListener)
			delegateTreeModelListeners.add((DelegateTreeJTree.TreeModelListener) listener);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener)
	{
		super.removeTreeModelListener(listener);
		if (listener instanceof DelegateTreeJTree.TreeModelListener)
			delegateTreeModelListeners.remove(listener);
	}

}
