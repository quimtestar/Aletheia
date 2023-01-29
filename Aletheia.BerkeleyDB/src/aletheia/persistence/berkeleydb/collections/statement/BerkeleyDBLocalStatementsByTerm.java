/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.UUIDKeyTermHash;
import aletheia.persistence.collections.statement.LocalStatementsByTerm;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

public class BerkeleyDBLocalStatementsByTerm extends AbstractCloseableMap<Term, CloseableSet<Statement>> implements LocalStatementsByTerm
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKeyTermHash, UUIDKey, BerkeleyDBStatementEntity> statementEntityTermHashSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final Context context;
	private final UUIDKey uuidKeyContext;

	public BerkeleyDBLocalStatementsByTerm(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.statementEntityTermHashSecondaryIndex = persistenceManager.getEntityStore().statementEntityTermHashSecondaryIndex();
			this.transaction = transaction;
			this.context = context;
			this.uuidKeyContext = ((BerkeleyDBStatementEntity) context.getEntity()).getUuidKey();
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
	public Context getContext()
	{
		return context;
	}

	private class FilteredSet extends AbstractCloseableSet<Statement>
	{
		private final Term term;

		public FilteredSet(Term term)
		{
			super();
			this.term = term;
		}

		@Override
		public boolean contains(Object o)
		{
			if (!(o instanceof Statement))
				return false;
			Statement statement = (Statement) o;
			if (!statement.getTerm().equals(term) || !persistenceManager.localStatements(transaction, context).containsKey(statement.getVariable()))
				return false;
			return true;
		}

		@Override
		public CloseableIterator<Statement> iterator()
		{
			UUIDKeyTermHash uuidKeyTermHash = new UUIDKeyTermHash();
			uuidKeyTermHash.setUUIDKey(uuidKeyContext);
			uuidKeyTermHash.setTermHash(term.hashCode());
			final EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityTermHashSecondaryIndex, uuidKeyTermHash, true,
					uuidKeyTermHash, true);
			return new CloseableIterator<>()
			{
				BerkeleyDBStatementEntity next;

				{
					do
					{
						next = transaction.next(cursor);
						if (next == null)
							break;
					} while (!next.getVariable().getType().equals(term));
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
				public Statement next()
				{
					if (!hasNext())
						throw new NoSuchElementException();
					BerkeleyDBStatementEntity entity = next;
					do
					{
						next = transaction.next(cursor);
						if (next == null)
							break;
					} while (!next.getVariable().getType().equals(term));
					return persistenceManager.entityToStatement(entity);
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
			int j = 0;
			for (Iterator<Statement> i = iterator(); i.hasNext(); i.next())
				j++;
			return j;
		}

		@Override
		public boolean isEmpty()
		{
			CloseableIterator<Statement> i = iterator();
			try
			{
				return !i.hasNext();
			}
			finally
			{
				i.close();
			}
		}

	}

	@Override
	public CloseableSet<Statement> get(Object key)
	{
		if (!(key instanceof Term))
			return null;
		Term term = (Term) key;
		return new FilteredSet(term);
	}

	@Override
	public CloseableSet<Entry<Term, CloseableSet<Statement>>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

}
