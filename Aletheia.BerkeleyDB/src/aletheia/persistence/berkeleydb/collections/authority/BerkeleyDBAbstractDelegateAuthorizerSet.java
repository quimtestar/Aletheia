/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import com.sleepycat.persist.PrimaryIndex;

import aletheia.model.authority.DelegateAuthorizer;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData;
import aletheia.persistence.collections.authority.DelegateAuthorizerSet;
import aletheia.persistence.entities.authority.DelegateAuthorizerEntity;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

public abstract class BerkeleyDBAbstractDelegateAuthorizerSet extends AbstractCloseableSet<DelegateAuthorizer> implements DelegateAuthorizerSet
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final PrimaryIndex<PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> primaryIndex;

	public BerkeleyDBAbstractDelegateAuthorizerSet(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.primaryIndex = persistenceManager.getEntityStore().delegateAuthorizerEntityPrimaryIndex();
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

	protected PrimaryIndex<PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> getPrimaryIndex()
	{
		return primaryIndex;
	}

	protected abstract EntityIndex<PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> index();

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof DelegateAuthorizer))
			return false;
		DelegateAuthorizer da = (DelegateAuthorizer) o;
		return transaction.contains(index(), new PrimaryKeyData(da.getStatementUuid(), da.getPrefix(), da.getDelegateUuid()));
	}

	@Override
	public CloseableIterator<DelegateAuthorizer> iterator()
	{
		final EntityCursor<BerkeleyDBDelegateAuthorizerEntity> cursor = transaction.entities(index());
		return new CloseableIterator<>()
		{
			private DelegateAuthorizerEntity next;

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
			public DelegateAuthorizer next()
			{
				if (next == null)
					throw new NoSuchElementException();
				DelegateAuthorizerEntity entity = next;
				next = transaction.next(cursor);
				if (next == null)
					transaction.close(cursor);
				return persistenceManager.entityToDelegateAuthorizer(entity);
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
