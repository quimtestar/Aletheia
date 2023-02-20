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
package aletheia.gui.authority;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import aletheia.gui.common.datatransfer.StatementTransferable;
import aletheia.gui.common.renderer.AbstractPersistentRenderer;
import aletheia.gui.common.renderer.AbstractRenderer;
import aletheia.gui.common.renderer.BoldTextLabelRenderer;
import aletheia.gui.common.renderer.TextLabelRenderer;
import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaLookAndFeel;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.statement.Statement;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

public class StatementListJTable extends JTable
{
	private static final long serialVersionUID = 2156074004820098390L;
	private final static int transactionTimeout = 100;

	private class MyCellRendererComponent extends JPanel
	{
		private static final long serialVersionUID = -1601600202021024971L;

		private final AbstractRenderer inner;

		private class Listener implements MouseListener
		{
			boolean draggable = false;

			@Override
			public void mouseClicked(MouseEvent e)
			{
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				draggable = true;
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				draggable = false;
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				draggable = false;
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				if (draggable && ((e.getModifiersEx() & MouseEvent.MOUSE_PRESSED) != 0))
					StatementListJTable.this.getTransferHandler().exportAsDrag(StatementListJTable.this, e, TransferHandler.COPY);
			}
		}

		private final Listener listener;

		private MyCellRendererComponent(AbstractRenderer inner, LayoutManager layout)
		{
			super(layout);
			this.inner = inner;
			this.listener = new Listener();
			addMouseListener(listener);
			setBackground(inner.getBackground());
			add(inner);

		}

		@SuppressWarnings("unused")
		private AbstractRenderer getInner()
		{
			return inner;
		}

		private void setSelected(boolean selected)
		{
			inner.setSelected(selected);
			setBackground(inner.getBackground());
		}

		private void setHasFocus(boolean hasFocus)
		{
		}

		private void setSelectedHasFocus(boolean selected, boolean hasFocus)
		{
			setSelected(selected);
			setHasFocus(hasFocus);
		}

		public void stopEditing()
		{
			inner.stopEditing();
		}

		public void cancelEditing()
		{
			inner.cancelEditing();
		}

	}

	private class MyTableCellRenderer implements TableCellRenderer
	{
		private class StatementRenderer extends AbstractPersistentRenderer
		{
			private static final long serialVersionUID = -6986954260059659756L;
			private final Statement statement;
			private final StatementAuthority statementAuthority;

			public StatementRenderer(Statement statement)
			{
				super(StatementListJTable.this.getFontManager(), StatementListJTable.this.getPersistenceManager(), true);
				Transaction transaction = beginTransaction();
				try
				{
					this.statement = statement.refresh(transaction);
					if (this.statement == null)
						this.statementAuthority = null;
					else
					{
						this.statementAuthority = this.statement.getAuthority(transaction);
						addAuthorityLabel();
						addSpaceLabel();
						addVariableReferenceComponent(this.statement.parentVariableToIdentifier(transaction), this.statement.getVariable());
					}
				}
				finally
				{
					transaction.abort();
				}
			}

			@Override
			protected Transaction beginTransaction()
			{
				return getPersistenceManager().beginTransaction(transactionTimeout);
			}

			private JLabel addAuthorityLabel()
			{
				JLabel jLabel;
				if (statementAuthority != null)
					jLabel = addSignatureStatusSymbolLabel(statementAuthority.signatureStatus());
				else
					jLabel = addSpaceLabel();
				return jLabel;
			}

			@Override
			protected void mouseClickedOnVariableReference(VariableTerm variable, MouseEvent ev)
			{
				mouseClickedOnStatement(statement);
			}

		}

		private final Map<Statement, MyCellRendererComponent> rendererMap;

		public MyTableCellRenderer()
		{
			this.rendererMap = new HashMap<>();
		}

		protected MyCellRendererComponent getCellRendererComponent(Statement statement, boolean isSelected, boolean hasFocus)
		{
			MyCellRendererComponent renderer = rendererMap.get(statement);
			if (renderer == null)
			{
				renderer = new MyCellRendererComponent(new StatementRenderer(statement), myCellRendererComponentLayout());
				rendererMap.put(statement, renderer);
			}
			renderer.setSelectedHasFocus(isSelected, hasFocus);
			return renderer;
		}

		private LayoutManager myCellRendererComponentLayout()
		{
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
			fl.setHgap(2);
			fl.setVgap(0);
			return fl;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Statement statement;
			if (value == null)
				statement = getModel().getValueAt(row, column);
			else if (value instanceof Statement)
				statement = (Statement) value;
			else
				throw new IllegalArgumentException();
			return getCellRendererComponent(statement, isSelected, hasFocus);
		}

		public void clearRenderer(Statement statement)
		{
			rendererMap.remove(statement);
		}
	}

	private class MyTableCellEditor implements TableCellEditor
	{
		private final MyTableCellRenderer myTableCellRenderer;
		private final Set<CellEditorListener> listeners;
		private MyCellRendererComponent myCellRendererComponent;

		public MyTableCellEditor(MyTableCellRenderer myTableCellRenderer)
		{
			this.myTableCellRenderer = myTableCellRenderer;
			this.listeners = Collections.synchronizedSet(new HashSet<CellEditorListener>());
			this.myCellRendererComponent = null;
		}

		@Override
		public Object getCellEditorValue()
		{
			return null;
		}

		@Override
		public boolean isCellEditable(EventObject anEvent)
		{
			return true;
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent)
		{
			return true;
		}

		@Override
		public boolean stopCellEditing()
		{
			if (myCellRendererComponent != null)
				myCellRendererComponent.stopEditing();
			ChangeEvent e = new ChangeEvent(this);
			synchronized (listeners)
			{
				for (CellEditorListener l : listeners)
				{
					l.editingStopped(e);
				}
			}
			return true;
		}

		@Override
		public void cancelCellEditing()
		{
			if (myCellRendererComponent != null)
				myCellRendererComponent.cancelEditing();
			ChangeEvent e = new ChangeEvent(this);
			synchronized (listeners)
			{
				for (CellEditorListener l : listeners)
				{
					l.editingCanceled(e);
				}
			}
		}

		@Override
		public void addCellEditorListener(CellEditorListener l)
		{
			listeners.add(l);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l)
		{
			listeners.remove(l);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			Statement statement;
			if (value == null)
				statement = getModel().getValueAt(row, column);
			else if (value instanceof Statement)
				statement = (Statement) value;
			else
				throw new IllegalArgumentException();
			myCellRendererComponent = myTableCellRenderer.getCellRendererComponent(statement, true, true);
			return myCellRendererComponent;
		}

	}

	private class HeaderTableCellRenderer implements TableCellRenderer
	{
		private final Map<String, MyCellRendererComponent> rendererMap;

		public HeaderTableCellRenderer()
		{
			this.rendererMap = new HashMap<>();
		}

		protected MyCellRendererComponent getCellRendererComponent(String text, boolean isSelected, boolean hasFocus)
		{
			MyCellRendererComponent renderer = rendererMap.get(text);
			if (renderer == null)
			{
				TextLabelRenderer r = new BoldTextLabelRenderer(getFontManager(), text);
				r.setBackground(AletheiaLookAndFeel.theme().getTableBackground());
				renderer = new MyCellRendererComponent(r, myCellRendererComponentLayout());
				Border border = BorderFactory.createMatteBorder(0, 0, 1, 1, getGridColor());
				renderer.setBorder(border);
				rendererMap.put(text, renderer);
			}
			return renderer;
		}

		protected LayoutManager myCellRendererComponentLayout()
		{
			return new FlowLayout(FlowLayout.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if (!(value instanceof String))
				throw new IllegalArgumentException();
			return getCellRendererComponent((String) value, isSelected, hasFocus);
		}
	}

	private final AuthorityHeaderJPanel authorityHeaderJPanel;
	private final MyTableCellRenderer myTableCellRenderer;

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

	public StatementListJTable(AuthorityHeaderJPanel authorityHeaderJPanel, String name, List<Statement> statementList)
	{
		super(new StatementListTableModel(name, statementList));
		this.authorityHeaderJPanel = authorityHeaderJPanel;
		this.myTableCellRenderer = new MyTableCellRenderer();
		setDragEnabled(true);
		setTransferHandler(new MyTransferHandler());
		setDefaultRenderer(Statement.class, myTableCellRenderer);
		MyTableCellEditor editor = new MyTableCellEditor(myTableCellRenderer);
		setDefaultEditor(Statement.class, editor);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setDefaultRenderer(new HeaderTableCellRenderer());
		int width = name.length();
		for (Statement st : statementList)
		{
			int l = st.getIdentifier() != null ? st.getIdentifier().qualifiedName().length() : 9;
			if (l + 2 > width)
				width = l + 2;
		}
		setRowHeight(computeHeight());
		setPreferredScrollableViewportSize(new Dimension(computeWidth(width), 5 * computeHeight()));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, null);
		setFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, null);
	}

	@Override
	public StatementListTableModel getModel()
	{
		return (StatementListTableModel) super.getModel();
	}

	protected AuthorityHeaderJPanel getAuthorityHeaderJPanel()
	{
		return authorityHeaderJPanel;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return getAuthorityHeaderJPanel().getPersistenceManager();
	}

	protected FontManager getFontManager()
	{
		return getAuthorityHeaderJPanel().getFontManager();
	}

	private int computeWidth(int textLength)
	{
		return getFontManager().fontMetrics().charWidth('x') * textLength + 10;
	}

	private int computeHeight()
	{
		return getFontManager().fontMetrics().getHeight();
	}

	protected void mouseClickedOnStatement(Statement statement)
	{

	}

	public void close()
	{
		getModel().shutdown();
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		cancelEditing();
		if (e instanceof StatementListTableModel.MyTableModelEvent)
		{
			Statement statement = ((StatementListTableModel.MyTableModelEvent) e).getStatement();
			myTableCellRenderer.clearRenderer(statement);
		}
		super.tableChanged(e);
	}

	@SuppressWarnings("unused")
	private void navigateToRow(int i)
	{
		clearSelection();
		getSelectionModel().setSelectionInterval(i, i);
		editCellAt(i, 0);
	}

	private Statement getSelectedStatement()
	{
		if (getSelectedRow() >= 0)
			return getModel().getStatementList().get(getSelectedRow());
		else
			return null;
	}

	public void cancelEditing()
	{
		TableCellEditor editor = getCellEditor();
		if (editor != null)
			editor.cancelCellEditing();
	}

}
