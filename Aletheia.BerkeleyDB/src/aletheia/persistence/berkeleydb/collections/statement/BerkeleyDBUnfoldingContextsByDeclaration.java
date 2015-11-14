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

import aletheia.model.statement.Declaration;
import aletheia.model.statement.UnfoldingContext;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBUnfoldingContextEntity;
import aletheia.persistence.collections.statement.UnfoldingContextsByDeclaration;
import aletheia.persistence.entities.statement.UnfoldingContextEntity;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBUnfoldingContextsByDeclaration extends AbstractCloseableSet<UnfoldingContext> implements UnfoldingContextsByDeclaration
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBUnfoldingContextEntity> unfoldingContextEntityDeclarationSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final Declaration declaration;
	private final UUIDKey uuidKey;

	public BerkeleyDBUnfoldingContextsByDeclaration(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Declaration declaration)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.unfoldingContextEntityDeclarationSecondaryIndex = persistenceManager.getEntityStore().unfoldingContextEntityDeclarationSecondaryIndex();
			this.transaction = transaction;
			this.declaration = declaration;
			this.uuidKey = ((BerkeleyDBStatementEntity) declaration.getEntity()).getUuidKey();
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
	public Declaration getDeclaration()
	{
		return declaration;
	}

	@Override
	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableIterator<UnfoldingContext> iterator()
	{
		final EntityCursor<BerkeleyDBUnfoldingContextEntity> cursor = transaction.entities(unfoldingContextEntityDeclarationSecondaryIndex, uuidKey, true,
				uuidKey, true);
		return new CloseableIterator<UnfoldingContext>()
		{
			UnfoldingContextEntity next;

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
			public UnfoldingContext next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				UnfoldingContextEntity entity = next;
				next = transaction.next(cursor);
				return persistenceManager.unfoldingContextEntityToStatement(entity);
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
		EntityCursor<BerkeleyDBUnfoldingContextEntity> cursor = transaction.entities(unfoldingContextEntityDeclarationSecondaryIndex, uuidKey, true, uuidKey,
				true);
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
		EntityCursor<UUIDKey> cursor = transaction.keys(unfoldingContextEntityDeclarationSecondaryIndex, uuidKey, true, uuidKey, true);
		try
		{
			return transaction.first(cursor) == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

}
