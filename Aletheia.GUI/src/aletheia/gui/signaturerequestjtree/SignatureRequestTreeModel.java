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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import aletheia.gui.common.PersistentTreeModel;
import aletheia.model.authority.PackedSignatureRequest;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CacheWithCleanerMap;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredSet;
import aletheia.utilities.collections.WeakCacheWithCleanerMap;
import aletheia.utilities.collections.WeakHashSet;

public class SignatureRequestTreeModel extends PersistentTreeModel
{
	private final RootSignatureRequestTreeNode rootSignatureRequestTreeNode;

	private class NodeStateListener implements SignatureRequest.AddStateListener, SignatureRequest.StateListener, Statement.StateListener,
			StatementAuthority.StateListener, Nomenclator.Listener, RootContext.TopStateListener
	{

		@Override
		public void signatureRequestAdded(Transaction transaction, SignatureRequest signatureRequest)
		{
			SignatureRequestTreeNode branchNode_ = null;
			SignatureRequestTreeNode parent = rootSignatureRequestTreeNode;
			for (UUID contextUuid : signatureRequest.contextUuidPath())
			{
				ContextSignatureRequestTreeNode node = makeContextNode(parent, transaction, contextUuid);
				if (!parent.isCachedChild(node))
				{
					parent.rebuild();
					if (branchNode_ == null)
						branchNode_ = parent;
				}
				parent = node;
			}
			parent.rebuild();
			if (branchNode_ == null)
				branchNode_ = parent;
			final SignatureRequestTreeNode branchNode = branchNode_;
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							TreeModelEvent e = new TreeModelEvent(this, branchNode.path());
							Collection<TreeModelListener> listeners = getListeners();
							synchronized (listeners)
							{
								for (TreeModelListener listener : listeners)
									persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(listener, e);
							}
						}
					});
				}
			});
		}

		@Override
		public void signatureRequestModified(Transaction transaction, SignatureRequest signatureRequest)
		{
			final RequestSignatureRequestTreeNode node = cachedRequestNode(signatureRequest);
			if (node != null)
			{
				node.rebuild();
				node.clearRenderer();
				transaction.runWhenCommit(new Transaction.Hook()
				{
					@Override
					public void run(Transaction closedTransaction)
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								TreeModelEvent e = new TreeModelEvent(this, node.path());
								Collection<TreeModelListener> listeners = getListeners();
								synchronized (listeners)
								{
									for (TreeModelListener listener : listeners)
										persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(listener, e);
								}
							}
						});
					}
				});
			}

		}

		@Override
		public void signatureRequestRemoved(Transaction transaction, SignatureRequest signatureRequest)
		{
			final RequestSignatureRequestTreeNode node = cachedRequestNode(signatureRequest);
			if (node != null)
			{
				transaction.runWhenCommit(new Transaction.Hook()
				{
					@Override
					public void run(Transaction closedTransaction)
					{
						removeFromCache(node);
						SignatureRequestTreeNode node_ = node.getParent();
						while (true)
						{
							node_.rebuild();
							if (node_.getChildCount() > 0)
								break;
							removeFromCache(node_);
							if (node_ instanceof RootSignatureRequestTreeNode)
								break;
							node_ = node_.getParent();
						}
						final SignatureRequestTreeNode node__ = node_;
						SwingUtilities.invokeLater(new Runnable()
						{

							@Override
							public void run()
							{
								TreeModelEvent e = new TreeModelEvent(this, node__.path());
								Collection<TreeModelListener> listeners = getListeners();
								synchronized (listeners)
								{
									for (TreeModelListener listener : listeners)
										persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(listener, e);
								}
							}
						});
					}
				});
			}

		}

		private void clearStatementRenderers(Transaction transaction, final Statement statement)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					final Set<StatementSignatureRequestTreeNode> nodes = statementReverseCachedNodesMap(statement);
					if (nodes != null)
					{
						SwingUtilities.invokeLater(new Runnable()
						{

							@Override
							public void run()
							{
								synchronized (nodes)
								{
									for (SignatureRequestTreeNode node : nodes)
									{
										node.clearRenderer();
										TreeModelEvent e = new TreeModelEvent(this, node.path());
										Collection<TreeModelListener> listeners = getListeners();
										synchronized (listeners)
										{
											for (TreeModelListener listener : listeners)
												persistenceLockTimeoutSwingInvokeLaterTreeNodesChanged(listener, e);
										}
									}
								}
							}
						});
					}

				}
			});
		}

		@Override
		public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
		{
			clearStatementRenderers(transaction, statement);
		}

		private void statementAddedOrRemovedFromNode(Transaction transaction, final SignatureRequestTreeNode node)
		{
			node.rebuildChildNodeList();
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							TreeModelEvent e = new TreeModelEvent(this, node.path());
							Collection<TreeModelListener> listeners = getListeners();
							synchronized (listeners)
							{
								for (TreeModelListener listener : listeners)
									persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(listener, e);
							}
						}
					});

				}
			});
		}

		private void rootContextAddedOrRemoved(Transaction transaction, RootContext rootContext)
		{
			if (rootSignatureRequestTreeNode.signatureRequestSubContextUuidsCollection(transaction).contains(rootContext.getUuid()))
				statementAddedOrRemovedFromNode(transaction, rootSignatureRequestTreeNode);
		}

		private void statementAddedOrRemovedFromContext(Transaction transaction, Context context, Statement statement)
		{
			ActualContextSignatureRequestTreeNode contextNode = cachedActualContextNode(context);
			if (contextNode != null && contextNode.signatureRequestSubContextUuidsCollection(transaction).contains(statement.getUuid()))
				statementAddedOrRemovedFromNode(transaction, contextNode);
		}

		@Override
		public void statementAddedToContext(Transaction transaction, Context context, Statement statement)
		{
			statementAddedOrRemovedFromContext(transaction, context, statement);
		}

		@Override
		public void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier)
		{
			statementAddedOrRemovedFromContext(transaction, context, statement);
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
		}

		@Override
		public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
		{
			clearStatementRenderers(transaction, statementAuthority.getStatement(transaction));
		}

		@Override
		public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
			clearStatementRenderers(transaction, statementAuthority.getStatement(transaction));
		}

		@Override
		public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
		{
			clearStatementRenderers(transaction, statementAuthority.getStatement(transaction));
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

		@Override
		public void statementIdentified(Transaction transaction, Statement statement, Identifier identifier)
		{
			clearStatementRenderers(transaction, statement);
		}

		@Override
		public void statementUnidentified(Transaction transaction, Statement statement, Identifier identifier)
		{
			clearStatementRenderers(transaction, statement);
		}

		@Override
		public void rootContextAdded(Transaction transaction, RootContext rootContext)
		{
			rootContextAddedOrRemoved(transaction, rootContext);
		}

		@Override
		public void rootContextDeleted(Transaction transaction, RootContext rootContext, Identifier identifier)
		{
			rootContextAddedOrRemoved(transaction, rootContext);
		}

	}

	private final NodeStateListener nodeStateListener;

	private static abstract class CacheKey
	{
		@Override
		public int hashCode()
		{
			return 1;
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
			return true;
		}

	}

	private static class ContextCacheKey extends CacheKey
	{
		private final UUID contextUuid;

		public ContextCacheKey(UUID contextUuid)
		{
			this.contextUuid = contextUuid;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + contextUuid.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContextCacheKey other = (ContextCacheKey) obj;
			if (!contextUuid.equals(other.contextUuid))
				return false;
			return true;
		}

	}

	private static class RequestCacheKey extends CacheKey
	{
		private final UUID requestUuid;

		public RequestCacheKey(UUID requestUuid)
		{
			super();
			this.requestUuid = requestUuid;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + requestUuid.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			RequestCacheKey other = (RequestCacheKey) obj;
			if (!requestUuid.equals(other.requestUuid))
				return false;
			return true;
		}

	}

	private static class StatementCacheKey extends CacheKey
	{
		private final UUID requestUuid;
		private final UUID statementUuid;

		public StatementCacheKey(UUID requestUuid, UUID statementUuid)
		{
			super();
			this.requestUuid = requestUuid;
			this.statementUuid = statementUuid;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((requestUuid == null) ? 0 : requestUuid.hashCode());
			result = prime * result + ((statementUuid == null) ? 0 : statementUuid.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatementCacheKey other = (StatementCacheKey) obj;
			if (!requestUuid.equals(other.requestUuid))
				return false;
			if (!statementUuid.equals(other.statementUuid))
				return false;
			return true;
		}

	}

	private final WeakCacheWithCleanerMap<CacheKey, SignatureRequestTreeNode> nodeCacheMap;

	private final Map<UUID, Set<StatementSignatureRequestTreeNode>> statementReverseCachedNodesMap;

	private class NodeCacheMapListener implements CacheWithCleanerMap.Listener<CacheKey>
	{

		@Override
		public void keyCleaned(final CacheKey key)
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{
					unlistenCacheKey(key);
				}

			});
		}
	}

	public SignatureRequestTreeModel(PersistenceManager persistenceManager)
	{
		super(persistenceManager);
		this.rootSignatureRequestTreeNode = new RootSignatureRequestTreeNode(this);
		this.nodeStateListener = new NodeStateListener();
		getPersistenceManager().getListenerManager().getSignatureRequestAddStateListeners().add(nodeStateListener);
		getPersistenceManager().getListenerManager().getRootContextTopStateListeners().add(nodeStateListener);
		this.nodeCacheMap = new WeakCacheWithCleanerMap<>();
		this.statementReverseCachedNodesMap = new HashMap<UUID, Set<StatementSignatureRequestTreeNode>>();
		this.nodeCacheMap.addListener(new NodeCacheMapListener());
	}

	protected NodeStateListener getNodeStateListener()
	{
		return nodeStateListener;
	}

	private synchronized void putToStatementReverseCachedNodesMap(Statement statement, StatementSignatureRequestTreeNode node)
	{
		Set<StatementSignatureRequestTreeNode> nodes = statementReverseCachedNodesMap.get(statement.getUuid());
		if (nodes == null)
		{
			nodes = new WeakHashSet<>();
			statementReverseCachedNodesMap.put(statement.getUuid(), nodes);

		}
		nodes.add(node);
	}

	private synchronized Set<StatementSignatureRequestTreeNode> statementReverseCachedNodesMap(Statement statement)
	{
		Set<StatementSignatureRequestTreeNode> set = statementReverseCachedNodesMap.get(statement.getUuid());
		if (set == null)
			return null;
		return Collections.unmodifiableSet(statementReverseCachedNodesMap.get(statement.getUuid()));
	}

	private synchronized boolean removeFromStatementReverseCachedNodesMap(final StatementCacheKey statementCacheKey)
	{
		boolean removed = false;
		Set<StatementSignatureRequestTreeNode> set = statementReverseCachedNodesMap.get(statementCacheKey.statementUuid);
		if (set == null)
			removed = true;
		else
		{
			synchronized (set)
			{
				set.removeAll(new BufferedList<>(new FilteredSet<>(new Filter<StatementSignatureRequestTreeNode>()
				{

					@Override
					public boolean filter(StatementSignatureRequestTreeNode node)
					{
						return node.getUnpackedSignatureRequest().getUuid().equals(statementCacheKey.requestUuid);
					}
				}, set)));
				if (set.isEmpty())
				{
					statementReverseCachedNodesMap.remove(statementCacheKey.statementUuid);
					removed = true;
				}
			}
		}
		return removed;
	}

	public synchronized ContextSignatureRequestTreeNode cachedContextNode(UUID contextUuid)
	{
		return (ContextSignatureRequestTreeNode) nodeCacheMap.get(new ContextCacheKey(contextUuid));
	}

	public synchronized ActualContextSignatureRequestTreeNode cachedActualContextNode(Context context)
	{
		ContextSignatureRequestTreeNode node = cachedContextNode(context.getUuid());
		return node instanceof ActualContextSignatureRequestTreeNode ? (ActualContextSignatureRequestTreeNode) node : null;
	}

	private synchronized void putCachedActualContextNode(Context context, ActualContextSignatureRequestTreeNode contextNode)
	{
		nodeCacheMap.put(new ContextCacheKey(context.getUuid()), contextNode);
	}

	public synchronized ActualContextSignatureRequestTreeNode makeActualContextNode(SignatureRequestTreeNode parent, Context context)
	{
		ActualContextSignatureRequestTreeNode node = cachedActualContextNode(context);
		if (node == null || !parent.equals(node.getParent()))
		{
			node = new ActualContextSignatureRequestTreeNode(this, parent, context);
			putCachedActualContextNode(context, node);
		}
		return node;
	}

	public synchronized ActualContextSignatureRequestTreeNode makeActualContextNode(RootContext rootContext)
	{
		return makeActualContextNode(rootSignatureRequestTreeNode, rootContext);
	}

	public synchronized VirtualContextSignatureRequestTreeNode cachedVirtualContextNode(UUID uuid)
	{
		ContextSignatureRequestTreeNode node = cachedContextNode(uuid);
		return node instanceof VirtualContextSignatureRequestTreeNode ? (VirtualContextSignatureRequestTreeNode) node : null;
	}

	private synchronized void putCachedVirtualContextNode(UUID contextUuid, VirtualContextSignatureRequestTreeNode contextNode)
	{
		nodeCacheMap.put(new ContextCacheKey(contextUuid), contextNode);
	}

	public synchronized VirtualContextSignatureRequestTreeNode makeVirtualContextNode(SignatureRequestTreeNode parent, UUID contextUuid)
	{
		VirtualContextSignatureRequestTreeNode node = cachedVirtualContextNode(contextUuid);
		if (node == null || !parent.equals(node.getParent()))
		{
			node = new VirtualContextSignatureRequestTreeNode(this, parent, contextUuid);
			putCachedVirtualContextNode(contextUuid, node);
		}
		return node;
	}

	public synchronized VirtualContextSignatureRequestTreeNode makeVirtualContextNode(UUID rootContextUuid)
	{
		return makeVirtualContextNode(rootSignatureRequestTreeNode, rootContextUuid);
	}

	public synchronized ContextSignatureRequestTreeNode makeContextNode(SignatureRequestTreeNode parent, Transaction transaction, UUID contextUuid)
	{
		Context context = getPersistenceManager().getContext(transaction, contextUuid);
		return context == null ? makeVirtualContextNode(parent, contextUuid) : makeActualContextNode(parent, context);
	}

	public synchronized ContextSignatureRequestTreeNode makeContextNode(Transaction transaction, UUID rootContextUuid)
	{
		return makeContextNode(rootSignatureRequestTreeNode, transaction, rootContextUuid);
	}

	public synchronized RequestSignatureRequestTreeNode cachedRequestNode(SignatureRequest request)
	{
		RequestSignatureRequestTreeNode node = (RequestSignatureRequestTreeNode) nodeCacheMap.get(new RequestCacheKey(request.getUuid()));
		if (request instanceof PackedSignatureRequest)
			return (node instanceof PackedRequestSignatureRequestTreeNode ? (PackedRequestSignatureRequestTreeNode) node : null);
		else if (request instanceof UnpackedSignatureRequest)
			return (node instanceof UnpackedRequestSignatureRequestTreeNode ? (UnpackedRequestSignatureRequestTreeNode) node : null);
		else
			throw new Error();
	}

	private synchronized void putCachedRequestNode(SignatureRequest request, RequestSignatureRequestTreeNode requestNode)
	{
		nodeCacheMap.put(new RequestCacheKey(request.getUuid()), requestNode);
	}

	public synchronized RequestSignatureRequestTreeNode makeRequestNode(ContextSignatureRequestTreeNode parent, SignatureRequest request)
	{
		RequestSignatureRequestTreeNode node = cachedRequestNode(request);
		if (node == null || !parent.equals(node.getParent()))
		{
			if (request instanceof UnpackedSignatureRequest)
				node = new UnpackedRequestSignatureRequestTreeNode(this, (ActualContextSignatureRequestTreeNode) parent, (UnpackedSignatureRequest) request);
			else if (request instanceof PackedSignatureRequest)
				node = new PackedRequestSignatureRequestTreeNode(this, parent, (PackedSignatureRequest) request);
			else
				throw new Error();
			putCachedRequestNode(request, node);
		}
		return node;
	}

	public synchronized StatementSignatureRequestTreeNode cachedStatementNode(UnpackedSignatureRequest request, Statement statement)
	{
		return (StatementSignatureRequestTreeNode) nodeCacheMap.get(new StatementCacheKey(request.getUuid(), statement.getUuid()));
	}

	private synchronized void putCachedStatementNode(UnpackedSignatureRequest request, Statement statement, StatementSignatureRequestTreeNode statementNode)
	{
		nodeCacheMap.put(new StatementCacheKey(request.getUuid(), statement.getUuid()), statementNode);
		putToStatementReverseCachedNodesMap(statement, statementNode);
	}

	public synchronized StatementSignatureRequestTreeNode makeStatementNode(UnpackedRequestSignatureRequestTreeNode parent, Statement statement)
	{
		StatementSignatureRequestTreeNode node = cachedStatementNode(parent.getSignatureRequest(), statement);
		if (node == null || !parent.equals(node.getParent()))
		{
			node = new StatementSignatureRequestTreeNode(this, parent, statement);
			putCachedStatementNode(parent.getSignatureRequest(), statement, node);
		}
		return node;
	}

	@Override
	public int getChildCount(Object parent)
	{
		if (parent instanceof SignatureRequestTreeNode)
			return ((SignatureRequestTreeNode) parent).getChildCount();
		else
			return 0;
	}

	@Override
	public boolean isLeaf(Object node)
	{
		if (node instanceof SignatureRequestTreeNode)
			return ((SignatureRequestTreeNode) node).isLeaf();
		else
			return false;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent instanceof SignatureRequestTreeNode && child instanceof SignatureRequestTreeNode)
			return ((SignatureRequestTreeNode) parent).getIndex((SignatureRequestTreeNode) child);
		else
			return -1;
	}

	@Override
	public synchronized void cleanRenderers()
	{
		rootSignatureRequestTreeNode.clearRenderer();
		for (SignatureRequestTreeNode node : nodeCacheMap.values())
			node.clearRenderer();
	}

	@Override
	public RootSignatureRequestTreeNode getRoot()
	{
		return rootSignatureRequestTreeNode;
	}

	@Override
	public SignatureRequestTreeNode getChild(Object parent, int index)
	{
		if (parent instanceof SignatureRequestTreeNode)
			return ((SignatureRequestTreeNode) parent).getChildAt(index);
		else
			return null;
	}

	public TreePath pathForUnpackedSignatureRequest(Transaction transaction, UnpackedSignatureRequest unpackedSignatureRequest)
	{
		UnpackedRequestSignatureRequestTreeNode node = nodeForUnpackedSignatureRequest(transaction, unpackedSignatureRequest);
		if (node == null)
			return null;
		return node.path();
	}

	public TreePath pathForContext(Transaction transaction, Context context)
	{
		ActualContextSignatureRequestTreeNode node = nodeForContext(transaction, context);
		if (node == null)
			return null;
		return node.path();
	}

	public TreePath pathForStatement(Transaction transaction, UnpackedSignatureRequest unpackedSignatureRequest, Statement statement)
	{
		StatementSignatureRequestTreeNode node = nodeForStatement(transaction, unpackedSignatureRequest, statement);
		if (node == null)
			return null;
		return node.path();
	}

	private UnpackedRequestSignatureRequestTreeNode nodeForUnpackedSignatureRequest(Transaction transaction, UnpackedSignatureRequest unpackedSignatureRequest)
	{
		ActualContextSignatureRequestTreeNode contextNode = nodeForContext(transaction, unpackedSignatureRequest.getContext(transaction));
		if (contextNode == null)
			return null;
		return (UnpackedRequestSignatureRequestTreeNode) contextNode.requestNode(transaction, unpackedSignatureRequest);
	}

	private ActualContextSignatureRequestTreeNode nodeForContext(Transaction transaction, Context context)
	{
		ActualContextSignatureRequestTreeNode node = null;
		for (Context ctx : context.statementPath(transaction))
		{
			if (ctx instanceof RootContext)
				node = rootSignatureRequestTreeNode.contextNode(transaction, (RootContext) ctx);
			else
				node = node.actualContextNode(transaction, ctx);
			if (node == null)
				return null;
		}
		return node;
	}

	private StatementSignatureRequestTreeNode nodeForStatement(Transaction transaction, UnpackedSignatureRequest unpackedSignatureRequest, Statement statement)
	{
		UnpackedRequestSignatureRequestTreeNode requestNode = nodeForUnpackedSignatureRequest(transaction, unpackedSignatureRequest);
		if (requestNode == null)
			return null;
		return requestNode.statementNode(transaction, statement);
	}

	public synchronized void shutdown()
	{
		for (CacheKey key : nodeCacheMap.keySet())
			unlistenCacheKey(key);
		getPersistenceManager().getListenerManager().getSignatureRequestAddStateListeners().remove(getNodeStateListener());
	}

	private synchronized void unlistenCacheKey(CacheKey cacheKey)
	{
		if (!nodeCacheMap.containsKey(cacheKey))
		{
			if (cacheKey instanceof ContextCacheKey)
			{
				ContextCacheKey contextCacheKey = (ContextCacheKey) cacheKey;
				getPersistenceManager().getListenerManager().getStatementStateListeners().remove(contextCacheKey.contextUuid, nodeStateListener);
				getPersistenceManager().getListenerManager().getSubNomenclatorListeners().remove(contextCacheKey.contextUuid, nodeStateListener);
			}
			else if (cacheKey instanceof RequestCacheKey)
			{
				RequestCacheKey requestCacheKey = (RequestCacheKey) cacheKey;
				getPersistenceManager().getListenerManager().getSignatureRequestStateListeners().remove(requestCacheKey.requestUuid, nodeStateListener);
			}
			else if (cacheKey instanceof StatementCacheKey)
			{
				StatementCacheKey statementCacheKey = (StatementCacheKey) cacheKey;
				if (removeFromStatementReverseCachedNodesMap(statementCacheKey))
				{
					getPersistenceManager().getListenerManager().getStatementStateListeners().remove(statementCacheKey.statementUuid, nodeStateListener);
					getPersistenceManager().getListenerManager().getStatementAuthorityStateListeners()
							.remove(statementCacheKey.statementUuid, nodeStateListener);
				}
			}
			else
				throw new Error();
		}
	}

	private CacheKey nodeCacheKey(SignatureRequestTreeNode node)
	{
		if (node instanceof ActualContextSignatureRequestTreeNode)
			return new ContextCacheKey(((ActualContextSignatureRequestTreeNode) node).getContext().getUuid());
		else if (node instanceof VirtualContextSignatureRequestTreeNode)
			return new ContextCacheKey(((VirtualContextSignatureRequestTreeNode) node).getContextUuid());
		else if (node instanceof RequestSignatureRequestTreeNode)
			return new RequestCacheKey(((RequestSignatureRequestTreeNode) node).getSignatureRequest().getUuid());
		else if (node instanceof StatementSignatureRequestTreeNode)
			return new StatementCacheKey(((StatementSignatureRequestTreeNode) node).getUnpackedSignatureRequest().getUuid(),
					((StatementSignatureRequestTreeNode) node).getStatement().getUuid());
		else
			throw new IllegalArgumentException();
	}

	private synchronized void removeFromCache(SignatureRequestTreeNode node)
	{
		if (node instanceof RootSignatureRequestTreeNode)
			return;
		CacheKey cacheKey = nodeCacheKey(node);
		nodeCacheMap.remove(cacheKey);
		unlistenCacheKey(cacheKey);
	}

	@Deprecated
	public synchronized void clearCache()
	{
		for (CacheKey key : new BufferedList<>(nodeCacheMap.keySet()))
		{
			nodeCacheMap.remove(key);
			unlistenCacheKey(key);
		}
	}

}
