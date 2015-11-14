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

import java.util.Collection;
import java.util.NoSuchElementException;

import aletheia.model.authority.SignatureRequest;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBKeyComparator;
import aletheia.persistence.collections.authority.GenericSignatureRequestCollection;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableCollection;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public abstract class BerkeleyDBGenericSignatureRequestCollection<S extends SignatureRequest, K, E extends BerkeleyDBSignatureRequestEntity>
		extends AbstractCloseableCollection<S> implements GenericSignatureRequestCollection<S>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final EntityIndex<K, E> index;
	private final BerkeleyDBTransaction transaction;
	private final BerkeleyDBKeyComparator<K> keyComparator;
	private final K fromKey;
	private final boolean fromInclusive;
	private final K toKey;
	private final boolean toInclusive;

	public BerkeleyDBGenericSignatureRequestCollection(BerkeleyDBPersistenceManager persistenceManager, EntityIndex<K, E> index,
			BerkeleyDBTransaction transaction, Class<K> keyClass, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
	{
		this.persistenceManager = persistenceManager;
		this.index = index;
		this.transaction = transaction;
		this.keyComparator = keyClass != null ? new BerkeleyDBKeyComparator<K>(keyClass) : null;
		this.fromKey = fromKey;
		this.fromInclusive = fromInclusive;
		this.toKey = toKey;
		this.toInclusive = toInclusive;
	}

	public BerkeleyDBGenericSignatureRequestCollection(BerkeleyDBPersistenceManager persistenceManager, EntityIndex<K, E> index,
			BerkeleyDBTransaction transaction)
	{
		this(persistenceManager, index, transaction, null, null, false, null, false);
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected EntityIndex<K, E> getIndex()
	{
		return index;
	}

	@Override
	public Transaction getTransaction()
	{
		return transaction;
	}

	protected K getFromKey()
	{
		return fromKey;
	}

	protected boolean isFromInclusive()
	{
		return fromInclusive;
	}

	protected K getToKey()
	{
		return toKey;
	}

	protected boolean isToInclusive()
	{
		return toInclusive;
	}

	@SuppressWarnings("unchecked")
	protected S entityToSignatureRequest(E entity)
	{
		return (S) persistenceManager.entityToSignatureRequest(entity);
	}

	@Override
	public CloseableIterator<S> iterator()
	{
		final EntityCursor<E> cursor = transaction.entities(index, fromKey, fromInclusive, toKey, toInclusive);
		return new CloseableIterator<S>()
		{
			private E next;

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
			public S next()
			{
				if (next == null)
					throw new NoSuchElementException();
				E entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
				return entityToSignatureRequest(entity);
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

	@Override
	public int size()
	{
		EntityCursor<K> cursor = transaction.keys(index, fromKey, fromInclusive, toKey, toInclusive);
		try
		{
			int n = 0;
			while (true)
			{
				K k = transaction.next(cursor);
				if (k == null)
					break;
				n += cursor.count();
			}
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
		EntityCursor<K> cursor = transaction.keys(index, fromKey, fromInclusive, toKey, toInclusive);
		try
		{
			K k = transaction.first(cursor);
			return k == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	protected abstract Collection<K> entityKeys(E entity);

	private boolean keyInInterval(K k)
	{
		if (keyComparator == null)
			return true;
		if (fromKey != null)
		{
			int c = keyComparator.compare(fromKey, k);
			if (c >= 0 && (!fromInclusive || c != 0))
				if (c > 0 || (!fromInclusive && c >= 0))
					return false;
		}
		if (toKey != null)
		{
			int c = keyComparator.compare(k, toKey);
			if (c >= 0 && (!toInclusive || c != 0))
				if (c > 0 || (!toInclusive && c >= 0))
					return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o)
	{
		try
		{
			for (K k : entityKeys((E) ((S) o).getEntity()))
			{
				if (keyInInterval(k) && transaction.contains(index, k))
					return true;
			}
			return false;
		}
		catch (ClassCastException e)
		{
			return false;
		}

	}

	@Override
	public Object[] toArray()
	{
		return MiscUtilities.iterableToArray(this);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return MiscUtilities.iterableToArray(this, a);
	}

}
