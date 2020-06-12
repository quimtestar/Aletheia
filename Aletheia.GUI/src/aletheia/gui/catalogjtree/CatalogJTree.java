/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Stack;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.cli.CliJPanel;
import aletheia.gui.common.PersistentJTree;
import aletheia.gui.common.datatransfer.AletheiaTransferable;
import aletheia.gui.common.datatransfer.NamespaceTransferable;
import aletheia.gui.common.datatransfer.StatementTransferable;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.model.catalog.RootCatalog;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;

public class CatalogJTree extends PersistentJTree
{
	private static final long serialVersionUID = 7368344464255236286L;

	private abstract class CellComponentManager
	{
		public CatalogJTreeNodeRenderer getComponent(CatalogTreeNode node, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			CatalogJTreeNodeRenderer renderer = getNodeRenderer(node);
			if (renderer != null)
			{
				renderer.setSelected(selected);
				renderer.setHasFocus(hasFocus);
			}
			return renderer;
		}

		public CatalogJTreeNodeRenderer getNodeRenderer(CatalogTreeNode node)
		{
			synchronized (getTreeLock())
			{
				return node.renderer(CatalogJTree.this);
			}
		}

	}

	private class MyTreeCellRenderer extends CellComponentManager implements TreeCellRenderer
	{
		TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			if (value instanceof CatalogTreeNode)
				return getComponent((CatalogTreeNode) value, selected, expanded, leaf, row, hasFocus);
			else
				return defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		}

	}

	private class MyTreeCellEditor extends AbstractCellEditor implements TreeCellEditor
	{
		private static final long serialVersionUID = -3580055690645280605L;

		private final MyTreeCellRenderer cellRenderer;

		public MyTreeCellEditor(MyTreeCellRenderer cellRenderer)
		{
			super();
			this.cellRenderer = cellRenderer;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
		{
			return cellRenderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}

		@Override
		public Object getCellEditorValue()
		{
			return null;
		}

		@Override
		public void cancelCellEditing()
		{
			TreePath treePath = getSelectionPath();
			if (treePath != null)
			{
				CatalogTreeNode node = (CatalogTreeNode) treePath.getLastPathComponent();
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
			CatalogTreeNode node = (CatalogTreeNode) getSelectionPath().getLastPathComponent();
			if (node != null)
			{
				TreeCellRenderer renderer = getCellRenderer();
				if (renderer instanceof CellComponentManager)
					((CellComponentManager) renderer).getNodeRenderer(node).stopEditing();
			}
			return super.stopCellEditing();
		}

	}

	private class MyListener implements KeyListener, TreeSelectionListener
	{

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_F4:
			{
				try
				{
					Statement statement = getSelectedStatement();
					if (statement != null)
						getContextJTree().selectStatement(statement, true);
				}
				catch (PersistenceLockTimeoutException e)
				{

				}
				break;
			}
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0)
		{
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			expandBySelection(e.getPath());
		}

	}

	private final CliJPanel cliJPanel;
	private final MyListener listener;

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
			Namespace prefix = getSelectedPrefix();
			if (prefix != null)
				return new NamespaceTransferable(prefix);
			return null;
		}

		@Override
		protected void exportDone(JComponent c, Transferable t, int action)
		{
		}

	}

	private final Stack<TreePath> expandedBySelectionPathStack;
	private boolean expandBySelection;

	public CatalogJTree(CliJPanel cliJPanel)
	{
		super(new CatalogTreeModel(cliJPanel.getPersistenceManager()), cliJPanel.getFontManager());
		this.setLargeModel(true);
		this.setDragEnabled(true);
		this.setTransferHandler(new MyTransferHandler());
		this.cliJPanel = cliJPanel;
		this.setRootVisible(false);
		this.setEditable(true);
		MyTreeCellRenderer cellRenderer = new MyTreeCellRenderer();
		this.setCellRenderer(cellRenderer);
		this.setCellEditor(new MyTreeCellEditor(cellRenderer));
		this.setShowsRootHandles(true);
		ToolTipManager.sharedInstance().registerComponent(this);
		this.listener = new MyListener();
		this.addKeyListener(this.listener);
		this.addTreeSelectionListener(this.listener);
		this.selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.expandBySelection = false;
		this.expandedBySelectionPathStack = new Stack<>();
	}

	@Override
	public CatalogTreeModel getModel()
	{
		return (CatalogTreeModel) super.getModel();
	}

	public CliJPanel getCliJPanel()
	{
		return cliJPanel;
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return getCliJPanel().getAletheiaJPanel();
	}

	public ContextJTree getContextJTree()
	{
		return getAletheiaJPanel().getContextJTree();
	}

	@Override
	protected TreeModelListener createTreeModelListener()
	{
		return new TreeModelListener();
	}

	private class TreeModelListener extends TreeModelHandler
	{

		@Override
		public void treeNodesChanged(TreeModelEvent ev)
		{
			super.treeNodesChanged(ev);
		}

		@Override
		public void treeNodesInserted(TreeModelEvent ev)
		{
			super.treeNodesInserted(ev);
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent ev)
		{
			super.treeNodesRemoved(ev);
		}

		@Override
		public void treeStructureChanged(TreeModelEvent ev)
		{
			// Copied from ContextJTree.TreeModelListener.treeStructureChanged
			TreePath selPath = getSelectionModel().getSelectionPath();
			if (selPath != null && (ev.getTreePath().isDescendant(selPath)))
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
			super.treeStructureChanged(ev);
		}
	}

	public void setRootCatalog(RootCatalog catalog)
	{
		cancelEditing();
		getModel().setRootCatalog(catalog);
	}

	public Statement getSelectedStatement()
	{
		TreePath path = getSelectionModel().getSelectionPath();
		if (path == null)
			return null;
		CatalogTreeNode node = (CatalogTreeNode) path.getLastPathComponent();
		if (node instanceof SubCatalogTreeNode)
		{
			Transaction transaction = getModel().beginTransaction();
			try
			{
				return ((SubCatalogTreeNode) node).getCatalog().statement(transaction);
			}
			finally
			{
				transaction.abort();
			}
		}
		return null;
	}

	public Namespace getSelectedPrefix()
	{
		TreePath path = getSelectionModel().getSelectionPath();
		if (path == null)
			return null;
		CatalogTreeNode node = (CatalogTreeNode) path.getLastPathComponent();
		return node.prefix();
	}

	public void selectStatement(Statement statement, boolean edit)
	{
		selectPrefix(statement.getIdentifier(), edit);
	}

	public void selectPrefix(Namespace prefix, boolean edit)
	{
		selectTreePath(getModel().pathForPrefix(prefix), edit);
	}

	private void selectTreePath(TreePath path, boolean edit)
	{
		cancelEditing();
		if (edit)
			startEditingAtPath(path);
		else
			getSelectionModel().setSelectionPath(path);
	}

	public void scrollPrefixToVisible(Namespace prefix)
	{
		scrollPathToVisible(getModel().pathForPrefix(prefix));
	}

	public void close() throws InterruptedException
	{
		getModel().shutdown();
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
