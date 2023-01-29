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

import java.security.PrivateKey;

import com.sleepycat.persist.model.Persistent;

import aletheia.persistence.berkeleydb.entities.PrivateKeyCapsule;
import aletheia.persistence.entities.authority.PlainPrivateSignatoryEntity;

@Persistent(version = 0)
public class BerkeleyDBPlainPrivateSignatoryEntity extends BerkeleyDBPrivateSignatoryEntity implements PlainPrivateSignatoryEntity
{
	private PrivateKeyCapsule privateKeyCapsule;

	@Override
	public PrivateKey getPrivateKey()
	{
		if (privateKeyCapsule == null)
			return null;
		return privateKeyCapsule.getObject();
	}

	@Override
	public void setPrivateKey(PrivateKey privateKey)
	{
		this.privateKeyCapsule = new PrivateKeyCapsule(privateKey);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((privateKeyCapsule == null) ? 0 : privateKeyCapsule.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBPlainPrivateSignatoryEntity other = (BerkeleyDBPlainPrivateSignatoryEntity) obj;
		if (privateKeyCapsule == null)
		{
			if (other.privateKeyCapsule != null)
				return false;
		}
		else if (!privateKeyCapsule.equals(other.privateKeyCapsule))
			return false;
		return true;
	}

}
