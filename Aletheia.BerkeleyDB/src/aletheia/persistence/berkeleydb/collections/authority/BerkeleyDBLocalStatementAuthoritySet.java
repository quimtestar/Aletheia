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

import aletheia.model.authority.StatementAuthority;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;
import aletheia.persistence.collections.authority.LocalStatementAuthoritySet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBLocalStatementAuthoritySet extends BerkeleyDBAbstractStatementAuthoritySet implements LocalStatementAuthoritySet
{
	private final StatementAuthority contextAuthority;
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementAuthorityEntity> secondaryIndex;
	private final EntityIndex<UUIDKey, BerkeleyDBStatementAuthorityEntity> subIndex;

	public BerkeleyDBLocalStatementAuthoritySet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			StatementAuthority contextAuthority)
	{
		super(persistenceManager, transaction);
		try
		{
			this.contextAuthority = contextAuthority;
			this.secondaryIndex = persistenceManager.getEntityStore().statementAuthorityEntityContextSecondaryIndex();
			this.subIndex = secondaryIndex.subIndex(new UUIDKey(contextAuthority.getStatementUuid()));
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	@Override
	protected EntityIndex<UUIDKey, BerkeleyDBStatementAuthorityEntity> index()
	{
		return subIndex;
	}

	@Override
	public boolean isEmpty()
	{
		return !getTransaction().contains(secondaryIndex, new UUIDKey(contextAuthority.getStatementUuid()));
	}

	@Override
	public int size()
	{
		UUIDKey uuidKey = new UUIDKey(contextAuthority.getStatementUuid());
		EntityCursor<BerkeleyDBStatementAuthorityEntity> cursor = getTransaction().entities(secondaryIndex, uuidKey, true, uuidKey, true);
		try
		{
			if (getTransaction().first(cursor) == null)
				return 0;
			return getTransaction().count(cursor);
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

}
