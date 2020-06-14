/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import java.util.Date;

import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.Person;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPersonEntity;
import aletheia.persistence.collections.authority.PersonsOrphanSinceSortedSet;
import aletheia.utilities.collections.CloseableSortedSet;

public class BerkeleyDBPersonsOrphanSinceSortedSet extends BerkeleyDBAbstractPersonsSortedSet<Date> implements PersonsOrphanSinceSortedSet
{

	private static SecondaryIndex<Date, UUIDKey, BerkeleyDBPersonEntity> makeIndex(BerkeleyDBPersistenceManager persistenceManager)
	{
		return persistenceManager.getEntityStore().personEntityOrphanSinceSecondaryIndex();
	}

	public BerkeleyDBPersonsOrphanSinceSortedSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super(persistenceManager, makeIndex(persistenceManager), transaction, Date.class);
	}

	private BerkeleyDBPersonsOrphanSinceSortedSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Date fromOrphanSince,
			Date toOrphanSince)
	{
		super(persistenceManager, makeIndex(persistenceManager), transaction, Date.class, fromOrphanSince, true, toOrphanSince, false);
	}

	@Override
	protected BerkeleyDBPersonsOrphanSinceSortedSet newBerkeleyDBPersonsSortedSet(Date fromKey, Date toKey)
	{
		return new BerkeleyDBPersonsOrphanSinceSortedSet(getPersistenceManager(), getTransaction(), fromKey, toKey);
	}

	@Override
	protected Date personKey(Person person)
	{
		return person.getOrphanSince();
	}

	@Override
	public CloseableSortedSet<Person> olderThanSet(Date date)
	{
		return new BerkeleyDBPersonsOrphanSinceSortedSet(getPersistenceManager(), getTransaction(), null, date);
	}

}
