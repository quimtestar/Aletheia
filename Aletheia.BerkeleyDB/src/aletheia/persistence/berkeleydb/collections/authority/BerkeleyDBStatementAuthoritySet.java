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

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;

public class BerkeleyDBStatementAuthoritySet extends BerkeleyDBAbstractStatementAuthoritySet
{

	public BerkeleyDBStatementAuthoritySet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super(persistenceManager, transaction);
	}

	@Override
	protected PrimaryIndex<UUIDKey, BerkeleyDBStatementAuthorityEntity> index()
	{
		return getPrimaryIndex();
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<UUIDKey> cursor = getTransaction().keys(index());
		try
		{
			return getTransaction().first(cursor) == null;
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBStatementAuthorityEntity> cursor = getTransaction().entities(index());
		try
		{
			int n = 0;
			while (getTransaction().next(cursor) != null)
				n++;
			return n;
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

}
