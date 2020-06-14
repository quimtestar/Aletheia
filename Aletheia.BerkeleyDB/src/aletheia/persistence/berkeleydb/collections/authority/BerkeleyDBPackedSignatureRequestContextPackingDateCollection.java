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

import java.util.Collection;
import java.util.UUID;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPackedSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPackedSignatureRequestEntity.ContextPackingDateSecondaryKeyData;
import aletheia.persistence.collections.authority.PackedSignatureRequestContextPackingDateCollection;

public class BerkeleyDBPackedSignatureRequestContextPackingDateCollection extends BerkeleyDBPackedSignatureRequestCollection<ContextPackingDateSecondaryKeyData>
		implements PackedSignatureRequestContextPackingDateCollection
{
	private static SecondaryIndex<ContextPackingDateSecondaryKeyData, UUIDKey, BerkeleyDBPackedSignatureRequestEntity> index(
			BerkeleyDBPersistenceManager persistenceManager)
	{
		try
		{
			return persistenceManager.getEntityStore().packedSignatureRequestEntityContextPackingDateSecondaryIndex();

		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	private final UUID contextUuid;

	public BerkeleyDBPackedSignatureRequestContextPackingDateCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID contextUuid)
	{
		super(persistenceManager, index(persistenceManager), transaction, ContextPackingDateSecondaryKeyData.class,
				ContextPackingDateSecondaryKeyData.min(contextUuid), true, ContextPackingDateSecondaryKeyData.max(contextUuid), true);
		this.contextUuid = contextUuid;
	}

	@Override
	public UUID getContextUuid()
	{
		return contextUuid;
	}

	@Override
	protected Collection<ContextPackingDateSecondaryKeyData> entityKeys(BerkeleyDBPackedSignatureRequestEntity entity)
	{
		return entity.getContextPackingDateSecondaryKeyDataList();
	}

}
