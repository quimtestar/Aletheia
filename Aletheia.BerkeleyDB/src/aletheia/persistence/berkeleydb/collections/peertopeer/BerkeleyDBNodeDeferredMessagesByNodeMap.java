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

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.NodeDeferredMessageRecipientSecondaryKeyData;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBKeyComparator;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesByNodeMap;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesByRecipientCollection;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBNodeDeferredMessagesByNodeMap extends AbstractCloseableMap<UUID, NodeDeferredMessagesByRecipientCollection>
		implements NodeDeferredMessagesByNodeMap
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final UUID nodeUuid;
	private final BerkeleyDBKeyComparator<NodeDeferredMessageRecipientSecondaryKeyData> keyComparator;
	private final SecondaryIndex<NodeDeferredMessageRecipientSecondaryKeyData, PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> index;
	private final NodeDeferredMessageRecipientSecondaryKeyData fromKey;
	private final NodeDeferredMessageRecipientSecondaryKeyData toKey;

	public BerkeleyDBNodeDeferredMessagesByNodeMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, UUID nodeUuid)
	{
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.nodeUuid = nodeUuid;
		this.keyComparator = new BerkeleyDBKeyComparator<>(NodeDeferredMessageRecipientSecondaryKeyData.class);
		this.index = persistenceManager.getTemporaryEntityStore().nodeDeferredMessageEntityNodeRecipientSecondaryIndex();
		this.fromKey = NodeDeferredMessageRecipientSecondaryKeyData.min(nodeUuid);
		this.toKey = NodeDeferredMessageRecipientSecondaryKeyData.max(nodeUuid);
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public Transaction getTransaction()
	{
		return transaction;
	}

	@Override
	public UUID getNodeUuid()
	{
		return nodeUuid;
	}

	@Override
	public CloseableSet<UUID> keySet()
	{
		return new AbstractCloseableSet<UUID>()
		{

			@Override
			public CloseableIterator<UUID> iterator()
			{
				final EntityCursor<NodeDeferredMessageRecipientSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, true);
				return new CloseableIterator<UUID>()
				{
					private NodeDeferredMessageRecipientSecondaryKeyData next;

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
					public UUID next()
					{
						if (next == null)
							throw new NoSuchElementException();
						NodeDeferredMessageRecipientSecondaryKeyData key = next;
						next = transaction.nextNoDup(cursor);
						if (next == null)
							transaction.close(cursor);
						return key.getDeferredMessageRecipientUuid();
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
						super.finalize();
					}

				};
			}

			@Override
			public int size()
			{
				EntityCursor<NodeDeferredMessageRecipientSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, true);
				try
				{
					int n = 0;
					while (true)
					{
						NodeDeferredMessageRecipientSecondaryKeyData k = transaction.nextNoDup(cursor);
						if (k == null)
							break;
						n += cursor.count();
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
				EntityCursor<NodeDeferredMessageRecipientSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, true);
				try
				{
					NodeDeferredMessageRecipientSecondaryKeyData k = transaction.first(cursor);
					return k == null;
				}
				finally
				{
					transaction.close(cursor);
				}
			}

			private boolean keyInInterval(NodeDeferredMessageRecipientSecondaryKeyData k)
			{
				if (fromKey != null)
				{
					int c = keyComparator.compare(fromKey, k);
					if (c > 0)
						return false;
				}
				if (toKey != null)
				{
					int c = keyComparator.compare(k, toKey);
					if (c > 0)
						return false;
				}
				return true;
			}

			@Override
			public boolean contains(Object o)
			{
				try
				{
					NodeDeferredMessageRecipientSecondaryKeyData k = new NodeDeferredMessageRecipientSecondaryKeyData(nodeUuid, (UUID) o);
					if (keyInInterval(k) && transaction.contains(index, k))
						return true;
					return false;
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
				EntityCursor<NodeDeferredMessageRecipientSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, true);
				try
				{
					while (true)
					{
						NodeDeferredMessageRecipientSecondaryKeyData k = transaction.next(cursor);
						if (k == null)
							break;
						cursor.delete();
					}
				}
				finally
				{
					transaction.close(cursor);
				}
			}

		};
	}

	@Override
	public CloseableSet<Entry<UUID, NodeDeferredMessagesByRecipientCollection>> entrySet()
	{
		return new BijectionCloseableSet<UUID, Entry<UUID, NodeDeferredMessagesByRecipientCollection>>(
				new Bijection<UUID, Entry<UUID, NodeDeferredMessagesByRecipientCollection>>()
				{

					@Override
					public Entry<UUID, NodeDeferredMessagesByRecipientCollection> forward(final UUID recipientUuid)
					{
						return new Entry<UUID, NodeDeferredMessagesByRecipientCollection>()
						{

							@Override
							public UUID getKey()
							{
								return recipientUuid;
							}

							@Override
							public NodeDeferredMessagesByRecipientCollection getValue()
							{
								return persistenceManager.nodeDeferredMessagesByRecipientCollection(transaction, nodeUuid, recipientUuid);
							}

							@Override
							public NodeDeferredMessagesByRecipientCollection setValue(NodeDeferredMessagesByRecipientCollection value)
							{
								throw new UnsupportedOperationException();
							}

							@Override
							public String toString()
							{
								return getKey() + " => " + getValue();
							}
						};
					}

					@Override
					public UUID backward(Entry<UUID, NodeDeferredMessagesByRecipientCollection> entry)
					{
						return entry.getKey();
					}
				}, keySet());
	}

}
