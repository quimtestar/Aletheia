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

import java.util.AbstractSet;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;

import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureMap;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;

public class BerkeleyDBStatementAuthoritySignatureMap extends AbstractCloseableMap<Signatory, StatementAuthoritySignature>
		implements StatementAuthoritySignatureMap
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;
	private final StatementAuthority statementAuthority;
	private final UUID statementUuid;
	private final UUIDKey statementUuidKey;
	private final EntityIndex<BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> index;

	public BerkeleyDBStatementAuthoritySignatureMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			StatementAuthority statementAuthority)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.transaction = transaction;
			this.statementAuthority = statementAuthority;
			this.statementUuid = statementAuthority.getStatementUuid();
			this.statementUuidKey = new UUIDKey(statementUuid);
			this.index = persistenceManager.getEntityStore().statementAuthoritySignatureEntityStatementSecondaryIndex().subIndex(statementUuidKey);
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
	public StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	public UUID getStatementUuid()
	{
		return statementUuid;
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (!(key instanceof Signatory))
			return false;
		Signatory signatory = (Signatory) key;
		BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData pk = new BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData(statementUuid,
				signatory.getUuid());
		return transaction.contains(index, pk);
	}

	@Override
	public StatementAuthoritySignature get(Object key)
	{
		if (!(key instanceof Signatory))
			return null;
		Signatory signatory = (Signatory) key;
		BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData pk = new BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData(statementUuid,
				signatory.getUuid());
		BerkeleyDBStatementAuthoritySignatureEntity entity = transaction.get(index, pk);
		if (entity == null)
			return null;
		return new StatementAuthoritySignature(persistenceManager, entity);
	}

	private class EntrySet extends AbstractSet<Entry<Signatory, StatementAuthoritySignature>>
			implements CloseableSet<Entry<Signatory, StatementAuthoritySignature>>
	{
		private EntrySet()
		{
			super();
		}

		@Override
		public CloseableIterator<Entry<Signatory, StatementAuthoritySignature>> iterator()
		{
			final EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity> cursor = transaction.entities(index);
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
				public Entry<Signatory, StatementAuthoritySignature> next()
				{
					if (nextEntity == null)
						throw new NoSuchElementException();
					final StatementAuthoritySignature statementAuthoritySignature = new StatementAuthoritySignature(persistenceManager, nextEntity);
					nextEntity = transaction.next(cursor);
					if (nextEntity == null)
						transaction.close(cursor);
					return new Entry<>()
					{

						@Override
						public Signatory getKey()
						{
							return statementAuthoritySignature.getAuthorizer(transaction);
						}

						@Override
						public StatementAuthoritySignature getValue()
						{
							return statementAuthoritySignature;
						}

						@Override
						public StatementAuthoritySignature setValue(StatementAuthoritySignature value)
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
			return BerkeleyDBStatementAuthoritySignatureMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return BerkeleyDBStatementAuthoritySignatureMap.this.isEmpty();
		}
	}

	@Override
	public CloseableSet<Entry<Signatory, StatementAuthoritySignature>> entrySet()
	{
		return new EntrySet();
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData> cursor = transaction.keys(index);
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
		EntityCursor<BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData> cursor = transaction.keys(index);
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
