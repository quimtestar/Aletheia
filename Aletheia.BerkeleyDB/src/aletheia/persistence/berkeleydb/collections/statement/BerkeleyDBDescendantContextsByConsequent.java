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

import java.util.Iterator;
import java.util.NoSuchElementException;

import aletheia.model.statement.Context;
import aletheia.model.term.SimpleTerm;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.UUIDKeyTermHash;
import aletheia.persistence.collections.statement.DescendantContextsByConsequent;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBDescendantContextsByConsequent extends AbstractCloseableSet<Context> implements DescendantContextsByConsequent
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKeyTermHash, UUIDKey, BerkeleyDBContextEntity> contextEntityConsequentHashSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final Context context;
	private final UUIDKey uuidKeyContext;
	private final SimpleTerm consequent;

	public BerkeleyDBDescendantContextsByConsequent(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context,
			SimpleTerm consequent)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.contextEntityConsequentHashSecondaryIndex = persistenceManager.getEntityStore().contextEntityConsequentHashSecondaryIndex();
			this.transaction = transaction;
			this.context = context;
			this.uuidKeyContext = ((BerkeleyDBStatementEntity) context.getEntity()).getUuidKey();
			this.consequent = consequent;
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

	@Override
	public SimpleTerm getConsequent()
	{
		return consequent;
	}

	@Override
	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableIterator<Context> iterator()
	{
		UUIDKeyTermHash uuidKeyTermHash = new UUIDKeyTermHash();
		uuidKeyTermHash.setUUIDKey(uuidKeyContext);
		uuidKeyTermHash.setTermHash(consequent.hashCode());
		final EntityCursor<BerkeleyDBContextEntity> cursor = transaction.entities(contextEntityConsequentHashSecondaryIndex, uuidKeyTermHash, true,
				uuidKeyTermHash, true);

		return new CloseableIterator<Context>()
				{
			BerkeleyDBContextEntity next;
			{
				do
				{
					next = transaction.next(cursor);
					if (next == null)
					{
						transaction.close(cursor);
						break;
					}

				} while (!next.getConsequent().equals(consequent));
			}

			@Override
			public boolean hasNext()
			{
				return next != null;
			}

			@Override
			public Context next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				BerkeleyDBContextEntity entity = next;
				do
				{
					next = transaction.next(cursor);
					if (next == null)
					{
						transaction.close(cursor);
						break;
					}
				} while (!next.getConsequent().equals(consequent));
				return persistenceManager.contextEntityToStatement(entity);
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
		int j = 0;
		for (Iterator<Context> i = iterator(); i.hasNext(); i.next())
			j++;
		return j;
	}

	@Override
	public boolean isEmpty()
	{
		CloseableIterator<Context> i = iterator();
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
