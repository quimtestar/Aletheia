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
package aletheia.persistence.berkeleydb.entities.authority;

import java.util.Date;
import java.util.TreeSet;
import java.util.UUID;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.DelegateAuthorizerEntity;
import aletheia.security.model.SignatureData;

@Entity(version = 3)
public class BerkeleyDBDelegateAuthorizerEntity implements DelegateAuthorizerEntity
{

	@Persistent(version = 0)
	public static class PrimaryKeyData implements Comparable<PrimaryKeyData>
	{
		@KeyField(1)
		private long statementUuidMostSigBits;

		@KeyField(2)
		private long statementUuidLeastSigBits;

		@KeyField(3)
		private String prefixString;

		@KeyField(4)
		private long delegateUuidMostSigBits;

		@KeyField(5)
		private long delegateUuidLeastSigBits;

		protected PrimaryKeyData()
		{

		}

		protected void setStatementUuid(UUID statementUuid)
		{
			this.statementUuidMostSigBits = statementUuid.getMostSignificantBits();
			this.statementUuidLeastSigBits = statementUuid.getLeastSignificantBits();
		}

		protected UUID getStatementUuid()
		{
			return new UUID(statementUuidMostSigBits, statementUuidLeastSigBits);
		}

		protected void setPrefix(Namespace prefix)
		{
			this.prefixString = prefix.qualifiedName();
		}

		protected Namespace getPrefix()
		{
			try
			{
				return Namespace.parse(prefixString);
			}
			catch (InvalidNameException e)
			{
				throw new RuntimeException(e);
			}
		}

		protected void setDelegateUuid(UUID delegateUuid)
		{
			this.delegateUuidMostSigBits = delegateUuid.getMostSignificantBits();
			this.delegateUuidLeastSigBits = delegateUuid.getLeastSignificantBits();
		}

		protected UUID getDelegateUuid()
		{
			return new UUID(delegateUuidMostSigBits, delegateUuidLeastSigBits);
		}

		public PrimaryKeyData(UUID statementUuid, Namespace prefix, UUID delegateUuid)
		{
			setStatementUuid(statementUuid);
			setPrefix(prefix);
			setDelegateUuid(delegateUuid);
		}

		@Override
		public int compareTo(PrimaryKeyData o)
		{
			int c = 0;
			c = getStatementUuid().compareTo(o.getStatementUuid());
			if (c != 0)
				return c;
			c = getPrefix().compareTo(o.getPrefix());
			if (c != 0)
				return c;
			c = getDelegateUuid().compareTo(o.getDelegateUuid());
			if (c != 0)
				return c;
			return c;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (statementUuidLeastSigBits ^ (statementUuidLeastSigBits >>> 32));
			result = prime * result + (int) (statementUuidMostSigBits ^ (statementUuidMostSigBits >>> 32));
			result = prime * result + (int) (delegateUuidLeastSigBits ^ (delegateUuidLeastSigBits >>> 32));
			result = prime * result + (int) (delegateUuidMostSigBits ^ (delegateUuidMostSigBits >>> 32));
			result = prime * result + ((prefixString == null) ? 0 : prefixString.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			PrimaryKeyData other = (PrimaryKeyData) obj;
			if ((statementUuidLeastSigBits != other.statementUuidLeastSigBits) || (statementUuidMostSigBits != other.statementUuidMostSigBits)
					|| (delegateUuidLeastSigBits != other.delegateUuidLeastSigBits) || (delegateUuidMostSigBits != other.delegateUuidMostSigBits))
				return false;
			if (prefixString == null)
			{
				if (other.prefixString != null)
					return false;
			}
			else if (!prefixString.equals(other.prefixString))
				return false;
			return true;
		}

	}

	@PrimaryKey
	private final PrimaryKeyData primaryKeyData;

	public static final String delegateTreeNodePrimaryKeyData_FieldName = "delegateTreeNodePrimaryKeyData";
	@SecondaryKey(name = delegateTreeNodePrimaryKeyData_FieldName, relatedEntity = BerkeleyDBDelegateTreeNodeEntity.class, relate = Relationship.MANY_TO_ONE)
	private final BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData delegateTreeNodePrimaryKeyData;

	public static final String statementUuidKey_FieldName = "statementUuidKey";
	@SecondaryKey(name = statementUuidKey_FieldName, relatedEntity = BerkeleyDBStatementAuthorityEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey statementUuidKey;

	private Namespace prefix;

	public static final String delegateUuidKey_FieldName = "delegateUuidKey";
	@SecondaryKey(name = delegateUuidKey_FieldName, relatedEntity = BerkeleyDBPersonEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey delegateUuidKey;

	public static final String authorizerUuidKey_FieldName = "authorizerUuidKey";
	@SecondaryKey(name = authorizerUuidKey_FieldName, relatedEntity = BerkeleyDBSignatoryEntity.class, relate = Relationship.ONE_TO_ONE)
	private UUIDKey authorizerUuidKey;

	@Persistent(version = 0)
	public static class StatementAuthorizerKeyData
	{
		@KeyField(1)
		private long statementUuidMostSigBits;

		@KeyField(2)
		private long statementUuidLeastSigBits;

		@KeyField(3)
		private String prefixString;

		@KeyField(4)
		private long authorizerUuidMostSigBits;

		@KeyField(5)
		private long authorizerUuidLeastSigBits;

		protected StatementAuthorizerKeyData()
		{
		}

		protected void setStatementUuid(UUID statementUuid)
		{
			this.statementUuidMostSigBits = statementUuid.getMostSignificantBits();
			this.statementUuidLeastSigBits = statementUuid.getLeastSignificantBits();
		}

		protected UUID getStatementUuid()
		{
			return new UUID(statementUuidMostSigBits, statementUuidLeastSigBits);
		}

		protected void setPrefix(Namespace prefix)
		{
			this.prefixString = prefix.qualifiedName();
		}

		protected Namespace getPrefix()
		{
			try
			{
				return Namespace.parse(prefixString);
			}
			catch (InvalidNameException e)
			{
				throw new RuntimeException(e);
			}
		}

		protected void setAuthorizerUuid(UUID authorizerUuid)
		{
			this.authorizerUuidMostSigBits = authorizerUuid.getMostSignificantBits();
			this.authorizerUuidLeastSigBits = authorizerUuid.getLeastSignificantBits();
		}

		protected UUID getAuthorizerUuid()
		{
			return new UUID(authorizerUuidMostSigBits, authorizerUuidLeastSigBits);
		}

		public StatementAuthorizerKeyData(UUID contextUuid, Namespace prefix, UUID authorizerUuid)
		{
			setStatementUuid(contextUuid);
			setPrefix(prefix);
			setAuthorizerUuid(authorizerUuid);
		}

		public static StatementAuthorizerKeyData first(UUID contextUuid, Namespace prefix)
		{
			StatementAuthorizerKeyData first = new StatementAuthorizerKeyData();
			first.setStatementUuid(contextUuid);
			first.setPrefix(prefix);
			first.authorizerUuidMostSigBits = Long.MIN_VALUE;
			first.authorizerUuidLeastSigBits = Long.MIN_VALUE;
			return first;
		}

		public static StatementAuthorizerKeyData last(UUID contextUuid, Namespace prefix)
		{
			StatementAuthorizerKeyData last = new StatementAuthorizerKeyData();
			last.setStatementUuid(contextUuid);
			last.setPrefix(prefix);
			last.authorizerUuidMostSigBits = Long.MAX_VALUE;
			last.authorizerUuidLeastSigBits = Long.MAX_VALUE;
			return last;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (authorizerUuidLeastSigBits ^ (authorizerUuidLeastSigBits >>> 32));
			result = prime * result + (int) (authorizerUuidMostSigBits ^ (authorizerUuidMostSigBits >>> 32));
			result = prime * result + (int) (statementUuidLeastSigBits ^ (statementUuidLeastSigBits >>> 32));
			result = prime * result + (int) (statementUuidMostSigBits ^ (statementUuidMostSigBits >>> 32));
			result = prime * result + ((prefixString == null) ? 0 : prefixString.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			StatementAuthorizerKeyData other = (StatementAuthorizerKeyData) obj;
			if ((authorizerUuidLeastSigBits != other.authorizerUuidLeastSigBits) || (authorizerUuidMostSigBits != other.authorizerUuidMostSigBits)
					|| (statementUuidLeastSigBits != other.statementUuidLeastSigBits) || (statementUuidMostSigBits != other.statementUuidMostSigBits))
				return false;
			if (prefixString == null)
			{
				if (other.prefixString != null)
					return false;
			}
			else if (!prefixString.equals(other.prefixString))
				return false;
			return true;
		}

	}

	public static final String statementAuthorizerKeyData_FieldName = "statementAuthorizerKeyData";
	@SecondaryKey(name = statementAuthorizerKeyData_FieldName, relate = Relationship.ONE_TO_ONE)
	private StatementAuthorizerKeyData statementAuthorizerKeyData;

	private final TreeSet<UUID> revokedSignatureUuids;

	private Date signatureDate;

	private int signatureVersion;

	private SignatureData signatureData;

	public BerkeleyDBDelegateAuthorizerEntity()
	{
		this.primaryKeyData = new PrimaryKeyData();
		this.delegateTreeNodePrimaryKeyData = new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData();
		this.statementAuthorizerKeyData = null;
		this.revokedSignatureUuids = new TreeSet<>();
	}

	@Override
	public UUID getStatementUuid()
	{
		return statementUuidKey.uuid();
	}

	@Override
	public void setStatementUuid(UUID statementUuid)
	{
		this.statementUuidKey = new UUIDKey(statementUuid);
		this.primaryKeyData.setStatementUuid(statementUuid);
		this.delegateTreeNodePrimaryKeyData.setContextUuid(statementUuid);
		if (this.statementAuthorizerKeyData != null)
			this.statementAuthorizerKeyData.setStatementUuid(statementUuid);
	}

	@Override
	public Namespace getPrefix()
	{
		return prefix;
	}

	@Override
	public void setPrefix(Namespace prefix)
	{
		this.prefix = prefix;
		this.primaryKeyData.setPrefix(prefix);
		this.delegateTreeNodePrimaryKeyData.setPrefix(prefix);
		if (this.statementAuthorizerKeyData != null)
			this.statementAuthorizerKeyData.setPrefix(prefix);
	}

	@Override
	public UUID getDelegateUuid()
	{
		return delegateUuidKey.uuid();
	}

	@Override
	public void setDelegateUuid(UUID delegateUuid)
	{
		this.delegateUuidKey = new UUIDKey(delegateUuid);
		this.primaryKeyData.setDelegateUuid(delegateUuid);
	}

	@Override
	public UUID getAuthorizerUuid()
	{
		if (authorizerUuidKey == null)
			return null;
		return authorizerUuidKey.uuid();
	}

	@Override
	public void setAuthorizerUuid(UUID authorizerUuid)
	{
		if (authorizerUuid == null)
		{
			this.authorizerUuidKey = null;
			this.statementAuthorizerKeyData = null;
		}
		else
		{
			this.authorizerUuidKey = new UUIDKey(authorizerUuid);
			if (this.statementAuthorizerKeyData == null)
			{
				this.statementAuthorizerKeyData = new StatementAuthorizerKeyData();
				this.statementAuthorizerKeyData.setStatementUuid(getStatementUuid());
				this.statementAuthorizerKeyData.setPrefix(getPrefix());
			}
			this.statementAuthorizerKeyData.setAuthorizerUuid(authorizerUuid);
		}
	}

	public StatementAuthorizerKeyData getContextAuthorizerKeyData()
	{
		return statementAuthorizerKeyData;
	}

	@Override
	public TreeSet<UUID> getRevokedSignatureUuids()
	{
		return revokedSignatureUuids;
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorizerUuidKey == null) ? 0 : authorizerUuidKey.hashCode());
		result = prime * result + ((delegateTreeNodePrimaryKeyData == null) ? 0 : delegateTreeNodePrimaryKeyData.hashCode());
		result = prime * result + ((delegateUuidKey == null) ? 0 : delegateUuidKey.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((primaryKeyData == null) ? 0 : primaryKeyData.hashCode());
		result = prime * result + ((revokedSignatureUuids == null) ? 0 : revokedSignatureUuids.hashCode());
		result = prime * result + ((signatureData == null) ? 0 : signatureData.hashCode());
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
		result = prime * result + signatureVersion;
		result = prime * result + ((statementAuthorizerKeyData == null) ? 0 : statementAuthorizerKeyData.hashCode());
		result = prime * result + ((statementUuidKey == null) ? 0 : statementUuidKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		BerkeleyDBDelegateAuthorizerEntity other = (BerkeleyDBDelegateAuthorizerEntity) obj;
		if (authorizerUuidKey == null)
		{
			if (other.authorizerUuidKey != null)
				return false;
		}
		else if (!authorizerUuidKey.equals(other.authorizerUuidKey))
			return false;
		if (delegateTreeNodePrimaryKeyData == null)
		{
			if (other.delegateTreeNodePrimaryKeyData != null)
				return false;
		}
		else if (!delegateTreeNodePrimaryKeyData.equals(other.delegateTreeNodePrimaryKeyData))
			return false;
		if (delegateUuidKey == null)
		{
			if (other.delegateUuidKey != null)
				return false;
		}
		else if (!delegateUuidKey.equals(other.delegateUuidKey))
			return false;
		if (prefix == null)
		{
			if (other.prefix != null)
				return false;
		}
		else if (!prefix.equals(other.prefix))
			return false;
		if (primaryKeyData == null)
		{
			if (other.primaryKeyData != null)
				return false;
		}
		else if (!primaryKeyData.equals(other.primaryKeyData))
			return false;
		if (revokedSignatureUuids == null)
		{
			if (other.revokedSignatureUuids != null)
				return false;
		}
		else if (!revokedSignatureUuids.equals(other.revokedSignatureUuids))
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
		if (signatureVersion != other.signatureVersion)
			return false;
		if (statementAuthorizerKeyData == null)
		{
			if (other.statementAuthorizerKeyData != null)
				return false;
		}
		else if (!statementAuthorizerKeyData.equals(other.statementAuthorizerKeyData))
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
