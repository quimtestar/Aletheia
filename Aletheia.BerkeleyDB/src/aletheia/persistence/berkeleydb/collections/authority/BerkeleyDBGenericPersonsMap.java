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

import java.util.Map;
import java.util.NoSuchElementException;

import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.collections.authority.GenericPersonsMap;
import aletheia.persistence.entities.authority.PersonEntity;
import aletheia.persistence.entities.authority.SignatoryEntity;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public class BerkeleyDBGenericPersonsMap<S extends Signatory, SE extends SignatoryEntity, P extends Person, PE extends PersonEntity> extends
		AbstractCloseableMap<S, P> implements GenericPersonsMap<S, P>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final EntityIndex<UUIDKey, PE> entityIndex;
	private final BerkeleyDBTransaction transaction;

	public BerkeleyDBGenericPersonsMap(BerkeleyDBPersistenceManager persistenceManager, EntityIndex<UUIDKey, PE> entityIndex, BerkeleyDBTransaction transaction)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.entityIndex = entityIndex;
		this.transaction = transaction;
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

	@SuppressWarnings("unchecked")
	@Override
	public P get(Object key)
	{
		if (!(key instanceof Signatory))
			return null;
		Signatory signatory = (Signatory) key;
		UUIDKey uuidKey = new UUIDKey(signatory.getUuid());
		PE entity = transaction.get(entityIndex, uuidKey);
		if (entity == null)
			return null;
		return (P) persistenceManager.entityToPerson(entity);
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof Signatory))
			return false;
		Signatory signatory = (Signatory) key;
		UUIDKey uuidKey = new UUIDKey(signatory.getUuid());
		return transaction.contains(entityIndex, uuidKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public P remove(Object key)
	{
		if (!(key instanceof Signatory))
			return null;
		Signatory signatory = (Signatory) key;
		UUIDKey uuidKey = new UUIDKey(signatory.getUuid());
		PE entity = transaction.get(entityIndex, uuidKey);
		if (entity == null)
			return null;
		transaction.delete(entityIndex, uuidKey);
		return (P) persistenceManager.entityToPerson(entity);
	}

	@Override
	public int size()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(entityIndex);
		try
		{
			int n = 0;
			while (transaction.next(cursor) != null)
				n++;
			return n;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(entityIndex);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	/**
	 * Faster computation (possibly not accurate) of the size of this map.
	 *
	 * @return The size
	 * @see #size()
	 */
	public int fastSize()
	{
		return (int) entityIndex.count();
	}

	public class EntrySet extends AbstractCloseableSet<Map.Entry<S, P>>
	{
		private class MyEntry implements Map.Entry<S, P>
		{
			final P person;

			public MyEntry(P person)
			{
				super();
				this.person = person;
			}

			@SuppressWarnings("unchecked")
			@Override
			public S getKey()
			{
				return (S) person.getSignatory(transaction);
			}

			@Override
			public P getValue()
			{
				return person;
			}

			@Override
			public P setValue(P value)
			{
				throw new UnsupportedOperationException();
			}

		}

		private final EntityIndex<UUIDKey, PE> index;
		private final UUIDKey from;
		private final UUIDKey to;

		public EntrySet(EntityIndex<UUIDKey, PE> index, UUIDKey from, UUIDKey to)
		{
			super();
			this.index = index;
			this.from = from;
			this.to = to;
		}

		public EntrySet(EntityIndex<UUIDKey, PE> index)
		{
			this(index, null, null);
		}

		public EntrySet()
		{
			this(entityIndex);
		}

		@Override
		public int size()
		{
			return BerkeleyDBGenericPersonsMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BerkeleyDBGenericPersonsMap.this.isEmpty();
		}

		@Override
		public CloseableIterator<Map.Entry<S, P>> iterator()
		{
			final EntityCursor<PE> cursor = transaction.entities(index, from, true, to, true);
			return new CloseableIterator<Map.Entry<S, P>>()
			{
				PE next;
				{
					next = transaction.next(cursor);
				}

				@Override
				public boolean hasNext()
				{
					if (next == null)
					{
						transaction.close(cursor);
						return false;
					}
					return true;
				}

				@SuppressWarnings("unchecked")
				@Override
				public Map.Entry<S, P> next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					PE entity = next;
					next = transaction.next(cursor);
					return new MyEntry((P) persistenceManager.entityToPerson(entity));
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
					super.finalize();
				}
			};
		}

	}

	@Override
	public EntrySet entrySet()
	{
		return new EntrySet();
	}

	@Override
	public void clear()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(entityIndex);
		try
		{
			while (transaction.next(cursor) != null)
				transaction.delete(cursor);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

}
