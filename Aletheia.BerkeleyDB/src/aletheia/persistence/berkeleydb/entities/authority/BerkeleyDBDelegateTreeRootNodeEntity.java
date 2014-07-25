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

import java.util.AbstractList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

import aletheia.model.identifier.RootNamespace;
import aletheia.model.security.SignatureData;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.entities.authority.DelegateTreeRootNodeEntity;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CastBijection;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Persistent(version = 3)
public class BerkeleyDBDelegateTreeRootNodeEntity extends BerkeleyDBDelegateTreeNodeEntity implements DelegateTreeRootNodeEntity
{
	@Persistent(version = 1)
	public static class BerkeleyDBSuccessorEntryEntity implements DelegateTreeRootNodeEntity.SuccessorEntryEntity
	{
		private UUID successorUuid;

		private Date signatureDate;

		private int signatureVersion;

		private SignatureData signatureData;

		private BerkeleyDBSuccessorEntryEntity()
		{

		}

		@Override
		public UUID getSuccessorUuid()
		{
			return successorUuid;
		}

		@Override
		public void setSuccessorUuid(UUID successorUuid)
		{
			this.successorUuid = successorUuid;
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

	}

	private final LinkedList<BerkeleyDBSuccessorEntryEntity> successorEntryEntities;

	public static final String successorUuidKeys_FieldName = "successorUuidKeys";
	@SecondaryKey(name = successorUuidKeys_FieldName, relatedEntity = BerkeleyDBPersonEntity.class, relate = Relationship.MANY_TO_MANY)
	private final HashSet<UUIDKey> successorUuidKeys;

	private static class BerkeleyDBSuccessorEntryEntitiesView extends AbstractList<BerkeleyDBSuccessorEntryEntity>
	{
		private final List<BerkeleyDBSuccessorEntryEntity> successorEntryEntities;
		private final Set<UUIDKey> successorUuidKeys;

		private BerkeleyDBSuccessorEntryEntitiesView(List<BerkeleyDBSuccessorEntryEntity> successorEntryEntities, Set<UUIDKey> successorUuidKeys)
		{
			this.successorEntryEntities = successorEntryEntities;
			this.successorUuidKeys = successorUuidKeys;
		}

		@Override
		public BerkeleyDBSuccessorEntryEntity get(int index)
		{
			return successorEntryEntities.get(index);
		}

		@Override
		public int size()
		{
			return successorEntryEntities.size();
		}

		@Override
		public BerkeleyDBSuccessorEntryEntity set(int index, BerkeleyDBSuccessorEntryEntity entry)
		{
			if (entry == null)
				throw new IllegalArgumentException();
			BerkeleyDBSuccessorEntryEntity last = successorEntryEntities.get(index);
			if (!last.getSuccessorUuid().equals(entry.getSuccessorUuid()))
			{
				if (!successorUuidKeys.add(new UUIDKey(entry.getSuccessorUuid())))
					throw new IllegalStateException();
				if (last != null)
					successorUuidKeys.remove(new UUIDKey(last.getSuccessorUuid()));
			}
			return successorEntryEntities.set(index, entry);
		}

		@Override
		public void add(int index, BerkeleyDBSuccessorEntryEntity entry)
		{
			if (entry == null)
				throw new IllegalArgumentException();
			if (!successorUuidKeys.add(new UUIDKey(entry.getSuccessorUuid())))
				throw new IllegalStateException();
			successorEntryEntities.add(index, entry);
		}

		@Override
		public BerkeleyDBSuccessorEntryEntity remove(int index)
		{
			BerkeleyDBSuccessorEntryEntity last = successorEntryEntities.get(index);
			if (!successorUuidKeys.remove(last.getSuccessorUuid()))
				throw new IllegalStateException();
			return successorEntryEntities.remove(index);
		}

		@Override
		public Iterator<BerkeleyDBSuccessorEntryEntity> iterator()
		{
			return listIterator();
		}

		@Override
		public ListIterator<BerkeleyDBSuccessorEntryEntity> listIterator()
		{
			return listIterator(0);
		}

		@Override
		public ListIterator<BerkeleyDBSuccessorEntryEntity> listIterator(int index)
		{
			final ListIterator<BerkeleyDBSuccessorEntryEntity> successorEntriesIterator = successorEntryEntities.listIterator(index);
			return new ListIterator<BerkeleyDBSuccessorEntryEntity>()
			{
				private BerkeleyDBSuccessorEntryEntity last = null;

				@Override
				public boolean hasNext()
				{
					return successorEntriesIterator.hasNext();
				}

				@Override
				public BerkeleyDBSuccessorEntryEntity next()
				{
					last = successorEntriesIterator.next();
					return last;
				}

				@Override
				public void remove()
				{
					if (last == null)
						throw new IllegalStateException();
					successorEntriesIterator.remove();
					successorUuidKeys.remove(new UUIDKey(last.getSuccessorUuid()));
				}

				@Override
				public boolean hasPrevious()
				{
					return successorEntriesIterator.hasPrevious();
				}

				@Override
				public BerkeleyDBSuccessorEntryEntity previous()
				{
					last = successorEntriesIterator.previous();
					return last;
				}

				@Override
				public int nextIndex()
				{
					return successorEntriesIterator.nextIndex();
				}

				@Override
				public int previousIndex()
				{
					return successorEntriesIterator.previousIndex();
				}

				@Override
				public void set(BerkeleyDBSuccessorEntryEntity e)
				{
					if (e == null)
						throw new IllegalArgumentException();
					if (last == null)
						throw new IllegalStateException();
					if (!last.getSuccessorUuid().equals(e.getSuccessorUuid()))
					{
						if (!successorUuidKeys.add(new UUIDKey(e.getSuccessorUuid())))
							throw new IllegalStateException();
						successorUuidKeys.remove(new UUIDKey(last.getSuccessorUuid()));
					}
					successorEntriesIterator.set(e);
				}

				@Override
				public void add(BerkeleyDBSuccessorEntryEntity e)
				{
					if (!successorUuidKeys.add(new UUIDKey(e.getSuccessorUuid())))
						throw new IllegalStateException();
					successorEntriesIterator.add(e);
				}

			};
		}

		@Override
		public List<BerkeleyDBSuccessorEntryEntity> subList(int fromIndex, int toIndex)
		{
			return new BerkeleyDBSuccessorEntryEntitiesView(successorEntryEntities.subList(fromIndex, toIndex), successorUuidKeys);
		}

	}

	private static class SuccessorEntryEntitiesView extends BijectionList<BerkeleyDBSuccessorEntryEntity, SuccessorEntryEntity>
	{
		public SuccessorEntryEntitiesView(List<BerkeleyDBSuccessorEntryEntity> successorEntryEntities, Set<UUIDKey> successorUuidKeys)
		{
			super(new CastBijection<BerkeleyDBSuccessorEntryEntity, SuccessorEntryEntity>(), new BerkeleyDBSuccessorEntryEntitiesView(successorEntryEntities,
					successorUuidKeys));
		}

	}

	private int successorIndex;

	private Date signatureDate;

	private int signatureVersion;

	private SignatureData signatureData;

	public BerkeleyDBDelegateTreeRootNodeEntity()
	{
		super();
		this.successorEntryEntities = new LinkedList<BerkeleyDBSuccessorEntryEntity>();
		this.successorUuidKeys = new HashSet<UUIDKey>();
	}

	@Override
	public void setPrefix(RootNamespace prefix)
	{
		super.setPrefix(prefix);
	}

	@Override
	public RootNamespace getPrefix()
	{
		return (RootNamespace) super.getPrefix();
	}

	@Override
	public BerkeleyDBSuccessorEntryEntity instantiateSuccessorEntryEntity()
	{
		return new BerkeleyDBSuccessorEntryEntity();
	}

	@Override
	public List<SuccessorEntryEntity> getSuccessorEntryEntities()
	{
		return new SuccessorEntryEntitiesView(successorEntryEntities, successorUuidKeys);
	}

	@Override
	public Set<UUID> successorUuids()
	{
		return new BijectionSet<UUIDKey, UUID>(new Bijection<UUIDKey, UUID>()
		{

			@Override
			public UUID forward(UUIDKey uuidKey)
			{
				return uuidKey.uuid();
			}

			@Override
			public UUIDKey backward(UUID uuid)
			{
				return new UUIDKey(uuid);
			}
		}, Collections.unmodifiableSet(successorUuidKeys));
	}

	@Override
	public int getSuccessorIndex()
	{
		return successorIndex;
	}

	@Override
	public void setSuccessorIndex(int successorIndex)
	{
		this.successorIndex = successorIndex;
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
		int result = super.hashCode();
		result = prime * result + ((signatureData == null) ? 0 : signatureData.hashCode());
		result = prime * result + ((signatureDate == null) ? 0 : signatureDate.hashCode());
		result = prime * result + signatureVersion;
		result = prime * result + ((successorEntryEntities == null) ? 0 : successorEntryEntities.hashCode());
		result = prime * result + successorIndex;
		result = prime * result + ((successorUuidKeys == null) ? 0 : successorUuidKeys.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BerkeleyDBDelegateTreeRootNodeEntity other = (BerkeleyDBDelegateTreeRootNodeEntity) obj;
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
		if (successorEntryEntities == null)
		{
			if (other.successorEntryEntities != null)
				return false;
		}
		else if (!successorEntryEntities.equals(other.successorEntryEntities))
			return false;
		if (successorIndex != other.successorIndex)
			return false;
		if (successorUuidKeys == null)
		{
			if (other.successorUuidKeys != null)
				return false;
		}
		else if (!successorUuidKeys.equals(other.successorUuidKeys))
			return false;
		return true;
	}

}
