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
import java.util.UUID;

import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity.ContextSubContextSecondaryKeyData;
import aletheia.persistence.collections.authority.SignatureRequestContextSubContextUuidsCollection;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.AbstractCloseableCollection;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBSignatureRequestContextSubContextUuidsCollection extends AbstractCloseableCollection<UUID>
		implements SignatureRequestContextSubContextUuidsCollection
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final SecondaryIndex<ContextSubContextSecondaryKeyData, UUIDKey, BerkeleyDBSignatureRequestEntity> index;
	private final UUID contextUuid;
	private final ContextSubContextSecondaryKeyData minContextSubContextSecondaryKeyData;
	private final ContextSubContextSecondaryKeyData maxContextSubContextSecondaryKeyData;

	public BerkeleyDBSignatureRequestContextSubContextUuidsCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			UUID contextUuid)
	{
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.index = persistenceManager.getEntityStore().signatureRequestEntityContextSubContextSecondaryIndex();
		this.contextUuid = contextUuid;
		this.minContextSubContextSecondaryKeyData = ContextSubContextSecondaryKeyData.min(contextUuid);
		this.maxContextSubContextSecondaryKeyData = ContextSubContextSecondaryKeyData.max(contextUuid);
	}

	public BerkeleyDBSignatureRequestContextSubContextUuidsCollection(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
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

	@Override
	public UUID getContextUuid()
	{
		return contextUuid;
	}

	@Override
	public CloseableIterator<UUID> iterator()
	{
		final EntityCursor<ContextSubContextSecondaryKeyData> cursor = transaction.keys(index, minContextSubContextSecondaryKeyData, true,
				maxContextSubContextSecondaryKeyData, true);
		return new CloseableIterator<>()
		{
			private ContextSubContextSecondaryKeyData next;

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
			public UUID next()
			{
				if (next == null)
					throw new NoSuchElementException();
				ContextSubContextSecondaryKeyData keyData = next;
				next = transaction.nextNoDup(cursor);
				if (next == null)
					transaction.close(cursor);
				return keyData.getSubContextUuid();
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
		EntityCursor<ContextSubContextSecondaryKeyData> cursor = transaction.keys(index, minContextSubContextSecondaryKeyData, true,
				maxContextSubContextSecondaryKeyData, true);
		try
		{
			int n = 0;
			while (true)
			{
				ContextSubContextSecondaryKeyData k = transaction.nextNoDup(cursor);
				if (k == null)
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
		EntityCursor<ContextSubContextSecondaryKeyData> cursor = transaction.keys(index, minContextSubContextSecondaryKeyData, true,
				maxContextSubContextSecondaryKeyData, true);
		try
		{
			ContextSubContextSecondaryKeyData k = transaction.first(cursor);
			return k == null;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof UUID))
			return false;
		UUID uuid = (UUID) o;
		ContextSubContextSecondaryKeyData k = new ContextSubContextSecondaryKeyData(contextUuid, uuid);
		return transaction.contains(index, k);
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
