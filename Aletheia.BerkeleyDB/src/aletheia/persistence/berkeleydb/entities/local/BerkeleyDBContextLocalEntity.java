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
package aletheia.persistence.berkeleydb.entities.local;

import java.util.UUID;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.local.ContextLocalEntity;

@Persistent(version = 0)
public class BerkeleyDBContextLocalEntity extends BerkeleyDBStatementLocalEntity implements ContextLocalEntity
{
	private boolean subscribeStatements;

	@Persistent(version = 0)
	public static class ContextSubscribeStatementsSecondaryKeyData
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private boolean subscribeStatements;

		protected ContextSubscribeStatementsSecondaryKeyData()
		{
		}

		public ContextSubscribeStatementsSecondaryKeyData(UUID contextUuid, boolean subscribeStatements)
		{
			setContextUuid(contextUuid);
			setSubscribeStatements(subscribeStatements);
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

		protected boolean isSubscribeStatements()
		{
			return subscribeStatements;
		}

		protected void setSubscribeStatements(boolean subscribeStatements)
		{
			this.subscribeStatements = subscribeStatements;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + (subscribeStatements ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			ContextSubscribeStatementsSecondaryKeyData other = (ContextSubscribeStatementsSecondaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
				return false;
			if (subscribeStatements != other.subscribeStatements)
				return false;
			return true;
		}

	}

	public static final String contextSubscribeStatementsSecondaryKeyData_FieldName = "contextSubscribeStatementsSecondaryKeyData";
	@SecondaryKey(name = contextSubscribeStatementsSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final ContextSubscribeStatementsSecondaryKeyData contextSubscribeStatementsSecondaryKeyData;

	public BerkeleyDBContextLocalEntity()
	{
		super();
		this.contextSubscribeStatementsSecondaryKeyData = new ContextSubscribeStatementsSecondaryKeyData();
	}

	@Override
	protected void setContextUuidKey(UUIDKey contextUuidKey)
	{
		super.setContextUuidKey(contextUuidKey);
		this.contextSubscribeStatementsSecondaryKeyData.setContextUuidKey(contextUuidKey);
	}

	@Override
	public boolean isSubscribeStatements()
	{
		return subscribeStatements;
	}

	@Override
	public void setSubscribeStatements(boolean subscribeStatements)
	{
		this.subscribeStatements = subscribeStatements;
		this.contextSubscribeStatementsSecondaryKeyData.setSubscribeStatements(subscribeStatements);
	}

}
