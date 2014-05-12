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
package aletheia.gui.authority;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.LayoutManager;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import aletheia.gui.app.AletheiaJFrame;
import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.cli.command.authority.DeleteSignatures;
import aletheia.gui.common.AbstractRenderer;
import aletheia.gui.common.BoldTextLabelRenderer;
import aletheia.gui.common.BooleanLabelRenderer;
import aletheia.gui.common.DateLabelRenderer;
import aletheia.gui.common.EmptyRenderer;
import aletheia.gui.common.NamespaceLabelRenderer;
import aletheia.gui.common.PersonLabelRenderer;
import aletheia.gui.common.SignatoryLabelRenderer;
import aletheia.gui.common.StatementAuthoritySignatureTransferable;
import aletheia.gui.common.StatementLabelRenderer;
import aletheia.gui.common.TextLabelRenderer;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeJPanel;
import aletheia.gui.font.FontManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;
import aletheia.persistence.exceptions.PersistenceLockTimeoutException;
import aletheia.utilities.collections.ArrayAsList;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionList;

public abstract class AbstractAuthoritySignatureJTable extends JTable
{
	private static final long serialVersionUID = -6172102057500617280L;

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

			public void stopEditing()
			{
				inner.stopEditing();
			}

			public void cancelEditing()
			{
				inner.cancelEditing();
			}

		}

		private final Map<T, MyCellRendererComponent> myCellRendererComponents;

		protected MyTableCellRenderer()
		{
			this.myCellRendererComponents = new IdentityHashMap<T, MyCellRendererComponent>();
		}

		@SuppressWarnings("unchecked")
		@Override
		public MyCellRendererComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return obtainTableCellRenderer((T) value, isSelected, hasFocus, row, column);
		}

		protected MyCellRendererComponent obtainTableCellRenderer(T value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			MyCellRendererComponent myCellRendererComponent = myCellRendererComponents.get(value);
			if (myCellRendererComponent == null)
			{
				AbstractRenderer renderer;
				if (value != null)
					renderer = buildRenderer(value);
				else
					renderer = new EmptyRenderer();
				myCellRendererComponent = buildMyCellRendererComponent(renderer);
				myCellRendererComponents.put(value, myCellRendererComponent);
			}
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

	protected class SignatoryTableCellRenderer extends MyTableCellRenderer<Signatory>
	{

		@Override
		protected SignatoryLabelRenderer buildRenderer(Signatory signatory)
		{
			return new SignatoryLabelRenderer(signatory);
		}

		@Override
		protected Class<Signatory> renderedClass()
		{
			return Signatory.class;
		}

	}

	protected class DateTableCellRenderer extends MyTableCellRenderer<Date>
	{
		@Override
		protected DateLabelRenderer buildRenderer(Date date)
		{
			return new DateLabelRenderer(date);
		}

		@Override
		protected Class<Date> renderedClass()
		{
			return Date.class;
		}

	}

	protected class BooleanTableCellRenderer extends MyTableCellRenderer<Boolean>
	{
		@Override
		protected BooleanLabelRenderer buildRenderer(Boolean bool)
		{
			return new BooleanLabelRenderer(bool);
		}

		@Override
		protected Class<Boolean> renderedClass()
		{
			return Boolean.class;
		}

		@Override
		protected LayoutManager myCellRendererComponentLayout()
		{
			LayoutManager lm = super.myCellRendererComponentLayout();
			if (lm instanceof FlowLayout)
				((FlowLayout) lm).setAlignment(FlowLayout.CENTER);
			return lm;
		}
	}

	protected class ContextTableCellRenderer extends MyTableCellRenderer<Context>
	{
		@Override
		protected StatementLabelRenderer buildRenderer(Context context)
		{
			return new StatementLabelRenderer(getModel().getPersistenceManager(), context, true)
			{
				private static final long serialVersionUID = -1084520290529382462L;

				@Override
				protected void mouseClickedOnVariableReference(VariableTerm variable, MouseEvent ev)
				{
					Transaction transaction = getModel().beginTransaction();
					try
					{
						Statement statement = getPersistenceManager().statements(transaction).get(variable);
						if (statement != null)
							getContextJTree().selectStatement(statement, true);
					}
					finally
					{
						transaction.abort();
					}
				}

			};
		}

		@Override
		protected Class<Context> renderedClass()
		{
			return Context.class;
		}
	}

	protected class NamespaceTableCellRenerer extends MyTableCellRenderer<Namespace>
	{

		@Override
		protected Class<Namespace> renderedClass()
		{
			return Namespace.class;
		}

		@Override
		protected AbstractRenderer buildRenderer(Namespace value)
		{
			return new NamespaceLabelRenderer(value);
		}

	}

	protected class DelegateTableCellRenderer extends MyTableCellRenderer<Person>
	{

		@Override
		protected Class<Person> renderedClass()
		{
			return Person.class;
		}

		@Override
		protected AbstractRenderer buildRenderer(Person value)
		{
			return new PersonLabelRenderer(value);
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
			TextLabelRenderer r = new BoldTextLabelRenderer(value.toString());
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

	protected class MyTableCellEditor<T> implements TableCellEditor
	{
		private final Set<CellEditorListener> listeners;
		private final MyTableCellRenderer<T> renderer;
		private MyTableCellRenderer<T>.MyCellRendererComponent myCellRendererComponent;

		protected MyTableCellEditor(MyTableCellRenderer<T> renderer)
		{
			this.listeners = Collections.synchronizedSet(new HashSet<CellEditorListener>());
			this.renderer = renderer;
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
			myCellRendererComponent = renderer.getTableCellRendererComponent(table, value, true, true, row, column);
			return myCellRendererComponent;
		}

	}

	//@formatter:off
	private final static Collection<Class<? extends MyTableCellRenderer<?>>> cellRendererClasses = Arrays.<Class<? extends MyTableCellRenderer<?>>> asList(
			SignatoryTableCellRenderer.class, 
			DateTableCellRenderer.class, 
			BooleanTableCellRenderer.class, 
			ContextTableCellRenderer.class,
			NamespaceTableCellRenerer.class, 
			DelegateTableCellRenderer.class
			);
	//@formatter:on

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
				try
				{
					deleteSignatures(new BijectionList<Integer, StatementAuthoritySignature>(new Bijection<Integer, StatementAuthoritySignature>()
					{

						@Override
						public StatementAuthoritySignature forward(Integer input)
						{
							return getModel().getSignatureList().get(input);
						}

						@Override
						public Integer backward(StatementAuthoritySignature output)
						{
							throw new UnsupportedOperationException();
						}
					}, new ArrayAsList<Integer>(getSelectedRows())));
				}
				catch (InterruptedException e)
				{
					throw new Error(e);
				}
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent ev)
		{
		}
	}

	private static int computeWidth(int textLength)
	{
		FontMetrics fontMetrics = FontManager.instance.fontMetrics(FontManager.instance.defaultFont());
		return fontMetrics.charWidth('x') * textLength + 10;
	}

	private static int computeHeight()
	{
		FontMetrics fontMetrics = FontManager.instance.fontMetrics(FontManager.instance.defaultFont());
		return fontMetrics.getHeight();
	}

	private final AuthorityJPanel authorityJPanel;
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
		protected StatementAuthoritySignatureTransferable createTransferable(JComponent c)
		{
			StatementAuthoritySignature statementAuthoritySignature = getSelectedStatementAuthoritySignature();
			if (statementAuthoritySignature == null)
				return null;
			return new StatementAuthoritySignatureTransferable(statementAuthoritySignature);
		}

		@Override
		protected void exportDone(JComponent c, Transferable t, int action)
		{
		}

	}

	protected AbstractAuthoritySignatureJTable(AuthorityJPanel authorityJPanel, AbstractAuthoritySignatureTableModel model)
	{
		super(model);
		this.setDragEnabled(true);
		this.setTransferHandler(new MyTransferHandler());
		this.authorityJPanel = authorityJPanel;
		this.listener = new Listener();
		addKeyListener(listener);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setAutoResizeMode(AUTO_RESIZE_OFF);
		try
		{
			for (Class<? extends MyTableCellRenderer<?>> clazz : cellRendererClasses)
			{
				MyTableCellRenderer<?> tcr = clazz.getDeclaredConstructor(AbstractAuthoritySignatureJTable.class).newInstance(this);
				setDefaultRenderer(tcr.renderedClass(), tcr);
				setDefaultEditor(tcr.renderedClass(), new MyTableCellEditor<>(tcr));
			}
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e)
		{
			throw new Error(e);
		}
		getTableHeader().setReorderingAllowed(false);
		getTableHeader().setDefaultRenderer(new HeaderTableCellRenderer());
		updateFontSize();
	}

	protected AuthorityJPanel getAuthorityJPanel()
	{
		return authorityJPanel;
	}

	@Override
	public AbstractAuthoritySignatureTableModel getModel()
	{
		return (AbstractAuthoritySignatureTableModel) super.getModel();
	}

	protected ContextJTreeJPanel getContextJTreeJPanel()
	{
		return authorityJPanel.getContextJTreeJPanel();
	}

	protected ContextJTree getContextJTree()
	{
		return getContextJTreeJPanel().getContextJTree();
	}

	protected AletheiaJPanel getAletheiaJPanel()
	{
		return getContextJTreeJPanel().getAletheiaJPanel();
	}

	protected AletheiaJFrame getAletheiaJFrame()
	{
		return getAletheiaJPanel().getAletheiaJFrame();
	}

	private void deleteSignatures(Collection<StatementAuthoritySignature> signatures) throws InterruptedException
	{
		getAuthorityJPanel()
				.getContextJTreeJPanel()
				.getAletheiaJPanel()
				.getCliJPanel()
				.command(
						new DeleteSignatures(getAuthorityJPanel().getContextJTreeJPanel().getAletheiaJPanel().getCliJPanel(), getModel().beginTransaction(),
								signatures), false);
	}

	public void updateFontSize()
	{
		for (int i = 0; i < getModel().getColumnCount(); i++)
			getColumnModel().getColumn(i).setPreferredWidth(computeWidth(getModel().getColumnWidth(i)));
		setRowHeight(computeHeight());
	}

	@SuppressWarnings("unused")
	private void navigateToRow(int i)
	{
		clearSelection();
		getSelectionModel().setSelectionInterval(i, i);
		int c = getEditingColumn();
		if (c < 0)
			c = 0;
		editCellAt(i, c);
	}

	@Override
	public Dimension getPreferredSize()
	{
		try
		{
			return super.getPreferredSize();
		}
		catch (PersistenceLockTimeoutException e)
		{
			authorityJPanel.setStatement(null);
			return new Dimension(1, 1);
		}
	}

	public StatementAuthoritySignature getSelectedStatementAuthoritySignature()
	{
		int selected = getSelectionModel().getAnchorSelectionIndex();
		List<StatementAuthoritySignature> list = getModel().getSignatureList();
		if (selected < 0 || selected >= list.size())
			return null;
		return list.get(selected);
	}

}
