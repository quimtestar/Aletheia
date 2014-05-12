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

import aletheia.model.authority.Person;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPersonEntity;
import aletheia.utilities.collections.CloseableSortedSet;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public abstract class BerkeleyDBAbstractPersonsSortedSet<K> extends BerkeleyDBAbstractPersonsSet<K> implements CloseableSortedSet<Person>
{
	private class MyComparator implements Comparator<Person>
	{
		private MyComparator()
		{
			if (getKeyComparator() == null)
				throw new IllegalStateException();
		}

		@Override
		public int compare(Person p1, Person p2)
		{
			return getKeyComparator().compare(personKey(p1), personKey(p2));
		}

	}

	private final MyComparator comparator = new MyComparator();

	public BerkeleyDBAbstractPersonsSortedSet(BerkeleyDBPersistenceManager persistenceManager, SecondaryIndex<K, UUIDKey, BerkeleyDBPersonEntity> index,
			BerkeleyDBTransaction transaction, Class<K> keyClass, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)
	{
		super(persistenceManager, index, transaction, keyClass, fromKey, fromInclusive, toKey, toInclusive);
	}

	public BerkeleyDBAbstractPersonsSortedSet(BerkeleyDBPersistenceManager persistenceManager, SecondaryIndex<K, UUIDKey, BerkeleyDBPersonEntity> index,
			BerkeleyDBTransaction transaction, Class<K> keyClass)
	{
		super(persistenceManager, index, transaction, keyClass, null, false, null, false);
	}

	@Override
	public Comparator<Person> comparator()
	{
		return comparator;
	}

	protected abstract BerkeleyDBAbstractPersonsSortedSet<K> newBerkeleyDBPersonsSortedSet(K fromKey, K toKey);

	private K subSetFromKey(Person fromPerson)
	{
		K key = personKey(fromPerson);
		if (getKeyComparator().compare(getFromKey(), key) > 0)
			return key;
		return getFromKey();
	}

	private K subSetToKey(Person toPerson)
	{
		K key = personKey(toPerson);
		if (getKeyComparator().compare(getToKey(), key) < 0)
			return key;
		return getToKey();
	}

	@Override
	public CloseableSortedSet<Person> subSet(Person fromPerson, Person toPerson)
	{
		return newBerkeleyDBPersonsSortedSet(subSetFromKey(fromPerson), subSetToKey(toPerson));
	}

	@Override
	public CloseableSortedSet<Person> headSet(Person toPerson)
	{
		return newBerkeleyDBPersonsSortedSet(null, subSetToKey(toPerson));
	}

	@Override
	public CloseableSortedSet<Person> tailSet(Person fromPerson)
	{
		return newBerkeleyDBPersonsSortedSet(subSetFromKey(fromPerson), null);
	}

	@Override
	public Person first()
	{
		EntityCursor<BerkeleyDBPersonEntity> cursor = getTransaction().entities(getIndex(), getFromKey(), isFromInclusive(), getToKey(), isToInclusive());
		try
		{
			BerkeleyDBPersonEntity entity = getTransaction().first(cursor);
			if (entity == null)
				return null;
			return getPersistenceManager().entityToPerson(entity);
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

	@Override
	public Person last()
	{
		EntityCursor<BerkeleyDBPersonEntity> cursor = getTransaction().entities(getIndex(), getFromKey(), isFromInclusive(), getToKey(), isToInclusive());
		try
		{
			BerkeleyDBPersonEntity entity = getTransaction().last(cursor);
			if (entity == null)
				return null;
			return getPersistenceManager().entityToPerson(entity);
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

}
