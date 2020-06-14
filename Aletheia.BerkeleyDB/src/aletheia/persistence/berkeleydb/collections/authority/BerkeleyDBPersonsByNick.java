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
package aletheia.persistence.berkeleydb.collections.authority;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.Person;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPersonEntity;
import aletheia.persistence.collections.authority.PersonsByNick;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;

public class BerkeleyDBPersonsByNick extends AbstractCloseableMap<String, CloseableSet<Person>> implements PersonsByNick
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<String, UUIDKey, BerkeleyDBPersonEntity> personEntityNickSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final String from;
	private final String to;

	private BerkeleyDBPersonsByNick(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, String from, String to)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.personEntityNickSecondaryIndex = persistenceManager.getEntityStore().personEntityNickSecondaryIndex();
			this.transaction = transaction;
			this.from = from;
			this.to = to;
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBPersonsByNick(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		this(persistenceManager, transaction, null, null);
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	private class FilteredSet extends AbstractCloseableSet<Person>
	{
		private final String nick;

		public FilteredSet(String nick)
		{
			super();
			this.nick = nick;
		}

		@Override
		public boolean contains(Object o)
		{
			if (!(o instanceof Person))
				return false;
			Person person = (Person) o;
			if (!person.getNick().equals(nick))
				return false;
			Person person_ = persistenceManager.getPerson(transaction, person.getUuid());
			if (person_ == null)
				return false;
			return true;
		}

		@Override
		public CloseableIterator<Person> iterator()
		{
			final EntityCursor<BerkeleyDBPersonEntity> cursor = transaction.entities(personEntityNickSecondaryIndex, nick, true, nick, true);
			return new CloseableIterator<>()
			{
				BerkeleyDBPersonEntity next;

				{
					do
					{
						next = transaction.next(cursor);
						if (next == null)
						{
							transaction.close(cursor);
							break;
						}
					} while (!next.getNick().equals(nick));
				}

				@Override
				public boolean hasNext()
				{
					return next != null;
				}

				@Override
				public Person next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					BerkeleyDBPersonEntity entity = next;
					do
					{
						next = transaction.next(cursor);
						if (next == null)
						{
							transaction.close(cursor);
							break;
						}
					} while (!next.getNick().equals(nick));
					return persistenceManager.entityToPerson(entity);
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public void close()
				{
					transaction.close(cursor);
				}

				@Override
				protected void finalize() throws Throwable
				{
					close();
				}
			};
		}

		@Override
		public int size()
		{
			int j = 0;
			for (Iterator<Person> i = iterator(); i.hasNext(); i.next())
				j++;
			return j;
		}

		@Override
		public boolean isEmpty()
		{
			CloseableIterator<Person> i = iterator();
			try
			{
				return !i.hasNext();
			}
			finally
			{
				i.close();
			}
		}

	}

	@Override
	public CloseableSet<Person> get(Object key)
	{
		if (!(key instanceof String))
			return null;
		String nick = (String) key;
		return new FilteredSet(nick);
	}

	@Override
	public Comparator<? super String> comparator()
	{
		return null;
	}

	@Override
	public CloseableSortedMap<String, CloseableSet<Person>> subMap(String fromKey, String toKey)
	{
		return new BerkeleyDBPersonsByNick(persistenceManager, transaction, fromKey, toKey);
	}

	@Override
	public CloseableSortedMap<String, CloseableSet<Person>> headMap(String toKey)
	{
		return new BerkeleyDBPersonsByNick(persistenceManager, transaction, null, toKey);
	}

	@Override
	public CloseableSortedMap<String, CloseableSet<Person>> tailMap(String fromKey)
	{
		return new BerkeleyDBPersonsByNick(persistenceManager, transaction, fromKey, null);
	}

	@Override
	public String firstKey()
	{
		EntityCursor<String> cursor = transaction.keys(personEntityNickSecondaryIndex, from, true, to, false);
		try
		{
			String first = transaction.first(cursor);
			if (first == null)
				throw new NoSuchElementException();
			return first;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public String lastKey()
	{
		EntityCursor<String> cursor = transaction.keys(personEntityNickSecondaryIndex, from, true, to, false);
		try
		{
			String last = transaction.last(cursor);
			if (last == null)
				throw new NoSuchElementException();
			return last;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public CloseableSet<String> keySet()
	{
		return new AbstractCloseableSet<>()
		{

			@Override
			public CloseableIterator<String> iterator()
			{
				final EntityCursor<String> cursor = transaction.keys(personEntityNickSecondaryIndex, from, true, to, false);
				return new CloseableIterator<>()
				{
					private String advance()
					{
						String next = transaction.nextNoDup(cursor);
						if (next == null)
							transaction.close(cursor);
						return next;
					}

					private String next = advance();

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public String next()
					{
						if (next == null)
							throw new NoSuchElementException();
						String next_ = next;
						next = advance();
						return next_;
					}

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public void close()
					{
						transaction.close(cursor);
					}

					@Override
					protected void finalize() throws Throwable
					{
						close();
					}

				};
			}

			@Override
			public int size()
			{
				CloseableIterator<String> i = iterator();
				try
				{
					int n = 0;
					while (i.hasNext())
						n++;
					return n;
				}
				finally
				{
					i.close();
				}
			}

			@Override
			public boolean isEmpty()
			{
				CloseableIterator<String> i = iterator();
				try
				{
					return !i.hasNext();
				}
				finally
				{
					i.close();
				}
			}

		};
	}

	@Override
	public CloseableSet<Entry<String, CloseableSet<Person>>> entrySet()
	{
		final CloseableSet<String> keySet = keySet();
		return new AbstractCloseableSet<>()
		{

			@Override
			public CloseableIterator<Entry<String, CloseableSet<Person>>> iterator()
			{
				final CloseableIterator<String> iterator = keySet.iterator();
				return new CloseableIterator<>()
				{

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public Entry<String, CloseableSet<Person>> next()
					{
						final String key = iterator.next();
						return new Entry<>()
						{

							@Override
							public CloseableSet<Person> setValue(CloseableSet<Person> value)
							{
								throw new UnsupportedOperationException();
							}

							@Override
							public CloseableSet<Person> getValue()
							{
								return new FilteredSet(key);
							}

							@Override
							public String getKey()
							{
								return key;
							}
						};
					}

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public void close()
					{
						iterator.close();
					}
				};
			}

			@Override
			public int size()
			{
				return keySet.size();
			}

			@Override
			public boolean isEmpty()
			{
				return keySet.isEmpty();
			}

		};
	}

}
