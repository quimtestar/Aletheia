/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.Term;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.SpecializationEntity;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 2)
public class BerkeleyDBSpecializationEntity extends BerkeleyDBStatementEntity implements SpecializationEntity
{
	public static final String uuidKeyGeneral_FieldName = "uuidKeyGeneral";
	@SecondaryKey(name = uuidKeyGeneral_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyGeneral;

	private Term instance;

	private ParameterIdentification instanceParameterIdentification;

	public static final String uuidKeyInstanceProof_FieldName = "uuidKeyInstanceProof";
	@SecondaryKey(name = uuidKeyInstanceProof_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyInstanceProof;

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
	public ParameterIdentification getInstanceParameterIdentification()
	{
		return instanceParameterIdentification;
	}

	@Override
	public void setInstanceParameterIdentification(ParameterIdentification instanceParameterIdentification)
	{
		this.instanceParameterIdentification = instanceParameterIdentification;
	}

	public UUIDKey getUuidKeyInstanceProof()
	{
		return uuidKeyInstanceProof;
	}

	public void setUuidKeyInstanceProof(UUIDKey uuidKeyInstanceProof)
	{
		this.uuidKeyInstanceProof = uuidKeyInstanceProof;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uuidKeyGeneral == null) ? 0 : uuidKeyGeneral.hashCode());
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + ((uuidKeyInstanceProof == null) ? 0 : uuidKeyInstanceProof.hashCode());
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
		if (uuidKeyGeneral == null)
		{
			if (other.uuidKeyGeneral != null)
				return false;
		}
		else if (!uuidKeyGeneral.equals(other.uuidKeyGeneral))
			return false;
		if (instance == null)
		{
			if (other.instance != null)
				return false;
		}
		else if (!instance.equals(other.instance))
			return false;
		if (uuidKeyInstanceProof == null)
		{
			if (other.uuidKeyInstanceProof != null)
				return false;
		}
		else if (!uuidKeyInstanceProof.equals(other.uuidKeyInstanceProof))
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

	@Override
	public UUID getInstanceProofUuid()
	{
		return uuidKeyInstanceProof.uuid();
	}

	@Override
	public void setInstanceProofUuid(UUID uuidInstanceProof)
	{
		uuidKeyInstanceProof = new UUIDKey(uuidInstanceProof);
	}

}
