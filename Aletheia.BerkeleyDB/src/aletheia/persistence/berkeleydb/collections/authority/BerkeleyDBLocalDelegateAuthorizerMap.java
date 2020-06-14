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
package aletheia.persistence.berkeleydb.collections.authority;

import java.util.NoSuchElementException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.Person;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity;
import aletheia.persistence.collections.authority.LocalDelegateAuthorizerMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

public class BerkeleyDBLocalDelegateAuthorizerMap extends AbstractCloseableMap<Person, DelegateAuthorizer> implements LocalDelegateAuthorizerMap
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final DelegateTreeNode delegateTreeNode;
	private final EntityIndex<PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> index;

	public BerkeleyDBLocalDelegateAuthorizerMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			DelegateTreeNode delegateTreeNode)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.delegateTreeNode = delegateTreeNode;
			this.index = persistenceManager.getEntityStore().delegateAuthorizerEntityTreeNodeSubindex(
					new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(delegateTreeNode.getStatementUuid(), delegateTreeNode.getPrefix()));
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
	public Transaction getTransaction()
	{
		return transaction;
	}

	public DelegateTreeNode getDelegateTreeNode()
	{
		return delegateTreeNode;
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBDelegateAuthorizerEntity> cursor = transaction.entities(index);
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
		EntityCursor<PrimaryKeyData> cursor = transaction.keys(index);
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
	public boolean containsKey(Object key)
	{
		if (key == null)
			return false;
		if (!(key instanceof Person))
			return false;
		Person person = (Person) key;
		BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData pk = new BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData(delegateTreeNode.getStatementUuid(),
				delegateTreeNode.getPrefix(), person.getUuid());
		return transaction.contains(index, pk);
	}

	@Override
	public DelegateAuthorizer get(Object key)
	{
		if (key == null)
			return null;
		if (!(key instanceof Person))
			return null;
		Person person = (Person) key;
		BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData pk = new BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData(delegateTreeNode.getStatementUuid(),
				delegateTreeNode.getPrefix(), person.getUuid());
		BerkeleyDBDelegateAuthorizerEntity entity = transaction.get(index, pk);
		if (entity == null)
			return null;
		return persistenceManager.entityToDelegateAuthorizer(entity);
	}

	@Override
	public CloseableSet<Entry<Person, DelegateAuthorizer>> entrySet()
	{
		return new AbstractCloseableSet<>()
		{

			@Override
			public CloseableIterator<Entry<Person, DelegateAuthorizer>> iterator()
			{
				final EntityCursor<BerkeleyDBDelegateAuthorizerEntity> cursor = transaction.entities(index);
				return new CloseableIterator<>()
				{
					private BerkeleyDBDelegateAuthorizerEntity next;

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
					public Entry<Person, DelegateAuthorizer> next()
					{
						if (next == null)
							throw new NoSuchElementException();
						final BerkeleyDBDelegateAuthorizerEntity entity = next;
						next = transaction.next(cursor);
						if (next == null)
							transaction.close(cursor);
						return new Entry<>()
						{

							@Override
							public Person getKey()
							{
								return persistenceManager.getPerson(transaction, entity.getDelegateUuid());
							}

							@Override
							public DelegateAuthorizer getValue()
							{
								return persistenceManager.entityToDelegateAuthorizer(entity);
							}

							@Override
							public DelegateAuthorizer setValue(DelegateAuthorizer value)
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
				return BerkeleyDBLocalDelegateAuthorizerMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return BerkeleyDBLocalDelegateAuthorizerMap.this.isEmpty();
			}

		};
	}

}
