/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.Logger;
import aletheia.gui.common.PersistentTreeModel;
import aletheia.gui.contextjtree.node.ConsequentContextJTreeNode;
import aletheia.gui.contextjtree.node.ContextSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.ContextJTreeNode;
import aletheia.gui.contextjtree.node.GroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.RootContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
import aletheia.gui.contextjtree.node.TopGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Identifier;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.SimpleTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.ListChanges;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.ReverseList;

public class ContextJTreeModel extends PersistentTreeModel
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final SorterTreeNodeMap nodeMap;
	private final Set<ContextJTree.TreeModelListener> contextJTreeListeners;
	private final StatementListener statementListener;
	private final BlockingDeque<StateChange> stateChangeQueue;
	private final StatementStateProcessorThread statementStateProcessorThread;
	private final RootContextJTreeNode rootTreeNode;

	private Context activeContext;

	public ContextJTreeModel(PersistenceManager persistenceManager)
	{
		super(persistenceManager);
		this.nodeMap = new SorterTreeNodeMap(this);
		this.contextJTreeListeners = Collections.synchronizedSet(new HashSet<ContextJTree.TreeModelListener>());
		this.statementListener = new StatementListener();
		persistenceManager.getListenerManager().getRootContextTopStateListeners().add(statementListener);
		persistenceManager.getListenerManager().getRootContextLocalStateListeners().add(statementListener);
		listenRootContextNomenclators();
		this.stateChangeQueue = new LinkedBlockingDeque<>();
		this.statementStateProcessorThread = new StatementStateProcessorThread();
		this.statementStateProcessorThread.start();
		this.rootTreeNode = new RootContextJTreeNode(this);
		this.activeContext = null;
	}

	private void listenRootContextNomenclators()
	{
		Transaction transaction = beginTransaction();
		try
		{
			for (RootContext rootCtx : getPersistenceManager().rootContexts(transaction).values())
				rootCtx.getParentNomenclator(transaction).addListener(statementListener);
		}
		finally
		{
			transaction.abort();
		}
	}

	private void unlistenRootContextNomenclators()
	{
		Transaction transaction = beginTransaction();
		try
		{
			for (RootContext rootCtx : getPersistenceManager().rootContexts(transaction).values())
				rootCtx.getParentNomenclator(transaction).removeListener(statementListener);
		}
		finally
		{
			transaction.abort();
		}

	}

	public RootContextJTreeNode getRootTreeNode()
	{
		return rootTreeNode;
	}

	public SorterTreeNodeMap getNodeMap()
	{
		return nodeMap;
	}

	public Context getActiveContext()
	{
		return activeContext;
	}

	public void setActiveContext(Context activeContext)
	{
		if (((activeContext == null) != (this.activeContext == null)) || (activeContext != null && !activeContext.equals(this.activeContext)))
		{
			putSetActiveContext(null, activeContext, this.activeContext);
			this.activeContext = activeContext;
		}
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

	public synchronized TreePath pathForContextConsequent(Context context)
	{
		ContextSorterContextJTreeNode node = (ContextSorterContextJTreeNode) nodeMap.getByStatement(context);
		if (node == null)
			return null;
		return node.getConsequentNode().path();
	}

	public synchronized TreePath pathForSorter(Sorter sorter)
	{
		SorterContextJTreeNode node = nodeMap.get(sorter);
		if (node == null)
			return null;
		return node.path();
	}

	private synchronized StatementContextJTreeNode deleteStatement(Statement statement)
	{
		return nodeMap.cachedByStatement(statement);
	}

	private synchronized StatementContextJTreeNode addStatement(Statement statement)
	{
		return nodeMap.getByStatement(statement);
	}

	private abstract class StateChange
	{
		private final Transaction transaction;

		public StateChange(Transaction transaction)
		{
			super();
			this.transaction = transaction;
		}

		public Transaction getTransaction()
		{
			return transaction;
		}

	}

	private abstract class StatementStateChange extends StateChange
	{
		private final Statement statement;

		public StatementStateChange(Transaction transaction, Statement statement)
		{
			super(transaction);
			this.statement = statement;
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

		public IdentifierStateChange(Transaction transaction, Statement statement)
		{
			super(transaction, statement);
		}

	}

	private class StatementIdentifiedChange extends IdentifierStateChange
	{
		private final Identifier identifier;

		public StatementIdentifiedChange(Transaction transaction, Statement statement, Identifier identifier)
		{
			super(transaction, statement);
			this.identifier = identifier;
		}

		public Identifier getIdentifier()
		{
			return identifier;
		}
	}

	private class StatementUnidentifiedChange extends IdentifierStateChange
	{

		public StatementUnidentifiedChange(Transaction transaction, Statement statement)
		{
			super(transaction, statement);
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

	private class ContextConsequentSelect extends StatementSelect
	{

		public ContextConsequentSelect(Transaction transaction, Context context, ContextJTree contextJTree)
		{
			super(transaction, context, contextJTree);
		}

		@Override
		public Context getStatement()
		{
			return (Context) super.getStatement();
		}

		public Context getContext()
		{
			return getStatement();
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

	private class SetActiveContextStateChange extends StatementStateChange
	{

		private final Context oldActiveContext;

		public SetActiveContextStateChange(Transaction transaction, Context activeContext, Context oldActiveContext)
		{
			super(transaction, activeContext);
			this.oldActiveContext = oldActiveContext;
		}

		@Override
		public Context getStatement()
		{
			return (Context) super.getStatement();
		}

		public Context getActiveContext()
		{
			return getStatement();
		}

		public Context getOldActiveContext()
		{
			return oldActiveContext;
		}
	}

	private class ParameterIdentificationChange extends StatementStateChange
	{
		public ParameterIdentificationChange(Transaction transaction, Statement statement)
		{
			super(transaction, statement);
		}
	}

	private class ConsequentParameterIdentificationChange extends ParameterIdentificationChange
	{

		public ConsequentParameterIdentificationChange(Transaction transaction, Context context)
		{
			super(transaction, context);
		}

		@Override
		public Context getStatement()
		{
			return (Context) super.getStatement();
		}

		public Context getContext()
		{
			return getStatement();
		}

	}

	private class StructureChange extends StateChange
	{
		public StructureChange(Transaction transaction)
		{
			super(transaction);
		}
	}

	private class ContextStructureChange extends StructureChange
	{
		private final Context context;

		public ContextStructureChange(Transaction transaction, Context context)
		{
			super(transaction);
			this.context = context;
		}

		public Context getContext()
		{
			return context;
		}
	}

	private class RootStructureChange extends StructureChange
	{
		public RootStructureChange(Transaction transaction)
		{
			super(transaction);
		}
	}

	private class CompactHookedChanges extends StateChange
	{
		public CompactHookedChanges(Transaction transaction)
		{
			super(transaction);
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
				stateChangeQueue.put(new ProvedStateChange(transaction, statement, proved));
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
				stateChangeQueue.put(new StatementAddedToContextChange(transaction, context, statement));
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
				stateChangeQueue.put(new StatementDeletedFromContextChange(transaction, context, statement));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementIdentified(Transaction transaction, Statement statement, Identifier identifier)
		{
			try
			{
				stateChangeQueue.put(new StatementIdentifiedChange(transaction, statement, identifier));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void statementUnidentified(Transaction transaction, Statement statement, Identifier identifier)
		{
			try
			{
				stateChangeQueue.put(new StatementUnidentifiedChange(transaction, statement));
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
				stateChangeQueue.put(new AuthorityStateChange(transaction, statement));
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
				stateChangeQueue.put(new AuthorityStateChange(transaction, statement));
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
				stateChangeQueue.put(new RootContextAddedChange(transaction, rootContext));
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
				stateChangeQueue.put(new RootContextDeletedChange(transaction, rootContext));
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
				stateChangeQueue.put(new SubscribedStateChange(transaction, rootContextLocal.getStatement(transaction)));
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
				stateChangeQueue.put(new SubscribedStateChange(transaction, rootContextLocal.getStatement(transaction)));
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
				stateChangeQueue.put(new SubscribedStateChange(transaction, statementLocal.getStatement(transaction)));
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
				stateChangeQueue.put(new SubscribedStateChange(transaction, contextLocal_.getStatement(transaction)));
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
				stateChangeQueue.put(new AuthorityStateChange(transaction, statementAuthority.getStatement(transaction)));
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
				stateChangeQueue.put(new AuthorityStateChange(transaction, statementAuthority.getStatement(transaction)));
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
				stateChangeQueue.put(new AuthorityStateChange(transaction, statementAuthority.getStatement(transaction)));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void valueParameterIdentificationUpdated(Transaction transaction, Declaration declaration, ParameterIdentification valueParameterIdentification)
		{
			try
			{
				stateChangeQueue.put(new ParameterIdentificationChange(transaction, declaration));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void instanceParameterIdentificationUpdated(Transaction transaction, Specialization specialization,
				ParameterIdentification instanceParameterIdentification)
		{
			try
			{
				stateChangeQueue.put(new ParameterIdentificationChange(transaction, specialization));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void termParameterIdentificationUpdated(Transaction transaction, Statement statement, ParameterIdentification termParameterIdentification)
		{
			try
			{
				stateChangeQueue.put(new ParameterIdentificationChange(transaction, statement));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void consequentParameterIdentificationUpdated(Transaction transaction, Context context,
				ParameterIdentification consequentParameterIdentification)
		{
			try
			{
				stateChangeQueue.put(new ConsequentParameterIdentificationChange(transaction, context));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

	}

	public void putSelectStatement(Transaction transaction, Statement statement, ContextJTree contextJTree)
	{
		try
		{
			stateChangeQueue.put(new StatementSelect(transaction, statement, contextJTree));
		}
		catch (InterruptedException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public void putSelectContextConsequent(Transaction transaction, Context context, ContextJTree contextJTree)
	{
		try
		{
			stateChangeQueue.put(new ContextConsequentSelect(transaction, context, contextJTree));
		}
		catch (InterruptedException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public void putSetActiveContext(Transaction transaction, Context activeContext, Context oldActiveContext)
	{
		try
		{
			stateChangeQueue.put(new SetActiveContextStateChange(transaction, activeContext, oldActiveContext));
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
		unlistenRootContextNomenclators();
		nodeMap.clear();
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
			class MyTransactionHook implements Transaction.Hook
			{
				private final static int compactationThreshold = 10;

				private final Transaction transaction;
				private final Map<Transaction, MyTransactionHook> transactionHooks;
				private final Set<StateChange> changeSet;
				private final Map<Context, Set<StateChange>> compactablesByContext;
				private final Set<StateChange> rootCompactables;

				public MyTransactionHook(Transaction transaction, Map<Transaction, MyTransactionHook> transactionHooks)
				{
					this.transaction = transaction;
					this.transactionHooks = transactionHooks;
					this.changeSet = new LinkedHashSet<>();
					this.compactablesByContext = new HashMap<>();
					this.rootCompactables = new HashSet<>();

					transactionHooks.put(transaction, this);
					transaction.runWhenCommit(this);
				}

				@Override
				public void run(Transaction closedTransaction)
				{
					try
					{
						stateChangeQueue.put(new CompactHookedChanges(transaction));
					}
					catch (InterruptedException e)
					{
						logger.error(e.getMessage(), e);
					}
				}

				public void compactChanges()
				{
					List<StructureChange> structureChanges = new ArrayList<>();
					if (rootCompactables.size() >= compactationThreshold)
					{
						changeSet.removeAll(rootCompactables);
						for (Set<StateChange> compactables : compactablesByContext.values())
							changeSet.removeAll(compactables);
						structureChanges.add(new RootStructureChange(transaction));
					}
					else
					{
						for (Map.Entry<Context, Set<StateChange>> e : compactablesByContext.entrySet())
						{
							Context context = e.getKey();
							Set<StateChange> compactables = e.getValue();
							boolean push = true;
							try (Transaction transaction = getPersistenceManager().beginTransaction(1000))
							{
								context = context.refresh(transaction);
								if (context != null)
								{
									for (Context context_ : new ReverseList<>(context.statementPath(transaction)))
										if (context_ != context)
										{
											Set<StateChange> compactables_ = compactablesByContext.get(context_);
											if (compactables_ != null && compactables_.size() >= compactationThreshold)
											{
												push = false;
												break;
											}
										}
								}
							}
							catch (PersistenceLockTimeoutException exc)
							{
							}
							if (compactables.size() >= compactationThreshold)
							{
								changeSet.removeAll(compactables);
								if (push && context != null)
									structureChanges.add(new ContextStructureChange(transaction, context));
							}
						}
					}
					for (StateChange c : new ReverseList<>(new BufferedList<>(changeSet)))
						stateChangeQueue.addFirst(c);
					for (StructureChange c : structureChanges)
						stateChangeQueue.addFirst(c);
					transactionHooks.remove(transaction);
				}

				private void addCompactable(Context context, StateChange c)
				{
					Set<StateChange> compactable = compactablesByContext.get(context);
					if (compactable == null)
					{
						compactable = new HashSet<>();
						compactablesByContext.put(context, compactable);
					}
					compactable.add(c);
				}

				public void addChange(StateChange c)
				{
					changeSet.add(c);
					if (c instanceof IdentifierStateChange)
					{
						if (((IdentifierStateChange) c).getStatement() instanceof RootContext)
							rootCompactables.add(c);
						else
						{
							if (transaction.isOpen())
								addCompactable(((IdentifierStateChange) c).getStatement().getContext(transaction), c);
						}
					}
					else if (c instanceof ContextStateChange)
						addCompactable(((ContextStateChange) c).getContext(), c);
					else if (c instanceof RootContextStateChange)
						rootCompactables.add(c);
				}
			}
			Map<Transaction, MyTransactionHook> transactionHooks = Collections.synchronizedMap(new HashMap<>());
			while (!shutdown)
			{
				try
				{
					StateChange c = stateChangeQueue.take();
					boolean hooked = false;
					if (c.getTransaction() != null)
						synchronized (c.getTransaction())
						{
							if (c instanceof CompactHookedChanges)
							{
								MyTransactionHook hook = transactionHooks.get(c.getTransaction());
								if (hook != null)
									hook.compactChanges();
								hooked = true;
							}
							else if (c.getTransaction().isOpen() || c.getTransaction().isCommited())
							{
								MyTransactionHook hook = transactionHooks.get(c.getTransaction());
								if (hook != null || c.getTransaction().isOpen())
								{
									if (hook == null)
										hook = new MyTransactionHook(c.getTransaction(), transactionHooks);
									hook.addChange(c);
									hooked = true;
								}
							}
						}
					if (c.getTransaction() == null || (!hooked && c.getTransaction().isCommited()))
					{
						for (Transaction transaction : new BufferedList<>(transactionHooks.keySet()))
							transaction.waitForClose();
						synchronized (ContextJTreeModel.this)
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
								else if (c instanceof ContextConsequentSelect)
									contextConsequentSelect((ContextConsequentSelect) c, transaction);
								else if (c instanceof StatementSelect)
									statementSelect((StatementSelect) c, transaction);
								else if (c instanceof SubscribedStateChange)
									subscribedStateChange((SubscribedStateChange) c, transaction);
								else if (c instanceof AuthorityStateChange)
									authorityStateChange((AuthorityStateChange) c, transaction);
								else if (c instanceof SetActiveContextStateChange)
									setActiveContextStateChange((SetActiveContextStateChange) c, transaction);
								else if (c instanceof ParameterIdentificationChange)
									parameterIdentificationChange((ParameterIdentificationChange) c, transaction);
								else if (c instanceof ContextStructureChange)
									contextStructureChange((ContextStructureChange) c, transaction);
								else if (c instanceof RootStructureChange)
									rootStructureChange((RootStructureChange) c, transaction);
								else
									throw new Error();
							}
							catch (PersistenceLockTimeoutException e)
							{
								stateChangeQueue.put(c);
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
			rootContext = rootContext.refresh(transaction);
			StatementContextJTreeNode node = addStatement(rootContext);
			if (node != null)
			{
				GroupSorterContextJTreeNode<? extends Statement> pNode = node.getParent();
				boolean changed = false;
				if (!pNode.checkStatementInsert(rootContext))
				{
					changed = nodeStructureChanged(pNode);
					Identifier prefix = node.parentSorter().getPrefix();
					if (prefix != null && prefix.equals(rootContext.getIdentifier()))
						nodeChangedNoDep(pNode);
				}
				while (!changed && !(pNode instanceof RootContextJTreeNode))
				{
					pNode = pNode.getParent();
					changed = nodeStructureChanged(pNode);
				}
			}
		}

		private void rootContextDeleted(RootContextDeletedChange c, Transaction transaction)
		{
			rootContextDeleted(c.getRootContext(), transaction);
		}

		private void rootContextDeleted(RootContext rootContext, Transaction transaction)
		{
			GroupSorterContextJTreeNode<? extends Statement> pNode = deleteStatement(null, rootContext);
			if ((pNode != null) && !pNode.checkStatementRemove(rootContext))
			{
				nodeStructureChanged(pNode);
				Identifier prefix = pNode.getSorter().getPrefix();
				if (prefix != null && prefix.equals(rootContext.getIdentifier()))
					nodeChangedNoDep(pNode);
				boolean changed = true;
				while (changed && !(pNode instanceof RootContextJTreeNode) && pNode.isDegenerate())
				{
					pNode = pNode.getParent();
					changed = nodeStructureChanged(pNode);
				}
			}

		}

		private void provedStateChanged(ProvedStateChange c, Transaction transaction)
		{
			provedStateChanged(c.getStatement(), c.isProved(), transaction);
		}

		private void provedStateChanged(Statement statement, boolean proved, Transaction transaction)
		{
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementContextJTreeNode node = nodeMap.getByStatement(statement);
				nodeChanged((ContextJTreeNode) node);
				CloseableIterator<Statement> iterator = statement.dependents(transaction).iterator();
				try
				{
					while (iterator.hasNext())
					{
						Statement dep = iterator.next();
						ContextJTreeNode depNode = (ContextJTreeNode) nodeMap.cachedByStatement(dep);
						if (depNode != null)
							nodeChanged(depNode);
					}
				}
				finally
				{
					iterator.close();
				}
				if (node instanceof ContextSorterContextJTreeNode)
				{
					ConsequentContextJTreeNode nodecons = ((ContextSorterContextJTreeNode) node).getConsequentNode();
					nodeChanged(nodecons);
				}
			}
		}

		private void statementAddedToContext(StatementAddedToContextChange c, Transaction transaction)
		{
			statementAddedToContext(c.getContext(), c.getStatement(), transaction);
		}

		private void statementAddedToContext(Context context, Statement statement, Transaction transaction)
		{
			statement = statement.refresh(transaction);
			StatementContextJTreeNode node = addStatement(statement);
			if (node != null)
			{
				GroupSorterContextJTreeNode<? extends Statement> pNode = node.getParent();
				boolean changed = false;
				if (!pNode.checkStatementInsert(statement))
				{
					changed = nodeStructureChanged(pNode);
					Identifier prefix = node.parentSorter().getPrefix();
					if (prefix != null && prefix.equals(statement.getIdentifier()))
						nodeChangedNoDep(pNode);
				}
				while (!changed && !(pNode instanceof ContextSorterContextJTreeNode))
				{
					pNode = pNode.getParent();
					changed = nodeStructureChanged(pNode);
				}
			}
			Context ctx = statement.getContext(transaction);
			if (statement.getTerm() instanceof SimpleTerm)
			{
				CloseableIterator<Context> iterator = ctx.descendantContextsByConsequent(transaction, statement.getTerm()).iterator();
				try
				{
					while (iterator.hasNext())
					{
						Context ctx_ = iterator.next();
						ContextSorterContextJTreeNode ctxNode_ = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(ctx_);
						if (ctxNode_ != null)
							nodeChanged(ctxNode_.getConsequentNode());
					}
				}
				finally
				{
					iterator.close();
				}
			}
		}

		private void statementIdentified(StatementIdentifiedChange c, Transaction transaction)
		{
			statementIdentified(c.getStatement(), c.getIdentifier(), transaction);
		}

		private void statementIdentified(Statement statement, Identifier identifier, Transaction transaction)
		{
			statementIdentifierChanged(statement, transaction);
		}

		private void statementUnidentified(StatementUnidentifiedChange c, Transaction transaction)
		{
			statementUnidentified(c.getStatement(), transaction);
		}

		private void statementUnidentified(Statement statement, Transaction transaction)
		{
			statementIdentifierChanged(statement, transaction);
		}

		private void statementIdentifierChanged(Statement statement, Transaction transaction)
		{
			GroupSorterContextJTreeNode<?> pNode = null;
			StatementContextJTreeNode node = nodeMap.cachedByStatement(statement);
			if (node != null)
			{
				pNode = node.getParent();
				if ((pNode != null) && !pNode.checkStatementRemove(statement))
				{
					nodeStructureChanged(pNode);
					Identifier prefix = pNode.getSorter().getPrefix();
					if (prefix != null && prefix.equals(statement.getIdentifier()))
						nodeChangedNoDep(pNode);
					boolean changed = true;
					while (changed && !(pNode instanceof TopGroupSorterContextJTreeNode) && pNode.isDegenerate())
					{
						pNode = pNode.getParent();
						changed = nodeStructureChanged(pNode);
					}
				}
			}
			statement = statement.refresh(transaction);
			node = addStatement(statement);
			if (node != null)
			{
				node.getNodeMapSorter().setIdentifier(statement.getIdentifier());
				nodeChanged((ContextJTreeNode) node);
				GroupSorterContextJTreeNode<? extends Statement> pNode_ = node.getParent();
				if (!pNode_.equals(pNode))
				{
					pNode = pNode_;
					boolean changed = false;
					if (!pNode.checkStatementInsert(statement))
					{
						changed = nodeStructureChanged(pNode);
						Identifier prefix = pNode.getSorter().getPrefix();
						if (prefix != null && prefix.equals(statement.getIdentifier()))
							nodeChangedNoDep(pNode);
					}
					while (!changed && !(pNode instanceof TopGroupSorterContextJTreeNode))
					{
						pNode = pNode.getParent();
						changed = nodeStructureChanged(pNode);
					}
				}
			}
			if (!(statement instanceof RootContext))
			{
				Context ctx = statement.getContext(transaction);
				StatementContextJTreeNode node_ = nodeMap.cachedByStatement(ctx);
				if (node_ != null)
				{
					if (ctx.getConsequent().isFreeVariable(statement.getVariable()) || ctx.getConsequent().equals(statement.getTerm()))
					{
						ContextSorterContextJTreeNode node__ = (ContextSorterContextJTreeNode) nodeMap.getByStatement(ctx);
						nodeChanged(node__.getConsequentNode());
					}
				}
			}
			CloseableIterator<Statement> iterator = statement.dependents(transaction).iterator();
			try
			{
				long t0 = System.nanoTime();
				while (iterator.hasNext())
				{
					if (System.nanoTime() - t0 >= 5 * 1e9)
					{
						logger.warn("5 second timeout expired updating identifier change on dependent nodes");
						break;
					}
					Statement user = iterator.next();
					StatementContextJTreeNode node_ = nodeMap.cachedByStatement(user);
					if (node_ != null)
					{
						nodeChanged((ContextJTreeNode) node_);
						if (user instanceof Context)
						{
							Context ctx_ = (Context) user;
							if (ctx_.getConsequent().isFreeVariable(statement.getVariable()) || ctx_.getConsequent().equals(statement.getTerm()))
							{
								ContextSorterContextJTreeNode node__ = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(ctx_);
								if (node__ != null)
									nodeChanged(node__.getConsequentNode());
							}
							if (user instanceof Declaration)
							{
								Declaration dec = (Declaration) user;
								for (UnfoldingContext unf : dec.unfoldingContexts(transaction))
								{
									ContextSorterContextJTreeNode node__ = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(unf);
									if (node__ != null)
									{
										if (unf.getConsequent().isFreeVariable(statement.getVariable()) || unf.getConsequent().equals(statement.getTerm()))
											nodeChanged(node__.getConsequentNode());
									}
								}
							}
						}
					}
					else
					{
						SorterContextJTreeNode node__ = cachedByPrefix(transaction, user);
						if (node__ != null)
							nodeChangedNoDep(node__);
					}
				}
			}
			finally
			{
				iterator.close();
			}
			if (statement.getTerm() instanceof SimpleTerm)
			{
				CloseableIterator<Context> iterator2;
				if (statement instanceof RootContext)
					iterator2 = ((RootContext) statement).descendantContextsByConsequent(transaction, statement.getTerm()).iterator();
				else
					iterator2 = statement.getContext(transaction).descendantContextsByConsequent(transaction, statement.getTerm()).iterator();
				try
				{
					while (iterator2.hasNext())
					{
						Context ctx_ = iterator2.next();
						if (nodeMap.isCachedByStatement(ctx_))
						{
							ContextSorterContextJTreeNode ctxNode_ = (ContextSorterContextJTreeNode) nodeMap.getByStatement(ctx_);
							nodeChanged(ctxNode_.getConsequentNode());
						}
					}
				}
				finally
				{
					iterator2.close();
				}
			}

		}

		private SorterContextJTreeNode cachedByPrefix(Transaction transaction, Statement statement)
		{
			if (statement.getIdentifier() == null)
				return null;
			synchronized (nodeMap)
			{
				GroupSorterContextJTreeNode<?> node;
				if (statement instanceof RootContext)
					node = getRootTreeNode();
				else
					node = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(statement.getContext(transaction));
				if (node == null)
					return null;
				Sorter sorter = node.getSorter().findByPrefix(transaction, statement.getIdentifier());
				if (sorter == null)
					return null;
				return nodeMap.cached(sorter);
			}
		}

		private void statementDeletedFromContext(StatementDeletedFromContextChange c, Transaction transaction)
		{
			statementDeletedFromContext(c.getContext(), c.getStatement(), transaction);
		}

		private GroupSorterContextJTreeNode<? extends Statement> deleteStatement(Context context, Statement statement)
		{
			StatementContextJTreeNode node = ContextJTreeModel.this.deleteStatement(statement);
			if (node != null)
				return node.getParent();
			else
			{
				GroupSorterContextJTreeNode<? extends Statement> pNode;
				if (context == null)
					pNode = getRootTreeNode();
				else
				{
					pNode = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(context);
					if (pNode == null)
						return null;
				}
				Identifier id = statement.getIdentifier();
				if (id == null)
					return pNode;
				loop: while (true)
				{
					synchronized (pNode)
					{
						if (pNode.computedSorterList())
						{
							for (ContextJTreeNode n : pNode.childrenIterable())
							{
								if (n instanceof GroupSorterContextJTreeNode && !(n instanceof StatementContextJTreeNode))
								{
									@SuppressWarnings("unchecked")
									GroupSorterContextJTreeNode<? extends Statement> n_ = (GroupSorterContextJTreeNode<? extends Statement>) n;
									if (n_.getSorter().getPrefix().isPrefixOf(id))
									{
										pNode = n_;
										continue loop;
									}
								}
							}
						}
						break loop;
					}
				}
				return pNode;
			}
		}

		private void statementDeletedFromContext(Context context, Statement statement, Transaction transaction)
		{
			if (statement.getTerm() instanceof SimpleTerm)
			{
				CloseableIterator<Context> iterator = context.descendantContextsByConsequent(transaction, statement.getTerm()).iterator();
				try
				{
					while (iterator.hasNext())
					{
						Context ctx_ = iterator.next();
						if (nodeMap.isCachedByStatement(ctx_))
						{
							ContextSorterContextJTreeNode ctxNode_ = (ContextSorterContextJTreeNode) nodeMap.getByStatement(ctx_);
							nodeChanged(ctxNode_.getConsequentNode());
						}
					}
				}
				finally
				{
					iterator.close();
				}
			}

			GroupSorterContextJTreeNode<? extends Statement> pNode = deleteStatement(context, statement);
			if ((pNode != null) && !pNode.checkStatementRemove(statement))
			{
				nodeStructureChanged(pNode);
				Identifier prefix = pNode.getSorter().getPrefix();
				if (prefix != null && prefix.equals(statement.getIdentifier()))
					nodeChangedNoDep(pNode);
				boolean changed = true;
				while (changed && !(pNode instanceof ContextSorterContextJTreeNode) && pNode.isDegenerate())
				{
					pNode = pNode.getParent();
					changed = nodeStructureChanged(pNode);
				}
			}
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
					try
					{
						if (statement == null)
						{
							contextJTree.clearSelection();
						}
						else
						{
							if (!statement.isProved())
								contextJTree.expandStatement(statement);
							contextJTree.selectStatement(statement, false);
							contextJTree.scrollStatementToVisible(statement);
						}
					}
					catch (PersistenceLockTimeoutException e)
					{
						logger.error(e.getMessage(), e);
					}
				}
			});
		}

		private void contextConsequentSelect(ContextConsequentSelect c, Transaction transaction)
		{
			contextConsequentSelect(c.getContext(), c.getContextJTree(), transaction);
		}

		private void contextConsequentSelect(final Context context, final ContextJTree contextJTree, Transaction transaction)
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						if (context == null)
						{
							contextJTree.clearSelection();
						}
						else
						{
							contextJTree.selectContextConsequent(context, false);
							contextJTree.scrollToVisibleConsequent(context);
						}
					}
					catch (PersistenceLockTimeoutException e)
					{
						logger.error(e.getMessage(), e);
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
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementContextJTreeNode node = nodeMap.getByStatement(statement);
				nodeChanged((ContextJTreeNode) node);
			}
		}

		private void authorityStateChange(AuthorityStateChange c, Transaction transaction)
		{
			authorityStateChange(c.getStatement(), transaction);
		}

		private void authorityStateChange(Statement statement, Transaction transaction)
		{
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementContextJTreeNode node = nodeMap.getByStatement(statement);
				nodeChanged((ContextJTreeNode) node);
			}
		}

		private void setActiveContextStateChange(SetActiveContextStateChange c, Transaction transaction)
		{
			setActiveContextStateChange(c.getActiveContext(), c.getOldActiveContext(), transaction);
		}

		private void setActiveContextStateChange(Context activeContext, Context oldActiveContext, Transaction transaction)
		{
			if (oldActiveContext != null)
			{
				try
				{
					ContextSorterContextJTreeNode oldActiveContextNode = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(oldActiveContext);
					if (oldActiveContextNode != null)
					{
						oldActiveContextNode.setActiveContext(false);
						nodeChangedNoDep(oldActiveContextNode);
						nodeChangedNoDep(oldActiveContextNode.getConsequentNode());
					}
				}
				catch (ClassCastException e)
				{

				}
			}
			if (activeContext != null)
			{
				try
				{
					ContextSorterContextJTreeNode activeContextNode = (ContextSorterContextJTreeNode) nodeMap.cachedByStatement(activeContext);
					if (activeContextNode != null)
					{
						activeContextNode.setActiveContext(true);
						nodeChangedNoDep(activeContextNode);
						nodeChangedNoDep(activeContextNode.getConsequentNode());
					}
				}
				catch (ClassCastException e)
				{

				}
			}
		}

		private void parameterIdentificationChange(ParameterIdentificationChange c, Transaction transaction)
		{
			if (c instanceof ConsequentParameterIdentificationChange)
				consequentParameterIdentificationChange(((ConsequentParameterIdentificationChange) c).getContext(), transaction);
			else
				parameterIdentificationChange(c.getStatement(), transaction);
		}

		private void parameterIdentificationChange(Statement statement, Transaction transaction)
		{
			statement = statement.refresh(transaction);
			if (statement != null)
			{
				StatementContextJTreeNode node = nodeMap.getByStatement(statement);
				nodeChangedNoDep((ContextJTreeNode) node);
			}
		}

		private void consequentParameterIdentificationChange(Context context, Transaction transaction)
		{
			context = context.refresh(transaction);
			if (context != null)
			{
				ContextSorterContextJTreeNode node = (ContextSorterContextJTreeNode) nodeMap.getByStatement(context);
				nodeChangedNoDep(node.getConsequentNode());
			}
		}

		private void contextStructureChange(ContextStructureChange c, Transaction transaction)
		{
			contextStructureChange(c.getContext(), transaction);
		}

		private void contextStructureChange(Context context, Transaction transaction)
		{
			context = context.refresh(transaction);
			if (context != null)
			{
				ContextSorterContextJTreeNode node = (ContextSorterContextJTreeNode) nodeMap.removeByStatement(context);
				if (node != null)
				{
					List<Sorter> sorterList = node.getSorterList();
					if (sorterList != null)
						for (Sorter sorter : sorterList)
							nodeMapRemoveRecursive(sorter);
				}
				final TreeModelEvent eStructure = new TreeModelEvent(this, node.path());
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						for (TreeModelListener l : getListeners())
							persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(l, eStructure);
					}
				});
			}
		}

		private void rootStructureChange(RootStructureChange c, Transaction transaction)
		{
			rootStructureChange(transaction);
		}

		private void rootStructureChange(Transaction transaction)
		{
			nodeMap.clear();
			final TreeModelEvent eStructure = new TreeModelEvent(this, getRootTreeNode().path());
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					for (TreeModelListener l : getListeners())
						persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(l, eStructure);
				}
			});
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

	private void nodeChangedNoDep(ContextJTreeNode node)
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

	void nodeChanged(ContextJTreeNode node)
	{
		nodeChangedNoDep(node);

		if (node instanceof StatementContextJTreeNode)
		{
			StatementContextJTreeNode statementNode = (StatementContextJTreeNode) node;
			Identifier prefix = statementNode.parentSorter().getPrefix();
			if (prefix != null && prefix.equals(statementNode.getIdentifier()))
				nodeChangedNoDep(statementNode.getParent());
		}
	}

	private void nodeMapRemoveRecursive(Sorter sorter)
	{
		Stack<Sorter> stack = new Stack<>();
		stack.push(sorter);
		while (!stack.isEmpty())
		{
			Sorter s = stack.pop();
			SorterContextJTreeNode n = nodeMap.remove(s);
			if (n != null)
			{
				n.cleanRenderer();
				if (n instanceof GroupSorterContextJTreeNode)
				{
					GroupSorterContextJTreeNode<?> gn = (GroupSorterContextJTreeNode<?>) n;
					List<Sorter> sl = gn.getSorterList();
					if (sl != null)
						stack.addAll(sl);
				}
			}
		}
	}

	private boolean nodeStructureChanged(final GroupSorterContextJTreeNode<? extends Statement> node)
	{
		node.cleanRenderer();
		ListChanges<Sorter> changes = node.changeSorterList();
		if (changes != null)
		{
			final TreeModelEvent eRemoves;
			if (!changes.removedElements().isEmpty())
			{
				int indexes[] = new int[changes.removedElements().size()];
				Object objects[] = new Object[changes.removedElements().size()];
				int i = 0;
				for (ListChanges<Sorter>.Element e : changes.removedElements())
				{
					indexes[i] = e.index;
					if (objects != null)
					{
						objects[i] = nodeMap.cached(e.object);
						if (objects[i] == null)
						{
							objects = null;
							logger.trace("Setting remove tree model event's children object to null: " + node);
						}
					}
					i++;
				}
				eRemoves = new TreeModelEvent(this, node.path(), indexes, objects);
			}
			else
				eRemoves = null;
			for (ListChanges<Sorter>.Element e : changes.removedElements())
				nodeMapRemoveRecursive(e.object);
			final TreeModelEvent eInserts;
			if (!changes.insertedElements().isEmpty())
			{
				int indexes[] = new int[changes.insertedElements().size()];
				Object objects[] = new Object[changes.insertedElements().size()];
				int i = 0;
				for (ListChanges<Sorter>.Element e : changes.insertedElements())
				{
					indexes[i] = e.index;
					objects[i] = nodeMap.get(e.object);
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
						try
						{
							synchronized (getListeners())
							{
								for (TreeModelListener l : getListeners())
								{
									if (eRemoves != null)
										l.treeNodesRemoved(eRemoves);
									if (eInserts != null)
										l.treeNodesInserted(eInserts);
								}
							}
						}
						catch (PersistenceLockTimeoutException e)
						{
							logger.trace(e.getMessage(), e);
							List<Sorter> sorterList = node.getSorterList();
							if (sorterList != null)
								for (Sorter sorter : sorterList)
									nodeMapRemoveRecursive(sorter);
							synchronized (getListeners())
							{
								for (TreeModelListener l : getListeners())
									persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(l, eStructure);
							}
						}

					}

				});
				return true;
			}
		}
		return false;
	}

	public boolean nodeStructureChangedDegenerateCheck(GroupSorterContextJTreeNode<? extends Statement> node)
	{
		boolean change = false;
		while (node != null)
		{
			if (nodeStructureChanged(node))
			{
				change = true;
				if (!node.isDegenerate())
					break;
			}
			else
				break;
			node = node.getParent();
		}
		return change;
	}

	public void nodeStructureReset(Context context)
	{
		ContextSorterContextJTreeNode node = (ContextSorterContextJTreeNode) nodeMap.getByStatement(context);
		if (node != null)
			nodeStructureReset(node);
	}

	public void nodeStructureReset(GroupSorterContextJTreeNode<?> node)
	{
		List<Sorter> sorterList = node.getSorterList();
		if (sorterList != null)
			for (Sorter sorter : sorterList)
				nodeMapRemoveRecursive(sorter);
		node.changeSorterList();
		final TreeModelEvent eStructure = new TreeModelEvent(this, node.path());
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (TreeModelListener l : getListeners())
					persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(l, eStructure);
			}

		});
	}

}
