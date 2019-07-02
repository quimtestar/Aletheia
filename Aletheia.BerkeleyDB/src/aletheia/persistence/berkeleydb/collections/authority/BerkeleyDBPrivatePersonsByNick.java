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
package aletheia.persistence.berkeleydb.collections.authority;

import java.util.Comparator;
import java.util.NoSuchElementException;

import aletheia.model.authority.PrivatePerson;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPrivatePersonEntity;
import aletheia.persistence.collections.authority.PrivatePersonsByNick;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBPrivatePersonsByNick extends AbstractCloseableMap<String, PrivatePerson> implements PrivatePersonsByNick
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<String, UUIDKey, BerkeleyDBPrivatePersonEntity> privatePersonEntityNickSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final String from;
	private final String to;

	private BerkeleyDBPrivatePersonsByNick(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, String from, String to)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.privatePersonEntityNickSecondaryIndex = persistenceManager.getEntityStore().privatePersonEntityNickSecondaryIndex();
			this.transaction = transaction;
			this.from = from;
			this.to = to;
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBPrivatePersonsByNick(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
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

	@Override
	public PrivatePerson get(Object key)
	{
		if (!(key instanceof String))
			return null;
		String nick = (String) key;
		BerkeleyDBPrivatePersonEntity entity = transaction.get(privatePersonEntityNickSecondaryIndex, nick);
		if (entity == null)
			return null;
		return (PrivatePerson) persistenceManager.entityToPerson(entity);
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof String))
			return false;
		String nick = (String) key;
		return transaction.contains(privatePersonEntityNickSecondaryIndex, nick);
	}

	@Override
	public Comparator<? super String> comparator()
	{
		return null;
	}

	@Override
	public CloseableSortedMap<String, PrivatePerson> subMap(String fromKey, String toKey)
	{
		return new BerkeleyDBPrivatePersonsByNick(persistenceManager, transaction, fromKey, toKey);
	}

	@Override
	public CloseableSortedMap<String, PrivatePerson> headMap(String toKey)
	{
		return new BerkeleyDBPrivatePersonsByNick(persistenceManager, transaction, null, toKey);
	}

	@Override
	public CloseableSortedMap<String, PrivatePerson> tailMap(String fromKey)
	{
		return new BerkeleyDBPrivatePersonsByNick(persistenceManager, transaction, fromKey, null);
	}

	@Override
	public String firstKey()
	{
		EntityCursor<String> cursor = transaction.keys(privatePersonEntityNickSecondaryIndex, from, true, to, false);
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
		EntityCursor<String> cursor = transaction.keys(privatePersonEntityNickSecondaryIndex, from, true, to, false);
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
				final EntityCursor<String> cursor = transaction.keys(privatePersonEntityNickSecondaryIndex, from, true, to, false);
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
	public CloseableSet<Entry<String, PrivatePerson>> entrySet()
	{
		final CloseableSet<String> keySet = keySet();
		return new AbstractCloseableSet<>()
		{

			@Override
			public CloseableIterator<Entry<String, PrivatePerson>> iterator()
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
					public Entry<String, PrivatePerson> next()
					{
						final String key = iterator.next();
						return new Entry<>()
						{

							@Override
							public PrivatePerson setValue(PrivatePerson value)
							{
								throw new UnsupportedOperationException();
							}

							@Override
							public PrivatePerson getValue()
							{
								return get(key);
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
