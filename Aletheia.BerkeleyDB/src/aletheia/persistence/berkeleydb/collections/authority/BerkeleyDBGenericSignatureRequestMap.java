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

import aletheia.model.authority.SignatureRequest;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity;
import aletheia.persistence.collections.authority.GenericSignatureRequestMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public abstract class BerkeleyDBGenericSignatureRequestMap<S extends SignatureRequest, E extends BerkeleyDBSignatureRequestEntity>
		extends AbstractCloseableMap<UUID, S> implements GenericSignatureRequestMap<S>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final EntityIndex<UUIDKey, E> index;
	private final BerkeleyDBTransaction transaction;

	public BerkeleyDBGenericSignatureRequestMap(BerkeleyDBPersistenceManager persistenceManager, EntityIndex<UUIDKey, E> index,
			BerkeleyDBTransaction transaction)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.index = index;
		this.transaction = transaction;
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected EntityIndex<UUIDKey, E> getIndex()
	{
		return index;
	}

	@Override
	public Transaction getTransaction()
	{
		return transaction;
	}

	@SuppressWarnings("unchecked")
	protected S entityToSignatureRequest(E entity)
	{
		return (S) persistenceManager.entityToSignatureRequest(entity);
	}

	public class EntrySet extends AbstractCloseableSet<Entry<UUID, S>>
	{
		private class MyEntry implements Entry<UUID, S>
		{
			private final S signatureRequest;

			public MyEntry(S signatureRequest)
			{
				super();
				this.signatureRequest = signatureRequest;
			}

			@Override
			public UUID getKey()
			{
				return signatureRequest.getUuid();
			}

			@Override
			public S getValue()
			{
				return signatureRequest;
			}

			@Override
			public S setValue(S value)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString()
			{
				return getKey().toString() + " => " + getValue().toString();
			}
		}

		@Override
		public CloseableIterator<Entry<UUID, S>> iterator()
		{
			final EntityCursor<E> cursor = transaction.entities(index);
			return new CloseableIterator<>()
			{
				E next;

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

				@Override
				public Entry<UUID, S> next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					E entity = next;
					next = transaction.next(cursor);
					return new MyEntry(entityToSignatureRequest(entity));
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
			return BerkeleyDBGenericSignatureRequestMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BerkeleyDBGenericSignatureRequestMap.this.isEmpty();
		}

	}

	@Override
	public CloseableSet<Entry<UUID, S>> entrySet()
	{
		return new EntrySet();
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

	@Override
	public S get(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		E entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		return entityToSignatureRequest(entity);
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof UUID))
			return false;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		return transaction.contains(index, uuidKey);

	}

	@Override
	public S remove(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		E entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		transaction.delete(index, uuidKey);
		return entityToSignatureRequest(entity);
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
