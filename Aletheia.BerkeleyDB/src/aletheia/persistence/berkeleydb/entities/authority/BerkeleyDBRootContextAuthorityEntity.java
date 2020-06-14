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
package aletheia.persistence.berkeleydb.entities.authority;

import java.util.UUID;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.RootContextAuthorityEntity;

@Persistent(version = 0)
public class BerkeleyDBRootContextAuthorityEntity extends BerkeleyDBContextAuthorityEntity implements RootContextAuthorityEntity
{
	public static final String signatureUuidKey_FieldName = "signatureUuidKey";
	@SecondaryKey(name = signatureUuidKey_FieldName, relate = Relationship.ONE_TO_ONE)
	private UUIDKey signatureUuidKey;

	public BerkeleyDBRootContextAuthorityEntity()
	{
		super();
	}

	@Override
	public UUID getSignatureUuid()
	{
		if (signatureUuidKey == null)
			return null;
		return signatureUuidKey.uuid();
	}

	@Override
	public void setSignatureUuid(UUID uuid)
	{
		if (uuid == null)
			this.signatureUuidKey = null;
		else
			this.signatureUuidKey = new UUIDKey(uuid);
	}

}
