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

import com.sleepycat.persist.EntityIndex;

import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBUnpackedSignatureRequestEntity;
import aletheia.persistence.collections.authority.UnpackedSignatureRequestCollection;

public abstract class BerkeleyDBUnpackedSignatureRequestCollection<K>
		extends BerkeleyDBGenericSignatureRequestCollection<UnpackedSignatureRequest, K, BerkeleyDBUnpackedSignatureRequestEntity>
		implements UnpackedSignatureRequestCollection
{

	public BerkeleyDBUnpackedSignatureRequestCollection(BerkeleyDBPersistenceManager persistenceManager,
			EntityIndex<K, BerkeleyDBUnpackedSignatureRequestEntity> index, BerkeleyDBTransaction transaction)
	{
		super(persistenceManager, index, transaction);
	}

	public BerkeleyDBUnpackedSignatureRequestCollection(BerkeleyDBPersistenceManager persistenceManager,
			EntityIndex<K, BerkeleyDBUnpackedSignatureRequestEntity> index, BerkeleyDBTransaction transaction, Class<K> keyClass, K fromKey,
			boolean fromInclusive, K toKey, boolean toInclusive)
	{
		super(persistenceManager, index, transaction, keyClass, fromKey, fromInclusive, toKey, toInclusive);
	}

}
