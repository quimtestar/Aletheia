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
package aletheia.persistence.berkeleydb.entities.local;

import java.util.UUID;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.entities.local.StatementLocalEntity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version = 0)
public class BerkeleyDBStatementLocalEntity implements StatementLocalEntity
{
	@PrimaryKey
	private UUIDKey statementUuidKey;

	public static final String statementUuidKey__FieldName = "statementUuidKey_";
	@SecondaryKey(name = statementUuidKey__FieldName, relatedEntity = BerkeleyDBStatementEntity.class, relate = Relationship.ONE_TO_ONE)
	private UUIDKey statementUuidKey_;

	public static final String contextUuidKey_FieldName = "contextUuidKey";
	@SecondaryKey(name = contextUuidKey_FieldName, relatedEntity = BerkeleyDBStatementLocalEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey contextUuidKey;

	private boolean subscribeProof;

	@Persistent(version = 0)
	public static class ContextSubscribeProofSecondaryKeyData
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private boolean subscribeProof;

		protected ContextSubscribeProofSecondaryKeyData()
		{
		}

		public ContextSubscribeProofSecondaryKeyData(UUID contextUuid, boolean subscribeProof)
		{
			setContextUuid(contextUuid);
			setSubscribeProof(subscribeProof);
		}

		protected UUID getContextUuid()
		{
			return new UUID(contextUuidMostSigBits, contextUuidLeastSigBits);
		}

		protected void setContextUuid(UUID contextUuid)
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

		protected void setContextUuidKey(UUIDKey contextUuidKey)
		{
			if (contextUuidKey == null)
			{
				this.contextUuidMostSigBits = 0;
				this.contextUuidLeastSigBits = 0;
			}
			else
			{
				this.contextUuidMostSigBits = contextUuidKey.getMostSigBits();
				this.contextUuidLeastSigBits = contextUuidKey.getLeastSigBits();
			}
		}

		protected boolean isSubscribeProof()
		{
			return subscribeProof;
		}

		protected void setSubscribeProof(boolean subscribeProof)
		{
			this.subscribeProof = subscribeProof;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + (subscribeProof ? 1231 : 1237);
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
			ContextSubscribeProofSecondaryKeyData other = (ContextSubscribeProofSecondaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
				return false;
			if (subscribeProof != other.subscribeProof)
				return false;
			return true;
		}

	}

	public static final String contextSubscribeProofSecondaryKeyData_FieldName = "contextSubscribeProofSecondaryKeyData";
	@SecondaryKey(name = contextSubscribeProofSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final ContextSubscribeProofSecondaryKeyData contextSubscribeProofSecondaryKeyData;

	public BerkeleyDBStatementLocalEntity()
	{
		super();
		this.contextSubscribeProofSecondaryKeyData = new ContextSubscribeProofSecondaryKeyData();
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

	protected void setContextUuidKey(UUIDKey contextUuidKey)
	{
		this.contextUuidKey = contextUuidKey;
		this.contextSubscribeProofSecondaryKeyData.setContextUuidKey(contextUuidKey);
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
		return getContextUuidKey().uuid();
	}

	@Override
	public void setContextUuid(UUID uuid)
	{
		if (uuid == null)
			setContextUuidKey(null);
		else
			setContextUuidKey(new UUIDKey(uuid));
	}

	@Override
	public boolean isSubscribeProof()
	{
		return subscribeProof;
	}

	@Override
	public void setSubscribeProof(boolean subscribeProof)
	{
		this.subscribeProof = subscribeProof;
		this.contextSubscribeProofSecondaryKeyData.setSubscribeProof(subscribeProof);
	}

}
