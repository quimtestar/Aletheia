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
package aletheia.gui.catalogjtree;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.Logger;

import aletheia.gui.common.PersistentTreeModel;
import aletheia.log4j.LoggerManager;
import aletheia.model.catalog.RootCatalog;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;

public class CatalogTreeModel extends PersistentTreeModel
{
	private static final Logger logger = LoggerManager.instance.logger();

	private class VirtualRootTreeNode implements TreeNode
	{

		@Override
		public Enumeration<RootCatalogTreeNode> children()
		{
			return new Enumeration<RootCatalogTreeNode>()
			{

				boolean hasNext = true;

				@Override
				public boolean hasMoreElements()
				{
					return hasNext;
				}

				@Override
				public RootCatalogTreeNode nextElement()
				{
					if (hasNext)
						return rootNode;
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
		public TreeNode getChildAt(int childIndex)
		{
			if (childIndex == 0)
				return rootNode;
			else
				return null;
		}

		@Override
		public int getChildCount()
		{
			return 1;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			if (node.equals(rootNode))
				return 0;
			else
				return -1;
		}

		@Override
		public TreeNode getParent()
		{
			return null;
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

	}

	private abstract class StatementChange
	{
		private final Transaction transaction;
		private final Statement statement;

		public StatementChange(Transaction transaction, Statement statement)
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

	private class ProvedStateChange extends StatementChange
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

	private abstract class ContextStateChange extends StatementChange
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
		private final Identifier identifier;

		public StatementDeletedFromContextChange(Transaction transaction, Context context, Statement statement, Identifier identifier)
		{
			super(transaction, context, statement);
			this.identifier = identifier;
		}

		public Identifier getIdentifier()
		{
			return identifier;
		}

	}

	private abstract class RootContextChange extends StatementChange
	{

		public RootContextChange(Transaction transaction, RootContext rootContext)
		{
			super(transaction, rootContext);
		}

		public RootContext getRootContext()
		{
			return getStatement();
		}

		@Override
		public RootContext getStatement()
		{
			return (RootContext) super.getStatement();
		}

	}

	private class RootContextAddedChange extends RootContextChange
	{

		public RootContextAddedChange(Transaction transaction, RootContext rootContext)
		{
			super(transaction, rootContext);
		}
	}

	private class RootContextDeletedStateChange extends RootContextChange
	{
		private final Identifier identifier;

		public RootContextDeletedStateChange(Transaction transaction, RootContext rootContext, Identifier identifier)
		{
			super(transaction, rootContext);
			this.identifier = identifier;
		}

		public Identifier getIdentifier()
		{
			return identifier;
		}

	}

	private abstract class IdentifierStateChange extends StatementChange
	{
		private final Identifier identifier;

		public IdentifierStateChange(Transaction transaction, Statement statement, Identifier identifier)
		{
			super(transaction, statement);
			this.identifier = identifier;
		}

		public Identifier getIdentifier()
		{
			return identifier;
		}

	}

	private class StatementIdentifiedChange extends IdentifierStateChange
	{

		public StatementIdentifiedChange(Transaction transaction, Statement statement, Identifier identifier)
		{
			super(transaction, statement, identifier);
		}

	}

	private class StatementUnidentifiedChange extends IdentifierStateChange
	{

		public StatementUnidentifiedChange(Transaction transaction, Statement statement, Identifier identifier)
		{
			super(transaction, statement, identifier);
		}

	}

	private class MyStatementStateListener implements Statement.StateListener, Nomenclator.Listener, RootContext.TopStateListener
	{

		private final Set<Statement> listeningTo;

		public MyStatementStateListener()
		{
			super();
			listeningTo = new HashSet<>();
		}

		public synchronized void listenTo(Statement statement)
		{
			listeningTo.add(statement);
			statement.addStateListener(this);
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.addNomenclatorListener(this);
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.addRootNomenclatorListener(this);
				}
			}
		}

		public synchronized void unlisten()
		{
			for (Statement st : listeningTo)
			{
				st.removeStateListener(this);
				if (st instanceof Context)
				{
					Context ctx = (Context) st;
					ctx.removeNomenclatorListener(this);
					if (ctx instanceof RootContext)
					{
						RootContext rootCtx = (RootContext) ctx;
						rootCtx.removeNomenclatorListener(this);
					}
				}
			}
			listeningTo.clear();
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
				statementStateChangeQueue.put(new StatementDeletedFromContextChange(transaction, context, statement, identifier));
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
				statementStateChangeQueue.put(new StatementIdentifiedChange(transaction, statement, identifier));
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
				statementStateChangeQueue.put(new StatementUnidentifiedChange(transaction, statement, identifier));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

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
				statementStateChangeQueue.put(new RootContextDeletedStateChange(transaction, rootContext, identifier));
			}
			catch (InterruptedException e)
			{
				logger.error(e.getMessage(), e);
			}
		}

	}

	private class StatementStateProcessorThread extends Thread
	{

		private boolean shutdown = false;

		public StatementStateProcessorThread()
		{
			super("CatalogTreeModel.StatementStateProcessorThread");
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
					StatementChange c = statementStateChangeQueue.take();
					if (c.getTransaction() != null)
						c.getTransaction().waitForClose();
					synchronized (CatalogTreeModel.this)
					{
						if (c.getTransaction() == null || c.getTransaction().isCommited())
						{
							Transaction transaction = getPersistenceManager().beginTransaction(1000);
							try
							{
								if (c instanceof RootContextAddedChange)
									rootContextAdded((RootContextAddedChange) c, transaction);
								else if (c instanceof RootContextDeletedStateChange)
									rootContextDeleted((RootContextDeletedStateChange) c, transaction);
								else if (c instanceof ProvedStateChange)
									provedStateChanged((ProvedStateChange) c, transaction);
								else if (c instanceof StatementAddedToContextChange)
									statementAddedToContext((StatementAddedToContextChange) c, transaction);
								else if (c instanceof StatementDeletedFromContextChange)
									statementDeletedFromContext((StatementDeletedFromContextChange) c, transaction);
								else if (c instanceof StatementIdentifiedChange)
									statementIdentified((StatementIdentifiedChange) c);
								else if (c instanceof StatementUnidentifiedChange)
									statementUnidentified((StatementUnidentifiedChange) c);
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
			Identifier id = rootContext.identifier(transaction);
			if (id != null)
				statementIdentifierAddedOrDeleted(id);
		}

		private void rootContextDeleted(RootContextDeletedStateChange c, Transaction transaction)
		{
			rootContextDeleted(c.getRootContext(), c.getIdentifier(), transaction);
		}

		private void rootContextDeleted(RootContext rootContext, Identifier identifier, Transaction transaction)
		{
			if (identifier != null)
			{
				if (rootNode.getCatalog() != null && rootNode.getCatalog().getContext().refresh(transaction) != null)
					statementIdentifierAddedOrDeleted(identifier);
			}

		}

		private void provedStateChanged(ProvedStateChange c, Transaction transaction)
		{
			provedStateChanged(c.getStatement(), c.isProved(), transaction);
		}

		private void statementAddedToContext(StatementAddedToContextChange c, Transaction transaction)
		{
			statementAddedToContext(c.getContext(), c.getStatement(), transaction);
		}

		private void statementIdentified(StatementIdentifiedChange c)
		{
			statementIdentified(c.getStatement(), c.getIdentifier());
		}

		private void statementUnidentified(StatementUnidentifiedChange c)
		{
			statementUnidentified(c.getStatement(), c.getIdentifier());
		}

		private void statementDeletedFromContext(StatementDeletedFromContextChange c, Transaction transaction)
		{
			statementDeletedFromContext(c.getContext(), c.getStatement(), c.getIdentifier(), transaction);
		}

		private CatalogTreeNode searchNode(Namespace namespace)
		{
			CatalogTreeNode node = rootNode;
			for (NodeNamespace nns : namespace.prefixList())
			{
				if (node == null || !node.childrenLoaded())
					return null;
				node = node.getChild(nns);
			}
			return node;
		}

		private synchronized void statementIdentifierAddedOrDeleted(Identifier identifier)
		{
			// What if suddenly appears a namespace at a higher level than the identifier of the present statement
			Namespace namespace = identifier.getNamespace();
			CatalogTreeNode node = searchNode(namespace);
			if (node != null)
			{
				node.rebuildChildren();
				final TreeModelEvent ev = new TreeModelEvent(this, node.path().getParentPath());
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (getListeners())
						{
							for (TreeModelListener listener : getListeners())
								persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(listener, ev);
						}
					}
				});
			}

		}

		public void statementAddedToContext(Context context, Statement statement, Transaction transaction)
		{
			Identifier id = statement.identifier(transaction);
			if (id != null)
				statementIdentifierAddedOrDeleted(id);
		}

		public void statementDeletedFromContext(Context context, Statement statement, Identifier identifier, Transaction transaction)
		{
			if (identifier != null)
			{
				if (rootNode.getCatalog() != null && rootNode.getCatalog().getContext().refresh(transaction) != null)
					statementIdentifierAddedOrDeleted(identifier);
			}
		}

		public void statementIdentified(Statement statement, Identifier identifier)
		{
			statementIdentifierAddedOrDeleted(identifier);
		}

		public void statementUnidentified(Statement statement, Identifier identifier)
		{
			statementIdentifierAddedOrDeleted(identifier);
		}

		public void provedStateChanged(Statement statement, boolean proved, Transaction transaction)
		{
			Identifier id = statement.identifier(transaction);
			if (id != null)
			{
				CatalogTreeNode node = searchNode(statement.identifier(transaction));
				if (node != null)
				{
					node.cleanRenderer();
					final TreeModelEvent ev = new TreeModelEvent(this, node.path());
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							synchronized (getListeners())
							{
								for (TreeModelListener listener : getListeners())
									persistenceLockTimeoutSwingInvokeLaterTreeNodesChanged(listener, ev);
							}
						}
					});

				}
			}
		}

	}

	private final VirtualRootTreeNode virtualRootTreeNode;
	private final MyStatementStateListener statementStateListener;
	private final BlockingQueue<StatementChange> statementStateChangeQueue;
	private final StatementStateProcessorThread statementStateProcessorThread;

	private RootCatalogTreeNode rootNode;

	public CatalogTreeModel(PersistenceManager persistenceManager)
	{
		super(persistenceManager);
		virtualRootTreeNode = new VirtualRootTreeNode();
		statementStateListener = new MyStatementStateListener();
		persistenceManager.getListenerManager().getRootContextTopStateListeners().add(statementStateListener);
		statementStateChangeQueue = new LinkedBlockingQueue<>();
		statementStateProcessorThread = new StatementStateProcessorThread();
		statementStateProcessorThread.start();

		rootNode = new RootCatalogTreeNode(this, null);
	}

	protected VirtualRootTreeNode getVirtualRootTreeNode()
	{
		return virtualRootTreeNode;
	}

	protected void listenTo(Statement statement)
	{
		statementStateListener.listenTo(statement);
	}

	public RootCatalog getRootCatalog()
	{
		return rootNode.getCatalog();
	}

	public synchronized void setRootCatalog(RootCatalog rootCatalog)
	{
		if (rootNode.getCatalog() != rootCatalog)
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				List<? extends Statement> path = null;
				if (rootCatalog != null)
				{
					Context context = rootCatalog.getContext();
					if (context.persists(transaction))
						path = context.statementPath(transaction);
				}
				statementStateListener.unlisten();
				if (path != null)
				{
					for (Statement ctx : path)
						statementStateListener.listenTo(ctx);
				}
				rootNode = new RootCatalogTreeNode(this, rootCatalog);
				final TreeModelEvent ev = new TreeModelEvent(this, new TreePath(rootNode));
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (getListeners())
						{
							for (TreeModelListener listener : getListeners())
								persistenceLockTimeoutSwingInvokeLaterTreeStructureChanged(listener, ev);
						}
					}
				});
			}
			finally
			{
				transaction.abort();
			}
		}
	}

	@Override
	public TreeNode getChild(Object o, int i)
	{
		return ((TreeNode) o).getChildAt(i);
	}

	@Override
	public int getChildCount(Object o)
	{
		return ((TreeNode) o).getChildCount();
	}

	@Override
	public int getIndexOfChild(Object o1, Object o2)
	{
		return ((TreeNode) o1).getIndex((TreeNode) o2);
	}

	@Override
	public TreeNode getRoot()
	{
		return virtualRootTreeNode;
	}

	@Override
	public boolean isLeaf(Object o)
	{
		return ((TreeNode) o).isLeaf();
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1)
	{
	}

	public void shutdown() throws InterruptedException
	{
		getPersistenceManager().getListenerManager().getRootContextTopStateListeners().remove(statementStateListener);
		statementStateProcessorThread.shutdown();
		statementStateProcessorThread.join();
	}

	@Override
	public void cleanRenderers()
	{
		rootNode.cleanRenderers();
	}

	public synchronized TreePath pathForPrefix(Namespace prefix)
	{
		CatalogTreeNode node = rootNode;
		for (NodeNamespace p : prefix.prefixList())
		{
			if (node == null)
				return null;
			node = node.getChild(p);
		}
		if (node == null)
			return null;
		return node.path();
	}

}
