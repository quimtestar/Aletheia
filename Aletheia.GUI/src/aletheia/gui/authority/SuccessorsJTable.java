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
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
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

import aletheia.gui.authority.SuccessorsTableModel.SuccessorEntryData;
import aletheia.gui.common.renderer.AbstractRenderer;
import aletheia.gui.common.renderer.BoldTextLabelRenderer;
import aletheia.gui.common.renderer.DateLabelRenderer;
import aletheia.gui.common.renderer.PersonLabelRenderer;
import aletheia.gui.common.renderer.TextLabelRenderer;
import aletheia.gui.fonts.FontManager;
import aletheia.gui.lookandfeel.AletheiaLookAndFeel;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.Person;
import aletheia.persistence.PersistenceManager;

public class SuccessorsJTable extends JTable
{
	private static final long serialVersionUID = 2156074004820098390L;

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
					SuccessorsJTable.this.getTransferHandler().exportAsDrag(SuccessorsJTable.this, e, TransferHandler.COPY);
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

	private abstract class MyTableCellRenderer<T> implements TableCellRenderer
	{
		private final Map<T, MyCellRendererComponent> rendererMap;

		public MyTableCellRenderer()
		{
			this.rendererMap = new HashMap<>();
		}

		protected abstract MyCellRendererComponent makeMyCellRendererComponent(T object);

		protected MyCellRendererComponent getCellRendererComponent(T object, boolean isSelected, boolean hasFocus)
		{
			MyCellRendererComponent renderer = rendererMap.get(object);
			if (renderer == null)
				renderer = makeMyCellRendererComponent(object);
			renderer.setSelectedHasFocus(isSelected, hasFocus);
			return renderer;
		}

		protected LayoutManager myCellRendererComponentLayout()
		{
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
			fl.setHgap(2);
			fl.setVgap(0);
			return fl;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return getCellRendererComponent((T) value, isSelected, hasFocus);
		}

		public void clearRenderer(T object)
		{
			rendererMap.remove(object);
		}
	}

	private class MyTableDateCellRenderer extends MyTableCellRenderer<Date>
	{

		@Override
		protected MyCellRendererComponent makeMyCellRendererComponent(Date date)
		{
			return new MyCellRendererComponent(new DateLabelRenderer(getFontManager(), date), myCellRendererComponentLayout());
		}

	}

	private class MyTablePersonCellRenderer extends MyTableCellRenderer<Person>
	{

		@Override
		protected MyCellRendererComponent makeMyCellRendererComponent(Person person)
		{
			return new MyCellRendererComponent(new PersonLabelRenderer(getFontManager(), person), myCellRendererComponentLayout());
		}

	}

	private class MyTableCellEditor<T> implements TableCellEditor
	{
		private final MyTableCellRenderer<T> myTableCellRenderer;
		private final Set<CellEditorListener> listeners;
		private MyCellRendererComponent myCellRendererComponent;

		public MyTableCellEditor(MyTableCellRenderer<T> myTableCellRenderer)
		{
			this.myTableCellRenderer = myTableCellRenderer;
			this.listeners = Collections.synchronizedSet(new HashSet<>());
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

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			myCellRendererComponent = myTableCellRenderer.getCellRendererComponent((T) value, true, true);
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
	private final MyTableDateCellRenderer myTableDateCellRenderer;
	private final MyTablePersonCellRenderer myTablePersonCellRenderer;

	public SuccessorsJTable(AuthorityHeaderJPanel authorityHeaderJPanel, DelegateTreeRootNode delegateTreeRootNode)
	{
		super(new SuccessorsTableModel(authorityHeaderJPanel.getPersistenceManager(), delegateTreeRootNode));
		this.authorityHeaderJPanel = authorityHeaderJPanel;
		this.myTableDateCellRenderer = new MyTableDateCellRenderer();
		setDefaultRenderer(Date.class, myTableDateCellRenderer);
		this.myTablePersonCellRenderer = new MyTablePersonCellRenderer();
		setDefaultRenderer(Person.class, myTablePersonCellRenderer);
		MyTableCellEditor<Person> editor = new MyTableCellEditor<>(myTablePersonCellRenderer);
		setDefaultEditor(Person.class, editor);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setDefaultRenderer(new HeaderTableCellRenderer());
		setRowHeight(computeHeight());
		int dateWidth = 15;
		getColumnModel().getColumn(0).setPreferredWidth(computeWidth(dateWidth));
		int nickWidth = getModel().getColumnName(1).length();
		for (int i = 0; i < getModel().getRowCount(); i++)
		{
			SuccessorEntryData successorEntryData = getModel().getSuccessorEntryData(i);
			int l = successorEntryData.getSuccessor().getNick().length();
			if (l + 2 > nickWidth)
				nickWidth = l + 2;
		}
		getColumnModel().getColumn(1).setPreferredWidth(computeWidth(nickWidth));
		setPreferredScrollableViewportSize(new Dimension(computeWidth(dateWidth + nickWidth), 5 * computeHeight()));
	}

	@Override
	public SuccessorsTableModel getModel()
	{
		return (SuccessorsTableModel) super.getModel();
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

	public void close()
	{
		getModel().shutdown();
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		cancelEditing();
		if (e instanceof SuccessorsTableModel.MyTableModelEvent)
		{
			SuccessorEntryData successorEntryData = ((SuccessorsTableModel.MyTableModelEvent) e).getSuccessorEntryData();
			myTableDateCellRenderer.clearRenderer(successorEntryData.getSignatureDate());
			myTablePersonCellRenderer.clearRenderer(successorEntryData.getSuccessor());
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

	public void cancelEditing()
	{
		TableCellEditor editor = getCellEditor();
		if (editor != null)
			editor.cancelCellEditing();
	}

}
