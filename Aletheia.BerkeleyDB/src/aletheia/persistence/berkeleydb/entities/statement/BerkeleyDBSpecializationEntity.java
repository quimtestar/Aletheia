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
package aletheia.persistence.berkeleydb.entities.statement;

import java.util.UUID;

import aletheia.model.term.Term;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.SpecializationEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 0)
public class BerkeleyDBSpecializationEntity extends BerkeleyDBStatementEntity implements SpecializationEntity
{
	public static final String uuidKeyGeneral_FieldName = "uuidKeyGeneral";
	@SecondaryKey(name = uuidKeyGeneral_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyGeneral;

	private Term instance;

	public BerkeleyDBSpecializationEntity()
	{
		super();
	}

	public UUIDKey getUuidKeyGeneral()
	{
		return uuidKeyGeneral;
	}

	public void setUuidKeyGeneral(UUIDKey uuidKeyGeneral)
	{
		this.uuidKeyGeneral = uuidKeyGeneral;
	}

	@Override
	public Term getInstance()
	{
		return instance;
	}

	@Override
	public void setInstance(Term instance)
	{
		this.instance = instance;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + ((uuidKeyGeneral == null) ? 0 : uuidKeyGeneral.hashCode());
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
		BerkeleyDBSpecializationEntity other = (BerkeleyDBSpecializationEntity) obj;
		if (instance == null)
		{
			if (other.instance != null)
				return false;
		}
		else if (!instance.equals(other.instance))
			return false;
		if (uuidKeyGeneral == null)
		{
			if (other.uuidKeyGeneral != null)
				return false;
		}
		else if (!uuidKeyGeneral.equals(other.uuidKeyGeneral))
			return false;
		return true;
	}

	@Override
	public UUID getGeneralUuid()
	{
		return uuidKeyGeneral.uuid();
	}

	@Override
	public void setGeneralUuid(UUID uuidGeneral)
	{
		uuidKeyGeneral = new UUIDKey(uuidGeneral);
	}

}
