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
package aletheia.persistence.berkeleydb.collections.authority;

import java.util.Comparator;
import java.util.NoSuchElementException;

import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.identifier.Namespace;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData;
import aletheia.persistence.collections.authority.LocalDelegateTreeSubNodeMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public class BerkeleyDBLocalDelegateTreeSubNodeMap extends AbstractCloseableMap<Namespace, DelegateTreeSubNode> implements LocalDelegateTreeSubNodeMap
{

	private final BerkeleyDBPersistenceManager persistenceManager;

	private final BerkeleyDBTransaction transaction;

	private final DelegateTreeNode parent;

	private final EntityIndex<PrimaryKeyData, BerkeleyDBDelegateTreeNodeEntity> index;

	private final Namespace from;

	private final BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData primaryKeyFrom;

	private final Namespace to;

	private final BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData primaryKeyTo;

	protected BerkeleyDBLocalDelegateTreeSubNodeMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, DelegateTreeNode parent,
			Namespace from, Namespace to)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.parent = parent;
			this.index = persistenceManager.getEntityStore().delegateTreeSubNodeEntityParentSubindex(
					new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(parent.getStatementUuid(), parent.getPrefix()));
			this.from = from;
			this.primaryKeyFrom = new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(parent.getStatementUuid(), from);
			this.to = to;
			this.primaryKeyTo = new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(parent.getStatementUuid(), to);
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBLocalDelegateTreeSubNodeMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, DelegateTreeNode parent)
	{
		this(persistenceManager, transaction, parent, parent.getPrefix().initiator(), parent.getPrefix().terminator());
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

	public DelegateTreeNode getParent()
	{
		return parent;
	}

	protected Namespace getFrom()
	{
		return from;
	}

	protected Namespace getTo()
	{
		return to;
	}

	@Override
	public Comparator<? super Namespace> comparator()
	{
		return null;
	}

	@Override
	public CloseableSortedMap<Namespace, DelegateTreeSubNode> subMap(Namespace fromKey, Namespace toKey)
	{
		return new BerkeleyDBLocalDelegateTreeSubNodeMap(persistenceManager, transaction, parent, from.max(fromKey), to.min(toKey));
	}

	@Override
	public CloseableSortedMap<Namespace, DelegateTreeSubNode> headMap(Namespace toKey)
	{
		return new BerkeleyDBLocalDelegateTreeSubNodeMap(persistenceManager, transaction, parent, from, to.min(toKey));
	}

	@Override
	public CloseableSortedMap<Namespace, DelegateTreeSubNode> tailMap(Namespace fromKey)
	{
		return new BerkeleyDBLocalDelegateTreeSubNodeMap(persistenceManager, transaction, parent, from.max(fromKey), to);
	}

	@Override
	public Namespace firstKey()
	{
		EntityCursor<BerkeleyDBDelegateTreeNodeEntity> cursor = transaction.entities(index, primaryKeyFrom, true, primaryKeyTo, false);
		try
		{
			BerkeleyDBDelegateTreeNodeEntity entity = transaction.first(cursor);
			if (entity == null)
				throw new NoSuchElementException();
			return entity.getPrefix();
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public Namespace lastKey()
	{
		EntityCursor<BerkeleyDBDelegateTreeNodeEntity> cursor = transaction.entities(index, primaryKeyFrom, true, primaryKeyTo, false);
		try
		{
			BerkeleyDBDelegateTreeNodeEntity entity = transaction.last(cursor);
			if (entity == null)
				throw new NoSuchElementException();
			return entity.getPrefix();
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public CloseableSet<Entry<Namespace, DelegateTreeSubNode>> entrySet()
	{
		return new AbstractCloseableSet<>()
		{

			@Override
			public CloseableIterator<Entry<Namespace, DelegateTreeSubNode>> iterator()
			{
				final EntityCursor<BerkeleyDBDelegateTreeNodeEntity> cursor = transaction.entities(index, primaryKeyFrom, true, primaryKeyTo, false);
				return new CloseableIterator<>()
				{
					private BerkeleyDBDelegateTreeNodeEntity next;

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
					public Entry<Namespace, DelegateTreeSubNode> next()
					{
						if (next == null)
							throw new NoSuchElementException();
						final BerkeleyDBDelegateTreeNodeEntity entity = next;
						next = transaction.next(cursor);
						if (next == null)
							transaction.close(cursor);
						return new Entry<>()
						{

							@Override
							public Namespace getKey()
							{
								return entity.getPrefix();
							}

							@Override
							public DelegateTreeSubNode getValue()
							{
								return (DelegateTreeSubNode) persistenceManager.entityToDelegateTreeNode(entity);
							}

							@Override
							public DelegateTreeSubNode setValue(DelegateTreeSubNode value)
							{
								throw new UnsupportedOperationException();
							}

						};
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
				return BerkeleyDBLocalDelegateTreeSubNodeMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return BerkeleyDBLocalDelegateTreeSubNodeMap.this.isEmpty();
			}

		};
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBDelegateTreeNodeEntity> cursor = transaction.entities(index, primaryKeyFrom, true, primaryKeyTo, false);
		try
		{
			int n = 0;
			while (true)
			{
				if (transaction.next(cursor) == null)
					break;
				n++;
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
		EntityCursor<PrimaryKeyData> cursor = transaction.keys(index, primaryKeyFrom, true, primaryKeyTo, false);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (key == null)
			return false;
		if (!(key instanceof Namespace))
			return false;
		Namespace prefix = (Namespace) key;
		if (prefix.compareTo(from) < 0)
			return false;
		if (prefix.compareTo(to) >= 0)
			return false;
		BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData pk = new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(parent.getStatementUuid(), prefix);
		return transaction.contains(index, pk);
	}

	@Override
	public DelegateTreeSubNode get(Object key)
	{
		if (key == null)
			return null;
		if (!(key instanceof Namespace))
			return null;
		Namespace prefix = (Namespace) key;
		if (prefix.compareTo(from) < 0)
			return null;
		if (prefix.compareTo(to) >= 0)
			return null;
		BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData pk = new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(parent.getStatementUuid(), prefix);
		BerkeleyDBDelegateTreeNodeEntity entity = transaction.get(index, pk);
		if (entity == null)
			return null;
		return (DelegateTreeSubNode) persistenceManager.entityToDelegateTreeNode(entity);
	}

}
