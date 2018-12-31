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

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.logging.log4j.Logger;

import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.cli.command.authority.DeleteSignatureRequest;
import aletheia.gui.cli.command.authority.RemoveStatementFromSignatureRequest;
import aletheia.gui.common.PersistentJTree;
import aletheia.gui.common.datatransfer.AletheiaTransferable;
import aletheia.gui.common.datatransfer.SignatureRequestTransferable;
import aletheia.gui.common.datatransfer.StatementDataFlavor;
import aletheia.gui.common.datatransfer.StatementTransferable;
import aletheia.gui.common.datatransfer.UUIDTransferable;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class SignatureRequestJTree extends PersistentJTree
{
	private static final long serialVersionUID = 8695555047460498899L;

	private final static Logger logger = LoggerManager.instance.logger();

	private final AletheiaJPanel aletheiaJPanel;

	private class MyTreeCellRenderer implements TreeCellRenderer
	{
		@Override
		public SignatureRequestTreeNodeRenderer getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
				int row, boolean hasFocus)
		{
			if (!(value instanceof SignatureRequestTreeNode))
				throw new Error();
			SignatureRequestTreeNode node = (SignatureRequestTreeNode) value;
			SignatureRequestTreeNodeRenderer renderer = node.renderer(SignatureRequestJTree.this);
			renderer.setSelected(selected);
			renderer.setHasFocus(hasFocus);
			return renderer;
		}
	}

	private class MyTreeCellEditor extends AbstractCellEditor implements TreeCellEditor
	{
		private static final long serialVersionUID = -2408690930878899976L;
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
				SignatureRequestTreeNode node = (SignatureRequestTreeNode) treePath.getLastPathComponent();
				if (node != null)
					node.renderer(SignatureRequestJTree.this).cancelEditing();
			}
			super.cancelCellEditing();
		}

		@Override
		public boolean stopCellEditing()
		{
			SignatureRequestTreeNode node = (SignatureRequestTreeNode) getSelectionPath().getLastPathComponent();
			if (node != null)
				node.renderer(SignatureRequestJTree.this).stopEditing();
			return super.stopCellEditing();
		}

	}

	private class MyTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = -4265378865649796747L;

		@Override
		public int getSourceActions(JComponent c)
		{
			return COPY;
		}

		@Override
		protected AletheiaTransferable createTransferable(JComponent c)
		{
			SignatureRequestTreeNode node = getSelectedSignatureRequestNode();
			if (node == null)
				return null;
			if (node instanceof RequestSignatureRequestTreeNode)
				return new SignatureRequestTransferable(((RequestSignatureRequestTreeNode) node).getSignatureRequest());
			else if (node instanceof StatementSignatureRequestTreeNode)
				return new StatementTransferable(((StatementSignatureRequestTreeNode) node).getStatement());
			else if (node instanceof ActualContextSignatureRequestTreeNode)
				return new StatementTransferable(node.getContext());
			else if (node instanceof VirtualContextSignatureRequestTreeNode)
				return new UUIDTransferable(((VirtualContextSignatureRequestTreeNode) node).getContextUuid());
			else
				return null;
		}

		@Override
		protected void exportDone(JComponent c, Transferable t, int action)
		{
		}

		@Override
		public boolean importData(JComponent comp, Transferable t)
		{
			SignatureRequestTreeNode node = getSelectedSignatureRequestNode();
			if (node instanceof UnpackedRequestSignatureRequestTreeNode)
			{
				if (t.isDataFlavorSupported(StatementDataFlavor.instance))
				{
					Transaction transaction = getModel().beginTransaction();
					try
					{
						UnpackedSignatureRequest unpackedSignatureRequest = ((UnpackedRequestSignatureRequestTreeNode) node).getSignatureRequest()
								.refresh(transaction);
						Statement statement = (Statement) t.getTransferData(StatementDataFlavor.instance);
						unpackedSignatureRequest.addStatement(transaction, statement);
						transaction.commit();
						return true;
					}
					catch (Exception e)
					{
						try
						{
							getAletheiaJPanel().getCliJPanel().exception(e);
							logger.error(e.getMessage(), e);
						}
						catch (InterruptedException e1)
						{
							logger.error(e1.getMessage(), e1);
						}
						return false;
					}
					finally
					{
						transaction.abort();
					}
				}
			}
			return false;
		}

		@Override
		public boolean canImport(TransferSupport support)
		{
			DropLocation dl = support.getDropLocation();
			if (dl instanceof JTree.DropLocation)
			{
				JTree.DropLocation jtreeDl = (JTree.DropLocation) dl;
				TreePath path = jtreeDl.getPath();
				if (path != null)
				{
					Object last = path.getLastPathComponent();
					if (last instanceof UnpackedRequestSignatureRequestTreeNode)
					{
						//@formatter:off
						if (Arrays.asList(support.getDataFlavors()).contains(StatementDataFlavor.instance))
							return true;
						//@formatter:on
						return false;
					}
				}
			}
			return false;
		}

	}

	private class Listener implements KeyListener
	{

		@Override
		public void keyTyped(KeyEvent ev)
		{
		}

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_DELETE:
			{
				SignatureRequestTreeNode node = getSelectedSignatureRequestNode();
				if (node != null)
				{
					if (node instanceof RequestSignatureRequestTreeNode)
					{
						SignatureRequest signatureRequest = ((RequestSignatureRequestTreeNode) node).getSignatureRequest();
						if (signatureRequest != null)
						{
							try
							{
								deleteSignatureRequest(signatureRequest);
							}
							catch (InterruptedException e)
							{
								logger.error(e.getMessage(), e);
							}
						}
					}
					else if (node instanceof StatementSignatureRequestTreeNode)
					{
						UnpackedSignatureRequest unpackedSignatureRequest = ((StatementSignatureRequestTreeNode) node).getUnpackedSignatureRequest();
						Statement statement = ((StatementSignatureRequestTreeNode) node).getStatement();
						if (unpackedSignatureRequest != null && statement != null)
						{
							try
							{
								removeStatementFromUnpackedSignatureRequest(unpackedSignatureRequest, statement);
							}
							catch (InterruptedException e)
							{
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
				break;
			}
			case KeyEvent.VK_F3:
			{
				SignatureRequestTreeNode node = getSelectedSignatureRequestNode();
				if (node != null)
				{
					Context context = node.getContext();
					if (context != null)
						getAletheiaJPanel().getCliJPanel().setActiveContext(node.getContext());
				}
				break;
			}

			}
		}

		@Override
		public void keyReleased(KeyEvent ev)
		{
		}
	}

	private class MyTreeModelHandler extends TreeModelHandler
	{

		@Override
		public void treeNodesChanged(TreeModelEvent e)
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

			boolean editing = false;
			if (getEditingPath() != null && getEditingPath().equals(e.getTreePath()))
			{
				if (isFocusOwner())
					editing = true;
				cancelEditing();
			}
			super.treeStructureChanged(e);
			if (editing)
				startEditingAtPath(e.getTreePath());
		}

	}

	public SignatureRequestJTree(AletheiaJPanel aletheiaJPanel)
	{
		super(new SignatureRequestTreeModel(aletheiaJPanel.getPersistenceManager()), aletheiaJPanel.getFontManager());
		this.aletheiaJPanel = aletheiaJPanel;
		addKeyListener(new Listener());
		MyTreeCellRenderer myTreeCellRenderer = new MyTreeCellRenderer();
		setCellRenderer(myTreeCellRenderer);
		MyTreeCellEditor myTreeCellEditor = new MyTreeCellEditor(myTreeCellRenderer);
		setCellEditor(myTreeCellEditor);
		setRootVisible(true);
		setShowsRootHandles(true);
		setEditable(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setTransferHandler(new MyTransferHandler());
	}

	protected AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	@Override
	public SignatureRequestTreeModel getModel()
	{
		return (SignatureRequestTreeModel) super.getModel();
	}

	public void selectUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest)
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			cancelEditing();
			TreePath path = getModel().pathForUnpackedSignatureRequest(transaction, unpackedSignatureRequest);
			if (path != null)
				getSelectionModel().setSelectionPath(path);
		}
		finally
		{
			transaction.abort();
		}
	}

	public SignatureRequestTreeNode getSelectedSignatureRequestNode()
	{
		TreePath path = getSelectionModel().getSelectionPath();
		if (path == null)
			return null;
		return (SignatureRequestTreeNode) path.getLastPathComponent();
	}

	public void deleteSignatureRequest(SignatureRequest signatureRequest) throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel()
				.command(new DeleteSignatureRequest(getAletheiaJPanel().getCliJPanel(), getPersistenceManager().beginTransaction(), signatureRequest), false);
	}

	public void selectStatement(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement)
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			cancelEditing();
			TreePath path = getModel().pathForStatement(transaction, unpackedSignatureRequest, statement);
			if (path != null)
			{
				getSelectionModel().setSelectionPath(path);
			}
		}
		finally
		{
			transaction.abort();
		}
	}

	public void removeStatementFromUnpackedSignatureRequest(UnpackedSignatureRequest unpackedSignatureRequest, Statement statement) throws InterruptedException
	{
		getAletheiaJPanel().getCliJPanel().command(new RemoveStatementFromSignatureRequest(getAletheiaJPanel().getCliJPanel(),
				getPersistenceManager().beginTransaction(), unpackedSignatureRequest, statement), false);
	}

	@Override
	protected MyTreeModelHandler createTreeModelListener()
	{
		return new MyTreeModelHandler();
	}

	public void close()
	{
		getModel().shutdown();
	}

}
