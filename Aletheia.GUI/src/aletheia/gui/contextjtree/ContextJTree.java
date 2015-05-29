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

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.logging.log4j.Logger;

import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.statement.DeleteStatement;
import aletheia.gui.cli.command.statement.DeleteStatementCascade;
import aletheia.gui.common.PersistentJTree;
import aletheia.gui.common.StatementTransferable;
import aletheia.gui.contextjtree.node.old.AbstractTreeNode;
import aletheia.gui.contextjtree.node.old.BranchTreeNode;
import aletheia.gui.contextjtree.node.old.ConsequentTreeNode;
import aletheia.gui.contextjtree.node.old.ContextTreeNode;
import aletheia.gui.contextjtree.node.old.StatementTreeNode;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.log4j.LoggerManager;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.local.ContextLocal;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Context.StatementNotInContextException;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;

public class ContextJTree extends PersistentJTree
{
	private static final long serialVersionUID = -3960303519547692814L;
	private static final Logger logger = LoggerManager.instance.logger();

	private abstract class CellComponentManager
	{
		public ContextJTreeNodeRenderer getComponent(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (!(value instanceof AbstractTreeNode))
				throw new Error();
			AbstractTreeNode node = (AbstractTreeNode) value;
			ContextJTreeNodeRenderer renderer = getNodeRenderer(node);
			if (renderer != null)
			{
				renderer.setSelected(selected);
				renderer.setHasFocus(hasFocus);
			}
			return renderer;
		}

		public ContextJTreeNodeRenderer getNodeRenderer(AbstractTreeNode node)
		{
			try
			{
				return node.renderer(ContextJTree.this);
			}
			catch (PersistenceLockTimeoutException e)
			{
				getModel().nodeChanged(node);
				return new EmptyContextJTreeNodeRenderer(ContextJTree.this);
			}
		}

	}

	private class MyTreeCellRenderer extends CellComponentManager implements TreeCellRenderer
	{
		@Override
		public ContextJTreeNodeRenderer getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus)
		{
			return getComponent(value, selected, expanded, leaf, row, hasFocus);
		}

	}

	private class MyTreeCellEditor extends AbstractCellEditor implements TreeCellEditor
	{
		private static final long serialVersionUID = 4537638318953278903L;

		final MyTreeCellRenderer renderer;

		public MyTreeCellEditor(MyTreeCellRenderer renderer)
		{
			this.renderer = renderer;
		}

		@Override
		public Object getCellEditorValue()
		{
			return null;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
		{
			return renderer.getComponent(value, true, expanded, leaf, row, true);
		}

		@Override
		public void cancelCellEditing()
		{
			TreePath treePath = getSelectionPath();
			if (treePath != null)
			{
				AbstractTreeNode node = (AbstractTreeNode) treePath.getLastPathComponent();
				if (node != null)
				{
					TreeCellRenderer renderer = getCellRenderer();
					if (renderer instanceof CellComponentManager)
						((CellComponentManager) renderer).getNodeRenderer(node).cancelEditing();
				}
			}
			super.cancelCellEditing();
		}

		@Override
		public boolean stopCellEditing()
		{
			AbstractTreeNode node = (AbstractTreeNode) getSelectionPath().getLastPathComponent();
			if (node != null)
			{
				TreeCellRenderer renderer = getCellRenderer();
				if (renderer instanceof CellComponentManager)
					((CellComponentManager) renderer).getNodeRenderer(node).stopEditing();
			}
			return super.stopCellEditing();
		}

	}

	private class Listener implements KeyListener, MouseListener, TreeSelectionListener
	{

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_ENTER:
			{
				startEditingAtPath(selectionModel.getSelectionPath());
				break;
			}
			case KeyEvent.VK_F2:
			{
				startEditingAtPath(selectionModel.getSelectionPath());
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						AbstractTreeNode node = (AbstractTreeNode) selectionModel.getSelectionPath().getLastPathComponent();
						ContextJTreeNodeRenderer nodeRenderer = renderer.getNodeRenderer(node);
						if (nodeRenderer instanceof StatementContextJTreeNodeRenderer)
							((StatementContextJTreeNodeRenderer) nodeRenderer).editName();
					}
				});
				break;
			}
			case KeyEvent.VK_DELETE:
			{
				Statement statement = getSelectedStatement();
				if (statement != null)
				{
					try
					{
						if (ev.isShiftDown())
							deleteStatementCascade(statement);
						else
							deleteStatement(statement);

					}
					catch (InterruptedException e1)
					{
						logger.error(e1.getMessage(), e1);
					}
				}
				break;
			}
			case KeyEvent.VK_F3:
			{
				Statement statement = getSelectedStatement();
				if (statement != null)
				{
					CliJPanel cliJPanel = getAletheiaJPanel().getCliJPanel();
					if (statement instanceof RootContext)
						cliJPanel.setActiveContext((RootContext) statement);
					else
					{
						Transaction transaction = getPersistenceManager().beginTransaction();
						try
						{
							cliJPanel.setActiveContext(statement.getContext(transaction));
						}
						finally
						{
							transaction.abort();
						}
					}
				}
				break;
			}
			}

		}

		@Override
		public void keyReleased(KeyEvent ev)
		{
		}

		@Override
		public void keyTyped(KeyEvent ev)
		{
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
		}

		@Override
		public void valueChanged(TreeSelectionEvent ev)
		{
			AbstractTreeNode node = (AbstractTreeNode) ev.getPath().getLastPathComponent();
			if (node instanceof StatementTreeNode)
			{
				Statement statement = ((StatementTreeNode) node).getStatement();
				for (SelectionListener sl : selectionListeners)
					sl.statementSelected(statement);
			}
			else if (node instanceof ConsequentTreeNode)
			{
				Context ctx = ((ConsequentTreeNode) node).getContext();
				for (SelectionListener sl : selectionListeners)
					sl.consequentSelected(ctx);
			}
			else
				throw new Error();
		}

	}

	public interface SelectionListener
	{
		public void statementSelected(Statement statement);

		public void consequentSelected(Context context);
	}

	private final AletheiaJPanel aletheiaJPanel;
	private final MyTreeCellRenderer renderer;
	private final MyTreeCellEditor editor;
	private final Set<SelectionListener> selectionListeners;
	private final Listener listener;

	private class MyTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = -1741614222815375487L;

		@Override
		public int getSourceActions(JComponent c)
		{
			return COPY;
		}

		@Override
		protected StatementTransferable createTransferable(JComponent c)
		{
			Statement statement = getSelectedStatement();
			if (statement == null)
				return null;
			return new StatementTransferable(statement);
		}

		@Override
		protected void exportDone(JComponent c, Transferable t, int action)
		{
		}

	}

	public ContextJTree(AletheiaJPanel aletheiaJPanel)
	{
		super(new ContextJTreeModel(aletheiaJPanel.getPersistenceManager()));
		this.setLargeModel(true);
		this.setTransferHandler(new MyTransferHandler());
		this.aletheiaJPanel = aletheiaJPanel;
		this.listener = new Listener();
		this.renderer = new MyTreeCellRenderer();
		this.setCellRenderer(renderer);
		this.editor = new MyTreeCellEditor(renderer);
		this.setCellEditor(editor);
		this.selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.addKeyListener(listener);
		this.addMouseListener(listener);
		this.selectionModel.addTreeSelectionListener(listener);
		this.setEditable(true);
		this.selectionListeners = new HashSet<SelectionListener>();
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
	}

	public class TreeModelListener extends TreeModelHandler
	{
		@Override
		public void treeNodesChanged(final TreeModelEvent e)
		{
			boolean editing = false;
			if (getEditingPath() != null && getEditingPath().equals(e.getTreePath()))
			{
				if (isFocusOwner())
					editing = true;
				cancelEditing();
			}
			super.treeNodesChanged(e);
			if (editing)
				startEditingAtPath(e.getTreePath());
		}

		@Override
		public void treeNodesInserted(final TreeModelEvent e)
		{
			// WorkAround
			TreePath path = getSelectionModel().getSelectionPath();
			if (path != null)
			{
				Object o = path.getLastPathComponent();
				if (o instanceof ConsequentTreeNode)
				{
					ContextTreeNode ctxTn1 = ((ConsequentTreeNode) o).getParent();
					if (e.getChildren() != null)
					{
						for (Object c : e.getChildren())
						{
							if (c instanceof StatementTreeNode)
							{
								BranchTreeNode ctxTn2 = ((StatementTreeNode) c).getParent();
								if (ctxTn1 == ctxTn2)
								{
									cancelEditing();
									break;
								}
							}
						}
					}
				}
			}
			super.treeNodesInserted(e);
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e)
		{
			TreePath selPath = getSelectionModel().getSelectionPath();
			if (selPath != null && (selPath.getParentPath().equals(e.getTreePath())))
			{
				try
				{
					getSelectionModel().clearSelection();
				}
				catch (NullPointerException ex)
				{
					// This is a workaround; seems to happen when i got selected a node
					// in a part of the subtree of the node whose structure changes and
					// the change will make that subtree to fold.
					// Don't know why this exception is fired.
				}
			}
			super.treeNodesRemoved(e);
		}

		@Override
		public void treeStructureChanged(TreeModelEvent e)
		{
			TreePath selPath = getSelectionModel().getSelectionPath();
			if (selPath != null && (e.getTreePath().isDescendant(selPath)))
			{
				try
				{
					getSelectionModel().clearSelection();
				}
				catch (NullPointerException ex)
				{
					// This is a workaround; seems to happen when i got selected a node
					// in a part of the subtree of the node whose structure changes and
					// the change will make that subtree to fold.
					// Don't know why this exception is fired.
				}
			}
			super.treeStructureChanged(e);
		}

	}

	@Override
	protected TreeModelListener createTreeModelListener()
	{
		return new TreeModelListener();
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	@Override
	public ContextJTreeModel getModel()
	{
		return (ContextJTreeModel) super.getModel();
	}

	public void editStatementName(Statement statement, String newName) throws InvalidNameException, StatementNotInContextException, NomenclatorException
	{
		Identifier newId = null;
		if (!newName.isEmpty())
			newId = Identifier.parse(newName);

		Transaction transaction = getModel().beginTransaction();
		try
		{
			Identifier id = statement.identifier(transaction);
			if ((id == null && newId != null) || (id != null && newId == null) || (id != null && newId != null && !id.equals(newId)))
			{
				if (id != null)
					statement.unidentify(transaction);
				if (newId != null)
					statement.identify(transaction, newId);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
		stopEditing();
	}

	public void selectStatement(Statement statement, boolean edit)
	{
		cancelEditing();
		TreePath path = getModel().pathForStatement(statement);
		if (edit)
			startEditingAtPath(path);
		else
			getSelectionModel().setSelectionPath(path);
	}

	public void expandStatement(Statement statement)
	{
		TreePath path = getModel().pathForStatement(statement);
		if (path != null)
			expandPath(path);
	}

	@Override
	public void cancelEditing()
	{
		TreePath treePath = getEditingPath();
		if (treePath != null)
		{
			AbstractTreeNode node = (AbstractTreeNode) getEditingPath().getLastPathComponent();
			if (node != null)
			{
				TreeCellRenderer renderer = getCellRenderer();
				if (renderer instanceof CellComponentManager)
					((CellComponentManager) renderer).getNodeRenderer(node).cancelEditing();
			}
		}
		super.cancelEditing();
	}

	public void addSelectionListener(SelectionListener selectionListener)
	{
		selectionListeners.add(selectionListener);
	}

	public void removeSelectionListener(SelectionListener selectionListener)
	{
		selectionListeners.remove(selectionListener);
	}

	public Statement getSelectedStatement()
	{
		TreePath path = getSelectionModel().getSelectionPath();
		if (path == null)
			return null;
		AbstractTreeNode node = (AbstractTreeNode) path.getLastPathComponent();
		if (node instanceof StatementTreeNode)
			return ((StatementTreeNode) node).getStatement();
		else if (node instanceof ConsequentTreeNode)
			return ((ConsequentTreeNode) node).getContext();
		else
			throw new Error();

	}

	public void deleteStatement(Statement statement) throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel().command(
				new DeleteStatement(getAletheiaJPanel().getCliJPanel(), getPersistenceManager().beginTransaction(), statement), false);
	}

	public void deleteStatementCascade(Statement statement) throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel().command(
				new DeleteStatementCascade(getAletheiaJPanel().getCliJPanel(), getPersistenceManager().beginTransaction(), statement), false);
	}

	public Listener getListener()
	{
		return listener;
	}

	public void scrollToVisible(Statement statement)
	{
		scrollPathToVisible(getModel().pathForStatement(statement));
	}

	public void expandUnprovedContexts(final Context context)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				Transaction transaction = getPersistenceManager().beginTransaction();
				try
				{
					Stack<Context> stack = new Stack<Context>();
					stack.push(context);
					while (!stack.isEmpty())
					{
						Context ctx = stack.pop();
						if (!ctx.isProved())
						{
							expandPath(getModel().pathForStatement(ctx));
							stack.addAll(ctx.subContexts(transaction));
						}
						else
							collapsePath(getModel().pathForStatement(ctx));
					}
				}
				finally
				{
					transaction.abort();
				}
			}

		});

	}

	public void expandSubscribedContexts(final Context context)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				Transaction transaction = getPersistenceManager().beginTransaction();
				try
				{
					Stack<Context> stack = new Stack<Context>();
					stack.push(context);
					while (!stack.isEmpty())
					{
						Context ctx = stack.pop();
						ContextLocal ctxLocal = ctx.getLocal(transaction);
						if (ctxLocal != null && ctxLocal.isSubscribeStatements())
						{
							expandPath(getModel().pathForStatement(ctx));
							stack.addAll(ctx.subContexts(transaction));
						}
						else
							collapsePath(getModel().pathForStatement(ctx));
					}
				}
				finally
				{
					transaction.abort();
				}
			}

		});

	}

	public void pushSelectStatement(Transaction transaction, Statement statement)
	{
		getModel().pushSelectStatement(transaction, statement, this);
	}

	public void pushSelectStatement(Statement statement)
	{
		getModel().pushSelectStatement(null, statement, this);
	}

	public void close() throws InterruptedException
	{
		getModel().shutdown();
	}

}
