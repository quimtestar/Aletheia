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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.logging.log4j.Logger;

import aletheia.gui.authority.AuthorityJPanel;
import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.authority.DeleteDelegateAuthorizer;
import aletheia.gui.cli.command.authority.DeleteDelegateTreeNode;
import aletheia.gui.common.PersistentJTree;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;

public class DelegateTreeJTree extends PersistentJTree
{
	private static final long serialVersionUID = 5047015418425845594L;
	private static final Logger logger = LoggerManager.instance.logger();

	private class MyTreeCellRenderer implements TreeCellRenderer
	{
		@Override
		public DelegateTreeModelNodeRenderer getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus)
		{
			if (!(value instanceof DelegateTreeModelNode))
				throw new Error();
			DelegateTreeModelNode node = (DelegateTreeModelNode) value;
			DelegateTreeModelNodeRenderer renderer = node.renderer(DelegateTreeJTree.this);
			renderer.setSelected(selected);
			renderer.setHasFocus(hasFocus);
			return renderer;
		}
	}

	private class MyTreeCellEditor extends AbstractCellEditor implements TreeCellEditor
	{

		private static final long serialVersionUID = 3313053835899064895L;
		private final MyTreeCellRenderer myTreeCellRenderer;

		public MyTreeCellEditor(MyTreeCellRenderer myTreeCellRenderer)
		{
			super();
			this.myTreeCellRenderer = myTreeCellRenderer;
		}

		@Override
		public Object getCellEditorValue()
		{
			return null;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
		{
			return myTreeCellRenderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}

		@Override
		public void cancelCellEditing()
		{
			TreePath treePath = getSelectionPath();
			if (treePath != null)
			{
				DelegateTreeModelNode node = (DelegateTreeModelNode) treePath.getLastPathComponent();
				if (node != null)
					node.renderer(DelegateTreeJTree.this).cancelEditing();
			}
			super.cancelCellEditing();
		}

		@Override
		public boolean stopCellEditing()
		{
			DelegateTreeModelNode node = (DelegateTreeModelNode) getSelectionPath().getLastPathComponent();
			if (node != null)
				node.renderer(DelegateTreeJTree.this).stopEditing();
			return super.stopCellEditing();
		}

	}

	private final AuthorityJPanel authorityJPanel;

	private class Listener implements KeyListener, TreeSelectionListener
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_DELETE:
				try
				{
					deleteSelected();
				}
				catch (InterruptedException e1)
				{
					logger.error(e1.getMessage(), e1);
				}
				break;
			}
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
		}
	}

	private final Listener listener;

	public class TreeModelListener extends TreeModelHandler
	{

		public void preTreeStructureChanged()
		{
			cancelEditing();
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

		public void postTreeStructureChanged()
		{
			expandAll();
		}

	}

	public DelegateTreeJTree(AuthorityJPanel authorityJPanel, StatementAuthority statementAuthority)
	{
		super(new DelegateTreeModel(authorityJPanel.getPersistenceManager(), statementAuthority), authorityJPanel.getFontManager());
		this.authorityJPanel = authorityJPanel;
		this.listener = new Listener();
		addKeyListener(this.listener);
		getSelectionModel().addTreeSelectionListener(this.listener);
		MyTreeCellRenderer myTreeCellRenderer = new MyTreeCellRenderer();
		setCellRenderer(myTreeCellRenderer);
		MyTreeCellEditor myTreeCellEditor = new MyTreeCellEditor(myTreeCellRenderer);
		setCellEditor(myTreeCellEditor);
		setRootVisible(false);
		setShowsRootHandles(true);
		setEditable(true);
		this.selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		expandAll();
	}

	@Override
	public DelegateTreeModel getModel()
	{
		return (DelegateTreeModel) super.getModel();
	}

	protected AuthorityJPanel getAuthorityJPanel()
	{
		return authorityJPanel;
	}

	public StatementAuthority getStatementAuthority()
	{
		return getModel().getStatementAuthority();
	}

	public void close()
	{
		getModel().shutdown();
	}

	protected DelegateTreeModelNode getSelectedNode()
	{
		TreePath treePath = getSelectionModel().getSelectionPath();
		if (treePath == null)
			return null;
		Object o = treePath.getLastPathComponent();
		if (!(o instanceof DelegateTreeModelNode))
			return null;
		return (DelegateTreeModelNode) o;
	}

	protected void deleteNode(DelegateTreeModelNode node) throws InterruptedException
	{
		CliJPanel cliJPanel = authorityJPanel.getContextJTreeJPanel().getAletheiaJPanel().getCliJPanel();
		if (node instanceof DelegateTreeModelBranchNode)
			cliJPanel.command(new DeleteDelegateTreeNode(cliJPanel, getPersistenceManager().beginTransaction(),
					((DelegateTreeModelBranchNode) node).getDelegateTreeNode()), false);
		else if (node instanceof DelegateTreeModelLeafNode)
			cliJPanel.command(new DeleteDelegateAuthorizer(cliJPanel, getPersistenceManager().beginTransaction(),
					((DelegateTreeModelLeafNode) node).getDelegateAuthorizer()), false);
		else
			throw new Error();
	}

	protected void deleteSelected() throws InterruptedException
	{
		DelegateTreeModelNode node = getSelectedNode();
		if (node != null)
			deleteNode(node);
	}

	@Override
	protected TreeModelListener createTreeModelListener()
	{
		return new TreeModelListener();
	}

	private void expandAll()
	{
		for (int i = 0; i < getRowCount(); i++)
			expandRow(i);
	}

}
