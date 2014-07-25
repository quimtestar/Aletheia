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
import aletheia.persistence.collections.authority.SignedDependenciesLocalStatementAuthoritySet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBSignedDependenciesLocalStatementAuthoritySet extends BerkeleyDBFlagLocalStatementAuthoritySet implements
SignedDependenciesLocalStatementAuthoritySet
{
	private static SecondaryIndex<ContextFlagSecondaryKeyData, UUIDKey, BerkeleyDBStatementAuthorityEntity> secondaryIndex(
			BerkeleyDBPersistenceManager persistenceManager)
			{
		try
		{
			return persistenceManager.getEntityStore().statementAuthorityEntityContextSignedDependenciesSecondaryIndex();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
			}

	public BerkeleyDBSignedDependenciesLocalStatementAuthoritySet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			ContextAuthority contextAuthority)
	{
		super(persistenceManager, transaction, secondaryIndex(persistenceManager), contextAuthority);
	}

}
