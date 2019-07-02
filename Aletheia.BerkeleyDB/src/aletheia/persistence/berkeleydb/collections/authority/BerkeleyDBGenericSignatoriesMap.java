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

import java.util.NoSuchElementException;
import java.util.UUID;

import aletheia.model.authority.Signatory;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatoryEntity;
import aletheia.persistence.collections.authority.GenericSignatoriesMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public abstract class BerkeleyDBGenericSignatoriesMap<S extends Signatory, E extends BerkeleyDBSignatoryEntity> extends AbstractCloseableMap<UUID, S>
		implements GenericSignatoriesMap<S>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final EntityIndex<UUIDKey, E> index;
	private final BerkeleyDBTransaction transaction;

	public BerkeleyDBGenericSignatoriesMap(BerkeleyDBPersistenceManager persistenceManager, EntityIndex<UUIDKey, E> index, BerkeleyDBTransaction transaction)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.index = index;
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
	public S get(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		BerkeleyDBSignatoryEntity entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		return (S) persistenceManager.entityToSignatory(entity);
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof UUID))
			return false;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		return transaction.contains(index, uuidKey);

	}

	@SuppressWarnings("unchecked")
	@Override
	public S remove(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		BerkeleyDBSignatoryEntity entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		transaction.delete(index, uuidKey);
		return (S) persistenceManager.entityToSignatory(entity);
	}

	@Override
	public int size()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(index);
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
		EntityCursor<UUIDKey> cursor = transaction.keys(index);
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
		return (int) index.count();
	}

	public class EntrySet extends AbstractCloseableSet<Entry<UUID, S>>
	{
		private class MyEntry implements Entry<UUID, S>
		{
			final S signatory;

			public MyEntry(S signatory)
			{
				super();
				this.signatory = signatory;
			}

			@Override
			public UUID getKey()
			{
				return signatory.getUuid();
			}

			@Override
			public S getValue()
			{
				return signatory;
			}

			@Override
			public S setValue(Signatory value)
			{
				throw new UnsupportedOperationException();
			}

		}

		private final EntityIndex<UUIDKey, E> index;
		private final UUIDKey from;
		private final UUIDKey to;

		public EntrySet(EntityIndex<UUIDKey, E> index, UUIDKey from, UUIDKey to)
		{
			super();
			this.index = index;
			this.from = from;
			this.to = to;
		}

		public EntrySet(EntityIndex<UUIDKey, E> index)
		{
			this(index, null, null);
		}

		public EntrySet()
		{
			this(BerkeleyDBGenericSignatoriesMap.this.index);
		}

		@Override
		public int size()
		{
			return BerkeleyDBGenericSignatoriesMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BerkeleyDBGenericSignatoriesMap.this.isEmpty();
		}

		@Override
		public CloseableIterator<Entry<UUID, S>> iterator()
		{
			final EntityCursor<E> cursor = transaction.entities(index, from, true, to, true);
			return new CloseableIterator<Entry<UUID, S>>()
			{
				BerkeleyDBSignatoryEntity next;

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
				public Entry<UUID, S> next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					BerkeleyDBSignatoryEntity entity = next;
					next = transaction.next(cursor);
					return new MyEntry((S) persistenceManager.entityToSignatory(entity));
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

	}

	@Override
	public EntrySet entrySet()
	{
		return new EntrySet();
	}

	@Override
	public void clear()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(index);
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
