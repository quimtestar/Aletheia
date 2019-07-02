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

import java.util.NoSuchElementException;

import aletheia.model.statement.Statement;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.DependentsSet;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBDependentsSet extends AbstractCloseableSet<Statement> implements DependentsSet
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementEntity> statementEntityDependenciesSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final Statement statement;
	private final UUIDKey uuidKey;

	public BerkeleyDBDependentsSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Statement statement)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.statementEntityDependenciesSecondaryIndex = persistenceManager.getEntityStore().statementEntityDependenciesSecondaryIndex();
			this.transaction = transaction;
			this.statement = statement;
			this.uuidKey = ((BerkeleyDBStatementEntity) statement.getEntity()).getUuidKey();
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

	@Override
	public Statement getStatement()
	{
		return statement;
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof Statement))
			return false;
		Statement statement = (Statement) o;
		return ((BerkeleyDBStatementEntity) statement.getEntity()).getUuidKeyDependencies().contains(uuidKey);
	}

	@Override
	public CloseableIterator<Statement> iterator()
	{
		final EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityDependenciesSecondaryIndex, uuidKey, true, uuidKey, true);
		return new CloseableIterator<Statement>()
		{
			BerkeleyDBStatementEntity next;

			{
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
			}

			@Override
			public boolean hasNext()
			{
				return next != null;
			}

			@Override
			public Statement next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				BerkeleyDBStatementEntity entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
				return persistenceManager.entityToStatement(entity);
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
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityDependenciesSecondaryIndex, uuidKey, true, uuidKey, true);
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
		EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityDependenciesSecondaryIndex, uuidKey, true, uuidKey, true);
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
