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

import java.util.UUID;

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPackedSignatureRequestEntity;
import aletheia.persistence.collections.authority.PackedSignatureRequestSetByContextPath;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityIndex;

public class BerkeleyDBPackedSignatureRequestSetByContextPath extends BerkeleyDBPackedSignatureRequestUUIDKeySet
		implements PackedSignatureRequestSetByContextPath
{

	private static EntityIndex<UUIDKey, BerkeleyDBPackedSignatureRequestEntity> index(BerkeleyDBPersistenceManager persistenceManager, UUID contextUuid)
	{
		try
		{
			return persistenceManager.getEntityStore().packedSignatureRequestEntityContextPathSubIndex(new UUIDKey(contextUuid));

		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	private final UUID contextUuid;

	public BerkeleyDBPackedSignatureRequestSetByContextPath(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID contextUuid)
	{
		super(persistenceManager, index(persistenceManager, contextUuid), transaction);
		this.contextUuid = contextUuid;
	}

	@Override
	public UUID getContextUuid()
	{
		return contextUuid;
	}

}
