/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
package aletheia.persistence.berkeleydb.collections.statement;

import java.util.NoSuchElementException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.statement.Context;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.SubContextsSet;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

public class BerkeleyDBSubContextsSet extends AbstractCloseableSet<Context> implements SubContextsSet
{

	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBContextEntity> contextEntityContextSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final Context context;
	private final UUIDKey uuidKey;

	public BerkeleyDBSubContextsSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.contextEntityContextSecondaryIndex = persistenceManager.getEntityStore().contextEntityContextSecondaryIndex();
			this.transaction = transaction;
			this.context = context;
			this.uuidKey = ((BerkeleyDBStatementEntity) context.getEntity()).getUuidKey();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}

	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof Context))
			return false;
		Context context = (Context) o;
		return uuidKey.equals(((BerkeleyDBStatementEntity) context.getEntity()).getUuidKeyContext());
	}

	@Override
	public CloseableIterator<Context> iterator()
	{
		final EntityCursor<BerkeleyDBContextEntity> cursor = transaction.entities(contextEntityContextSecondaryIndex, uuidKey, true, uuidKey, true);
		return new CloseableIterator<>()
		{
			BerkeleyDBContextEntity next;

			{
				next = transaction.next(cursor);
			}

			@Override
			public boolean hasNext()
			{
				if (next == null)
				{
					transaction.close(cursor);
					return false;
				}
				return true;
			}

			@Override
			public Context next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				BerkeleyDBContextEntity entity = next;
				next = transaction.next(cursor);
				return persistenceManager.contextEntityToStatement(entity);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected void finalize() throws Throwable
			{
				close();
			}

			@Override
			public void close()
			{
				transaction.close(cursor);
			}

		};

	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBContextEntity> cursor = transaction.entities(contextEntityContextSecondaryIndex, uuidKey, true, uuidKey, true);
		try
		{
			if (transaction.first(cursor) == null)
				return 0;
			return transaction.count(cursor);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(contextEntityContextSecondaryIndex, uuidKey, true, uuidKey, true);
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
	public Object[] toArray()
	{
		return MiscUtilities.iterableToArray(this);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return MiscUtilities.iterableToArray(this, a);
	}

}
