/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeRootNode.SuccessorEntry;
import aletheia.model.authority.Person;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;

public class SuccessorsTableModel implements TableModel
{
	private final PersistenceManager persistenceManager;
	private final DelegateTreeRootNode delegateTreeRootNode;

	private class StateListener implements Person.StateListener
	{

		@Override
		public void personModified(Transaction transaction, Person person)
		{
			SuccessorsTableModel.this.personModified(transaction, person);
		}

		@Override
		public void personRemoved(Transaction transaction, Person person)
		{
		}

	}

	private final StateListener stateListener;
	private final Set<TableModelListener> listeners;

	public class SuccessorEntryData
	{
		private final Date signatureDate;
		private Person successor;

		private SuccessorEntryData(Transaction transaction, SuccessorEntry successorEntry)
		{
			this.signatureDate = successorEntry.getSignatureDate();
			this.successor = successorEntry.getSuccessor(transaction);
		}

		public Date getSignatureDate()
		{
			return signatureDate;
		}

		public Person getSuccessor()
		{
			return successor;
		}

		public void setSuccessor(Person successor)
		{
			this.successor = successor;
		}
	}

	private final List<SuccessorEntryData> successorEntryDataList;

	public SuccessorsTableModel(PersistenceManager persistenceManager, DelegateTreeRootNode delegateTreeRootNode)
	{
		this.persistenceManager = persistenceManager;
		this.delegateTreeRootNode = delegateTreeRootNode;
		this.stateListener = new StateListener();
		this.listeners = Collections.synchronizedSet(new HashSet<TableModelListener>());

		final Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			this.successorEntryDataList = new BufferedList<>(new BijectionCollection<>(new Bijection<SuccessorEntry, SuccessorEntryData>()
			{

				@Override
				public SuccessorEntryData forward(SuccessorEntry successorEntry)
				{
					return new SuccessorEntryData(transaction, successorEntry);
				}

				@Override
				public SuccessorEntry backward(SuccessorEntryData output)
				{
					throw new UnsupportedOperationException();
				}
			}, delegateTreeRootNode.successorEntries()));
			for (SuccessorEntryData se : successorEntryDataList)
				se.successor.addStateListener(stateListener);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public DelegateTreeRootNode getDelegateTreeRootNode()
	{
		return delegateTreeRootNode;
	}

	@Override
	public int getRowCount()
	{
		return successorEntryDataList.size();
	}

	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return "Date";
		case 1:
			return "Successor";
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return Date.class;
		case 1:
			return Person.class;
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return false;
		case 1:
			return true;
		default:
			throw new RuntimeException();
		}
	}

	public SuccessorEntryData getSuccessorEntryData(int rowIndex)
	{
		return successorEntryDataList.get(rowIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		SuccessorEntryData successorEntryData = getSuccessorEntryData(rowIndex);
		switch (columnIndex)
		{
		case 0:
			return successorEntryData.signatureDate;
		case 1:
			return successorEntryData.successor;
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listeners.remove(l);
	}

	public void shutdown()
	{
		for (SuccessorEntryData se : successorEntryDataList)
			se.successor.removeStateListener(stateListener);
	}

	protected class MyTableModelEvent extends TableModelEvent
	{

		private static final long serialVersionUID = -3280412990120035916L;
		private final SuccessorEntryData successorEntryData;

		private MyTableModelEvent(TableModel source, int row, int column, SuccessorEntryData successorEntryData)
		{
			super(source, row, row, column, TableModelEvent.UPDATE);
			this.successorEntryData = successorEntryData;
		}

		protected SuccessorEntryData getSuccessorEntryData()
		{
			return successorEntryData;
		}

	}

	private void personModified(Transaction transaction, final Person person)
	{
		final Collection<MyTableModelEvent> events = new ArrayList<>();
		for (ListIterator<SuccessorEntryData> iterator = successorEntryDataList.listIterator(); iterator.hasNext();)
		{
			int i = iterator.nextIndex();
			SuccessorEntryData successorEntryData = iterator.next();
			if (successorEntryData.getSuccessor().equals(person))
				events.add(new MyTableModelEvent(this, i, 1, successorEntryData));
		}
		transaction.runWhenCommit(new Transaction.Hook()
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				for (MyTableModelEvent event : events)
					event.getSuccessorEntryData().setSuccessor(person);
				synchronized (listeners)
				{
					for (TableModelListener l : listeners)
					{
						for (MyTableModelEvent event : events)
							l.tableChanged(event);
					}
				}
			}
		});
	}

}
