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

import aletheia.model.peertopeer.Hook;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBHookEntity;
import aletheia.persistence.collections.peertopeer.HookList;
import aletheia.utilities.collections.AbstractCloseableList;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBHookList extends AbstractCloseableList<Hook> implements HookList
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final SecondaryIndex<Long, UUIDKey, BerkeleyDBHookEntity> index;

	public BerkeleyDBHookList(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.index = persistenceManager.getEntityStore().hookEntityPrioritySecondaryIndex();
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

	private CloseableIterator<Hook> iterator(Long fromPriority, Long toPriority)
	{
		final EntityCursor<BerkeleyDBHookEntity> cursor = transaction.entities(index, fromPriority, true, toPriority, false);
		return new CloseableIterator<Hook>()
		{
			private BerkeleyDBHookEntity next;

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
			public Hook next()
			{
				if (next == null)
					throw new NoSuchElementException();
				BerkeleyDBHookEntity entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
				return persistenceManager.entityToHook(entity);
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
	public CloseableIterator<Hook> iterator()
	{
		return iterator(null, null);
	}

	@Override
	public int size()
	{
		EntityCursor<Long> cursor = transaction.keys(index);
		try
		{
			int n = 0;
			while (true)
			{
				Long k = transaction.next(cursor);
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
		EntityCursor<Long> cursor = transaction.keys(index);
		try
		{
			Long k = transaction.first(cursor);
			return k == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public Hook getNullOverflow(int i)
	{
		if (i < 0)
			throw new NoSuchElementException();
		EntityCursor<BerkeleyDBHookEntity> cursor = transaction.entities(index);
		try
		{
			int n = 0;
			while (true)
			{
				BerkeleyDBHookEntity e = transaction.next(cursor);
				if (e == null)
					return null;
				n++;
				if (n > i)
					return persistenceManager.entityToHook(e);
			}
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public Hook get(int i)
	{
		Hook hook = getNullOverflow(i);
		if (hook == null)
			throw new NoSuchElementException();
		return hook;
	}

	@Override
	public CloseableIterable<Hook> tail(final long fromPriority)
	{
		return new CloseableIterable<Hook>()
		{

			@Override
			public CloseableIterator<Hook> iterator()
			{
				return BerkeleyDBHookList.this.iterator(fromPriority, null);
			}
		};
	}

}
