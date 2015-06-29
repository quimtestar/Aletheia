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

import aletheia.model.identifier.Identifier;
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

public abstract class BerkeleyDBSortedStatements<S extends Statement> extends AbstractCloseableCollection<S>implements SortedStatements<S>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final SecondaryIndex<LocalSortKey, UUIDKey, BerkeleyDBStatementEntity> index;
	private final LocalSortKey from;
	private final boolean fromInclusive;
	private final LocalSortKey to;
	private final boolean toInclusive;

	protected BerkeleyDBSortedStatements(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from,
			boolean fromInclusive, LocalSortKey to, boolean toInclusive)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.index = persistenceManager.getEntityStore().statementEntityLocalSortKeySecondaryIndex();
			this.from = from;
			this.fromInclusive = fromInclusive;
			this.to = to;
			this.toInclusive = toInclusive;
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	protected BerkeleyDBSortedStatements(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from, LocalSortKey to)
	{
		this(persistenceManager, transaction, from, true, to, false);
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
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, fromInclusive, to, toInclusive);
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
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, fromInclusive, to, toInclusive);
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
	public boolean contains(Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			S statement = (S) o;
			LocalSortKey key = ((BerkeleyDBStatementEntity) statement.getEntity()).getLocalSortKey();
			int cf = from.compareTo(key);
			int ct = to.compareTo(key);
			if (!((cf < 0 || (fromInclusive && cf <= 0)) && (ct > 0 || (toInclusive && ct >= 0))))
				return false;
			return transaction.contains(index.subIndex(key), new UUIDKey(statement.getUuid()));
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public CloseableIterator<S> iterator()
	{
		final EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, fromInclusive, to, toInclusive);
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
	public boolean smaller(int size)
	{
		CloseableIterator<S> iterator = iterator();
		try
		{
			int n = 0;
			while (iterator.hasNext())
			{
				iterator.next();
				n++;
				if (n >= size)
					return false;
			}
		}
		finally
		{
			iterator.close();
		}
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(index, from, fromInclusive, to, toInclusive);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	private LocalSortKey fromSortKey(Statement statement)
	{
		LocalSortKey dsk = ((BerkeleyDBStatementEntity) statement.getEntity()).getLocalSortKey();
		if (from.compareTo(dsk) > 0)
			return from;
		return dsk;
	}

	private LocalSortKey toSortKey(Statement statement)
	{
		LocalSortKey dsk = ((BerkeleyDBStatementEntity) statement.getEntity()).getLocalSortKey();
		if (to.compareTo(dsk) < 0)
			return to;
		return dsk;
	}

	private LocalSortKey fromSortKey(Identifier identifier)
	{
		LocalSortKey dsk = new LocalSortKey();
		dsk.setUuidKeyContext(from.getUuidKeyContext());
		dsk.setAssumptionOrder(Integer.MAX_VALUE);
		dsk.setIdentifier(identifier);
		if (from.compareTo(dsk) > 0)
			return from;
		return dsk;
	}

	private LocalSortKey toSortKey(Identifier identifier)
	{
		LocalSortKey dsk = new LocalSortKey();
		dsk.setUuidKeyContext(to.getUuidKeyContext());
		dsk.setAssumptionOrder(Integer.MAX_VALUE);
		dsk.setIdentifier(identifier);
		if (to.compareTo(dsk) < 0)
			return to;
		return dsk;
	}

	protected abstract BerkeleyDBSortedStatements<S> newBerkeleyDBSortedStatementsBounds(LocalSortKey from, boolean fromInclusive, LocalSortKey to,
			boolean toInclusive);

	protected BerkeleyDBSortedStatements<S> newBerkeleyDBSortedStatementsBounds(LocalSortKey from, LocalSortKey to)
	{
		return newBerkeleyDBSortedStatementsBounds(from, true, to, false);
	}

	@Override
	public BerkeleyDBSortedStatements<S> subSet(S fromElement, S toElement)
	{
		return newBerkeleyDBSortedStatementsBounds(fromSortKey(fromElement), toSortKey(toElement));
	}

	@Override
	public BerkeleyDBSortedStatements<S> headSet(S toElement)
	{
		return newBerkeleyDBSortedStatementsBounds(from, toSortKey(toElement));
	}

	@Override
	public BerkeleyDBSortedStatements<S> tailSet(S fromElement)
	{
		return newBerkeleyDBSortedStatementsBounds(fromSortKey(fromElement), to);
	}

	@Override
	public BerkeleyDBSortedStatements<S> subSet(Identifier from, Identifier to)
	{
		return newBerkeleyDBSortedStatementsBounds(fromSortKey(from), toSortKey(to));
	}

	@Override
	public BerkeleyDBSortedStatements<S> headSet(Identifier to)
	{
		return newBerkeleyDBSortedStatementsBounds(from, toSortKey(to));
	}

	@Override
	public BerkeleyDBSortedStatements<S> tailSet(Identifier from)
	{
		return newBerkeleyDBSortedStatementsBounds(fromSortKey(from), to);
	}

	@Override
	public SortedStatements<S> identifierSet(Identifier identifier)
	{
		return newBerkeleyDBSortedStatementsBounds(fromSortKey(identifier), true, toSortKey(identifier), true);
	}

	@Override
	public SortedStatements<S> postIdentifierSet(Identifier identifier)
	{
		return newBerkeleyDBSortedStatementsBounds(fromSortKey(identifier), false, to, false);
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
