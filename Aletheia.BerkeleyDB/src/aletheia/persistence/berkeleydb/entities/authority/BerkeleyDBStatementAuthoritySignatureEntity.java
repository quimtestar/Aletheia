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

import aletheia.model.security.SignatureData;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.StatementAuthoritySignatureEntity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version = 5)
public class BerkeleyDBStatementAuthoritySignatureEntity implements StatementAuthoritySignatureEntity
{

	@Persistent(version = 0)
	public static class PrimaryKeyData
	{
		@KeyField(1)
		private long statementUuidMostSigBits;

		@KeyField(2)
		private long statementUuidLeastSigBits;

		@KeyField(3)
		private long authorizerUuidMostSigBits;

		@KeyField(4)
		private long authorizerUuidLeastSigBits;

		private PrimaryKeyData()
		{

		}

		public PrimaryKeyData(UUID statementUuid, UUID authorizerUuid)
		{
			setStatementUuid(statementUuid);
			setAuthorizerUuid(authorizerUuid);
		}

		public UUID getStatementUuid()
		{
			return new UUID(statementUuidMostSigBits, statementUuidLeastSigBits);
		}

		public void setStatementUuid(UUID statementUuid)
		{
			this.statementUuidMostSigBits = statementUuid.getMostSignificantBits();
			this.statementUuidLeastSigBits = statementUuid.getLeastSignificantBits();
		}

		public UUID getAuthorizerUuid()
		{
			return new UUID(authorizerUuidMostSigBits, authorizerUuidLeastSigBits);
		}

		public void setAuthorizerUuid(UUID authorizerUuid)
		{
			this.authorizerUuidMostSigBits = authorizerUuid.getMostSignificantBits();
			this.authorizerUuidLeastSigBits = authorizerUuid.getLeastSignificantBits();
		}
	};

	@PrimaryKey
	private final PrimaryKeyData primaryKey;

	public static final String statementUuidKey_FieldName = "statementUuidKey";
	@SecondaryKey(name = statementUuidKey_FieldName, relatedEntity = BerkeleyDBStatementAuthorityEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey statementUuidKey;

	public static final String authorizerUuidKey_FieldName = "authorizerUuidKey";
	@SecondaryKey(name = authorizerUuidKey_FieldName, relatedEntity = BerkeleyDBSignatoryEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey authorizerUuidKey;

	private Date signatureDate;

	@Persistent(version = 0)
	public static class StatementSignatureDateKeyData implements Comparable<StatementSignatureDateKeyData>
	{
		@KeyField(1)
		private long statementUuidMostSigBits;

		@KeyField(2)
		private long statementUuidLeastSigBits;

		@KeyField(3)
		private Date signatureDate;

		private StatementSignatureDateKeyData()
		{

		}

		public StatementSignatureDateKeyData(UUID statementUuid, Date signatureDate)
		{
			super();
			setStatementUuid(statementUuid);
			setSignatureDate(signatureDate);
		}

		public UUID getStatementUuid()
		{
			return new UUID(statementUuidMostSigBits, statementUuidLeastSigBits);
		}

		public void setStatementUuid(UUID statementUuid)
		{
			this.statementUuidMostSigBits = statementUuid.getMostSignificantBits();
			this.statementUuidLeastSigBits = statementUuid.getLeastSignificantBits();
		}

		private void setStatementUuidKey(UUIDKey statementUuidKey)
		{
			this.statementUuidMostSigBits = statementUuidKey.getMostSigBits();
			this.statementUuidLeastSigBits = statementUuidKey.getLeastSigBits();
		}

		public Date getSignatureDate()
		{
			return signatureDate;
		}

		public void setSignatureDate(Date signatureDate)
		{
			this.signatureDate = signatureDate;
		}

		@Override
		public int compareTo(StatementSignatureDateKeyData o)
		{
			int c;
			c = getStatementUuid().compareTo(o.getStatementUuid());
			if (c != 0)
				return c;
			c = getSignatureDate().compareTo(o.getSignatureDate());
			if (c != 0)
				return c;
			return c;
		}
	}

	public static final String statementSignatureDateKeyData_FieldName = "statementSignatureDateKeyData";
	@SecondaryKey(name = statementSignatureDateKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private StatementSignatureDateKeyData statementSignatureDateKeyData;

	private int signatureVersion;

	private SignatureData signatureData;

	private UUID signatureUuid;

	@Persistent(version = 0)
	public static class AuthorizerSignatureUuidKeyData
	{
		@KeyField(1)
		private long authorizerUuidMostSigBits;

		@KeyField(2)
		private long authorizerUuidLeastSigBits;

		@KeyField(3)
		private long signatureUuidMostSigBits;

		@KeyField(4)
		private long signatureUuidLeastSigBits;

		private AuthorizerSignatureUuidKeyData()
		{

		}

		public AuthorizerSignatureUuidKeyData(UUID authorizerUuid, UUID signatureUuid)
		{
			setAuthorizerUuid(authorizerUuid);
			setSignatureUuid(signatureUuid);
		}

		public UUID getAuthorizerUuid()
		{
			return new UUID(authorizerUuidMostSigBits, authorizerUuidLeastSigBits);
		}

		public void setAuthorizerUuid(UUID authorizerUuid)
		{
			this.authorizerUuidMostSigBits = authorizerUuid.getMostSignificantBits();
			this.authorizerUuidLeastSigBits = authorizerUuid.getLeastSignificantBits();
		}

		private void setAuthorizerUuidKey(UUIDKey authorizerUuidKey)
		{
			this.authorizerUuidMostSigBits = authorizerUuidKey.getMostSigBits();
			this.authorizerUuidLeastSigBits = authorizerUuidKey.getLeastSigBits();
		}

		public UUID getSignatureUuid()
		{
			return new UUID(signatureUuidMostSigBits, signatureUuidLeastSigBits);
		}

		public void setSignatureUuid(UUID signatureDataUuid)
		{
			this.signatureUuidMostSigBits = signatureDataUuid.getMostSignificantBits();
			this.signatureUuidLeastSigBits = signatureDataUuid.getLeastSignificantBits();
		}

	}

	public static final String authorizerSignatureUuidKeyData_FieldName = "authorizerSignatureUuidKeyData";
	@SecondaryKey(name = authorizerSignatureUuidKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private AuthorizerSignatureUuidKeyData authorizerSignatureUuidKeyData;

	private boolean valid;

	public BerkeleyDBStatementAuthoritySignatureEntity()
	{
		super();
		this.primaryKey = new PrimaryKeyData();
	}

	@Override
	public UUID getStatementUuid()
	{
		return statementUuidKey.uuid();
	}

	@Override
	public void setStatementUuid(UUID uuid)
	{
		UUIDKey uuidKey = new UUIDKey(uuid);
		this.statementUuidKey = uuidKey;
		this.primaryKey.setStatementUuid(uuid);
		updateStatementSignatureDateKeyData();
	}

	@Override
	public UUID getAuthorizerUuid()
	{
		return authorizerUuidKey.uuid();
	}

	@Override
	public void setAuthorizerUuid(UUID uuid)
	{
		UUIDKey uuidKey = new UUIDKey(uuid);
		this.authorizerUuidKey = uuidKey;
		this.primaryKey.setAuthorizerUuid(uuid);
		updateAuthorizerSignatureDataUuidKeyData();

	}

	@Override
	public Date getSignatureDate()
	{
		return signatureDate;
	}

	@Override
	public void setSignatureDate(Date signatureDate)
	{
		this.signatureDate = signatureDate;
		updateStatementSignatureDateKeyData();
	}

	public StatementSignatureDateKeyData getStatementSignatureDateKeyData()
	{
		return statementSignatureDateKeyData;
	}

	private void updateStatementSignatureDateKeyData()
	{
		if (this.statementUuidKey != null && this.signatureDate != null)
		{
			if (this.statementSignatureDateKeyData == null)
				this.statementSignatureDateKeyData = new StatementSignatureDateKeyData();
			this.statementSignatureDateKeyData.setStatementUuidKey(statementUuidKey);
			this.statementSignatureDateKeyData.setSignatureDate(signatureDate);
		}
		else
			this.statementSignatureDateKeyData = null;
	}

	@Override
	public int getSignatureVersion()
	{
		return signatureVersion;
	}

	@Override
	public void setSignatureVersion(int signatureVersion)
	{
		this.signatureVersion = signatureVersion;
	}

	@Override
	public SignatureData getSignatureData()
	{
		return signatureData;
	}

	@Override
	public void setSignatureData(SignatureData signatureData)
	{
		this.signatureData = signatureData;
	}

	@Override
	public UUID getSignatureUuid()
	{
		return signatureUuid;
	}

	@Override
	public void setSignatureUuid(UUID signatureDataUuid)
	{
		this.signatureUuid = signatureDataUuid;
		updateAuthorizerSignatureDataUuidKeyData();
	}

	private void updateAuthorizerSignatureDataUuidKeyData()
	{
		if (this.authorizerUuidKey != null && this.signatureUuid != null)
		{
			if (this.authorizerSignatureUuidKeyData == null)
				this.authorizerSignatureUuidKeyData = new AuthorizerSignatureUuidKeyData();
			this.authorizerSignatureUuidKeyData.setAuthorizerUuidKey(authorizerUuidKey);
			this.authorizerSignatureUuidKeyData.setSignatureUuid(signatureUuid);
		}
		else
			this.authorizerSignatureUuidKeyData = null;
	}

	@Override
	public boolean isValid()
	{
		return valid;
	}

	@Override
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizerUuidKey == null) ? 0 : authorizerUuidKey.hashCode());
		result = prime * result + ((signatureData == null) ? 0 : signatureData.hashCode());
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
		result = prime * result + ((signatureUuid == null) ? 0 : signatureUuid.hashCode());
		result = prime * result + signatureVersion;
		result = prime * result + ((statementUuidKey == null) ? 0 : statementUuidKey.hashCode());
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
		BerkeleyDBStatementAuthoritySignatureEntity other = (BerkeleyDBStatementAuthoritySignatureEntity) obj;
		if (authorizerUuidKey == null)
		{
			if (other.authorizerUuidKey != null)
				return false;
		}
		else if (!authorizerUuidKey.equals(other.authorizerUuidKey))
			return false;
		if (signatureData == null)
		{
			if (other.signatureData != null)
				return false;
		}
		else if (!signatureData.equals(other.signatureData))
			return false;
		if (signatureDate == null)
		{
			if (other.signatureDate != null)
				return false;
		}
		else if (!signatureDate.equals(other.signatureDate))
			return false;
		if (signatureUuid == null)
		{
			if (other.signatureUuid != null)
				return false;
		}
		else if (!signatureUuid.equals(other.signatureUuid))
			return false;
		if (signatureVersion != other.signatureVersion)
			return false;
		if (statementUuidKey == null)
		{
			if (other.statementUuidKey != null)
				return false;
		}
		else if (!statementUuidKey.equals(other.statementUuidKey))
			return false;
		return true;
	}

}
