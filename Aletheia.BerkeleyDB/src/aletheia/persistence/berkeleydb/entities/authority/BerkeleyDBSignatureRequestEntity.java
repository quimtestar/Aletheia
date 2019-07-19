/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.SignatureRequestEntity;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version = 3)
public abstract class BerkeleyDBSignatureRequestEntity implements SignatureRequestEntity
{
	@PrimaryKey
	private UUIDKey uuidKey;

	public final static String creationDate_FieldName = "creationDate";
	@SecondaryKey(name = creationDate_FieldName, relate = Relationship.MANY_TO_ONE)
	private Date creationDate;

	static class UUIDAdapter
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

		private UUIDKey uuidKey()
		{
			if ((mostSigBits == 0) && (leastSigBits == 0))
				return null;
			else
				return new UUIDKey(mostSigBits, leastSigBits);
		}

	}

	@Persistent(version = 0)
	public static class ContextCreationDateSecondaryKeyData
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private Date creationDate;

		private ContextCreationDateSecondaryKeyData()
		{
		}

		private void setContextUuidAdapter(UUIDAdapter uuidAdapter)
		{
			this.contextUuidMostSigBits = uuidAdapter.mostSigBits;
			this.contextUuidLeastSigBits = uuidAdapter.leastSigBits;
		}

		private void setContextUuid(UUID contextUuid)
		{
			setContextUuidAdapter(new UUIDAdapter(contextUuid));
		}

		private void setContextUuidKey(UUIDKey contextUuidKey)
		{
			setContextUuidAdapter(new UUIDAdapter(contextUuidKey));
		}

		private void setCreationDate(Date creationDate)
		{
			this.creationDate = creationDate;
		}

		public ContextCreationDateSecondaryKeyData(UUID contextUuid, Date creationDate)
		{
			setContextUuid(contextUuid);
			setCreationDate(creationDate);
		}

		private static final Date minDate = new Date(Long.MIN_VALUE);

		public static ContextCreationDateSecondaryKeyData min(UUID contextUuid)
		{
			return new ContextCreationDateSecondaryKeyData(contextUuid, minDate);
		}

		private static final Date maxDate = new Date(Long.MAX_VALUE);

		public static ContextCreationDateSecondaryKeyData max(UUID contextUuid)
		{
			return new ContextCreationDateSecondaryKeyData(contextUuid, maxDate);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
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
			ContextCreationDateSecondaryKeyData other = (ContextCreationDateSecondaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
				return false;
			if (creationDate == null)
			{
				if (other.creationDate != null)
					return false;
			}
			else if (!creationDate.equals(other.creationDate))
				return false;
			return true;
		}
	}

	public final static String contextCreationDateSecondaryKeyData_FieldName = "contextCreationDateSecondaryKeyData";
	@SecondaryKey(name = contextCreationDateSecondaryKeyData_FieldName, relate = Relationship.MANY_TO_ONE)
	private final ContextCreationDateSecondaryKeyData contextCreationDateSecondaryKeyData;

	@Persistent(version = 0)
	public static class ContextSubContextSecondaryKeyData
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private long subContextUuidMostSigBits;

		@KeyField(4)
		private long subContextUuidLeastSigBits;

		@SuppressWarnings("unused")
		private ContextSubContextSecondaryKeyData()
		{
		}

		private UUIDAdapter getContextUuidAdapter()
		{
			return new UUIDAdapter(contextUuidLeastSigBits, contextUuidLeastSigBits);
		}

		@SuppressWarnings("unused")
		private UUID getContextUuid()
		{
			return getContextUuidAdapter().uuid();
		}

		@SuppressWarnings("unused")
		private UUIDKey getContextUuidKey()
		{
			return getContextUuidAdapter().uuidKey();
		}

		private void setContextUuidAdapter(UUIDAdapter uuidAdapter)
		{
			this.contextUuidMostSigBits = uuidAdapter.mostSigBits;
			this.contextUuidLeastSigBits = uuidAdapter.leastSigBits;
		}

		private void setContextUuid(UUID contextUuid)
		{
			setContextUuidAdapter(new UUIDAdapter(contextUuid));
		}

		private void setContextUuidKey(UUIDKey contextUuidKey)
		{
			setContextUuidAdapter(new UUIDAdapter(contextUuidKey));
		}

		private UUIDAdapter getSubContextUuidAdapter()
		{
			return new UUIDAdapter(subContextUuidMostSigBits, subContextUuidLeastSigBits);
		}

		public UUID getSubContextUuid()
		{
			return getSubContextUuidAdapter().uuid();
		}

		@SuppressWarnings("unused")
		private UUIDKey getSubContextUuidKey()
		{
			return getSubContextUuidAdapter().uuidKey();
		}

		private void setSubContextUuidAdapter(UUIDAdapter uuidAdapter)
		{
			this.subContextUuidMostSigBits = uuidAdapter.mostSigBits;
			this.subContextUuidLeastSigBits = uuidAdapter.leastSigBits;
		}

		private void setSubContextUuid(UUID subContextUuid)
		{
			setSubContextUuidAdapter(new UUIDAdapter(subContextUuid));
		}

		private void setSubContextUuidKey(UUIDKey subContextUuidKey)
		{
			setSubContextUuidAdapter(new UUIDAdapter(subContextUuidKey));
		}

		public ContextSubContextSecondaryKeyData(UUID contextUuid, UUID subContextUuid)
		{
			setContextUuid(contextUuid);
			setSubContextUuid(subContextUuid);
		}

		private ContextSubContextSecondaryKeyData(UUIDKey contextUuidKey, UUIDKey subContextUuidKey)
		{
			setContextUuidKey(contextUuidKey);
			setSubContextUuidKey(subContextUuidKey);
		}

		public ContextSubContextSecondaryKeyData(UUID subContextUuid)
		{
			this(null, subContextUuid);
		}

		@SuppressWarnings("unused")
		private ContextSubContextSecondaryKeyData(UUIDKey subContextUuidKey)
		{
			this(null, subContextUuidKey);
		}

		private static final UUID minUuid = new UUID(Long.MIN_VALUE, Long.MIN_VALUE);

		public static ContextSubContextSecondaryKeyData min(UUID contextUuid)
		{
			return new ContextSubContextSecondaryKeyData(contextUuid, minUuid);
		}

		private static final UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);

		public static ContextSubContextSecondaryKeyData max(UUID contextUuid)
		{
			return new ContextSubContextSecondaryKeyData(contextUuid, maxUuid);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + (int) (subContextUuidLeastSigBits ^ (subContextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (subContextUuidMostSigBits ^ (subContextUuidMostSigBits >>> 32));
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
			ContextSubContextSecondaryKeyData other = (ContextSubContextSecondaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
				return false;
			if (subContextUuidLeastSigBits != other.subContextUuidLeastSigBits)
				return false;
			if (subContextUuidMostSigBits != other.subContextUuidMostSigBits)
				return false;
			return true;
		}

	}

	public final static String contextSubContextSecondaryKeyDataList_FieldName = "contextSubContextSecondaryKeyDataList";
	@SecondaryKey(name = contextSubContextSecondaryKeyDataList_FieldName, relate = Relationship.MANY_TO_MANY)
	private final List<ContextSubContextSecondaryKeyData> contextSubContextSecondaryKeyDataList;

	private class ContextUuidKeyPathView extends AbstractList<UUIDKey> implements List<UUIDKey>
	{

		@Override
		public UUIDKey get(int index)
		{
			return getContextUuidKeyPath().get(index);
		}

		@Override
		public int size()
		{
			return getContextUuidKeyPath().size();
		}

		@Override
		public UUIDKey set(int index, UUIDKey element)
		{
			UUIDKey uuidKey = getContextUuidKeyPath().set(index, element);
			if (index == size() - 1)
				contextCreationDateSecondaryKeyData.setContextUuidKey(element);
			contextSubContextSecondaryKeyDataList.set(index, new ContextSubContextSecondaryKeyData(index > 0 ? get(index - 1) : null, element));
			if (index + 1 < size())
				contextSubContextSecondaryKeyDataList.set(index + 1, new ContextSubContextSecondaryKeyData(element, get(index + 1)));
			return uuidKey;
		}

		@Override
		public void add(int index, UUIDKey element)
		{
			getContextUuidKeyPath().add(index, element);
			if (index == size() - 1)
				contextCreationDateSecondaryKeyData.setContextUuidKey(element);
			contextSubContextSecondaryKeyDataList.add(index, new ContextSubContextSecondaryKeyData(index > 0 ? get(index - 1) : null, element));
			if (index + 1 < size())
				contextSubContextSecondaryKeyDataList.set(index + 1, new ContextSubContextSecondaryKeyData(element, get(index + 1)));
		}

		@Override
		public UUIDKey remove(int index)
		{
			UUIDKey uuidKey = getContextUuidKeyPath().remove(index);
			if (index == size())
				contextCreationDateSecondaryKeyData.setContextUuidKey(index > 0 ? get(index - 1) : null);
			contextSubContextSecondaryKeyDataList.remove(index);
			if (index < size())
				contextSubContextSecondaryKeyDataList.set(index, new ContextSubContextSecondaryKeyData(index > 0 ? get(index - 1) : null, get(index)));
			return uuidKey;
		}

	}

	private final transient ContextUuidKeyPathView contextUuidKeyPathView;

	public BerkeleyDBSignatureRequestEntity()
	{
		this.contextCreationDateSecondaryKeyData = new ContextCreationDateSecondaryKeyData();
		this.contextSubContextSecondaryKeyDataList = new ArrayList<>();
		this.contextUuidKeyPathView = new ContextUuidKeyPathView();
	}

	private UUIDKey getUuidKey()
	{
		return uuidKey;
	}

	private void setUuidKey(UUIDKey uuidKey)
	{
		this.uuidKey = uuidKey;
	}

	@Override
	public UUID getUuid()
	{
		return getUuidKey().uuid();
	}

	@Override
	public void setUuid(UUID uuid)
	{
		setUuidKey(new UUIDKey(uuid));
	}

	@Override
	public Date getCreationDate()
	{
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
		contextCreationDateSecondaryKeyData.setCreationDate(creationDate);
	}

	protected abstract List<UUIDKey> getContextUuidKeyPath();

	@Override
	public List<UUID> getContextUuidPath()
	{
		return new BijectionList<>(new Bijection<UUIDKey, UUID>()
		{

			@Override
			public UUID forward(UUIDKey input)
			{
				return input.uuid();
			}

			@Override
			public UUIDKey backward(UUID output)
			{
				return new UUIDKey(output);
			}
		}, contextUuidKeyPathView);
	}

	public ContextCreationDateSecondaryKeyData getContextCreationDateSecondaryKeyData()
	{
		return contextCreationDateSecondaryKeyData;
	}

}
