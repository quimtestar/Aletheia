/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
import com.sleepycat.persist.EntityIndex;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.Signatory;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.collections.authority.DelegateAuthorizerByAuthorizerMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

public abstract class BerkeleyDBGenericDelegateAuthorizerByAuthorizerMap<K> extends AbstractCloseableMap<Signatory, DelegateAuthorizer>
		implements DelegateAuthorizerByAuthorizerMap
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final EntityIndex<K, BerkeleyDBDelegateAuthorizerEntity> index;
	private final K firstKey;
	private final K lastKey;

	public BerkeleyDBGenericDelegateAuthorizerByAuthorizerMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			EntityIndex<K, BerkeleyDBDelegateAuthorizerEntity> index, K firstKey, K lastKey)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.index = index;
		this.firstKey = firstKey;
		this.lastKey = lastKey;
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

	protected abstract K uuidToKey(UUID authorizerUuid);

	private class EntrySet extends AbstractCloseableSet<Entry<Signatory, DelegateAuthorizer>>
	{

		@Override
		public CloseableIterator<Entry<Signatory, DelegateAuthorizer>> iterator()
		{
			final EntityCursor<BerkeleyDBDelegateAuthorizerEntity> cursor = transaction.entities(index, firstKey, true, lastKey, true);
			return new CloseableIterator<>()
			{
				BerkeleyDBDelegateAuthorizerEntity next;

				{
					next = transaction.first(cursor);
					if (next == null)
						transaction.close(cursor);
				}

				@Override
				public boolean hasNext()
				{
					return next != null;
				}

				@Override
				public Entry<Signatory, DelegateAuthorizer> next()
				{
					if (next == null)
						throw new NoSuchElementException();
					final BerkeleyDBDelegateAuthorizerEntity entity = next;
					next = transaction.next(cursor);
					if (next == null)
						transaction.close(cursor);
					return new Entry<>()
					{

						@Override
						public Signatory getKey()
						{
							return persistenceManager.getSignatory(transaction, entity.getAuthorizerUuid());
						}

						@Override
						public DelegateAuthorizer getValue()
						{
							return persistenceManager.entityToDelegateAuthorizer(entity);
						}

						@Override
						public DelegateAuthorizer setValue(DelegateAuthorizer value)
						{
							throw new UnsupportedOperationException();
						}

					};
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
			EntityCursor<K> cursor = transaction.keys(index, firstKey, true, lastKey, true);
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
			EntityCursor<K> cursor = transaction.keys(index, firstKey, true, lastKey, true);
			try
			{
				return transaction.first(cursor) == null;
			}
			finally
			{
				transaction.close(cursor);
			}
		}

	}

	@Override
	public CloseableSet<Entry<Signatory, DelegateAuthorizer>> entrySet()
	{
		return new EntrySet();
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof Signatory))
			return false;
		Signatory signatory = (Signatory) key;
		K key_ = uuidToKey(signatory.getUuid());
		return transaction.contains(index, key_);
	}

	@Override
	public DelegateAuthorizer get(Object key)
	{
		if (!containsKey(key) || !(key instanceof Signatory))
			return null;
		Signatory signatory = (Signatory) key;
		K key_ = uuidToKey(signatory.getUuid());
		BerkeleyDBDelegateAuthorizerEntity entity = transaction.get(index, key_);
		return persistenceManager.entityToDelegateAuthorizer(entity);
	}

	@Override
	public boolean isEmpty()
	{
		return entrySet().isEmpty();
	}

}
