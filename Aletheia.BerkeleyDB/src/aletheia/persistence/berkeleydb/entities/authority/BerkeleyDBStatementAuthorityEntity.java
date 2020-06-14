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

import java.util.Date;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.entities.authority.StatementAuthorityEntity;

@Entity(version = 4)
public class BerkeleyDBStatementAuthorityEntity implements StatementAuthorityEntity
{
	@PrimaryKey
	private UUIDKey statementUuidKey;

	public static final String statementUuidKey__FieldName = "statementUuidKey_";
	@SecondaryKey(name = statementUuidKey__FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.ONE_TO_ONE)
	private UUIDKey statementUuidKey_;

	public static final String contextUuidKey_FieldName = "contextUuidKey";
	@SecondaryKey(name = contextUuidKey_FieldName, relatedEntity = BerkeleyDBStatementAuthorityEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey contextUuidKey;

	public static final String authorUuidKey_FieldName = "authorUuidKey";
	@SecondaryKey(name = authorUuidKey_FieldName, relatedEntity = BerkeleyDBPersonEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey authorUuidKey;

	private Date creationDate;

	@Persistent(version = 0)
	public static class ContextFlagSecondaryKeyData
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private boolean flag;

		private ContextFlagSecondaryKeyData()
		{
		}

		private void setContextUuid(UUID contextUuid)
		{
			if (contextUuid == null)
			{
				this.contextUuidMostSigBits = 0;
				this.contextUuidLeastSigBits = 0;
			}
			else
			{
				this.contextUuidMostSigBits = contextUuid.getMostSignificantBits();
				this.contextUuidLeastSigBits = contextUuid.getLeastSignificantBits();
			}
		}

		private void setFlag(boolean flag)
		{
			this.flag = flag;
		}

		public ContextFlagSecondaryKeyData(UUID contextUuid, boolean flag)
		{
			setContextUuid(contextUuid);
			setFlag(flag);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + (flag ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContextFlagSecondaryKeyData other = (ContextFlagSecondaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
				return false;
			if (flag != other.flag)
				return false;
			return true;
		}
	}

	private boolean validSignature;

	private boolean signedDependencies;

	public static final String contextSignedDependenciesSecondaryKeyData_FieldName = "contextSignedDependenciesSecondaryKeyData";
	@SecondaryKey(name = contextSignedDependenciesSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final ContextFlagSecondaryKeyData contextSignedDependenciesSecondaryKeyData;

	private boolean signedProof;

	public static final String contextSignedProofSecondaryKeyData_FieldName = "contextSignedProofSecondaryKeyData";
	@SecondaryKey(name = contextSignedProofSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final ContextFlagSecondaryKeyData contextSignedProofSecondaryKeyData;

	public BerkeleyDBStatementAuthorityEntity()
	{
		this.contextSignedDependenciesSecondaryKeyData = new ContextFlagSecondaryKeyData();
		this.contextSignedProofSecondaryKeyData = new ContextFlagSecondaryKeyData();
	}

	private UUIDKey getStatementUuidKey()
	{
		return statementUuidKey;
	}

	private void setStatementUuidKey(UUIDKey statementUuidKey)
	{
		this.statementUuidKey = statementUuidKey;
		this.statementUuidKey_ = statementUuidKey;
	}

	private UUIDKey getContextUuidKey()
	{
		return contextUuidKey;
	}

	private void setContextUuidKey(UUIDKey contextUuidKey)
	{
		this.contextUuidKey = contextUuidKey;
	}

	private UUIDKey getAuthorUuidKey()
	{
		return authorUuidKey;
	}

	private void setAuthorUuidKey(UUIDKey authorUuidKey)
	{
		this.authorUuidKey = authorUuidKey;
	}

	@Override
	public UUID getStatementUuid()
	{
		return getStatementUuidKey().uuid();
	}

	@Override
	public void setStatementUuid(UUID uuid)
	{
		setStatementUuidKey(new UUIDKey(uuid));
	}

	@Override
	public UUID getContextUuid()
	{
		if (getContextUuidKey() == null)
			return null;
		else
			return getContextUuidKey().uuid();
	}

	@Override
	public void setContextUuid(UUID uuid)
	{
		if (uuid == null)
			setContextUuidKey(null);
		else
			setContextUuidKey(new UUIDKey(uuid));
		this.contextSignedDependenciesSecondaryKeyData.setContextUuid(uuid);
		this.contextSignedProofSecondaryKeyData.setContextUuid(uuid);
	}

	@Override
	public UUID getAuthorUuid()
	{
		return getAuthorUuidKey().uuid();
	}

	@Override
	public void setAuthorUuid(UUID uuid)
	{
		setAuthorUuidKey(new UUIDKey(uuid));
	}

	@Override
	public Date getCreationDate()
	{
		return creationDate;
	}

	@Override
	public void setCreationDate(Date date)
	{
		this.creationDate = date;
	}

	@Override
	public boolean isValidSignature()
	{
		return validSignature;
	}

	@Override
	public void setValidSignature(boolean validSignature)
	{
		this.validSignature = validSignature;
	}

	@Override
	public boolean isSignedDependencies()
	{
		return signedDependencies;
	}

	@Override
	public void setSignedDependencies(boolean signedDependencies)
	{
		this.signedDependencies = signedDependencies;
		this.contextSignedDependenciesSecondaryKeyData.setFlag(signedDependencies);
	}

	@Override
	public boolean isSignedProof()
	{
		return signedProof;
	}

	@Override
	public void setSignedProof(boolean signedProof)
	{
		this.signedProof = signedProof;
		this.contextSignedProofSecondaryKeyData.setFlag(signedProof);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorUuidKey == null) ? 0 : authorUuidKey.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((statementUuidKey == null) ? 0 : statementUuidKey.hashCode());
		result = prime * result + ((statementUuidKey_ == null) ? 0 : statementUuidKey_.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BerkeleyDBStatementAuthorityEntity other = (BerkeleyDBStatementAuthorityEntity) obj;
		if (authorUuidKey == null)
		{
			if (other.authorUuidKey != null)
				return false;
		}
		else if (!authorUuidKey.equals(other.authorUuidKey))
			return false;
		if (creationDate == null)
		{
			if (other.creationDate != null)
				return false;
		}
		else if (!creationDate.equals(other.creationDate))
			return false;
		if (statementUuidKey == null)
		{
			if (other.statementUuidKey != null)
				return false;
		}
		else if (!statementUuidKey.equals(other.statementUuidKey))
			return false;
		if (statementUuidKey_ == null)
		{
			if (other.statementUuidKey_ != null)
				return false;
		}
		else if (!statementUuidKey_.equals(other.statementUuidKey_))
			return false;
		return true;
	}

}
