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
import java.util.Collections;
import java.util.UUID;

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity.ContextCreationDateSecondaryKeyData;
import aletheia.persistence.collections.authority.SignatureRequestContextCreationDateCollection;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBSignatureRequestContextCreationDateCollection extends BerkeleyDBSignatureRequestCollection<ContextCreationDateSecondaryKeyData>
implements SignatureRequestContextCreationDateCollection
{
	private static SecondaryIndex<ContextCreationDateSecondaryKeyData, UUIDKey, BerkeleyDBSignatureRequestEntity> index(
			BerkeleyDBPersistenceManager persistenceManager)
			{
		try
		{
			return persistenceManager.getEntityStore().signatureRequestEntityContextCreationDateSecondaryIndex();

		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
			}

	private final UUID contextUuid;

	public BerkeleyDBSignatureRequestContextCreationDateCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID contextUuid)
	{
		super(persistenceManager, index(persistenceManager), transaction, ContextCreationDateSecondaryKeyData.class, ContextCreationDateSecondaryKeyData
				.min(contextUuid), true, ContextCreationDateSecondaryKeyData.max(contextUuid), true);
		this.contextUuid = contextUuid;
	}

	@Override
	public UUID getContextUuid()
	{
		return contextUuid;
	}

	@Override
	protected Collection<ContextCreationDateSecondaryKeyData> entityKeys(BerkeleyDBSignatureRequestEntity entity)
	{
		return Collections.singleton(entity.getContextCreationDateSecondaryKeyData());
	}

}
