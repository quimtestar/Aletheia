/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
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
import aletheia.gui.cli.command.statement.DeleteRootContexts;
import aletheia.gui.cli.command.statement.DeleteRootContextsCascade;
import aletheia.gui.cli.command.statement.DeleteStatement;
import aletheia.gui.cli.command.statement.DeleteStatementCascade;
import aletheia.gui.cli.command.statement.DeleteStatements;
import aletheia.gui.cli.command.statement.DeleteStatementsCascade;
import aletheia.gui.cli.command.statement.Undelete;
import aletheia.gui.common.PersistentJTree;
import aletheia.gui.common.datatransfer.AletheiaTransferable;
import aletheia.gui.common.datatransfer.SorterTransferable;
import aletheia.gui.common.datatransfer.StatementTransferable;
import aletheia.gui.common.datatransfer.TermTransferable;
import aletheia.gui.contextjtree.node.ConsequentContextJTreeNode;
import aletheia.gui.contextjtree.node.ContextJTreeNode;
import aletheia.gui.contextjtree.node.ContextSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.GroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.RootContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementSorterContextJTreeNode;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.GroupSorterContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootContextGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.StatementGroupSorter;
import aletheia.log4j.LoggerManager;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.local.ContextLocal;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.MemoryUsageMonitor;
import aletheia.utilities.MemoryUsageMonitor.MemoryUsageMonitorException;
import aletheia.utilities.collections.BufferedList;

public class ContextJTree extends PersistentJTree
{
	private static final long serialVersionUID = -3960303519547692814L;
	private static final Logger logger = LoggerManager.instance.logger();

	private abstract class CellComponentManager
	{
		public ContextJTreeNodeRenderer getComponent(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (!(value instanceof ContextJTreeNode))
				throw new Error();
			ContextJTreeNode node = (ContextJTreeNode) value;
			ContextJTreeNodeRenderer renderer = getNodeRenderer(node);
			if (renderer != null)
			{
				renderer.setSelected(selected);
				renderer.setHasFocus(hasFocus);
			}
			return renderer;
		}

		public ContextJTreeNodeRenderer getNodeRenderer(ContextJTreeNode node)
		{
			if (getPersistenceManager().isOpen())
			{
				try
				{
					synchronized (getTreeLock())
					{
						return node.renderer(ContextJTree.this);
					}
				}
				catch (PersistenceLockTimeoutException e)
				{
					logger.trace("Couldn't build renderer for node: " + this, e);
					getModel().nodeChanged(node);
					return new EmptyContextJTreeNodeRenderer(ContextJTree.this);
				}
			}
			else
				return new EmptyContextJTreeNodeRenderer(ContextJTree.this);
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
				ContextJTreeNode node = (ContextJTreeNode) treePath.getLastPathComponent();
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
			ContextJTreeNode node = (ContextJTreeNode) getSelectionPath().getLastPathComponent();
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
						TreePath path = selectionModel.getSelectionPath();
						if (path != null)
						{
							ContextJTreeNode node = (ContextJTreeNode) path.getLastPathComponent();
							ContextJTreeNodeRenderer nodeRenderer = renderer.getNodeRenderer(node);
							if (nodeRenderer instanceof StatementContextJTreeNodeRenderer<?>)
								((StatementContextJTreeNodeRenderer<?>) nodeRenderer).editName();
							else if (nodeRenderer instanceof GroupSorterContextJTreeNodeRenderer)
								((GroupSorterContextJTreeNodeRenderer) nodeRenderer).editName();
						}
					}
				});
				break;
			}
			case KeyEvent.VK_DELETE:
			{
				if (ev.isControlDown())
				{
					try
					{
						undelete();
					}
					catch (InterruptedException e1)
					{
						logger.error(e1.getMessage(), e1);
					}
				}
				else
				{
					Statement statement = getSelectedStatement();
					if (statement == null)
						statement = getSelectedConsequent();
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
					else
					{
						Sorter sorter = getSelectedSorter();
						if (sorter instanceof GroupSorter)
						{
							@SuppressWarnings("unchecked")
							GroupSorter<? extends Statement> groupSorter = (GroupSorter<? extends Statement>) sorter;
							try
							{
								if (ev.isShiftDown())
									deleteSorterCascade(groupSorter);
								else
									deleteSorter(groupSorter);
							}
							catch (InterruptedException e1)
							{
								logger.error(e1.getMessage(), e1);
							}
						}
					}
				}
				break;
			}
			case KeyEvent.VK_F3:
			{
				CliJPanel cliJPanel = getAletheiaJPanel().getCliJPanel();
				Statement statement = getSelectedStatement();
				if (statement != null)
				{
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
				else
				{
					Context context = getSelectedConsequent();
					if (context != null)
						cliJPanel.setActiveContext(context);
					else
					{
						Sorter sorter = getSelectedSorter();
						if (sorter instanceof StatementGroupSorter)
							cliJPanel.setActiveContext(((StatementGroupSorter) sorter).getContext());
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
			expandBySelection(ev.getPath());
			ContextJTreeNode node = (ContextJTreeNode) ev.getPath().getLastPathComponent();
			if (node instanceof SorterContextJTreeNode)
			{
				boolean expanded = isExpanded(ev.getPath());
				if (node instanceof StatementContextJTreeNode)
					fireStatementSelected(((StatementContextJTreeNode) node).getStatement(), expanded);
				else if (node instanceof GroupSorterContextJTreeNode)
					fireGroupSorterSelected(((GroupSorterContextJTreeNode<?>) node).getSorter(), expanded);
				else
					throw new Error();
			}
			else if (node instanceof ConsequentContextJTreeNode)
			{
				Context ctx = ((ConsequentContextJTreeNode) node).getContext();
				synchronized (selectionListeners)
				{
					for (SelectionListener sl : selectionListeners)
						sl.consequentSelected(ctx);
				}
			}
		}

	}

	public interface SelectionListener
	{
		public default void statementSelected(Statement statement, boolean expanded)
		{
		};

		public default void consequentSelected(Context context)
		{
		};

		public default void groupSorterSelected(GroupSorter<? extends Statement> groupSorter, boolean expanded)
		{
		};
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
		protected AletheiaTransferable createTransferable(JComponent c)
		{
			Statement statement = getSelectedStatement();
			if (statement != null)
				return new StatementTransferable(statement);
			Sorter sorter = getSelectedSorter();
			if (sorter != null)
				return new SorterTransferable(getPersistenceManager(), sorter);
			Context context = getSelectedConsequent();
			if (context != null)
			{
				return new TermTransferable(context.getConsequent());
			}
			return null;
		}

		@Override
		protected void exportDone(JComponent c, Transferable t, int action)
		{
		}

	}

	private class MyTreeExpansionListener implements TreeExpansionListener
	{

		private void stateUpdate(TreePath path)
		{
			boolean expanded = isExpanded(path);
			Object o = path.getLastPathComponent();
			if (o instanceof GroupSorterContextJTreeNode)
			{
				GroupSorterContextJTreeNode<?> node = (GroupSorterContextJTreeNode<?>) o;
				node.setExpanded(expanded);
			}
			if (o.equals(getSelectedNode()))
			{
				if (o instanceof StatementContextJTreeNode)
				{
					StatementContextJTreeNode node = (StatementContextJTreeNode) o;
					fireStatementSelected(node.getStatement(), expanded);
				}
				else if (o instanceof GroupSorterContextJTreeNode)
				{
					GroupSorterContextJTreeNode<?> node = (GroupSorterContextJTreeNode<?>) o;
					fireGroupSorterSelected(node.getSorter(), expanded);
				}
			}

		}

		@Override
		public void treeExpanded(TreeExpansionEvent event)
		{
			stateUpdate(event.getPath());
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event)
		{
			stateUpdate(event.getPath());
		}

	}

	private final MemoryUsageMonitor memoryUsageMonitor;

	private final Stack<TreePath> expandedBySelectionPathStack;
	private boolean expandBySelection;

	public ContextJTree(AletheiaJPanel aletheiaJPanel)
	{
		super(new ContextJTreeModel(aletheiaJPanel.getPersistenceManager()), aletheiaJPanel.getFontManager());
		this.setLargeModel(true);
		this.setDragEnabled(true);
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
		this.selectionListeners = Collections.synchronizedSet(new HashSet<>());
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
		this.addTreeExpansionListener(new MyTreeExpansionListener());
		this.memoryUsageMonitor = makeMemoryUsageMonitor();
		this.expandedBySelectionPathStack = new Stack<>();
		this.expandBySelection = false;
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
				if (o instanceof ConsequentContextJTreeNode)
				{
					ContextSorterContextJTreeNode ctxTn1 = ((ConsequentContextJTreeNode) o).getParent();
					if (e.getChildren() != null)
					{
						for (Object c : e.getChildren())
						{
							if (c instanceof StatementSorterContextJTreeNode)
							{
								ContextJTreeNode ctxTn2 = ((StatementSorterContextJTreeNode) c).getParent();
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
			if ((selPath != null) && (e.getTreePath().equals(selPath)))
				getSelectionModel().setSelectionPath(selPath);
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

	public void editStatementName(Statement statement, String newName) throws InvalidNameException, NomenclatorException
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

	public void editSorterPrefix(GroupSorter<? extends Statement> sorter, String newName) throws InvalidNameException, NomenclatorException
	{
		Identifier newPrefix = null;
		if (!newName.isEmpty())
			newPrefix = Identifier.parse(newName);
		Identifier oldPrefix = sorter.getPrefix();
		Transaction transaction = getModel().beginTransaction();
		try
		{
			for (Statement statement : new BufferedList<>(sorter.sortedStatements(transaction)))
			{
				Identifier id = statement.identifier(transaction);
				Namespace suffix = null;
				if (oldPrefix != null)
				{
					if (id != null)
					{
						suffix = id.makeSuffix(oldPrefix);
						if (suffix == null)
							throw new RuntimeException();
						else if (suffix.isRoot())
							suffix = null;
					}
					else
						throw new RuntimeException();
				}
				else
					suffix = id;
				Identifier newId;
				if (suffix != null)
					if (newPrefix != null)
						newId = newPrefix.concat(suffix).asIdentifier();
					else if (suffix instanceof NodeNamespace)
						newId = ((NodeNamespace) suffix).asIdentifier();
					else
						newId = null;
				else
					newId = newPrefix;
				if ((id == null && newId != null) || (id != null && newId == null) || (id != null && newId != null && !id.equals(newId)))
				{
					if (id != null)
						statement.unidentify(transaction);
					if (newId != null)
						statement.identify(transaction, newId);
				}
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
		selectTreePath(getModel().pathForStatement(statement), edit);
	}

	public void selectContextConsequent(Context context, boolean edit)
	{
		selectTreePath(getModel().pathForContextConsequent(context), edit);
	}

	public void selectSorter(Sorter sorter, boolean edit)
	{
		selectTreePath(getModel().pathForSorter(sorter), edit);
	}

	private void selectTreePath(TreePath path, boolean edit)
	{
		cancelEditing();
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

	public void expandSorter(Sorter sorter)
	{
		TreePath path = getModel().pathForSorter(sorter);
		if (path != null)
			expandPath(path);
	}

	@Override
	public void cancelEditing()
	{
		TreePath treePath = getEditingPath();
		if (treePath != null)
		{
			ContextJTreeNode node = (ContextJTreeNode) getEditingPath().getLastPathComponent();
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

	public ContextJTreeNode getSelectedNode()
	{
		TreePath path = getSelectionModel().getSelectionPath();
		if (path == null)
			return null;
		if (path.getLastPathComponent() instanceof ContextJTreeNode)
			return (ContextJTreeNode) path.getLastPathComponent();
		else
			return null;
	}

	public Statement getSelectedStatement()
	{
		ContextJTreeNode node = getSelectedNode();
		if (node instanceof StatementContextJTreeNode)
			return ((StatementContextJTreeNode) node).getStatement();
		else
			return null;
	}

	public Sorter getSelectedSorter()
	{
		ContextJTreeNode node = getSelectedNode();
		if (node instanceof SorterContextJTreeNode)
			return ((SorterContextJTreeNode) node).getNodeMapSorter();
		else
			return null;
	}

	public Context getSelectedConsequent()
	{

		ContextJTreeNode node = getSelectedNode();
		if (node instanceof ConsequentContextJTreeNode)
			return ((ConsequentContextJTreeNode) node).getContext();
		else
			return null;
	}

	public void deleteStatement(Statement statement) throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel().command(new DeleteStatement(getAletheiaJPanel().getCliJPanel(), getModel().beginTransaction(), statement), false);
	}

	public void deleteStatementCascade(Statement statement) throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel().command(new DeleteStatementCascade(getAletheiaJPanel().getCliJPanel(), getModel().beginTransaction(), statement),
				false);
	}

	public void deleteSorter(GroupSorter<? extends Statement> sorter) throws InterruptedException
	{
		Transaction transaction = getModel().beginTransaction();
		if (sorter instanceof StatementGroupSorter)
		{
			StatementGroupSorter statementGroupSorter = (StatementGroupSorter) sorter;
			getAletheiaJPanel().getCliJPanel().command(new DeleteStatements(getAletheiaJPanel().getCliJPanel(), transaction, statementGroupSorter.getContext(),
					statementGroupSorter.statements(transaction)), false);
		}
		else if (sorter instanceof RootContextGroupSorter)
		{
			RootContextGroupSorter rootContextGroupSorter = (RootContextGroupSorter) sorter;
			getAletheiaJPanel().getCliJPanel()
					.command(new DeleteRootContexts(getAletheiaJPanel().getCliJPanel(), transaction, rootContextGroupSorter.statements(transaction)), false);
		}
		else
			throw new Error();
	}

	public void deleteSorterCascade(GroupSorter<? extends Statement> sorter) throws InterruptedException
	{
		Transaction transaction = getModel().beginTransaction();
		if (sorter instanceof StatementGroupSorter)
		{
			StatementGroupSorter statementGroupSorter = (StatementGroupSorter) sorter;
			getAletheiaJPanel().getCliJPanel().command(new DeleteStatementsCascade(getAletheiaJPanel().getCliJPanel(), transaction,
					statementGroupSorter.getContext(), statementGroupSorter.statements(transaction)), false);
		}
		else if (sorter instanceof RootContextGroupSorter)
		{
			RootContextGroupSorter rootContextGroupSorter = (RootContextGroupSorter) sorter;
			getAletheiaJPanel().getCliJPanel().command(
					new DeleteRootContextsCascade(getAletheiaJPanel().getCliJPanel(), transaction, rootContextGroupSorter.statements(transaction)), false);
		}
		else
			throw new Error();
	}

	public void undelete() throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel().command(new Undelete(getAletheiaJPanel().getCliJPanel(), getModel().beginTransaction()), false);
	}

	public Listener getListener()
	{
		return listener;
	}

	public void scrollStatementToVisible(Statement statement)
	{
		scrollPathToVisible(getModel().pathForStatement(statement));
	}

	public void scrollSorterToVisible(Sorter sorter)
	{
		scrollPathToVisible(getModel().pathForSorter(sorter));
	}

	public void scrollToVisibleConsequent(Context context)
	{
		scrollPathToVisible(getModel().pathForContextConsequent(context));
	}

	private void expandUnprovedContexts_(Context context)
	{
		long t0 = System.nanoTime();
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			ContextJTreeNode selected = getSelectedNode();
			Stack<Context> stack = new Stack<>();
			{
				Context ctx = context.refresh(transaction);
				stack.push(ctx);
				TreePath path = getModel().pathForStatement(ctx);
				if (isVisible(path))
					collapsePath(path);
			}
			while (!stack.isEmpty())
			{
				if ((System.nanoTime() - t0) * 1e-9 >= 30f)
				{
					logger.trace("Expanding unproved contexts incomplete by timeout.");
					try
					{
						getAletheiaJPanel().getCliJPanel().message("Expanding unproved contexts incomplete by timeout.");
					}
					catch (InterruptedException e)
					{
						logger.warn("Interrupted while messaging cli panel", e);
					}
					break;
				}
				Context ctx = stack.pop();
				TreePath path = getModel().pathForStatement(ctx);
				if (!ctx.isProved())
				{
					expandPath(path);
					ContextSorterContextJTreeNode node = ((ContextSorterContextJTreeNode) path.getLastPathComponent());
					for (Context ctx_ : ctx.subContexts(transaction))
					{
						stack.push(ctx_);
						TreePath path_ = getModel().pathForStatement(ctx_);
						if (isVisible(path_))
							collapsePath(path_);
					}
					for (ContextJTreeNode n : node.childrenIterable())
					{
						if (n instanceof GroupSorterContextJTreeNode<?>)
						{
							TreePath path_ = n.path();
							if (isVisible(path_))
								collapsePath(path_);
						}
					}
				}
			}
			if (selected != null)
			{
				TreePath path = selected.path();
				if (isVisible(path))
					selectTreePath(path, false);
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	public void expandUnprovedContexts(final Context context)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				expandUnprovedContexts_(context);
			}

		});

	}

	private void expandSubscribedContexts_(ContextLocal ctxLocal)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Stack<ContextLocal> stack = new Stack<>();
			stack.push(ctxLocal);
			while (!stack.isEmpty())
			{
				ContextLocal ctxLocal_ = stack.pop();
				ContextSorterContextJTreeNode node = (ContextSorterContextJTreeNode) getModel().getNodeMap()
						.getByStatement(ctxLocal_.getStatement(transaction));
				boolean pushed = false;
				for (ContextLocal ctxLocal__ : ctxLocal_.subscribeStatementsContextLocalSet(transaction))
				{
					StatementContextJTreeNode node_ = getModel().getNodeMap().getByStatement(ctxLocal__.getStatement(transaction));
					GroupSorterContextJTreeNode<?> parent = node_.getParent();
					while (true)
					{
						for (ContextJTreeNode n : parent.childrenIterable())
							collapsePath(n.path());
						if (parent.equals(node))
							break;
						parent = parent.getParent();
					}
					expandPath(node_.path());
					stack.push(ctxLocal__);
					pushed = true;
				}
				if (!pushed)
				{
					for (ContextJTreeNode n : node.childrenIterable())
						collapsePath(n.path());
				}
			}
		}
		finally
		{
			transaction.abort();
		}

	}

	public void expandSubscribedContexts(Context context)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			final ContextLocal ctxLocal = context.getLocal(transaction);
			if (ctxLocal != null)
			{
				SwingUtilities.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						expandSubscribedContexts_(ctxLocal);
					}

				});

			}
		}
		finally
		{
			transaction.abort();
		}

	}

	private void expandGroup_(Context context, Namespace prefix)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Stack<GroupSorterContextJTreeNode<?>> stack = new Stack<>();
			{
				GroupSorterContextJTreeNode<?> initialNode = context == null ? getModel().getRootTreeNode()
						: (ContextSorterContextJTreeNode) getModel().getNodeMap().getByStatement(context);
				if (prefix instanceof NodeNamespace)
				{
					Sorter sorter = initialNode.getSorter().findByPrefix(transaction, (NodeNamespace) prefix);
					if (sorter != null)
					{
						SorterContextJTreeNode n = getModel().getNodeMap().get(sorter);
						if (n instanceof GroupSorterContextJTreeNode && !(n instanceof RootContextJTreeNode) && !(n instanceof ContextSorterContextJTreeNode))
							initialNode = (GroupSorterContextJTreeNode<?>) n;
						else
						{
							setSelectionPath(n.path());
							initialNode = null;
						}
					}
					else
						initialNode = null;
				}
				if (initialNode != null)
					stack.push(initialNode);
			}
			while (!stack.isEmpty())
			{
				GroupSorterContextJTreeNode<?> node = stack.pop();
				expandPath(node.path());
				for (ContextJTreeNode n : node.childrenIterable())
				{
					if (n instanceof GroupSorterContextJTreeNode && !(n instanceof RootContextJTreeNode) && !(n instanceof ContextSorterContextJTreeNode))
						stack.push((GroupSorterContextJTreeNode<?>) n);
				}
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	public void expandGroup(final Context context, final Namespace prefix)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				expandGroup_(context, prefix);
			}
		});

	}

	private void collapseAll_(Context context)
	{
		Stack<GroupSorterContextJTreeNode<?>> collapseStack = new Stack<>();
		Stack<SorterContextJTreeNode> stack = new Stack<>();
		stack.push((SorterContextJTreeNode) getModel().getNodeMap().cachedByStatement(context));
		while (!stack.isEmpty())
		{
			SorterContextJTreeNode node = stack.pop();
			if (node instanceof GroupSorterContextJTreeNode<?>)
			{
				GroupSorterContextJTreeNode<?> gNode = (GroupSorterContextJTreeNode<?>) node;
				collapseStack.push(gNode);
				List<Sorter> sorters = gNode.getSorterList();
				if (sorters != null)
					for (Sorter sorter : sorters)
						stack.push(getModel().getNodeMap().cached(sorter));
			}
		}
		while (!collapseStack.isEmpty())
			collapsePath(collapseStack.pop().path());
	}

	public void collapseAll(final Context context)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				collapseAll_(context);
			}
		});

	}

	private void expandAllContexts_(Context context)
	{
		long t0 = System.nanoTime();
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Stack<Context> stack = new Stack<>();
			stack.push(context);
			while (!stack.isEmpty())
			{
				if ((System.nanoTime() - t0) * 1e-9 >= 10f)
				{
					logger.trace("Expanding contexts incomplete by timeout.");
					try
					{
						getAletheiaJPanel().getCliJPanel().message("Expanding contexts incomplete by timeout.");
					}
					catch (InterruptedException e)
					{
						logger.warn("Interrupted while messaging cli panel", e);
					}
					break;
				}
				Context ctx = stack.pop();
				TreePath path = getModel().pathForStatement(ctx);
				expandPath(path);
				stack.addAll(ctx.subContexts(transaction));
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	public void expandAllContexts(final Context context)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				expandAllContexts_(context);
			}

		});

	}

	public void putSelectStatement(Transaction transaction, Statement statement)
	{
		getModel().putSelectStatement(transaction, statement, this);
	}

	public void putSelectStatement(Statement statement)
	{
		getModel().putSelectStatement(null, statement, this);
	}

	public void putSelectContextConsequent(Transaction transaction, Context context)
	{
		getModel().putSelectContextConsequent(transaction, context, this);
	}

	public void putSelectContextConsequent(Context context)
	{
		getModel().putSelectContextConsequent(null, context, this);
	}

	public void close() throws InterruptedException
	{
		cancelEditing();
		if (memoryUsageMonitor != null)
			memoryUsageMonitor.shutdown();
		getModel().shutdown();
	}

	private void fireStatementSelected(Statement statement, boolean expanded)
	{
		synchronized (selectionListeners)
		{
			for (SelectionListener sl : selectionListeners)
				sl.statementSelected(statement, expanded);
		}
	}

	private void fireGroupSorterSelected(GroupSorter<?> groupSorter, boolean expanded)
	{
		synchronized (selectionListeners)
		{
			for (SelectionListener sl : selectionListeners)
				sl.groupSorterSelected(groupSorter, expanded);
		}
	}

	public void resetCollapsedSubtrees()
	{
		Stack<GroupSorterContextJTreeNode<?>> stack = new Stack<>();
		stack.push(getModel().getRootTreeNode());
		while (!stack.isEmpty())
		{
			GroupSorterContextJTreeNode<?> node = stack.pop();
			for (ContextJTreeNode n : node.childrenIterable())
			{
				if (n instanceof GroupSorterContextJTreeNode<?>)
				{
					GroupSorterContextJTreeNode<?> gn = (GroupSorterContextJTreeNode<?>) n;
					TreePath path = gn.path();
					if (isExpanded(path) || path.equals(getSelectionPath()))
						stack.push(gn);
					else
						getModel().nodeStructureReset(gn);
				}
			}
		}

	}

	private MemoryUsageMonitor makeMemoryUsageMonitor()
	{
		try
		{
			MemoryUsageMonitor mum = new MemoryUsageMonitor(0.8f);
			mum.addListener(new MemoryUsageMonitor.Listener()
			{

				long lastReached = System.nanoTime();

				@Override
				public void thresholdReached(float usage)
				{
					long t = System.nanoTime();
					if (t - lastReached > 5l * 60 * 1000 * 1000 * 1000)
					{
						logger.warn("Resetting collapsed subtrees (memory usage threshold reached: " + usage + ")");
						lastReached = t;
						resetCollapsedSubtrees();
					}
				}
			});
			return mum;
		}
		catch (MemoryUsageMonitorException e)
		{
			return null;
		}
	}

	public void nodeStructureReset(Context context)
	{
		getModel().nodeStructureReset(context);
	}

	public void setActiveContext(Context activeContext)
	{
		getModel().setActiveContext(activeContext);
	}

	public boolean isExpandBySelection()
	{
		return expandBySelection;
	}

	public void setExpandBySelection(boolean expandBySelection)
	{
		this.expandBySelection = expandBySelection;
	}

	private void expandBySelection(TreePath path)
	{
		if (expandBySelection)
		{
			while (!expandedBySelectionPathStack.isEmpty())
				if (!expandedBySelectionPathStack.peek().isDescendant(path))
					collapsePath(expandedBySelectionPathStack.pop());
				else
					break;
			expandPath(path);
			expandedBySelectionPathStack.push(path);
		}
	}

}
