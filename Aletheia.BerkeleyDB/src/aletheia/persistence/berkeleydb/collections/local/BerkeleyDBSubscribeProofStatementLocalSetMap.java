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
package aletheia.persistence.berkeleydb.collections.local;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

import aletheia.model.local.ContextLocal;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBStatementLocalEntity;
import aletheia.persistence.collections.local.SubscribeProofStatementLocalSet;
import aletheia.persistence.collections.local.SubscribeProofStatementLocalSetMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

public class BerkeleyDBSubscribeProofStatementLocalSetMap extends AbstractCloseableMap<ContextLocal, SubscribeProofStatementLocalSet>
		implements SubscribeProofStatementLocalSetMap
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final EntityIndex<UUIDKey, BerkeleyDBStatementLocalEntity> index;

	public BerkeleyDBSubscribeProofStatementLocalSetMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.index = persistenceManager.getEntityStore().statementLocalEntityContextSecondaryIndex();
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
	public boolean containsKey(Object key)
	{
		if (!(key instanceof ContextLocal))
			return false;
		ContextLocal contextLocal = (ContextLocal) key;
		return transaction.contains(index, new UUIDKey(contextLocal.getContextUuid()));
	}

	@Override
	public SubscribeProofStatementLocalSet get(Object key)
	{
		if (!containsKey(key))
			return null;
		return ((ContextLocal) key).subscribeProofStatementLocalSet(transaction);
	}

	@Override
	public CloseableSet<Entry<ContextLocal, SubscribeProofStatementLocalSet>> entrySet()
	{
		return new AbstractCloseableSet<>()
		{

			@Override
			public CloseableIterator<Entry<ContextLocal, SubscribeProofStatementLocalSet>> iterator()
			{
				final EntityCursor<BerkeleyDBStatementLocalEntity> cursor = transaction.entities(index);
				return new CloseableIterator<>()
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
					public Entry<ContextLocal, SubscribeProofStatementLocalSet> next()
					{
						BerkeleyDBStatementLocalEntity entity = next;
						next = transaction.nextNoDup(cursor);
						if (next == null)
							transaction.close(cursor);
						final ContextLocal contextLocal = (ContextLocal) persistenceManager.getStatementLocal(transaction, entity.getContextUuid());
						final SubscribeProofStatementLocalSet subscribeProofStatementLocalSet = contextLocal.subscribeProofStatementLocalSet(transaction);
						return new Entry<>()
						{

							@Override
							public ContextLocal getKey()
							{
								return contextLocal;
							}

							@Override
							public SubscribeProofStatementLocalSet getValue()
							{
								return subscribeProofStatementLocalSet;
							}

							@Override
							public SubscribeProofStatementLocalSet setValue(SubscribeProofStatementLocalSet value)
							{
								throw new UnsupportedOperationException();
							}

						};
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

				};
			}

			@Override
			public int size()
			{
				return BerkeleyDBSubscribeProofStatementLocalSetMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return BerkeleyDBSubscribeProofStatementLocalSetMap.this.isEmpty();
			}

		};

	}

	@Override
	public int size()
	{
		EntityCursor<UUIDKey> cursor = transaction.keys(index);
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
