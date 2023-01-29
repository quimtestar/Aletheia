/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.Term;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.DeclarationEntity;

@Persistent(version = 4)
public class BerkeleyDBDeclarationEntity extends BerkeleyDBStatementEntity implements DeclarationEntity
{
	private Term value;

	private ParameterIdentification valueParameterIdentification;

	public static final String uuidKeyInstanceProof_FieldName = "uuidKeyValueProof";
	@SecondaryKey(name = uuidKeyInstanceProof_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyValueProof;

	public BerkeleyDBDeclarationEntity()
	{
		super();
	}

	@Override
	public Term getValue()
	{
		return value;
	}

	@Override
	public void setValue(Term value)
	{
		this.value = value;
	}

	@Override
	public ParameterIdentification getValueParameterIdentification()
	{
		return valueParameterIdentification;
	}

	@Override
	public void setValueParameterIdentification(ParameterIdentification valueParameterIdentification)
	{
		this.valueParameterIdentification = valueParameterIdentification;
	}

	public UUIDKey getUuidKeyValueProof()
	{
		return uuidKeyValueProof;
	}

	public void setUuidKeyValueProof(UUIDKey uuidKeyValueProof)
	{
		this.uuidKeyValueProof = uuidKeyValueProof;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((uuidKeyValueProof == null) ? 0 : uuidKeyValueProof.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBDeclarationEntity other = (BerkeleyDBDeclarationEntity) obj;
		if (value == null)
		{
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		if (uuidKeyValueProof == null)
		{
			if (other.uuidKeyValueProof != null)
				return false;
		}
		else if (!uuidKeyValueProof.equals(other.uuidKeyValueProof))
			return false;
		return true;
	}

	@Override
	public UUID getValueProofUuid()
	{
		return uuidKeyValueProof.uuid();
	}

	@Override
	public void setValueProofUuid(UUID uuidValueProof)
	{
		uuidKeyValueProof = new UUIDKey(uuidValueProof);
	}

}
