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

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.Signatory;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSetByAuthorizer;

public class BerkeleyDBStatementAuthoritySignatureSetByAuthorizer extends BerkeleyDBAbstractStatementAuthoritySignatureSet<UUIDKey>
		implements StatementAuthoritySignatureSetByAuthorizer
{
	private final Signatory authorizer;

	private static SecondaryIndex<UUIDKey, PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> index(BerkeleyDBPersistenceManager persistenceManager)
	{
		try
		{
			return persistenceManager.getEntityStore().statementAuthoritySignatureEntityAuthorizerSecondaryIndex();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBStatementAuthoritySignatureSetByAuthorizer(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			Signatory authorizer)
	{
		super(persistenceManager, index(persistenceManager), transaction, new UUIDKey(authorizer.getUuid()));
		this.authorizer = authorizer;
	}

	@Override
	public Signatory getAuthorizer()
	{
		return authorizer;
	}

}
