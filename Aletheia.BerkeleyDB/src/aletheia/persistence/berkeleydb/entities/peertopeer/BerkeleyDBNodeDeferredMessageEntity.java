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
package aletheia.persistence.berkeleydb.entities.peertopeer;

import java.util.Date;
import java.util.UUID;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.peertopeer.NodeDeferredMessageEntity;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class BerkeleyDBNodeDeferredMessageEntity implements NodeDeferredMessageEntity
{
	private UUIDKey nodeUuidKey;

	public final static String deferredMessageUuidKey_FieldName = "deferredMessageUuidKey";
	@SecondaryKey(name = deferredMessageUuidKey_FieldName, relatedEntity = BerkeleyDBDeferredMessageEntity.class, relate = Relationship.MANY_TO_ONE)
	private UUIDKey deferredMessageUuidKey;

	private static class UUIDAdapter
	{
		final long mostSigBits;
		final long leastSigBits;

		private UUIDAdapter(long mostSigBits, long leastSigBits)
		{
			super();
			this.mostSigBits = mostSigBits;
			this.leastSigBits = leastSigBits;
		}

		UUIDAdapter(UUID uuid)
		{
			if (uuid == null)
			{
				mostSigBits = 0;
				leastSigBits = 0;
			}
			else
			{
				mostSigBits = uuid.getMostSignificantBits();
				leastSigBits = uuid.getLeastSignificantBits();
			}
		}

		UUIDAdapter(UUIDKey uuidKey)
		{
			if (uuidKey == null)
			{
				mostSigBits = 0;
				leastSigBits = 0;
			}
			else
			{
				mostSigBits = uuidKey.getMostSigBits();
				leastSigBits = uuidKey.getLeastSigBits();
			}
		}

		private UUID uuid()
		{
			if ((mostSigBits == 0) && (leastSigBits == 0))
				return null;
			else
				return new UUID(mostSigBits, leastSigBits);
		}

		@SuppressWarnings("unused")
		private UUIDKey uuidKey()
		{
			if ((mostSigBits == 0) && (leastSigBits == 0))
				return null;
			else
				return new UUIDKey(mostSigBits, leastSigBits);
		}

	}

	@Persistent
	public static class PrimaryKeyData
	{
		@KeyField(1)
		private long nodeUuidMostSigBits;

		@KeyField(2)
		private long nodeUuidLeastSigBits;

		@KeyField(3)
		private long deferredMessageUuidMostSigBits;

		@KeyField(4)
		private long deferredMessageUuidLeastSigBits;

		private PrimaryKeyData()
		{
		}

		public PrimaryKeyData(UUIDKey nodeUuidKey, UUIDKey deferredMessageUuidKey)
		{
			setNodeUuidKey(nodeUuidKey);
			setDeferredMessageUuidKey(deferredMessageUuidKey);
		}

		public PrimaryKeyData(UUID nodeUuid, UUID deferredMessageUuid)
		{
			setNodeUuid(nodeUuid);
			setDeferredMessageUuid(deferredMessageUuid);
		}

		private void setNodeUuidAdapter(UUIDAdapter nodeUuidAdapter)
		{
			this.nodeUuidMostSigBits = nodeUuidAdapter.mostSigBits;
			this.nodeUuidLeastSigBits = nodeUuidAdapter.leastSigBits;
		}

		private void setNodeUuid(UUID nodeUuid)
		{
			setNodeUuidAdapter(new UUIDAdapter(nodeUuid));
		}

		private void setNodeUuidKey(UUIDKey nodeUuidKey)
		{
			setNodeUuidAdapter(new UUIDAdapter(nodeUuidKey));
		}

		private void setDeferredMessageUuidAdapter(UUIDAdapter deferredMessageUuidAdapter)
		{
			this.deferredMessageUuidMostSigBits = deferredMessageUuidAdapter.mostSigBits;
			this.deferredMessageUuidLeastSigBits = deferredMessageUuidAdapter.leastSigBits;
		}

		private void setDeferredMessageUuid(UUID deferredMessageUuid)
		{
			setDeferredMessageUuidAdapter(new UUIDAdapter(deferredMessageUuid));
		}

		private void setDeferredMessageUuidKey(UUIDKey deferredMessageUuidKey)
		{
			setDeferredMessageUuidAdapter(new UUIDAdapter(deferredMessageUuidKey));
		}

	}

	@PrimaryKey
	private final PrimaryKeyData primaryKeyData;

	private UUIDKey deferredMessageRecipientUuidKey;

	@Persistent
	public static class NodeDeferredMessageRecipientSecondaryKeyData
	{
		@KeyField(1)
		private long nodeUuidMostSigBits;

		@KeyField(2)
		private long nodeUuidLeastSigBits;

		@KeyField(3)
		private long deferredMessageRecipientUuidMostSigBits;

		@KeyField(4)
		private long deferredMessageRecipientUuidLeastSigBits;

		private NodeDeferredMessageRecipientSecondaryKeyData()
		{
		}

		public NodeDeferredMessageRecipientSecondaryKeyData(UUID nodeUuid, UUID deferredMessageRecipientUuid)
		{
			setNodeUuid(nodeUuid);
			setDeferredMessageRecipientUuid(deferredMessageRecipientUuid);
		}

		private void setNodeUuidAdapter(UUIDAdapter nodeUuidAdapter)
		{
			this.nodeUuidMostSigBits = nodeUuidAdapter.mostSigBits;
			this.nodeUuidLeastSigBits = nodeUuidAdapter.leastSigBits;
		}

		private void setNodeUuid(UUID nodeUuid)
		{
			setNodeUuidAdapter(new UUIDAdapter(nodeUuid));
		}

		private UUIDAdapter getNodeUuidAdapter()
		{
			return new UUIDAdapter(nodeUuidMostSigBits, nodeUuidLeastSigBits);
		}

		public UUID getNodeUuid()
		{
			return getNodeUuidAdapter().uuid();
		}

		private void setDeferredMessageRecipientUuidAdapter(UUIDAdapter deferredMessageRecipientUuidAdapter)
		{
			this.deferredMessageRecipientUuidMostSigBits = deferredMessageRecipientUuidAdapter.mostSigBits;
			this.deferredMessageRecipientUuidLeastSigBits = deferredMessageRecipientUuidAdapter.leastSigBits;
		}

		private void setDeferredMessageRecipientUuid(UUID deferredMessageRecipientUuid)
		{
			setDeferredMessageRecipientUuidAdapter(new UUIDAdapter(deferredMessageRecipientUuid));
		}

		private UUIDAdapter getDeferredMessageRecipientUuidAdapter()
		{
			return new UUIDAdapter(deferredMessageRecipientUuidMostSigBits, deferredMessageRecipientUuidLeastSigBits);
		}

		public UUID getDeferredMessageRecipientUuid()
		{
			return getDeferredMessageRecipientUuidAdapter().uuid();
		}

		private static final UUID minUuid = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

		public static NodeDeferredMessageRecipientSecondaryKeyData min(UUID nodeUuid)
		{
			return new NodeDeferredMessageRecipientSecondaryKeyData(nodeUuid, minUuid);
		}

		private static final UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);

		public static NodeDeferredMessageRecipientSecondaryKeyData max(UUID nodeUuid)
		{
			return new NodeDeferredMessageRecipientSecondaryKeyData(nodeUuid, maxUuid);
		}

	}

	public final static String nodeDeferredMessageRecipientSecondaryKeyData_FieldName = "nodeDeferredMessageRecipientSecondaryKeyData";
	@SecondaryKey(name = nodeDeferredMessageRecipientSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final NodeDeferredMessageRecipientSecondaryKeyData nodeDeferredMessageRecipientSecondaryKeyData;

	private Date deferredMessageDate;

	@Persistent
	public static class NodeDeferredMessageRecipientDateSecondaryKeyData
	{
		@KeyField(1)
		private long nodeUuidMostSigBits;

		@KeyField(2)
		private long nodeUuidLeastSigBits;

		@KeyField(3)
		private long deferredMessageRecipientUuidMostSigBits;

		@KeyField(4)
		private long deferredMessageRecipientUuidLeastSigBits;

		@KeyField(5)
		private Date deferredMessageDate;

		private NodeDeferredMessageRecipientDateSecondaryKeyData()
		{
		}

		public NodeDeferredMessageRecipientDateSecondaryKeyData(UUID nodeUuid, UUID deferredMessageRecipientUuid, Date deferredMessageDate)
		{
			setNodeUuid(nodeUuid);
			setDeferredMessageRecipientUuid(deferredMessageRecipientUuid);
			setDeferredMessageDate(deferredMessageDate);
		}

		private void setNodeUuidAdapter(UUIDAdapter nodeUuidAdapter)
		{
			this.nodeUuidMostSigBits = nodeUuidAdapter.mostSigBits;
			this.nodeUuidLeastSigBits = nodeUuidAdapter.leastSigBits;
		}

		private void setNodeUuid(UUID nodeUuid)
		{
			setNodeUuidAdapter(new UUIDAdapter(nodeUuid));
		}

		private UUIDAdapter getNodeUuidAdapter()
		{
			return new UUIDAdapter(nodeUuidMostSigBits, nodeUuidLeastSigBits);
		}

		public UUID getNodeUuid()
		{
			return getNodeUuidAdapter().uuid();
		}

		private void setDeferredMessageRecipientUuidAdapter(UUIDAdapter deferredMessageRecipientUuidAdapter)
		{
			this.deferredMessageRecipientUuidMostSigBits = deferredMessageRecipientUuidAdapter.mostSigBits;
			this.deferredMessageRecipientUuidLeastSigBits = deferredMessageRecipientUuidAdapter.leastSigBits;
		}

		private void setDeferredMessageRecipientUuid(UUID deferredMessageRecipientUuid)
		{
			setDeferredMessageRecipientUuidAdapter(new UUIDAdapter(deferredMessageRecipientUuid));
		}

		private UUIDAdapter getDeferredMessageRecipientUuidAdapter()
		{
			return new UUIDAdapter(deferredMessageRecipientUuidMostSigBits, deferredMessageRecipientUuidLeastSigBits);
		}

		public UUID getDeferredMessageRecipientUuid()
		{
			return getDeferredMessageRecipientUuidAdapter().uuid();
		}

		private void setDeferredMessageDate(Date deferredMessageDate)
		{
			this.deferredMessageDate = deferredMessageDate;
		}

		private static final Date minDate = new Date(Long.MIN_VALUE);

		public static NodeDeferredMessageRecipientDateSecondaryKeyData min(UUID nodeUuid, UUID deferredMessageRecipientUuid)
		{
			return new NodeDeferredMessageRecipientDateSecondaryKeyData(nodeUuid, deferredMessageRecipientUuid, minDate);
		}

		private static final Date maxDate = new Date(Long.MAX_VALUE);

		public static NodeDeferredMessageRecipientDateSecondaryKeyData max(UUID nodeUuid, UUID deferredMessageRecipientUuid)
		{
			return new NodeDeferredMessageRecipientDateSecondaryKeyData(nodeUuid, deferredMessageRecipientUuid, maxDate);
		}

	}

	public final static String nodeDeferredMessageRecipientDateSecondaryKeyData_FieldName = "nodeDeferredMessageRecipientDateSecondaryKeyData";
	@SecondaryKey(name = nodeDeferredMessageRecipientDateSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final NodeDeferredMessageRecipientDateSecondaryKeyData nodeDeferredMessageRecipientDateSecondaryKeyData;

	public BerkeleyDBNodeDeferredMessageEntity()
	{
		this.primaryKeyData = new PrimaryKeyData();
		this.nodeDeferredMessageRecipientSecondaryKeyData = new NodeDeferredMessageRecipientSecondaryKeyData();
		this.nodeDeferredMessageRecipientDateSecondaryKeyData = new NodeDeferredMessageRecipientDateSecondaryKeyData();
	}

	@Override
	public UUID getNodeUuid()
	{
		return nodeUuidKey.uuid();
	}

	@Override
	public void setNodeUuid(UUID nodeUuid)
	{
		this.nodeUuidKey = new UUIDKey(nodeUuid);
		primaryKeyData.setNodeUuid(nodeUuid);
		nodeDeferredMessageRecipientSecondaryKeyData.setNodeUuid(nodeUuid);
		nodeDeferredMessageRecipientDateSecondaryKeyData.setNodeUuid(nodeUuid);
	}

	@Override
	public UUID getDeferredMessageUuid()
	{
		return deferredMessageUuidKey.uuid();
	}

	@Override
	public void setDeferredMessageUuid(UUID deferredMessageUuid)
	{
		this.deferredMessageUuidKey = new UUIDKey(deferredMessageUuid);
		primaryKeyData.setDeferredMessageUuid(deferredMessageUuid);
	}

	@Override
	public UUID getDeferredMessageRecipientUuid()
	{
		return deferredMessageRecipientUuidKey.uuid();
	}

	@Override
	public void setDeferredMessageRecipientUuid(UUID deferredMessageRecipientUuid)
	{
		this.deferredMessageRecipientUuidKey = new UUIDKey(deferredMessageRecipientUuid);
		nodeDeferredMessageRecipientSecondaryKeyData.setDeferredMessageRecipientUuid(deferredMessageRecipientUuid);
		nodeDeferredMessageRecipientDateSecondaryKeyData.setDeferredMessageRecipientUuid(deferredMessageRecipientUuid);
	}

	@Override
	public Date getDeferredMessageDate()
	{
		return deferredMessageDate;
	}

	@Override
	public void setDeferredMessageDate(Date deferredMessageDate)
	{
		this.deferredMessageDate = deferredMessageDate;
		nodeDeferredMessageRecipientDateSecondaryKeyData.setDeferredMessageDate(deferredMessageDate);
	}

	public PrimaryKeyData getPrimaryKeyData()
	{
		return primaryKeyData;
	}

	public NodeDeferredMessageRecipientSecondaryKeyData getNodeDeferredMessageRecipientSecondaryKeyData()
	{
		return nodeDeferredMessageRecipientSecondaryKeyData;
	}

	public NodeDeferredMessageRecipientDateSecondaryKeyData getNodeDeferredMessageRecipientDateSecondaryKeyData()
	{
		return nodeDeferredMessageRecipientDateSecondaryKeyData;
	}

}
