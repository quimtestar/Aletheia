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
package aletheia.persistence.berkeleydb.collections.statement;

import java.util.Comparator;
import java.util.NoSuchElementException;

import aletheia.model.statement.Statement;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.LocalSortKey;
import aletheia.persistence.collections.statement.SortedStatements;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableCollection;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public abstract class BerkeleyDBSortedStatements<S extends Statement> extends AbstractCloseableCollection<S> implements SortedStatements<S>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final SecondaryIndex<LocalSortKey, UUIDKey, BerkeleyDBStatementEntity> index;
	private final LocalSortKey from;
	private final LocalSortKey to;

	protected BerkeleyDBSortedStatements(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from, LocalSortKey to)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.index = persistenceManager.getEntityStore().statementEntityLocalSortKeySecondaryIndex();
			this.from = from;
			this.to = to;
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
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

	private final static Comparator<Statement> comparator = new Comparator<Statement>()
	{

		@Override
		public int compare(Statement st1, Statement st2)
		{
			return ((BerkeleyDBStatementEntity) st1.getEntity()).getLocalSortKey().compareTo(((BerkeleyDBStatementEntity) st2.getEntity()).getLocalSortKey());
		}
	};

	@Override
	public Comparator<? super S> comparator()
	{
		return comparator;
	}

	protected abstract S entitytoStatement(BerkeleyDBStatementEntity entity);

	@Override
	public S first()
	{
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, true, to, false);
		try
		{
			BerkeleyDBStatementEntity entity = transaction.first(cursor);
			if (entity == null)
				throw new NoSuchElementException();
			return entitytoStatement(entity);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public S last()
	{
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, true, to, false);
		try
		{
			BerkeleyDBStatementEntity entity = transaction.last(cursor);
			if (entity == null)
				throw new NoSuchElementException();
			return entitytoStatement(entity);
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public CloseableIterator<S> iterator()
	{
		final EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, true, to, false);
		return new CloseableIterator<S>()
		{
			private BerkeleyDBStatementEntity next;
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
			public S next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				BerkeleyDBStatementEntity entity = next;
				next = transaction.next(cursor);
				return entitytoStatement(entity);
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
				super.finalize();
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
		CloseableIterator<S> iterator = iterator();
		try
		{
			int n = 0;
			while (iterator.hasNext())
			{
				iterator.next();
				n++;
			}
			return n;
		}
		finally
		{
			iterator.close();
		}

	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, true, to, false);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	private LocalSortKey fromDependencySortKey(Statement statement)
	{
		LocalSortKey dsk = ((BerkeleyDBStatementEntity) statement.getEntity()).getLocalSortKey();
		if (from.compareTo(dsk) > 0)
			return dsk;
		return from;
	}

	private LocalSortKey toDependencySortKey(Statement statement)
	{
		LocalSortKey dsk = ((BerkeleyDBStatementEntity) statement.getEntity()).getLocalSortKey();
		if (to.compareTo(dsk) < 0)
			return dsk;
		return to;
	}

	protected abstract BerkeleyDBSortedStatements<S> newBerkeleyDBSortedStatementsBounds(LocalSortKey from, LocalSortKey to);

	@Override
	public BerkeleyDBSortedStatements<S> subSet(S fromElement, S toElement)
	{
		return newBerkeleyDBSortedStatementsBounds(fromDependencySortKey(fromElement), toDependencySortKey(toElement));
	}

	@Override
	public BerkeleyDBSortedStatements<S> headSet(S toElement)
	{
		return newBerkeleyDBSortedStatementsBounds(from, toDependencySortKey(toElement));
	}

	@Override
	public BerkeleyDBSortedStatements<S> tailSet(S fromElement)
	{
		return newBerkeleyDBSortedStatementsBounds(fromDependencySortKey(fromElement), to);
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