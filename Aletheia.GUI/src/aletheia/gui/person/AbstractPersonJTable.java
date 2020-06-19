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
package aletheia.gui.person;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.logging.log4j.Logger;

import aletheia.gui.common.renderer.AbstractRenderer;
import aletheia.gui.common.renderer.BoldTextLabelRenderer;
import aletheia.gui.common.renderer.EmptyRenderer;
import aletheia.gui.common.renderer.TextLabelRenderer;
import aletheia.gui.common.renderer.UUIDLabelRenderer;
import aletheia.gui.fonts.FontManager;
import aletheia.gui.person.AbstractPersonTableModel.AddedPersonTableModelEvent;
import aletheia.gui.person.AbstractPersonTableModel.PersonTableModelEvent;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.IncompleteDataSignatureException;
import aletheia.model.authority.Person;
import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.exceptions.BerkeleyDBPersistenceException;
import aletheia.persistence.exceptions.PersistenceException;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.ArrayAsList;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;

public abstract class AbstractPersonJTable extends JTable
{
	private static final long serialVersionUID = -2555445093957096158L;

	private final static Logger logger = LoggerManager.instance.logger();

	protected abstract class MyTableCellRenderer<T> implements TableCellRenderer
	{

		protected class MyCellRendererComponent extends JPanel
		{
			private static final long serialVersionUID = -1601600202021024971L;
			private final AbstractRenderer inner;

			protected MyCellRendererComponent(AbstractRenderer inner, LayoutManager layout)
			{
				super(layout);
				this.inner = inner;
				setBackground(inner.getBackground());
				add(inner);

			}

			protected AbstractRenderer getInner()
			{
				return inner;
			}

			protected void setSelected(boolean selected)
			{
				inner.setSelected(selected);
				setBackground(inner.getBackground());
			}

			protected void setHasFocus(boolean hasFocus)
			{
			}

			protected void setSelectedHasFocus(boolean selected, boolean hasFocus)
			{
				setSelected(selected);
				setHasFocus(hasFocus);
			}

		}

		protected MyTableCellRenderer()
		{
		}

		@SuppressWarnings("unchecked")
		@Override
		public MyCellRendererComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return obtainTableCellRenderer((T) value, isSelected, hasFocus, row, column);
		}

		protected MyCellRendererComponent obtainTableCellRenderer(T value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			AbstractRenderer renderer;
			if (value != null)
				renderer = buildRenderer(value);
			else
				renderer = new EmptyRenderer(getFontManager());
			if (row >= 0 && !getModel().isRowPrivate(row))
				renderer.setNormalBackgroundColor(new Color(0xefefef));
			MyCellRendererComponent myCellRendererComponent = buildMyCellRendererComponent(renderer);
			if (row >= 0)
				myCellRendererComponent.setSelectedHasFocus(isSelected, hasFocus);
			return myCellRendererComponent;
		}

		protected abstract Class<T> renderedClass();

		protected abstract AbstractRenderer buildRenderer(T value);

		protected MyCellRendererComponent buildMyCellRendererComponent(AbstractRenderer renderer)
		{
			return new MyCellRendererComponent(renderer, myCellRendererComponentLayout());
		}

		protected LayoutManager myCellRendererComponentLayout()
		{
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
			fl.setHgap(2);
			fl.setVgap(0);
			return fl;
		}

	}

	protected class StringTableCellRenderer extends MyTableCellRenderer<String>
	{

		@Override
		protected Class<String> renderedClass()
		{
			return String.class;
		}

		@Override
		protected AbstractRenderer buildRenderer(String value)
		{
			return new TextLabelRenderer(getFontManager(), value);
		}

	}

	protected class UUIDTableCellRenderer extends MyTableCellRenderer<UUID>
	{

		@Override
		protected Class<UUID> renderedClass()
		{
			return UUID.class;
		}

		@Override
		protected AbstractRenderer buildRenderer(UUID uuid)
		{
			return new UUIDLabelRenderer(getFontManager(), uuid);
		}

	}

	protected class HeaderTableCellRenderer extends MyTableCellRenderer<String>
	{
		@Override
		protected Class<String> renderedClass()
		{
			return String.class;
		}

		@Override
		protected AbstractRenderer buildRenderer(String value)
		{
			TextLabelRenderer r = new BoldTextLabelRenderer(getFontManager(), value.toString());
			r.setBackground(new Color(0xeeeeee));
			return r;
		}

		@Override
		protected LayoutManager myCellRendererComponentLayout()
		{
			return new FlowLayout(FlowLayout.CENTER);
		}

		@Override
		protected MyCellRendererComponent buildMyCellRendererComponent(AbstractRenderer renderer)
		{
			MyCellRendererComponent myCellRendererComponent = new MyCellRendererComponent(renderer, myCellRendererComponentLayout());
			Border border = BorderFactory.createMatteBorder(0, 0, 1, 1, getGridColor());
			myCellRendererComponent.setBorder(border);
			return myCellRendererComponent;
		}

	}

	protected abstract class MyTableCellEditor<T> implements TableCellEditor
	{
		private final Set<CellEditorListener> listeners;

		protected MyTableCellEditor()
		{
			this.listeners = Collections.synchronizedSet(new HashSet<CellEditorListener>());
			addKeyListener(new KeyListener()
			{

				@Override
				public void keyTyped(KeyEvent e)
				{
				}

				@Override
				public void keyPressed(KeyEvent e)
				{
				}

				@Override
				public void keyReleased(KeyEvent e)
				{
					switch (e.getKeyCode())
					{
					case KeyEvent.VK_TAB:
					case KeyEvent.VK_SPACE:
						int row = getSelectedRow();
						int column = getSelectedColumn();
						if (row >= 0 && column >= 0)
							editCellAt(row, column);
						break;
					}
				}
			});
		}

		@Override
		public abstract T getCellEditorValue();

		@Override
		public abstract boolean isCellEditable(EventObject anEvent);

		@Override
		public abstract boolean shouldSelectCell(EventObject anEvent);

		@Override
		public abstract boolean stopCellEditing();

		@Override
		public abstract void cancelCellEditing();

		@Override
		public void addCellEditorListener(CellEditorListener l)
		{
			this.listeners.add(l);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l)
		{
			this.listeners.remove(l);
		}

		protected Set<CellEditorListener> getListeners()
		{
			return Collections.unmodifiableSet(listeners);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			return obtainTableCellEditor((T) value, isSelected, row, column);
		}

		protected abstract Component obtainTableCellEditor(T value, boolean isSelected, int row, int column);

		public abstract void updateFontSize();

	}

	protected class StringTableCellEditor extends MyTableCellEditor<String>
	{
		private final JTextField textField;

		protected StringTableCellEditor()
		{
			this.textField = new JTextField();
		}

		@Override
		public void updateFontSize()
		{
			this.textField.setFont(getFontManager().defaultFont());
		}

		@Override
		public String getCellEditorValue()
		{
			return textField.getText().trim();
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
			ChangeEvent event = new ChangeEvent(textField);
			try
			{
				for (CellEditorListener l : new BufferedList<>(getListeners()))
					l.editingStopped(event);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(AbstractPersonJTable.this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}

		@Override
		public void cancelCellEditing()
		{
			textField.setText(null);
			ChangeEvent event = new ChangeEvent(textField);
			try
			{
				for (CellEditorListener l : new BufferedList<>(getListeners()))
					l.editingCanceled(event);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(AbstractPersonJTable.this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		@Override
		protected Component obtainTableCellEditor(String value, boolean isSelected, int row, int column)
		{
			this.textField.setText(value);
			if (value != null)
			{
				this.textField.setSelectionStart(0);
				this.textField.setSelectionEnd(value.length());
			}
			return this.textField;
		}

	}

	private final StringTableCellEditor stringTableCellEditor;

	private class Listener implements KeyListener
	{

		@Override
		public void keyTyped(KeyEvent e)
		{
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_DELETE:
				delete();
				break;
			case KeyEvent.VK_INSERT:
				insert();
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}

	}

	private final PersonsDialog personsDialog;

	private final Listener listener;
	private PrivatePerson inserted;

	public AbstractPersonJTable(AbstractPersonTableModel personTableModel, PersonsDialog personsDialog)
	{
		super(personTableModel);
		this.personsDialog = personsDialog;
		this.stringTableCellEditor = new StringTableCellEditor();
		this.listener = new Listener();
		addKeyListener(listener);
		this.inserted = null;
		setAutoResizeMode(AUTO_RESIZE_OFF);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
		updateFontSize();
		setDefaultRenderer(String.class, new StringTableCellRenderer());
		setDefaultRenderer(UUID.class, new UUIDTableCellRenderer());
		setDefaultEditor(String.class, stringTableCellEditor);
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setDefaultRenderer(new HeaderTableCellRenderer());
	}

	@Override
	public AbstractPersonTableModel getModel()
	{
		return (AbstractPersonTableModel) super.getModel();
	}

	protected PersonsDialog getPersonsDialog()
	{
		return personsDialog;
	}

	protected FontManager getFontManager()
	{
		return getPersonsDialog().getFontManager();
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

	public void cancelEditing()
	{
		TableCellEditor editor = getCellEditor();
		if (editor != null)
			editor.cancelCellEditing();
	}

	@Override
	public boolean editCellAt(int row, int column, EventObject e)
	{
		boolean ret = super.editCellAt(row, column, e);
		if (ret)
		{
			getEditorComponent().requestFocus();
			scrollRectToVisible(getCellRect(row, column, true));
		}
		return ret;
	}

	public void updateFontSize()
	{
		getColumnModel().getColumn(0).setPreferredWidth(computeWidth(15));
		getColumnModel().getColumn(1).setPreferredWidth(computeWidth(40));
		getColumnModel().getColumn(2).setPreferredWidth(computeWidth(30));
		getColumnModel().getColumn(3).setPreferredWidth(computeWidth(36));
		setRowHeight(computeHeight());
		stringTableCellEditor.updateFontSize();
	}

	protected void delete()
	{
		int[] rows = getSelectedRows();
		clearSelection();
		Collection<Person> persons = new BijectionCollection<>(new Bijection<Integer, Person>()
		{

			@Override
			public Person forward(Integer row)
			{
				return getModel().getPerson(row);
			}

			@Override
			public Integer backward(Person output)
			{
				throw new UnsupportedOperationException();
			}
		}, new ArrayAsList<Integer>(rows));

		class DeleteException extends Exception
		{
			private static final long serialVersionUID = 6739395659850068590L;

			private DeleteException(String message)
			{
				super(message);
			}
		}

		Transaction transaction = getModel().beginTransaction();
		try
		{
			boolean notOrphans = true;
			boolean processed = false;
			for (Person person : persons)
				if (person != null)
				{
					processed = true;
					if (person.isOrphan())
					{
						person.delete(transaction);
						notOrphans = false;
					}
				}
			if (processed && notOrphans)
				throw new DeleteException("Not orphan(s).");
			transaction.commit();
		}
		catch (PersistenceException | DeleteException e)
		{
			JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			transaction.abort();
		}
	}

	protected void insert()
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			int n = -1;
			PrivatePerson person = null;
			while (true)
			{
				String nick = "*new person";
				if (n >= 0)
					nick = nick + n;
				if (!getModel().getPersistenceManager().privatePersonsByNick(transaction).containsKey(nick))
				{
					person = PrivatePerson.create(getModel().getPersistenceManager(), transaction, nick);
					try
					{
						person.sign(transaction);
					}
					catch (IncompleteDataSignatureException e)
					{
						throw new RuntimeException(e);
					}
					person.persistenceUpdate(transaction);
					break;
				}
				n++;
			}
			inserted = person;
			transaction.commit();
		}
		catch (BerkeleyDBPersistenceException e)
		{
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(this, MiscUtilities.wrapText(e.getMessage(), 80), "Error", JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		super.tableChanged(e);
		cancelEditing();
		if (e instanceof PersonTableModelEvent)
		{
			if (e instanceof AddedPersonTableModelEvent)
			{
				AddedPersonTableModelEvent pe = (AddedPersonTableModelEvent) e;
				if (pe.getPerson().equals(inserted))
				{
					int row = pe.getFirstRow();
					if (row >= 0)
					{
						setRowSelectionInterval(row, row);
						editCellAt(row, 0);
					}
					inserted = null;
				}
			}
		}
	}

}
