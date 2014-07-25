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

import aletheia.persistence.entities.authority.PrivateSignatoryEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 2)
public abstract class BerkeleyDBPrivateSignatoryEntity extends BerkeleyDBSignatoryEntity implements PrivateSignatoryEntity
{
	public static final String mark_FieldName = "mark";
	@SecondaryKey(name = mark_FieldName, relate = Relationship.MANY_TO_ONE)
	private boolean mark;

	private String signatureAlgorithm;

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
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (mark ? 1231 : 1237);
		result = prime * result + ((signatureAlgorithm == null) ? 0 : signatureAlgorithm.hashCode());
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
		if (mark != other.mark)
			return false;
		if (signatureAlgorithm == null)
		{
			if (other.signatureAlgorithm != null)
				return false;
		}
		else if (!signatureAlgorithm.equals(other.signatureAlgorithm))
			return false;
		return true;
	}

}
