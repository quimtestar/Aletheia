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
package aletheia.persistence.berkeleydb.collections.peertopeer;

import java.util.NoSuchElementException;
import java.util.UUID;

import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.NodeDeferredMessage;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesMap;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public class BerkeleyDBNodeDeferredMessagesMap extends AbstractCloseableMap<UUID, NodeDeferredMessage> implements NodeDeferredMessagesMap
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final DeferredMessage deferredMessage;
	private final UUID deferredMessageUuid;
	private final EntityIndex<PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> index;

	public BerkeleyDBNodeDeferredMessagesMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			DeferredMessage deferredMessage)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.deferredMessage = deferredMessage;
		this.deferredMessageUuid = deferredMessage.getUuid();
		this.index = persistenceManager.getTemporaryEntityStore().nodeDeferredMessageEntityDeferredMessageSubIndex(new UUIDKey(deferredMessageUuid));
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	@Override
	public DeferredMessage getDeferredMessage()
	{
		return deferredMessage;
	}

	@Override
	public int size()
	{
		EntityCursor<PrimaryKeyData> cursor = transaction.keys(index);
		try
		{
			int n = 0;
			while (true)
			{
				PrimaryKeyData k = transaction.next(cursor);
				if (k == null)
					break;
				n += transaction.count(cursor);
			}
			return n;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<PrimaryKeyData> cursor = transaction.keys(index);
		try
		{
			PrimaryKeyData k = transaction.first(cursor);
			return k == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof UUID))
			return false;
		UUID nodeUuid = (UUID) key;
		PrimaryKeyData primaryKeyData = new BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData(nodeUuid, deferredMessageUuid);
		return transaction.contains(index, primaryKeyData);
	}

	@Override
	public NodeDeferredMessage get(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUID nodeUuid = (UUID) key;
		PrimaryKeyData primaryKeyData = new BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData(nodeUuid, deferredMessageUuid);
		BerkeleyDBNodeDeferredMessageEntity entity = transaction.get(index, primaryKeyData);
		if (entity == null)
			return null;
		return new NodeDeferredMessage(persistenceManager, entity);
	}

	@Override
	public NodeDeferredMessage remove(Object key)
	{
		if (!(key instanceof UUID))
			return null;
		UUID nodeUuid = (UUID) key;
		PrimaryKeyData primaryKeyData = new BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData(nodeUuid, deferredMessageUuid);
		BerkeleyDBNodeDeferredMessageEntity entity = transaction.get(index, primaryKeyData);
		if (entity == null)
			return null;
		if (!transaction.delete(index, primaryKeyData))
			return null;
		return new NodeDeferredMessage(persistenceManager, entity);
	}

	@Override
	public void clear()
	{
		EntityCursor<PrimaryKeyData> cursor = transaction.keys(index);
		try
		{
			while (true)
			{
				PrimaryKeyData k = transaction.next(cursor);
				if (k == null)
					break;
				transaction.delete(cursor);
			}
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	private class MyEntry implements Entry<UUID, NodeDeferredMessage>
	{
		private final NodeDeferredMessage nodeDeferredMessage;

		private MyEntry(NodeDeferredMessage nodeDeferredMessage)
		{
			this.nodeDeferredMessage = nodeDeferredMessage;
		}

		@Override
		public UUID getKey()
		{
			return nodeDeferredMessage.getNodeUuid();
		}

		@Override
		public NodeDeferredMessage getValue()
		{
			return nodeDeferredMessage;
		}

		@Override
		public NodeDeferredMessage setValue(NodeDeferredMessage value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj)
		{
			try
			{
				@SuppressWarnings("unchecked")
				Entry<UUID, NodeDeferredMessage> entry = (Entry<UUID, NodeDeferredMessage>) obj;
				return getKey().equals(entry.getKey()) && getValue().equals(entry.getValue());
			}
			catch (ClassCastException e)
			{
				return false;
			}
		}

	}

	@Override
	public CloseableSet<Entry<UUID, NodeDeferredMessage>> entrySet()
	{
		return new AbstractCloseableSet<Entry<UUID, NodeDeferredMessage>>()
		{

			@Override
			public CloseableIterator<Entry<UUID, NodeDeferredMessage>> iterator()
			{
				final EntityCursor<BerkeleyDBNodeDeferredMessageEntity> cursor = transaction.entities(index);
				return new CloseableIterator<Entry<UUID, NodeDeferredMessage>>()
				{
					private BerkeleyDBNodeDeferredMessageEntity next;

					{
						next = transaction.first(cursor);
						if (next == null)
							transaction.close(cursor);
					}

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public Entry<UUID, NodeDeferredMessage> next()
					{
						if (next == null)
							throw new NoSuchElementException();
						BerkeleyDBNodeDeferredMessageEntity entity = next;
						next = transaction.next(cursor);
						if (next == null)
							transaction.close(cursor);
						return new MyEntry(persistenceManager.entityToNodeDeferredMessage(entity));
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public void close()
					{
						transaction.close(cursor);
					}

					@Override
					protected void finalize() throws Throwable
					{
						close();
					}

				};
			}

			@Override
			public int size()
			{
				return BerkeleyDBNodeDeferredMessagesMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return BerkeleyDBNodeDeferredMessagesMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				try
				{
					@SuppressWarnings("unchecked")
					Entry<UUID, NodeDeferredMessage> e = (Entry<UUID, NodeDeferredMessage>) o;
					NodeDeferredMessage nodeDeferredMessage = BerkeleyDBNodeDeferredMessagesMap.this.get(e.getKey());
					if (nodeDeferredMessage == null)
						return false;
					if (!nodeDeferredMessage.equals(e.getValue()))
						return false;
					return true;

				}
				catch (ClassCastException e)
				{
					return false;
				}
			}

			@Override
			public Object[] toArray()
			{
				return MiscUtilities.iterableToArray(this);
			}

			@Override
			public <T> T[] toArray(T[] a)
			{
				return MiscUtilities.iterableToArray(this, a);
			}

			@Override
			public void clear()
			{
				BerkeleyDBNodeDeferredMessagesMap.this.clear();
			}
		};
	}

	@Override
	public CloseableSet<UUID> keySet()
	{
		return new BijectionCloseableSet<>(new Bijection<Entry<UUID, NodeDeferredMessage>, UUID>()
		{

			@Override
			public UUID forward(Entry<UUID, NodeDeferredMessage> entry)
			{
				return entry.getKey();
			}

			@Override
			public Entry<UUID, NodeDeferredMessage> backward(UUID uuid)
			{
				NodeDeferredMessage nodeDeferredMessage = get(uuid);
				if (nodeDeferredMessage == null)
					return null;
				return new MyEntry(nodeDeferredMessage);
			}
		}, entrySet());
	}

	@Override
	public CloseableCollection<NodeDeferredMessage> values()
	{
		return new BijectionCloseableCollection<>(new Bijection<Entry<UUID, NodeDeferredMessage>, NodeDeferredMessage>()
		{

			@Override
			public NodeDeferredMessage forward(Entry<UUID, NodeDeferredMessage> entry)
			{
				return entry.getValue();
			}

			@Override
			public Entry<UUID, NodeDeferredMessage> backward(NodeDeferredMessage nodeDeferredMessage)
			{
				return new MyEntry(nodeDeferredMessage);
			}
		}, entrySet());
	}

}
