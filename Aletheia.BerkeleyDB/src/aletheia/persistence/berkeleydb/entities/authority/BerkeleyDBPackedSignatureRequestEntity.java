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
package aletheia.persistence.berkeleydb.entities.authority;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.PackedSignatureRequestEntity;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionSet;

@Persistent(version = 8)
public class BerkeleyDBPackedSignatureRequestEntity extends BerkeleyDBSignatureRequestEntity implements PackedSignatureRequestEntity
{
	public final static String contextUuidKeyPath_FieldName = "contextUuidKeyPath";
	@SecondaryKey(name = contextUuidKeyPath_FieldName, relate = Relationship.MANY_TO_MANY)
	private final List<UUIDKey> contextUuidKeyPath;

	public final static String packingDate_FieldName = "packingDate";
	@SecondaryKey(name = packingDate_FieldName, relate = Relationship.MANY_TO_ONE)
	private Date packingDate;

	@Persistent(version = 1)
	public static class ContextPackingDateSecondaryKeyData implements Comparable<ContextPackingDateSecondaryKeyData>
	{
		@KeyField(1)
		private long contextUuidMostSigBits;

		@KeyField(2)
		private long contextUuidLeastSigBits;

		@KeyField(3)
		private Date packingDate;

		@SuppressWarnings("unused")
		private ContextPackingDateSecondaryKeyData()
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

		private void setPackingDate(Date packingDate)
		{
			this.packingDate = packingDate;
		}

		public ContextPackingDateSecondaryKeyData(UUID contextUuid, Date packingDate)
		{
			setContextUuid(contextUuid);
			setPackingDate(packingDate);
		}

		private ContextPackingDateSecondaryKeyData(UUIDKey contextUuidKey, Date packingDate)
		{
			setContextUuidKey(contextUuidKey);
			setPackingDate(packingDate);
		}

		private static final Date maxDate = new Date(Long.MAX_VALUE);

		public static ContextPackingDateSecondaryKeyData min(UUID contextUuid)
		{
			return new ContextPackingDateSecondaryKeyData(contextUuid, maxDate);
		}

		private static final Date minDate = new Date(Long.MIN_VALUE);

		public static ContextPackingDateSecondaryKeyData max(UUID contextUuid)
		{
			return new ContextPackingDateSecondaryKeyData(contextUuid, minDate);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (contextUuidLeastSigBits ^ (contextUuidLeastSigBits >>> 32));
			result = prime * result + (int) (contextUuidMostSigBits ^ (contextUuidMostSigBits >>> 32));
			result = prime * result + ((packingDate == null) ? 0 : packingDate.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			ContextPackingDateSecondaryKeyData other = (ContextPackingDateSecondaryKeyData) obj;
			if (contextUuidLeastSigBits != other.contextUuidLeastSigBits)
				return false;
			if (contextUuidMostSigBits != other.contextUuidMostSigBits)
				return false;
			if (packingDate == null)
			{
				if (other.packingDate != null)
					return false;
			}
			else if (!packingDate.equals(other.packingDate))
				return false;
			return true;
		}

		@Override
		public int compareTo(ContextPackingDateSecondaryKeyData o)
		{
			int c;
			c = Long.compare(contextUuidMostSigBits, o.contextUuidMostSigBits);
			if (c != 0)
				return c;
			c = Long.compare(contextUuidLeastSigBits, o.contextUuidLeastSigBits);
			if (c != 0)
				return c;
			c = -packingDate.compareTo(o.packingDate);
			if (c != 0)
				return c;
			return 0;
		}
	}

	public final static String contextPackingDateSecondaryKeyDataList_FieldName = "contextPackingDateSecondaryKeyDataList";
	@SecondaryKey(name = contextPackingDateSecondaryKeyDataList_FieldName, relate = Relationship.MANY_TO_MANY)
	private final List<ContextPackingDateSecondaryKeyData> contextPackingDateSecondaryKeyDataList;

	private class ContextUuidKeyPathView extends AbstractList<UUIDKey> implements List<UUIDKey>
	{

		@Override
		public UUIDKey get(int index)
		{
			return contextUuidKeyPath.get(index);
		}

		@Override
		public int size()
		{
			return contextUuidKeyPath.size();
		}

		@Override
		public UUIDKey set(int index, UUIDKey element)
		{
			UUIDKey uuidKey = contextUuidKeyPath.set(index, element);
			contextPackingDateSecondaryKeyDataList.set(index, new ContextPackingDateSecondaryKeyData(element, packingDate));
			return uuidKey;
		}

		@Override
		public void add(int index, UUIDKey element)
		{
			contextUuidKeyPath.add(index, element);
			contextPackingDateSecondaryKeyDataList.add(index, new ContextPackingDateSecondaryKeyData(element, packingDate));
		}

		@Override
		public UUIDKey remove(int index)
		{
			UUIDKey uuidKey = contextUuidKeyPath.remove(index);
			contextPackingDateSecondaryKeyDataList.remove(index);
			return uuidKey;
		}
	}

	private final transient ContextUuidKeyPathView contextUuidKeyPathView;

	public final static String rootContextSignatureUuidKey_FieldName = "rootContextSignatureUuidKey";
	@SecondaryKey(name = rootContextSignatureUuidKey_FieldName, relate = Relationship.MANY_TO_ONE)
	private UUIDKey rootContextSignatureUuidKey;

	public final static String dependencyUuidKeys_FieldName = "dependendyUuidKeys";
	@SecondaryKey(name = dependencyUuidKeys_FieldName, relate = Relationship.MANY_TO_MANY)
	private final Set<UUIDKey> dependencyUuidKeys;

	private byte[] data;

	public BerkeleyDBPackedSignatureRequestEntity()
	{
		this.contextUuidKeyPath = new ArrayList<>();
		this.contextPackingDateSecondaryKeyDataList = new ArrayList<>();
		this.contextUuidKeyPathView = new ContextUuidKeyPathView();
		this.dependencyUuidKeys = new HashSet<>();
	}

	@Override
	protected List<UUIDKey> getContextUuidKeyPath()
	{
		return contextUuidKeyPathView;
	}

	@Override
	public Date getPackingDate()
	{
		return packingDate;
	}

	@Override
	public void setPackingDate(Date packingDate)
	{
		this.packingDate = packingDate;
		for (ContextPackingDateSecondaryKeyData contextPackingDateSecondaryKeyData : contextPackingDateSecondaryKeyDataList)
			contextPackingDateSecondaryKeyData.setPackingDate(packingDate);
	}

	public List<ContextPackingDateSecondaryKeyData> getContextPackingDateSecondaryKeyDataList()
	{
		return Collections.unmodifiableList(contextPackingDateSecondaryKeyDataList);
	}

	private UUIDKey getRootContextSignatureUuidKey()
	{
		return rootContextSignatureUuidKey;
	}

	private void setRootContextSignatureUuidKey(UUIDKey rootContextSignatureUuidKey)
	{
		this.rootContextSignatureUuidKey = rootContextSignatureUuidKey;
	}

	@Override
	public UUID getRootContextSignatureUuid()
	{
		if (getRootContextSignatureUuidKey() == null)
			return null;
		else
			return getRootContextSignatureUuidKey().uuid();
	}

	@Override
	public void setRootContextSignatureUuid(UUID rootContextSignatureUuid)
	{
		if (rootContextSignatureUuid == null)
			setRootContextSignatureUuidKey(null);
		else
			setRootContextSignatureUuidKey(new UUIDKey(rootContextSignatureUuid));
	}

	private Set<UUIDKey> getDependencyUuidKeys()
	{
		return dependencyUuidKeys;
	}

	@Override
	public Set<UUID> getDependencyUuids()
	{
		return new BijectionSet<>(new Bijection<UUIDKey, UUID>()
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
		}, getDependencyUuidKeys());
	}

	@Override
	public byte[] getData()
	{
		return data;
	}

	@Override
	public void setData(byte[] data)
	{
		this.data = data;
	}

}
