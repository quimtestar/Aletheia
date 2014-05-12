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
package aletheia.persistence.berkeleydb.entities.authority;

import java.security.PrivateKey;

import aletheia.persistence.berkeleydb.entities.PrivateKeyCapsule;
import aletheia.persistence.entities.authority.PrivateSignatoryEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 1)
public class BerkeleyDBPrivateSignatoryEntity extends BerkeleyDBSignatoryEntity implements PrivateSignatoryEntity
{
	public static final String mark_FieldName = "mark";
	@SecondaryKey(name = mark_FieldName, relate = Relationship.MANY_TO_ONE)
	private boolean mark;

	private String signatureAlgorithm;

	private PrivateKeyCapsule privateKeyCapsule;

	public BerkeleyDBPrivateSignatoryEntity()
	{
		this.mark = true;
	}

	@Override
	public String getSignatureAlgorithm()
	{
		return signatureAlgorithm;
	}

	@Override
	public void setSignatureAlgorithm(String signatureAlgorithm)
	{
		this.signatureAlgorithm = signatureAlgorithm;
	}

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
		result = prime * result + (mark ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BerkeleyDBPrivateSignatoryEntity other = (BerkeleyDBPrivateSignatoryEntity) obj;
		if (privateKeyCapsule == null)
		{
			if (other.privateKeyCapsule != null)
				return false;
		}
		else if (!privateKeyCapsule.equals(other.privateKeyCapsule))
			return false;
		if (mark != other.mark)
			return false;
		return true;
	}

}
