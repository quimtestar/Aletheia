/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import java.util.UUID;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.Signatory;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity.AuthorizerSignatureUuidKeyData;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid;

public class BerkeleyDBStatementAuthoritySignatureSetByAuthorizerAndSignatureUuid
		extends BerkeleyDBAbstractStatementAuthoritySignatureSet<BerkeleyDBStatementAuthoritySignatureEntity.AuthorizerSignatureUuidKeyData>
		implements StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid
{
	private final Signatory authorizer;
	private final UUID signatureUuid;

	private static SecondaryIndex<AuthorizerSignatureUuidKeyData, PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> index(
			BerkeleyDBPersistenceManager persistenceManager)
	{
		try
		{
			return persistenceManager.getEntityStore().statementAuthoritySignatureEntityAuthorizerSignatureUuidKeySecondaryIndex();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBStatementAuthoritySignatureSetByAuthorizerAndSignatureUuid(BerkeleyDBPersistenceManager persistenceManager,
			BerkeleyDBTransaction transaction, Signatory authorizer, UUID signatureUuid)
	{
		super(persistenceManager, index(persistenceManager), transaction,
				new BerkeleyDBStatementAuthoritySignatureEntity.AuthorizerSignatureUuidKeyData(authorizer.getUuid(), signatureUuid));
		this.authorizer = authorizer;
		this.signatureUuid = signatureUuid;
	}

	@Override
	public Signatory getAuthorizer()
	{
		return authorizer;
	}

	@Override
	public UUID getSignatureUuid()
	{
		return signatureUuid;
	}

}
