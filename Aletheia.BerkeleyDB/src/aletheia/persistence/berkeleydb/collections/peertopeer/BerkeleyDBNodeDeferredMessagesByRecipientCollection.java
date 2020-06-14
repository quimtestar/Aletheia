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
package aletheia.persistence.berkeleydb.collections.peertopeer;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.peertopeer.NodeDeferredMessage;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.NodeDeferredMessageRecipientDateSecondaryKeyData;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBKeyComparator;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesByRecipientCollection;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableCollection;
import aletheia.utilities.collections.CloseableIterator;

public class BerkeleyDBNodeDeferredMessagesByRecipientCollection extends AbstractCloseableCollection<NodeDeferredMessage>
		implements NodeDeferredMessagesByRecipientCollection
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final UUID nodeUuid;
	private final UUID recipientUuid;
	private final Date fromDate;
	private final Date toDate;
	private final BerkeleyDBKeyComparator<NodeDeferredMessageRecipientDateSecondaryKeyData> keyComparator;
	private final SecondaryIndex<NodeDeferredMessageRecipientDateSecondaryKeyData, PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> index;
	private final NodeDeferredMessageRecipientDateSecondaryKeyData fromKey;
	private final NodeDeferredMessageRecipientDateSecondaryKeyData toKey;

	public BerkeleyDBNodeDeferredMessagesByRecipientCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID nodeUuid, UUID recipientUuid, Date fromDate, Date toDate)
	{
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.nodeUuid = nodeUuid;
		this.recipientUuid = recipientUuid;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.keyComparator = new BerkeleyDBKeyComparator<>(NodeDeferredMessageRecipientDateSecondaryKeyData.class);
		this.index = persistenceManager.getTemporaryEntityStore().nodeDeferredMessageEntityNodeRecipientDateSecondaryIndex();
		this.fromKey = fromDate == null ? NodeDeferredMessageRecipientDateSecondaryKeyData.min(nodeUuid, recipientUuid)
				: new NodeDeferredMessageRecipientDateSecondaryKeyData(nodeUuid, recipientUuid, fromDate);
		this.toKey = toDate == null ? NodeDeferredMessageRecipientDateSecondaryKeyData.max(nodeUuid, recipientUuid)
				: new NodeDeferredMessageRecipientDateSecondaryKeyData(nodeUuid, recipientUuid, toDate);
	}

	public BerkeleyDBNodeDeferredMessagesByRecipientCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID nodeUuid, UUID recipientUuid, Date fromDate)
	{
		this(persistenceManager, transaction, nodeUuid, recipientUuid, fromDate, null);
	}

	public BerkeleyDBNodeDeferredMessagesByRecipientCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID nodeUuid, UUID recipientUuid)
	{
		this(persistenceManager, transaction, nodeUuid, recipientUuid, null, null);
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
	public UUID getRecipientUuid()
	{
		return recipientUuid;
	}

	@Override
	public Date getFromDate()
	{
		return fromDate;
	}

	@Override
	public Date getToDate()
	{
		return toDate;
	}

	@Override
	public CloseableIterator<NodeDeferredMessage> iterator()
	{
		final EntityCursor<BerkeleyDBNodeDeferredMessageEntity> cursor = transaction.entities(index, fromKey, true, toKey, false);
		return new CloseableIterator<>()
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
			public NodeDeferredMessage next()
			{
				if (next == null)
					throw new NoSuchElementException();
				BerkeleyDBNodeDeferredMessageEntity entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
				return persistenceManager.entityToNodeDeferredMessage(entity);
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
		EntityCursor<NodeDeferredMessageRecipientDateSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, false);
		try
		{
			int n = 0;
			while (true)
			{
				NodeDeferredMessageRecipientDateSecondaryKeyData k = transaction.next(cursor);
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
		EntityCursor<NodeDeferredMessageRecipientDateSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, false);
		try
		{
			NodeDeferredMessageRecipientDateSecondaryKeyData k = transaction.first(cursor);
			return k == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	private boolean keyInInterval(NodeDeferredMessageRecipientDateSecondaryKeyData k)
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
			if (c >= 0)
				return false;
		}
		return true;
	}

	@Override
	public boolean contains(Object o)
	{
		try
		{
			NodeDeferredMessageRecipientDateSecondaryKeyData k = ((BerkeleyDBNodeDeferredMessageEntity) ((NodeDeferredMessage) o).getEntity())
					.getNodeDeferredMessageRecipientDateSecondaryKeyData();
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
		EntityCursor<NodeDeferredMessageRecipientDateSecondaryKeyData> cursor = transaction.keys(index, fromKey, true, toKey, false);
		try
		{
			while (true)
			{
				NodeDeferredMessageRecipientDateSecondaryKeyData k = transaction.next(cursor);
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

}
