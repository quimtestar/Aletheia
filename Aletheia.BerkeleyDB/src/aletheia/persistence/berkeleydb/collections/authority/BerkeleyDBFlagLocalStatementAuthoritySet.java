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

import aletheia.model.authority.ContextAuthority;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity.ContextFlagSecondaryKeyData;
import aletheia.persistence.collections.authority.FlagLocalStatementAuthoritySet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

public abstract class BerkeleyDBFlagLocalStatementAuthoritySet extends BerkeleyDBAbstractStatementAuthoritySet implements FlagLocalStatementAuthoritySet
{
	private final SecondaryIndex<ContextFlagSecondaryKeyData, UUIDKey, BerkeleyDBStatementAuthorityEntity> secondaryIndex;
	private final ContextFlagSecondaryKeyData contextFlagSecondaryKeyData;
	private final EntityIndex<UUIDKey, BerkeleyDBStatementAuthorityEntity> subIndex;

	public BerkeleyDBFlagLocalStatementAuthoritySet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			SecondaryIndex<ContextFlagSecondaryKeyData, UUIDKey, BerkeleyDBStatementAuthorityEntity> secondaryIndex, ContextAuthority contextAuthority)
	{
		super(persistenceManager, transaction);
		try
		{
			this.secondaryIndex = secondaryIndex;
			this.contextFlagSecondaryKeyData = new ContextFlagSecondaryKeyData(contextAuthority.getStatementUuid(), true);
			this.subIndex = secondaryIndex.subIndex(this.contextFlagSecondaryKeyData);
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
		return !getTransaction().contains(secondaryIndex, contextFlagSecondaryKeyData);
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBStatementAuthorityEntity> cursor = getTransaction().entities(secondaryIndex, contextFlagSecondaryKeyData, true,
				contextFlagSecondaryKeyData, true);
		try
		{
			BerkeleyDBStatementAuthorityEntity entity = getTransaction().first(cursor);
			if (entity == null)
				return 0;
			return getTransaction().count(cursor);
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

}
