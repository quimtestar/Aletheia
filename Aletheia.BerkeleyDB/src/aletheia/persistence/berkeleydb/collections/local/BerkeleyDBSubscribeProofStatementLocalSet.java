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
package aletheia.persistence.berkeleydb.collections.local;

import java.util.NoSuchElementException;
import java.util.UUID;

import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBStatementLocalEntity;
import aletheia.persistence.collections.local.SubscribeProofStatementLocalSet;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

public class BerkeleyDBSubscribeProofStatementLocalSet extends AbstractCloseableSet<StatementLocal> implements SubscribeProofStatementLocalSet
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final ContextLocal contextLocal;
	private final EntityIndex<UUIDKey, BerkeleyDBStatementLocalEntity> index;

	public BerkeleyDBSubscribeProofStatementLocalSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			ContextLocal contextLocal)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.contextLocal = contextLocal;
			UUID statementUuid = null;
			if (contextLocal != null)
				statementUuid = contextLocal.getStatementUuid();
			this.index = persistenceManager.getEntityStore().statementLocalEntitySubscribeProofSecondaryIndex()
					.subIndex(new BerkeleyDBStatementLocalEntity.ContextSubscribeProofSecondaryKeyData(statementUuid, true));
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	protected BerkeleyDBSubscribeProofStatementLocalSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		this(persistenceManager, transaction, null);
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

	protected ContextLocal getContextLocal()
	{
		return contextLocal;
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof StatementLocal))
			return false;
		StatementLocal statementLocal = ((StatementLocal) o).refresh(transaction);
		return statementLocal.getContextUuid().equals(contextLocal.getStatementUuid()) && statementLocal.isSubscribeProof();
	}

	@Override
	public CloseableIterator<StatementLocal> iterator()
	{
		final EntityCursor<BerkeleyDBStatementLocalEntity> cursor = transaction.entities(index);
		return new CloseableIterator<StatementLocal>()
		{

			private BerkeleyDBStatementLocalEntity next;
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
			public StatementLocal next()
			{
				if (next == null)
					throw new NoSuchElementException();
				final BerkeleyDBStatementLocalEntity entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
				return persistenceManager.entityToStatementLocal(entity);
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
		EntityCursor<BerkeleyDBStatementLocalEntity> cursor = transaction.entities(index);
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

}
