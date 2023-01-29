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
package aletheia.persistence.berkeleydb.entities.statement;

import java.util.UUID;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.statement.UnfoldingContextEntity;

@Persistent(version = 0)
public class BerkeleyDBUnfoldingContextEntity extends BerkeleyDBContextEntity implements UnfoldingContextEntity
{
	public static final String uuidKeyDeclaration_FieldName = "uuidKeyDeclaration";
	@SecondaryKey(name = uuidKeyDeclaration_FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey uuidKeyDeclaration;

	public BerkeleyDBUnfoldingContextEntity()
	{
		super();
	}

	public UUIDKey getUuidKeyDeclaration()
	{
		return uuidKeyDeclaration;
	}

	public void setUuidKeyDeclaration(UUIDKey uuidKeyDeclaration)
	{
		this.uuidKeyDeclaration = uuidKeyDeclaration;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((uuidKeyDeclaration == null) ? 0 : uuidKeyDeclaration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBUnfoldingContextEntity other = (BerkeleyDBUnfoldingContextEntity) obj;
		if (uuidKeyDeclaration == null)
		{
			if (other.uuidKeyDeclaration != null)
				return false;
		}
		else if (!uuidKeyDeclaration.equals(other.uuidKeyDeclaration))
			return false;
		return true;
	}

	@Override
	public UUID getDeclarationUuid()
	{
		return uuidKeyDeclaration.uuid();
	}

	@Override
	public void setDeclarationUuid(UUID uuidDeclaration)
	{
		if (uuidDeclaration == null)
			uuidKeyDeclaration = null;
		else
			uuidKeyDeclaration = new UUIDKey(uuidDeclaration);
	}

}
