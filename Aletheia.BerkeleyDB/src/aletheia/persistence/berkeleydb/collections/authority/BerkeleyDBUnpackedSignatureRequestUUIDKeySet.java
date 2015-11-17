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

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBUnpackedSignatureRequestEntity;

import com.sleepycat.persist.EntityIndex;

public abstract class BerkeleyDBUnpackedSignatureRequestUUIDKeySet extends BerkeleyDBUnpackedSignatureRequestSet<UUIDKey>
{

	public BerkeleyDBUnpackedSignatureRequestUUIDKeySet(BerkeleyDBPersistenceManager persistenceManager,
			EntityIndex<UUIDKey, BerkeleyDBUnpackedSignatureRequestEntity> index, BerkeleyDBTransaction transaction, Class<UUIDKey> keyClass, UUIDKey fromKey,
			boolean fromInclusive, UUIDKey toKey, boolean toInclusive)
	{
		super(persistenceManager, index, transaction, keyClass, fromKey, fromInclusive, toKey, toInclusive);
	}

	public BerkeleyDBUnpackedSignatureRequestUUIDKeySet(BerkeleyDBPersistenceManager persistenceManager,
			EntityIndex<UUIDKey, BerkeleyDBUnpackedSignatureRequestEntity> index, BerkeleyDBTransaction transaction)
	{
		super(persistenceManager, index, transaction);
	}

	@Override
	protected Collection<UUIDKey> entityKeys(BerkeleyDBUnpackedSignatureRequestEntity entity)
	{
		return Collections.singleton(new UUIDKey(entity.getUuid()));
	}

}