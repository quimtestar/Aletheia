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

import java.util.NoSuchElementException;
import java.util.UUID;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.RootContextAuthority;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBRootContextAuthorityEntity;
import aletheia.persistence.collections.authority.RootContextAuthorityBySignatureUuid;
import aletheia.persistence.entities.authority.RootContextAuthorityEntity;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

public class BerkeleyDBRootContextAuthorityBySignatureUuid extends AbstractCloseableMap<UUID, RootContextAuthority>
		implements RootContextAuthorityBySignatureUuid
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBRootContextAuthorityEntity> index;

	public BerkeleyDBRootContextAuthorityBySignatureUuid(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.index = persistenceManager.getEntityStore().rootContextAuthorityEntitySignatureUuidSecondaryIndex();
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public Transaction getTransaction()
	{
		return transaction;
	}

	@Override
	public RootContextAuthority get(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUIDKey uuidKey = new UUIDKey((UUID) key);
		RootContextAuthorityEntity entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		return persistenceManager.entityToRootContextAuthority(entity);
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

	protected class EntrySet extends AbstractCloseableSet<Entry<UUID, RootContextAuthority>>
	{
		private class MyEntry implements Entry<UUID, RootContextAuthority>
		{
			final RootContextAuthority rootContextAuthority;

			public MyEntry(RootContextAuthority rootContextAuthority)
			{
				super();
				this.rootContextAuthority = rootContextAuthority;
			}

			@Override
			public UUID getKey()
			{
				return rootContextAuthority.getSignatureUuid();
			}

			@Override
			public RootContextAuthority getValue()
			{
				return rootContextAuthority;
			}

			@Override
			public RootContextAuthority setValue(RootContextAuthority value)
			{
				return null;
			}

			@Override
			public String toString()
			{
				return getKey() + "=" + getValue();
			}
		}

		public EntrySet()
		{
			super();
		}

		@Override
		public CloseableIterator<Entry<UUID, RootContextAuthority>> iterator()
		{
			final EntityCursor<BerkeleyDBRootContextAuthorityEntity> cursor = transaction.entities(index);
			return new CloseableIterator<>()
			{
				BerkeleyDBRootContextAuthorityEntity next;

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
				public Entry<UUID, RootContextAuthority> next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					BerkeleyDBRootContextAuthorityEntity entity = next;
					next = transaction.next(cursor);
					return new MyEntry(persistenceManager.entityToRootContextAuthority(entity));
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
			return BerkeleyDBRootContextAuthorityBySignatureUuid.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BerkeleyDBRootContextAuthorityBySignatureUuid.this.isEmpty();
		}

	}

	@Override
	public CloseableSet<Entry<UUID, RootContextAuthority>> entrySet()
	{
		return new EntrySet();
	}

}
