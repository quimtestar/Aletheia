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
package aletheia.gui.person;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import aletheia.model.authority.IncompleteDataSignatureException;
import aletheia.model.authority.Person;
import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BTreeCountedSortedSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;
import aletheia.utilities.collections.CountedSortedSet;
import aletheia.utilities.collections.UnionCloseableCollection;

public abstract class AbstractPersonTableModel implements TableModel
{
	private static final int transactionTimeOut = 100;

	private static final String[] columnNames =
	{ "Nick", "Name", "eMail", "UUID" };

	private static final Class<?>[] columnClasses =
	{ String.class, String.class, String.class, UUID.class };

	public enum PersonTableModelEventType
	{
		Added(PersonTableModelEvent.INSERT), Modified(PersonTableModelEvent.UPDATE), Removed(PersonTableModelEvent.DELETE), ;

		public int type;

		private PersonTableModelEventType(int type)
		{
			this.type = type;
		}
	}

	private final static int populateMargin = 10;

	public abstract class PersonTableModelEvent extends TableModelEvent
	{
		private static final long serialVersionUID = -5988699357930052516L;
		private final Person person;
		private final PersonTableModelEventType personTableModelEventType;

		public PersonTableModelEvent(Person person, PersonTableModelEventType personTableModelEventType, int row)
		{
			super(AbstractPersonTableModel.this, row, row, ALL_COLUMNS, personTableModelEventType.type);
			this.person = person;
			this.personTableModelEventType = personTableModelEventType;
		}

		public PersonTableModelEvent(Person person, PersonTableModelEventType personTableModelEventType)
		{
			super(AbstractPersonTableModel.this);
			this.person = person;
			this.personTableModelEventType = personTableModelEventType;
		}

		public Person getPerson()
		{
			return person;
		}

		public PersonTableModelEventType getPersonTableModelEventType()
		{
			return personTableModelEventType;
		}
	}

	public class AddedPersonTableModelEvent extends PersonTableModelEvent
	{
		private static final long serialVersionUID = -6144906273217364930L;

		public AddedPersonTableModelEvent(Person person, int row)
		{
			super(person, PersonTableModelEventType.Added, row);
		}

		public AddedPersonTableModelEvent(Person person)
		{
			super(person, PersonTableModelEventType.Added);
		}
	}

	public class RemovedPersonTableModelEvent extends PersonTableModelEvent
	{
		private static final long serialVersionUID = -7703720685210302489L;

		public RemovedPersonTableModelEvent(Person person, int row)
		{
			super(person, PersonTableModelEventType.Removed, row);
		}

		public RemovedPersonTableModelEvent(Person person)
		{
			super(person, PersonTableModelEventType.Removed);
		}
	}

	public class ModifiedPersonTableModelEvent extends PersonTableModelEvent
	{
		private static final long serialVersionUID = 7408957235387305668L;

		public ModifiedPersonTableModelEvent(Person person, int row)
		{
			super(person, PersonTableModelEventType.Modified, row);
		}

		public ModifiedPersonTableModelEvent(Person person)
		{
			super(person, PersonTableModelEventType.Modified);
		}
	}

	protected abstract class PersonStateListener implements Person.StateListener, Person.AddStateListener
	{

		@Override
		public void personAdded(Transaction transaction, final Person person)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					PersonData personData = getPersonData();
					PersonData.PersonAddedRowInfo rowInfo = personData.personAdded(person);
					if (rowInfo.addedRow >= 0)
					{
						PersonTableModelEvent ev = new AddedPersonTableModelEvent(person, rowInfo.addedRow);
						synchronized (listeners)
						{
							for (TableModelListener l : listeners)
							{
								l.tableChanged(ev);
							}
						}
					}
				}
			});
		}

		@Override
		public void personModified(Transaction transaction, final Person person)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{
				@Override
				public void run(Transaction closedTransaction)
				{
					PersonData personData = getPersonData();
					PersonData.PersonModifiedRowInfo rowInfo = personData.personModified(person);
					if (rowInfo.rowModifiedPre >= 0)
					{
						List<PersonTableModelEvent> events = new ArrayList<PersonTableModelEvent>();
						if (rowInfo.rowModifiedPre == rowInfo.rowModifiedPost)
							events.add(new ModifiedPersonTableModelEvent(person, rowInfo.rowModifiedPre));
						else
						{
							events.add(new RemovedPersonTableModelEvent(person, rowInfo.rowModifiedPre));
							if (rowInfo.rowModifiedPost >= 0)
								events.add(new AddedPersonTableModelEvent(person, rowInfo.rowModifiedPost));
						}
						synchronized (listeners)
						{
							for (TableModelListener l : listeners)
								for (PersonTableModelEvent ev : events)
									l.tableChanged(ev);
						}
					}
				}
			});
		}

		@Override
		public void personRemoved(Transaction transaction, final Person person)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					PersonData personData = getPersonData();
					PersonData.PersonRemovedRowInfo rowInfo = personData.personRemoved(person);
					if (rowInfo.rowRemoved >= 0)
					{
						RemovedPersonTableModelEvent ev = new RemovedPersonTableModelEvent(person, rowInfo.rowRemoved);
						synchronized (listeners)
						{
							for (TableModelListener l : listeners)
							{
								l.tableChanged(ev);
							}
						}
					}
				}
			});
		}
	}

	private final PersistenceManager persistenceManager;

	private final PersonStateListener personStateListener;

	private final Set<TableModelListener> listeners;

	private final static Comparator<Person> personComparator = new Comparator<Person>()
	{
		@Override
		public int compare(Person person1, Person person2)
		{
			int c;
			c = person1.getNick().compareTo(person2.getNick());
			if (c != 0)
				return c;
			c = person1.getUuid().compareTo(person2.getUuid());
			if (c != 0)
				return c;
			return 0;
		}
	};

	private class PersonData
	{
		private final CountedSortedSet<Person> countedSortedSet;
		private final Map<UUID, Person> uuidMap;
		private int highestQueriedRow;
		private boolean full;

		public PersonData()
		{
			this.countedSortedSet = new BTreeCountedSortedSet<>(personComparator);
			this.uuidMap = new HashMap<UUID, Person>();
			this.highestQueriedRow = 0;
			this.full = false;
			populate(0);
		}

		public synchronized int getRowCount()
		{
			return countedSortedSet.size();
		}

		public synchronized Person getPerson(int row)
		{
			if (row > highestQueriedRow)
			{
				populate(row);
				highestQueriedRow = row;
			}
			return countedSortedSet.get(row);
		}

		private synchronized void addPerson(Person person)
		{
			countedSortedSet.add(person);
			uuidMap.put(person.getUuid(), person);
			person.addStateListener(personStateListener);
		}

		private synchronized void populate(int ordinal)
		{
			Transaction transaction = beginTransaction();
			try
			{
				CloseableSortedMap<String, CloseableSet<Person>> personsByNick = personsByNick(transaction);
				Person lastPerson = null;
				if (!countedSortedSet.isEmpty())
				{
					lastPerson = countedSortedSet.last();
					personsByNick = personsByNick.tailMap(lastPerson.getNick());
				}
				CloseableIterator<Person> iterator = new UnionCloseableCollection<Person>(personsByNick.values()).iterator();
				try
				{
					boolean changed = false;
					while (iterator.hasNext() && countedSortedSet.size() <= ordinal + populateMargin)
					{
						Person person = iterator.next();
						if (lastPerson == null || personComparator.compare(person, lastPerson) > 0)
						{
							addPerson(person);
							changed = true;
						}
					}
					if (!iterator.hasNext())
						full = true;
					if (changed)
					{
						TableModelEvent ev = new TableModelEvent(AbstractPersonTableModel.this);
						synchronized (listeners)
						{
							for (TableModelListener l : listeners)
							{
								l.tableChanged(ev);
							}
						}
					}
				}
				finally
				{
					iterator.close();
				}
			}
			finally
			{
				transaction.abort();
			}
		}

		public synchronized void close()
		{
			for (Person person : countedSortedSet)
				person.removeStateListener(personStateListener);
		}

		public abstract class RowInfo
		{

		}

		public class PersonAddedRowInfo extends RowInfo
		{
			final int addedRow;

			public PersonAddedRowInfo(int addedRow)
			{
				this.addedRow = addedRow;
			}
		}

		public synchronized PersonAddedRowInfo personAdded(Person person)
		{
			int row = countedSortedSet.ordinalOf(person);
			if (row < getRowCount())
			{
				addPerson(person);
				return new PersonAddedRowInfo(row);
			}
			else
			{
				if (full)
					populate(row);
				return new PersonAddedRowInfo(-1);
			}
		}

		public class PersonModifiedRowInfo extends RowInfo
		{
			final int rowModifiedPre;
			final int rowModifiedPost;

			public PersonModifiedRowInfo(int rowModifiedPre, int rowModifiedPost)
			{
				super();
				this.rowModifiedPre = rowModifiedPre;
				this.rowModifiedPost = rowModifiedPost;
			}

		}

		public synchronized PersonModifiedRowInfo personModified(Person person)
		{
			Person old = uuidMap.get(person.getUuid());
			if (old == null || personFieldsEquals(person, old))
				return new PersonModifiedRowInfo(-1, -1);
			int preRow = countedSortedSet.ordinalOf(old);
			countedSortedSet.remove(preRow);
			int postRow = countedSortedSet.ordinalOf(person);
			if (postRow < getRowCount())
				addPerson(person);
			else
			{
				if (full)
					populate(postRow);
				postRow = -1;
			}
			return new PersonModifiedRowInfo(preRow, postRow);
		}

		public class PersonRemovedRowInfo extends RowInfo
		{
			final int rowRemoved;

			public PersonRemovedRowInfo(int rowRemoved)
			{
				super();
				this.rowRemoved = rowRemoved;
			}
		}

		public synchronized PersonRemovedRowInfo personRemoved(Person person)
		{
			Person old = uuidMap.remove(person.getUuid());
			if (old == null)
				return new PersonRemovedRowInfo(-1);
			int row = countedSortedSet.ordinalOf(old);
			countedSortedSet.remove(row);
			populate(highestQueriedRow);
			return new PersonRemovedRowInfo(row);
		}

		@Override
		protected void finalize() throws Throwable
		{
			close();
			super.finalize();
		}

	}

	private SoftReference<PersonData> personDataRef;

	public AbstractPersonTableModel(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.personStateListener = makePersonStateListener();
		this.persistenceManager.getListenerManager().getPersonAddStateListeners().add(this.personStateListener);
		this.listeners = Collections.synchronizedSet(new HashSet<TableModelListener>());
		this.personDataRef = null;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected abstract PersonStateListener makePersonStateListener();

	protected PersonStateListener getPersonStateListener()
	{
		return personStateListener;
	}

	public synchronized void shutdown()
	{
		this.persistenceManager.getListenerManager().getPersonAddStateListeners().remove(getPersonStateListener());
		if (getPersonDataNoCreate() != null)
			getPersonDataNoCreate().close();
	}

	protected abstract CloseableSortedMap<String, CloseableSet<Person>> personsByNick(Transaction transaction);

	protected synchronized PersonData getPersonData()
	{
		PersonData personData = null;
		if (personDataRef != null)
			personData = personDataRef.get();
		if (personData == null)
		{
			personData = new PersonData();
			personDataRef = new SoftReference<PersonData>(personData);
		}
		return personData;
	}

	protected synchronized PersonData getPersonDataNoCreate()
	{
		return personDataRef != null ? personDataRef.get() : null;
	}

	protected Transaction beginTransaction()
	{
		return persistenceManager.beginTransaction(transactionTimeOut);
	}

	protected Person getPerson(int row)
	{
		return getPersonData().getPerson(row);
	}

	@Override
	public int getRowCount()
	{
		return getPersonData().getRowCount();
	}

	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return columnNames[columnIndex];

	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return columnClasses[columnIndex];
	}

	protected boolean isRowPrivate(int rowIndex)
	{
		return getPerson(rowIndex) instanceof PrivatePerson;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		if (columnIndex == 3)
			return false;
		else if (isRowPrivate(rowIndex))
			return true;
		else
			return false;
	}

	protected Object getValue(Person person, int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return person.getNick();
		case 1:
			return person.getName();
		case 2:
			return person.getEmail();
		case 3:
			return person.getUuid();
		default:
			return null;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Person person = getPerson(rowIndex);
		if (person == null)
			return null;
		return getValue(person, columnIndex);
	}

	private static boolean nullableStringEquals(String s1, String s2)
	{
		if (s1 == null)
			s1 = "";
		if (s2 == null)
			s2 = "";
		return s1.equals(s2);
	}

	private static boolean nullableDateEquals(Date d1, Date d2)
	{
		if ((d1 == null) && (d2 == null))
			return true;
		if ((d1 == null) || (d2 == null))
			return false;
		return d1.equals(d2);

	}

	private static boolean personFieldsEquals(Person p1, Person p2)
	{
		if (!nullableStringEquals(p1.getNick(), p2.getNick()))
			return false;
		if (!nullableStringEquals(p1.getName(), p2.getName()))
			return false;
		if (!nullableStringEquals(p1.getEmail(), p2.getEmail()))
			return false;
		if (!nullableDateEquals(p1.getOrphanSince(), p2.getOrphanSince()))
			return false;
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		Person person = getPerson(rowIndex);
		if (!(person instanceof PrivatePerson))
			throw new IllegalArgumentException();
		PrivatePerson privatePerson = (PrivatePerson) person;
		Transaction transaction = beginTransaction();
		try
		{
			PrivatePerson privatePersonUpdated = privatePerson.refresh(transaction);
			boolean change = false;
			String value = (String) aValue;
			switch (columnIndex)
			{
			case 0:
				if (!nullableStringEquals(value, privatePersonUpdated.getNick()))
				{
					privatePersonUpdated.setNick(value);
					change = true;
				}
				break;
			case 1:
				if (!nullableStringEquals(value, privatePersonUpdated.getName()))
				{
					privatePersonUpdated.setName(value);
					change = true;
				}
				break;
			case 2:
				if (!nullableStringEquals(value, privatePersonUpdated.getEmail()))
				{
					privatePersonUpdated.setEmail(value);
					change = true;
				}
				break;
			default:
				throw new RuntimeException();
			}
			if (change)
			{
				privatePersonUpdated.sign(transaction);
				privatePersonUpdated.persistenceUpdate(transaction);
			}
			transaction.commit();
		}
		catch (IncompleteDataSignatureException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			transaction.abort();
		}
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

}
