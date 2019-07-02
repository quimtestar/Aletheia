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

import aletheia.model.authority.Person;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPersonEntity;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBKeyComparator;
import aletheia.persistence.collections.authority.PersonsSet;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public abstract class BerkeleyDBAbstractPersonsSet<K> extends AbstractCloseableSet<Person> implements PersonsSet
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<K, UUIDKey, BerkeleyDBPersonEntity> index;
	private final BerkeleyDBTransaction transaction;
	private final BerkeleyDBKeyComparator<K> keyComparator;
	private final K fromKey;
	private final boolean fromInclusive;
	private final K toKey;
	private final boolean toInclusive;

	public BerkeleyDBAbstractPersonsSet(BerkeleyDBPersistenceManager persistenceManager, SecondaryIndex<K, UUIDKey, BerkeleyDBPersonEntity> index,
			BerkeleyDBTransaction transaction, Class<K> keyClass, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.index = index;
		this.transaction = transaction;
		this.keyComparator = keyClass != null ? new BerkeleyDBKeyComparator<>(keyClass) : null;
		this.fromKey = fromKey;
		this.fromInclusive = fromInclusive;
		this.toKey = toKey;
		this.toInclusive = toInclusive;
	}

	public BerkeleyDBAbstractPersonsSet(BerkeleyDBPersistenceManager persistenceManager, SecondaryIndex<K, UUIDKey, BerkeleyDBPersonEntity> index,
			BerkeleyDBTransaction transaction)
	{
		this(persistenceManager, index, transaction, null, null, false, null, false);
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	protected SecondaryIndex<K, UUIDKey, BerkeleyDBPersonEntity> getIndex()
	{
		return index;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	protected BerkeleyDBKeyComparator<K> getKeyComparator()
	{
		return keyComparator;
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

	protected boolean keyInInterval(K k)
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

	protected abstract K personKey(Person person);

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof Person))
			return false;
		Person person = (Person) o;
		K key = personKey(person);
		if (key == null)
			return false;
		if (!keyInInterval(key))
			return false;
		return transaction.contains(index.subIndex(key), new UUIDKey(person.getUuid()));
	}

	@Override
	public CloseableIterator<Person> iterator()
	{
		final EntityCursor<BerkeleyDBPersonEntity> cursor = transaction.entities(index, fromKey, fromInclusive, toKey, toInclusive);
		return new CloseableIterator<Person>()
		{
			private BerkeleyDBPersonEntity next;

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
			public Person next()
			{
				if (next == null)
					throw new NoSuchElementException();
				BerkeleyDBPersonEntity entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
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
		EntityCursor<K> cursor = transaction.keys(index, fromKey, fromInclusive, toKey, toInclusive);
		try
		{
			int n = 0;
			while (true)
			{
				K k = transaction.next(cursor);
				if (k == null)
					break;
				n += transaction.count(cursor);
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

}
