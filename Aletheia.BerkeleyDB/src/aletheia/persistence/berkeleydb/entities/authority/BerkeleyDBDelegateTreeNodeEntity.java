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

import java.util.UUID;

import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.DelegateTreeNodeEntity;
import aletheia.security.model.MessageDigestData;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version = 0)
public abstract class BerkeleyDBDelegateTreeNodeEntity implements DelegateTreeNodeEntity
{

	@Persistent(version = 0)
	public static class PrimaryKeyData implements Comparable<PrimaryKeyData>
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private String prefixString;

		protected PrimaryKeyData()
		{

		}

		protected void setContextUuid(UUID contextUuid)
		{
			this.contextUuidMostSigBits = contextUuid.getMostSignificantBits();
			this.contextUuidLeastSigBits = contextUuid.getLeastSignificantBits();
		}

		protected UUID getContextUuid()
		{
			return new UUID(contextUuidMostSigBits, contextUuidLeastSigBits);
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

		public PrimaryKeyData(UUID contextUuid, Namespace prefix)
		{
			setContextUuid(contextUuid);
			setPrefix(prefix);
		}

		@Override
		public int compareTo(PrimaryKeyData o)
		{
			int c = 0;
			c = getContextUuid().compareTo(o.getContextUuid());
			if (c != 0)
				return c;
			c = getPrefix().compareTo(o.getPrefix());
			if (c != 0)
				return c;
			return c;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + ((prefixString == null) ? 0 : prefixString.hashCode());
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
			PrimaryKeyData other = (PrimaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
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

	};

	@PrimaryKey
	private final PrimaryKeyData primaryKeyData;

	public BerkeleyDBDelegateTreeNodeEntity()
	{
		this.primaryKeyData = new PrimaryKeyData();
	}

	public static final String contextUuidKey_FieldName = "contextUuidKey";
	@SecondaryKey(name = contextUuidKey_FieldName, relatedEntity = BerkeleyDBStatementAuthorityEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey contextUuidKey;

	private Namespace prefix;

	private MessageDigestData messageDigestData;

	@Override
	public UUID getStatementUuid()
	{
		return contextUuidKey.uuid();
	}

	@Override
	public void setStatementUuid(UUID contextUuid)
	{
		this.contextUuidKey = new UUIDKey(contextUuid);
		this.primaryKeyData.setContextUuid(contextUuid);
	}

	@Override
	public Namespace getPrefix()
	{
		return prefix;
	}

	public void setPrefix(Namespace prefix)
	{
		this.prefix = prefix;
		this.primaryKeyData.setPrefix(prefix);
	}

	@Override
	public MessageDigestData getMessageDigestData()
	{
		return messageDigestData;
	}

	@Override
	public void setMessageDigestData(MessageDigestData messageDigestData)
	{
		this.messageDigestData = messageDigestData;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextUuidKey == null) ? 0 : contextUuidKey.hashCode());
		result = prime * result + ((messageDigestData == null) ? 0 : messageDigestData.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((primaryKeyData == null) ? 0 : primaryKeyData.hashCode());
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
		BerkeleyDBDelegateTreeNodeEntity other = (BerkeleyDBDelegateTreeNodeEntity) obj;
		if (contextUuidKey == null)
		{
			if (other.contextUuidKey != null)
				return false;
		}
		else if (!contextUuidKey.equals(other.contextUuidKey))
			return false;
		if (messageDigestData == null)
		{
			if (other.messageDigestData != null)
				return false;
		}
		else if (!messageDigestData.equals(other.messageDigestData))
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
		return true;
	}

}
