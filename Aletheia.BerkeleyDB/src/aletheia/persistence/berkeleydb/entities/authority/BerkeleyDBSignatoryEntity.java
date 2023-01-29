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

import java.security.PublicKey;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import aletheia.persistence.berkeleydb.entities.PublicKeyCapsule;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.SignatoryEntity;

@Entity(version = 0)
public class BerkeleyDBSignatoryEntity implements SignatoryEntity
{
	@PrimaryKey
	private UUIDKey uuidKey;

	private PublicKeyCapsule publicKeyCapsule;

	@Override
	public UUID getUuid()
	{
		return uuidKey.uuid();
	}

	@Override
	public void setUuid(UUID uuid)
	{
		this.uuidKey = new UUIDKey(uuid);
	}

	@Override
	public PublicKey getPublicKey()
	{
		if (publicKeyCapsule == null)
			return null;
		return publicKeyCapsule.getObject();
	}

	@Override
	public void setPublicKey(PublicKey publicKey)
	{
		this.publicKeyCapsule = new PublicKeyCapsule(publicKey);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((publicKeyCapsule == null) ? 0 : publicKeyCapsule.hashCode());
		result = prime * result + ((uuidKey == null) ? 0 : uuidKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBSignatoryEntity other = (BerkeleyDBSignatoryEntity) obj;
		if (publicKeyCapsule == null)
		{
			if (other.publicKeyCapsule != null)
				return false;
		}
		else if (!publicKeyCapsule.equals(other.publicKeyCapsule))
			return false;
		if (uuidKey == null)
		{
			if (other.uuidKey != null)
				return false;
		}
		else if (!uuidKey.equals(other.uuidKey))
			return false;
		return true;
	}

}
