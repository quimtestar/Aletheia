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
package aletheia.gui.contextjtree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.Logger;

import aletheia.gui.common.PersistentTreeModel;
import aletheia.gui.contextjtree.node.ContextSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.ContextJTreeNode;
import aletheia.gui.contextjtree.node.GroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.RootContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.ListChanges;

public class ContextJTreeModel extends PersistentTreeModel
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final SorterTreeNodeMap nodeMap;
	private final Set<ContextJTree.TreeModelListener> contextJTreeListeners;
	private final StatementListener statementListener;
	private final BlockingQueue<StatementStateChange> statementStateChangeQueue;
	private final StatementStateProcessorThread statementStateProcessorThread;
	private RootContextJTreeNode rootTreeNode;

	public ContextJTreeModel(PersistenceManager persistenceManager)
	{
		super(persistenceManager);
		this.nodeMap = new SorterTreeNodeMap(this);
		this.contextJTreeListeners = Collections.synchronizedSet(new HashSet<ContextJTree.TreeModelListener>());
		this.statementListener = new StatementListener();
		persistenceManager.getListenerManager().getRootContextTopStateListeners().add(statementListener);
		persistenceManager.getListenerManager().getRootContextLocalStateListeners().add(statementListener);
		this.statementStateChangeQueue = new LinkedBlockingQueue<StatementStateChange>();
		this.statementStateProcessorThread = new StatementStateProcessorThread();
		this.statementStateProcessorThread.start();
		this.rootTreeNode = null;
	}

	public RootContextJTreeNode getRootTreeNode()
	{
		if (rootTreeNode == null)
			rootTreeNode = new RootContextJTreeNode(this);
		return rootTreeNode;
	}

	public SorterTreeNodeMap getNodeMap()
	{
		return nodeMap;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		super.addTreeModelListener(l);
		if (l instanceof ContextJTree.TreeModelListener)
			contextJTreeListeners.add((ContextJTree.TreeModelListener) l);
	}

	public StatementListener getStatementListener()
	{
		return statementListener;
	}

	@Override
	public synchronized ContextJTreeNode getChild(Object parent, int index)
	{
		return ((ContextJTreeNode) parent).getChildAt(index);
	}

	@Override
	public synchronized int getChildCount(Object parent)
	{
		return ((ContextJTreeNode) parent).getChildCount();
	}

	@Override
	public synchronized int getIndexOfChild(Object parent, Object child)
	{
		return ((ContextJTreeNode) parent).getIndex((ContextJTreeNode) child);
	}

	@Override
	public synchronized RootContextJTreeNode getRoot()
	{
		return getRootTreeNode();
	}

	@Override
	public synchronized boolean isLeaf(Object node)
	{
		return ((ContextJTreeNode) node).isLeaf();
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		super.removeTreeModelListener(l);
		contextJTreeListeners.remove(l);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	public synchronized TreePath pathForStatement(Statement statement)
	{
		StatementContextJTreeNode node = nodeMap.getByStatement(statement);
		if (node == null)
			return null;
		return node.path();
	}

	private synchronized GroupSorterContextJTreeNode<? extends Statement> deleteStatement(Statement statement)
	{
		StatementContextJTreeNode stNode = nodeMap.removeByStatement(statement);
		if (stNode == null)
			return null;
		return stNode.getParent();
	}

	private synchronized StatementContextJTreeNode addStatement(Statement statement)
	{
		return nodeMap.getByStatement(statement);
	}

	private abstract class StatementStateChange
	{
		private final Transaction transaction;
		private final Statement statement;

		public StatementStateChange(Transaction transaction, Statement statement)
		{
			super();
			this.transaction = transaction;
			this.statement = statement;
		}

		public Transaction getTransaction()
		{
			return transaction;
		}

		public Statement getStatement()
		{
			return statement;
		}

	}

	private class ProvedStateChange extends StatementStateChange
	{
		private final boolean proved;

		public ProvedStateChange(Transaction transaction, Statement statement, boolean proved)
		{
			super(transaction, statement);
			this.proved = proved;
		}

		public boolean isProved()
		{
			return proved;
		}
	}

	private abstract class ContextStateChange extends StatementStateChange
	{
		private final Statement statement;

		public ContextStateChange(Transaction transaction, Context context, Statement statement)
		{
			super(transaction, context);
			this.statement = statement;
		}

		public Context getContext()
		{
			return (Context) super.getStatement();
		}

		@Override
		public Statement getStatement()
		{
			return statement;
		}

	}

	private class StatementAddedToContextChange extends ContextStateChange
	{

		public StatementAddedToContextChange(Transaction transaction, Context context, Statement statement)
		{
			super(transaction, context, statement);
		}
	}

	private class StatementDeletedFromContextChange extends ContextStateChange
	{

		public StatementDeletedFromContextChange(Transaction transaction, Context context, Statement statement)
		{
			super(transaction, context, statement);
		}
	}

	private abstract class IdentifierStateChange extends StatementStateChange
	{
		private final Identifier oldId;

		public IdentifierStateChange(Transaction transaction, Statement statement, Identifier oldId)
		{
			super(transaction, statement);
			this.oldId = oldId;
		}

		public Identifier getOldId()
		{
			return oldId;
		}

	}

	private class StatementIdentifiedChange extends IdentifierStateChange
	{
		private final Identifier newId;

		public StatementIdentifiedChange(Transaction transaction, Statement statement, Identifier newId, Identifier oldId)
		{
			super(transaction, statement, oldId);
			this.newId = newId;
		}

		public Identifier getNewId()
		{
			return newId;
		}
	}

	private class StatementUnidentifiedChange extends IdentifierStateChange
	{

		public StatementUnidentifiedChange(Transaction transaction, Statement statement, Identifier oldId)
		{
			super(transaction, statement, oldId);
		}

	}

	private abstract class RootContextStateChange extends StatementStateChange
	{

		public RootContextStateChange(Transaction transaction, RootContext rootContext)
		{
			super(transaction, rootContext);
		}

		@Override
		public RootContext getStatement()
		{
			return (RootContext) super.getStatement();
		}

		public RootContext getRootContext()
		{
			return getStatement();
		}

	}

	private class RootContextAddedChange extends RootContextStateChange
	{

		public RootContextAddedChange(Transaction transaction, RootContext rootContext)
		{
			super(transaction, rootContext);
		}

	}

	private class RootContextDeletedChange extends RootContextStateChange
	{

		public RootContextDeletedChange(Transaction transaction, RootContext rootContext)
		{
			super(transaction, rootContext);
		}

	}

	private class StatementSelect extends StatementStateChange
	{
		private final ContextJTree contextJTree;

		public StatementSelect(Transaction transaction, Statement statement, ContextJTree contextJTree)
		{
			super(transaction, statement);
			this.contextJTree = contextJTree;
		}

		public ContextJTree getContextJTree()
		{
			return contextJTree;
		}

	}

	private class SubscribedStateChange extends StatementStateChange
	{
		public SubscribedStateChange(Transaction transaction, Statement statement)
		{
			super(transaction, statement);
		}
	}

	private class AuthorityStateChange extends StatementStateChange
	{
		public AuthorityStateChange(Transaction transaction, Statement statement)
		{
			super(transaction, statement);
		}
	}

	private class StatementListener implements Statement.StateListener, Nomenclator.Listener, RootContext.TopStateListener, ContextLocal.StateListener,
			RootContextLocal.StateListener, StatementAuthority.StateListener
	{

		@Override
		public void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
		{
			try
			{
				statementStateChangeQueue.put(new ProvedStateChange(transaction, statement, proved));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementAddedToContext(Transaction transaction, Context context, Statement statement)
		{
			try
			{
				statementStateChangeQueue.put(new StatementAddedToContextChange(transaction, context, statement));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier)
		{
			try
			{
				statementStateChangeQueue.put(new StatementDeletedFromContextChange(transaction, context, statement));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementIdentified(Transaction transaction, Statement statement, Identifier newId, Identifier oldId)
		{
			try
			{
				statementStateChangeQueue.put(new StatementIdentifiedChange(transaction, statement, newId, oldId));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementUnidentified(Transaction transaction, Statement statement, Identifier oldId)
		{
			try
			{
				statementStateChangeQueue.put(new StatementUnidentifiedChange(transaction, statement, oldId));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
			try
			{
				statementStateChangeQueue.put(new AuthorityStateChange(transaction, statement));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
			try
			{
				statementStateChangeQueue.put(new AuthorityStateChange(transaction, statement));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void rootContextAdded(Transaction transaction, RootContext rootContext)
		{
			try
			{
				statementStateChangeQueue.put(new RootContextAddedChange(transaction, rootContext));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void rootContextDeleted(Transaction transaction, RootContext rootContext, Identifier identifier)
		{
			try
			{
				statementStateChangeQueue.put(new RootContextDeletedChange(transaction, rootContext));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void subscribeProofChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
		{
			try
			{
				statementStateChangeQueue.put(new SubscribedStateChange(transaction, rootContextLocal.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void subscribeStatementsChanged(Transaction transaction, RootContextLocal rootContextLocal, boolean subscribed)
		{
			try
			{
				statementStateChangeQueue.put(new SubscribedStateChange(transaction, rootContextLocal.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void subscribeProofChanged(Transaction transaction, ContextLocal contextLocal, StatementLocal statementLocal, boolean subscribed)
		{
			try
			{
				statementStateChangeQueue.put(new SubscribedStateChange(transaction, statementLocal.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void subscribeStatementsChanged(Transaction transaction, ContextLocal contextLocal, ContextLocal contextLocal_, boolean subscribed)
		{
			try
			{
				statementStateChangeQueue.put(new SubscribedStateChange(transaction, contextLocal_.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
		{
			try
			{
				statementStateChangeQueue.put(new AuthorityStateChange(transaction, statementAuthority.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
			try
			{
				statementStateChangeQueue.put(new AuthorityStateChange(transaction, statementAuthority.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
		{
			try
			{
				statementStateChangeQueue.put(new AuthorityStateChange(transaction, statementAuthority.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
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

	}

	public void pushSelectStatement(Transaction transaction, Statement statement, ContextJTree contextJTree)
	{
		try
		{
			statementStateChangeQueue.put(new StatementSelect(transaction, statement, contextJTree));
		}
		catch (InterruptedException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public void shutdown() throws InterruptedException
	{
		getPersistenceManager().getListenerManager().getRootContextTopStateListeners().remove(statementListener);
		getPersistenceManager().getListenerManager().getRootContextLocalStateListeners().remove(statementListener);
		statementStateProcessorThread.shutdown();
		statementStateProcessorThread.join();
	}

	private class StatementStateProcessorThread extends Thread
	{

		private boolean shutdown = false;

		public StatementStateProcessorThread()
		{
			super("ContextTreeModel.StatementStateProcessorThread");
		}

		public void shutdown()
		{
			shutdown = true;
			interrupt();
		}

		@Override
		public void run()
		{
			while (!shutdown)
			{
				try
				{
					StatementStateChange c = statementStateChangeQueue.take();
					if (c.getTransaction() != null)
						c.getTransaction().waitForClose();
					synchronized (ContextJTreeModel.this)
					{
						if (c.getTransaction() == null || c.getTransaction().isCommited())
						{
							Transaction transaction = getPersistenceManager().beginTransaction(1000);
							try
							{
								if (c instanceof RootContextAddedChange)
									rootContextAdded((RootContextAddedChange) c, transaction);
								else if (c instanceof RootContextDeletedChange)
									rootContextDeleted((RootContextDeletedChange) c, transaction);
								else if (c instanceof ProvedStateChange)
									provedStateChanged((ProvedStateChange) c, transaction);
								else if (c instanceof StatementAddedToContextChange)
									statementAddedToContext((StatementAddedToContextChange) c, transaction);
								else if (c instanceof StatementDeletedFromContextChange)
									statementDeletedFromContext((StatementDeletedFromContextChange) c, transaction);
								else if (c instanceof StatementIdentifiedChange)
									statementIdentified((StatementIdentifiedChange) c, transaction);
								else if (c instanceof StatementUnidentifiedChange)
									statementUnidentified((StatementUnidentifiedChange) c, transaction);
								else if (c instanceof StatementSelect)
									statementSelect((StatementSelect) c, transaction);
								else if (c instanceof SubscribedStateChange)
									subscribedStateChange((SubscribedStateChange) c, transaction);
								else if (c instanceof AuthorityStateChange)
									authorityStateChange((AuthorityStateChange) c, transaction);
								else
									throw new Error();
							}
							catch (PersistenceLockTimeoutException e)
							{
								statementStateChangeQueue.put(c);
							}
							finally
							{
								transaction.abort();
							}
						}
					}
				}
				catch (InterruptedException e)
				{
					continue;
				}
			}
		}

		private void rootContextAdded(RootContextAddedChange c, Transaction transaction)
		{
			rootContextAdded(c.getRootContext(), transaction);
		}

		private void rootContextAdded(RootContext rootContext, Transaction transaction)
		{
			StatementContextJTreeNode node = addStatement(rootContext.refresh(transaction));
			if (node != null)
			{
				GroupSorterContextJTreeNode<? extends Statement> pNode = node.getParent();
				if (!pNode.checkStatementInsert(rootContext))
					nodeStructureChanged(pNode);
				if (!(pNode instanceof RootContextJTreeNode))
					nodeStructureChanged(pNode.getParent());
			}
		}

		private void rootContextDeleted(RootContextDeletedChange c, Transaction transaction)
		{
			rootContextDeleted(c.getRootContext(), transaction);
		}

		private void rootContextDeleted(RootContext rootContext, Transaction transaction)
		{
			GroupSorterContextJTreeNode<? extends Statement> pNode = deleteStatement(rootContext);
			if ((pNode != null) && !pNode.checkStatementRemove(rootContext))
			{
				nodeStructureChanged(pNode);
				if (pNode.isDegenerate())
				{
					GroupSorterContextJTreeNode<? extends Statement> ppNode = pNode.getParent();
					if (ppNode != null)
						nodeStructureChanged(ppNode);
				}
			}
		}

		private void provedStateChanged(ProvedStateChange c, Transaction transaction)
		{
			provedStateChanged(c.getStatement(), c.isProved(), transaction);
		}

		private void provedStateChanged(Statement statement, boolean proved, Transaction transaction)
		{
			//TODO
			/*
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementTreeNode node = nodeMap.getStatementTreeNode(statement);
				nodeChanged(node);
				CloseableIterator<Statement> iterator = statement.dependents(transaction).iterator();
				try
				{
					while (iterator.hasNext())
					{
						Statement dep = iterator.next();
						if (nodeMap.cachedStatement(dep))
							nodeChanged(nodeMap.getStatementTreeNode(dep));
					}
				}
				finally
				{
					iterator.close();
				}
				if (node instanceof ContextTreeNode)
				{
					ConsequentTreeNode nodecons = ((ContextTreeNode) node).getConsequentTreeNode();
					nodeChanged(nodecons);
				}
			}
			 */
		}

		private void statementAddedToContext(StatementAddedToContextChange c, Transaction transaction)
		{
			statementAddedToContext(c.getContext(), c.getStatement(), transaction);
		}

		private void statementAddedToContext(Context context, Statement statement, Transaction transaction)
		{
			//TODO
			/*
			ContextTreeNode ctxNode=(ContextTreeNode)nodeMap.getStatementTreeNode(context);
			if (ctxNode!=null)
			{
				BranchTreeNode btNode=ctxNode.findStatementInsertNode(statement.refresh(transaction));
				if (btNode!=null)
					nodeStructureChanged(btNode);
			}

			if (statement.getTerm() instanceof SimpleTerm)
			{
				CloseableIterator<Context> iterator = ctxNode.getContext().descendantContextsByConsequent(transaction, statement.getTerm()).iterator();
				try
				{
					while (iterator.hasNext())
					{
						Context ctx_ = iterator.next();
						if (nodeMap.cachedStatement(ctx_))
						{
							ContextTreeNode ctxNode_ = (ContextTreeNode) nodeMap.getStatementTreeNode(ctx_);
							nodeChanged(ctxNode_.getConsequentTreeNode());
						}
					}
				}
				finally
				{
					iterator.close();
				}
			}
			 */
		}

		private void statementIdentified(StatementIdentifiedChange c, Transaction transaction)
		{
			statementIdentified(c.getStatement(), c.getNewId(), c.getOldId(), transaction);
		}

		private void statementIdentified(Statement statement, Identifier newId, Identifier oldId, Transaction transaction)
		{
			statementIdentifierChanged(statement, newId, oldId, transaction);
		}

		private void statementUnidentified(StatementUnidentifiedChange c, Transaction transaction)
		{
			statementUnidentified(c.getStatement(), c.getOldId(), transaction);
		}

		private void statementUnidentified(Statement statement, Identifier oldId, Transaction transaction)
		{
			statementIdentifierChanged(statement, null, oldId, transaction);
		}

		private void statementIdentifierChanged(final Statement statement, Identifier newId, Identifier oldId, Transaction transaction)
		{
			//TODO
			System.out.println("statementIdentifierChanged");
			/*
			Context context=statement.getContext(transaction);
			ContextSorterContextJTreeNode ctxNode=(ContextSorterContextJTreeNode)nodeMap.getByStatement(context);
			if (ctxNode!=null)
			{
				BranchTreeNode btNode=ctxNode.findStatementDeleteNode(statement.refresh(transaction));
				if (btNode!=null)
					nodeStructureChanged(btNode);

			}





			StatementTreeNode node = nodeMap.getStatementTreeNode(statement);
			nodeChanged(node);
			if (!node.getParent().checkStatementInsert(statement))
				nodeStructureChanged(node.getParent());
			if (statement instanceof RootContext)
				nodeChanged(rootTreeNode);
			else
			{
				Context ctx = statement.getContext(transaction);
				if (nodeMap.cachedStatement(ctx))
				{
					StatementTreeNode node_ = nodeMap.getStatementTreeNode(ctx);
					nodeChanged(node_);
					if (ctx.getConsequent().freeVariables().contains(statement.getVariable()) || ctx.getConsequent().equals(statement.getTerm()))
					{
						ContextTreeNode node__ = (ContextTreeNode) nodeMap.getStatementTreeNode(ctx);
						nodeChanged(node__.getConsequentTreeNode());
					}
				}
			}
			CloseableIterator<Statement> iterator = statement.dependents(transaction).iterator();
			try
			{
				while (iterator.hasNext())
				{
					Statement user = iterator.next();
					if (nodeMap.cachedStatement(user))
					{
						StatementTreeNode node_ = nodeMap.getStatementTreeNode(user);
						nodeChanged(node_);
						if (user instanceof Context)
						{
							Context ctx_ = (Context) user;
							if (ctx_.getConsequent().freeVariables().contains(statement.getVariable()) || ctx_.getConsequent().equals(statement.getTerm()))
							{
								ContextTreeNode node__ = (ContextTreeNode) nodeMap.getStatementTreeNode(ctx_);
								nodeChanged(node__.getConsequentTreeNode());
							}
						}
						else if (user instanceof Declaration)
						{
							Declaration dec = (Declaration) user;
							for (UnfoldingContext unf : dec.unfoldingContexts(transaction))
							{
								if (nodeMap.cachedStatement(unf))
								{
									if (unf.getConsequent().freeVariables().contains(statement.getVariable())
											|| unf.getConsequent().equals(statement.getTerm()))
									{
										ContextTreeNode node__ = (ContextTreeNode) nodeMap.getStatementTreeNode(unf);
										nodeChanged(node__.getConsequentTreeNode());
									}
								}
							}
						}
					}
				}
			}
			finally
			{
				iterator.close();
			}
			if (statement.getTerm() instanceof SimpleTerm)
			{
				CloseableIterator<Context> iterator2 = statement.getContext(transaction).descendantContextsByConsequent(transaction, statement.getTerm())
						.iterator();
				try
				{
					while (iterator2.hasNext())
					{
						Context ctx_ = iterator2.next();
						if (nodeMap.cachedStatement(ctx_))
						{
							ContextTreeNode ctxNode_ = (ContextTreeNode) nodeMap.getStatementTreeNode(ctx_);
							nodeChanged(ctxNode_.getConsequentTreeNode());
						}
					}
				}
				finally
				{
					iterator2.close();
				}
			}
			*/
		}

		private void statementDeletedFromContext(StatementDeletedFromContextChange c, Transaction transaction)
		{
			statementDeletedFromContext(c.getContext(), c.getStatement(), transaction);
		}

		private void statementDeletedFromContext(Context context, Statement statement, Transaction transaction)
		{
			//TODO
			/*
			if (statement.getTerm() instanceof SimpleTerm)
			{
				CloseableIterator<Context> iterator = context.descendantContextsByConsequent(transaction, statement.getTerm()).iterator();
				try
				{
					while (iterator.hasNext())
					{
						Context ctx_ = iterator.next();
						if (nodeMap.cachedStatement(ctx_))
						{
							ContextTreeNode ctxNode_ = (ContextTreeNode) nodeMap.getStatementTreeNode(ctx_);
							nodeChanged(ctxNode_.getConsequentTreeNode());
						}
					}
				}
				finally
				{
					iterator.close();
				}
			}

			ContextTreeNode ctxNode=(ContextTreeNode)nodeMap.getStatementTreeNode(context);
			if (ctxNode!=null)
			{
				BranchTreeNode btNode=ctxNode.findStatementDeleteNode(statement.refresh(transaction));
				if (btNode!=null)
					nodeStructureChanged(btNode);

			}
			 */

		}

		private void statementSelect(StatementSelect c, Transaction transaction)
		{
			statementSelect(c.getStatement(), c.getContextJTree(), transaction);
		}

		private void statementSelect(final Statement statement, final ContextJTree contextJTree, Transaction transaction)
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					if (statement == null)
					{
						contextJTree.clearSelection();
					}
					else
					{
						contextJTree.expandStatement(statement);
						contextJTree.selectStatement(statement, false);
						contextJTree.scrollToVisible(statement);
					}
				}

			});
		}

		private void subscribedStateChange(SubscribedStateChange c, Transaction transaction)
		{
			subscribedStateChange(c.getStatement(), transaction);
		}

		private void subscribedStateChange(Statement statement, Transaction transaction)
		{
			//TODO
			/*
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementTreeNode node = nodeMap.getStatementTreeNode(statement);
				nodeChanged(node);
			}
			 */
		}

		private void authorityStateChange(AuthorityStateChange c, Transaction transaction)
		{
			authorityStateChange(c.getStatement(), transaction);
		}

		private void authorityStateChange(Statement statement, Transaction transaction)
		{
			//TODO
			/*
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementTreeNode node = nodeMap.getStatementTreeNode(statement);
				nodeChanged(node);
			}
			 */
		}

	}

	@Override
	public void cleanRenderers()
	{
		synchronized (nodeMap)
		{
			for (SorterContextJTreeNode node : nodeMap.values())
			{
				node.cleanRenderer();
				if (node instanceof ContextSorterContextJTreeNode)
					((ContextSorterContextJTreeNode) node).getConsequentNode().cleanRenderer();
			}
		}
	}

	void nodeChanged(ContextJTreeNode node)
	{
		node.cleanRenderer();
		final TreeModelEvent e = new TreeModelEvent(this, node.path());

		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (getListeners())
				{
					for (TreeModelListener l : getListeners())
						persistenceLockTimeoutSwingInvokeLaterTreeNodesChanged(l, e);
				}
			}

		});
	}

	private void nodeStructureChanged(GroupSorterContextJTreeNode<? extends Statement> node)
	{
		node.cleanRenderer();
		ListChanges<Sorter> changes = node.changeSorterList();
		for (ListChanges<Sorter>.Element e : changes.removedElements())
			nodeMap.removeKey(e.object);
		final TreeModelEvent eRemoves;
		if (!changes.removedElements().isEmpty())
		{
			int indexes[] = new int[changes.removedElements().size()];
			int i = 0;
			for (ListChanges<Sorter>.Element e : changes.removedElements())
			{
				indexes[i] = e.index;
				i++;
			}
			eRemoves = new TreeModelEvent(this, node.path(), indexes, null);
		}
		else
			eRemoves = null;
		final TreeModelEvent eInserts;
		if (!changes.insertedElements().isEmpty())
		{
			int indexes[] = new int[changes.insertedElements().size()];
			Object objects[] = new Object[changes.insertedElements().size()];
			int i = 0;
			for (ListChanges<Sorter>.Element e : changes.insertedElements())
			{
				indexes[i] = e.index;
				objects[i] = e.object;
				i++;
			}
			eInserts = new TreeModelEvent(this, node.path(), indexes, objects);
		}
		else
			eInserts = null;
		if (eRemoves != null || eInserts != null)
		{
			final TreeModelEvent eStructure = new TreeModelEvent(this, node.path());

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (getListeners())
					{
						for (TreeModelListener l : getListeners())
						{
							try
							{
								if (eRemoves != null)
									l.treeNodesRemoved(eRemoves);
								if (eInserts != null)
									l.treeNodesInserted(eInserts);
							}
							catch (PersistenceLockTimeoutException e)
							{
								persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(l, eStructure);
							}
						}
					}
				}

			});
		}
	}

}
