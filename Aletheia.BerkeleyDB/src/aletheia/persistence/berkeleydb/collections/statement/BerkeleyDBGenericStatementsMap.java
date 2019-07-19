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

import aletheia.model.statement.Statement;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.GenericStatementsMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public abstract class BerkeleyDBGenericStatementsMap<S extends Statement, E extends BerkeleyDBStatementEntity>
		extends AbstractCloseableMap<IdentifiableVariableTerm, S> implements GenericStatementsMap<S>
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final EntityIndex<UUIDKey, E> index;
	private final BerkeleyDBTransaction transaction;

	public BerkeleyDBGenericStatementsMap(BerkeleyDBPersistenceManager persistenceManager, EntityIndex<UUIDKey, E> index, BerkeleyDBTransaction transaction)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.index = index;
			this.transaction = transaction;
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

	public EntityIndex<UUIDKey, E> getIndex()
	{
		return index;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	@SuppressWarnings("unchecked")
	private S entityToStatement(E entity)
	{
		return (S) persistenceManager.entityToStatement(entity);
	}

	@Override
	public S get(Object key)
	{
		if (!(key instanceof IdentifiableVariableTerm))
			return null;
		IdentifiableVariableTerm var = (IdentifiableVariableTerm) key;
		UUIDKey uuidKey = new UUIDKey(var.getUuid());
		E entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		return entityToStatement(entity);
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof IdentifiableVariableTerm))
			return false;
		IdentifiableVariableTerm var = (IdentifiableVariableTerm) key;
		UUIDKey uuidKey = new UUIDKey(var.getUuid());
		return transaction.contains(index, uuidKey);
	}

	@Override
	public S remove(Object key)
	{
		if (!(key instanceof IdentifiableVariableTerm))
			return null;
		IdentifiableVariableTerm var = (IdentifiableVariableTerm) key;
		UUIDKey uuidKey = new UUIDKey(var.getUuid());
		E entity = transaction.get(index, uuidKey);
		if (entity == null)
			return null;
		transaction.delete(index, uuidKey);
		return entityToStatement(entity);
	}

	@Override
	public int size()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(index);
		try
		{
			int n = 0;
			while (transaction.next(cursor) != null)
				n++;
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
		EntityCursor<UUIDKey> cursor = transaction.keys(index);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	/**
	 * Faster computation (possibly not accurate) of the size of this map.
	 *
	 * @return The size
	 * @see #size()
	 */
	public int fastSize()
	{
		return (int) index.count();
	}

	protected class EntrySet extends AbstractCloseableSet<Entry<IdentifiableVariableTerm, S>>
	{
		private class MyEntry implements Entry<IdentifiableVariableTerm, S>
		{
			final S statement;

			public MyEntry(S statement)
			{
				super();
				this.statement = statement;
			}

			@Override
			public IdentifiableVariableTerm getKey()
			{
				return statement.getVariable();
			}

			@Override
			public S getValue()
			{
				return statement;
			}

			@Override
			public S setValue(S arg0)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString()
			{
				return getKey() + "=" + getValue();
			}
		}

		private final EntityIndex<UUIDKey, E> index;
		private final UUIDKey from;
		private final UUIDKey to;

		public EntrySet(EntityIndex<UUIDKey, E> index, UUIDKey from, UUIDKey to)
		{
			super();
			this.index = index;
			this.from = from;
			this.to = to;
		}

		public EntrySet(EntityIndex<UUIDKey, E> index)
		{
			this(index, null, null);
		}

		public EntrySet()
		{
			this(BerkeleyDBGenericStatementsMap.this.index);
		}

		@Override
		public int size()
		{
			return BerkeleyDBGenericStatementsMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BerkeleyDBGenericStatementsMap.this.isEmpty();
		}

		@Override
		public CloseableIterator<Entry<IdentifiableVariableTerm, S>> iterator()
		{
			final EntityCursor<E> cursor = transaction.entities(index, from, true, to, true);
			return new CloseableIterator<>()
			{
				E next;

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
				public Entry<IdentifiableVariableTerm, S> next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					E entity = next;
					next = transaction.next(cursor);
					return new MyEntry(entityToStatement(entity));
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

	}

	@Override
	public EntrySet entrySet()
	{
		return new EntrySet();
	}

}
