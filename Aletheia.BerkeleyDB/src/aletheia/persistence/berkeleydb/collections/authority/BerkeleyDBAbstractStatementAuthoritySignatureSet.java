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
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSet;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;

public abstract class BerkeleyDBAbstractStatementAuthoritySignatureSet<K> extends AbstractCloseableSet<StatementAuthoritySignature>
		implements StatementAuthoritySignatureSet
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<K, BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> secondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final K key;
	private final EntityIndex<BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> subIndex;

	public BerkeleyDBAbstractStatementAuthoritySignatureSet(BerkeleyDBPersistenceManager persistenceManager,
			SecondaryIndex<K, BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> secondaryIndex,
			BerkeleyDBTransaction transaction, K key)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.secondaryIndex = secondaryIndex;
		this.transaction = transaction;
		this.key = key;
		try
		{
			this.subIndex = secondaryIndex.subIndex(key);
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

	public K getKey()
	{
		return key;
	}

	@Override
	public CloseableIterator<StatementAuthoritySignature> iterator()
	{
		final EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = transaction.entities(subIndex);
		return new CloseableIterator<>()
		{
			BerkeleyDBStatementAuthoritySignatureEntity nextEntity;

			{
				nextEntity = transaction.first(cursor);
				if (nextEntity == null)
					transaction.close(cursor);
			}

			@Override
			public boolean hasNext()
			{
				return nextEntity != null;
			}

			@Override
			public StatementAuthoritySignature next()
			{
				if (nextEntity == null)
					throw new NoSuchElementException();
				StatementAuthoritySignature statementAuthoritySignature = new StatementAuthoritySignature(persistenceManager, nextEntity);
				nextEntity = transaction.next(cursor);
				if (nextEntity == null)
					transaction.close(cursor);
				return statementAuthoritySignature;
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
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = getTransaction().entities(secondaryIndex, key, true, key, true);
		try
		{
			BerkeleyDBStatementAuthoritySignatureEntity entity = transaction.first(cursor);
			if (entity == null)
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
		return !getTransaction().contains(secondaryIndex, key);
	}

	@Override
	public boolean contains(Object o)
	{
		if (!(o instanceof StatementAuthoritySignature))
			return false;
		StatementAuthoritySignature sas = (StatementAuthoritySignature) o;
		BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData pkd = new BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData(sas.getStatementUuid(),
				sas.getAuthorizerUuid());
		return transaction.contains(subIndex, pkd);
	}

}
