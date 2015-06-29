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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.UUIDContextIdentifier;
import aletheia.persistence.collections.statement.LocalIdentifierToStatement;
import aletheia.utilities.collections.CloseableIterator;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public abstract class BerkeleyDBLocalIdentifierToStatement extends AbstractMap<Identifier, Statement>implements LocalIdentifierToStatement
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDContextIdentifier, UUIDKey, BerkeleyDBStatementEntity> statementEntityContextIdentifierSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final UUIDKey uuidKeyContext;
	private final NodeNamespace from;
	private final NodeNamespace to;
	private final UUIDContextIdentifier uuidContextIdentifierMin;
	private final UUIDContextIdentifier uuidContextIdentifierMax;

	public BerkeleyDBLocalIdentifierToStatement(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, UUID contextUuid)
	{
		this(persistenceManager, transaction, contextUuid, null, null);
	}

	protected BerkeleyDBLocalIdentifierToStatement(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, UUID contextUuid,
			NodeNamespace from, NodeNamespace to)
	{
		this(persistenceManager, transaction, contextUuid != null ? new UUIDKey(contextUuid) : null, from, to);
	}

	private BerkeleyDBLocalIdentifierToStatement(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, UUIDKey uuidKeyContext,
			NodeNamespace from, NodeNamespace to)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.statementEntityContextIdentifierSecondaryIndex = persistenceManager.getEntityStore().statementEntityContextIdentifierSecondaryIndex();
			this.transaction = transaction;
			this.uuidKeyContext = uuidKeyContext;
			this.from = from;
			this.to = to;
			if (from == null)
				this.uuidContextIdentifierMin = UUIDContextIdentifier.minValue(uuidKeyContext);
			else
				this.uuidContextIdentifierMin = UUIDContextIdentifier.minValue(uuidKeyContext, from);
			if (to == null)
				this.uuidContextIdentifierMax = UUIDContextIdentifier.maxValue(uuidKeyContext);
			else
				this.uuidContextIdentifierMax = UUIDContextIdentifier.minValue(uuidKeyContext, to);
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

	protected NodeNamespace getFrom()
	{
		return from;
	}

	protected NodeNamespace getTo()
	{
		return to;
	}

	@Override
	public Statement get(Object o)
	{
		if (!(o instanceof Identifier))
			return null;
		Identifier id = (Identifier) o;
		UUIDContextIdentifier uuidContextIdentifier = new UUIDContextIdentifier();
		uuidContextIdentifier.setUUIDKey(uuidKeyContext);
		uuidContextIdentifier.setIdentifier(id);
		BerkeleyDBStatementEntity entity = transaction.get(statementEntityContextIdentifierSecondaryIndex, uuidContextIdentifier);
		if (entity == null)
			return null;
		return persistenceManager.entityToStatement(entity);
	}

	@Override
	public boolean containsKey(Object o)
	{
		if (!(o instanceof Identifier))
			return false;
		Identifier id = (Identifier) o;
		UUIDContextIdentifier uuidContextIdentifier = new UUIDContextIdentifier();
		uuidContextIdentifier.setUUIDKey(uuidKeyContext);
		uuidContextIdentifier.setIdentifier(id);
		return transaction.contains(statementEntityContextIdentifierSecondaryIndex, uuidContextIdentifier);
	}

	@Override
	public SortedSet<Entry<Identifier, Statement>> entrySet()
	{
		class MyEntry implements Entry<Identifier, Statement>
		{
			private final BerkeleyDBStatementEntity entity;

			public MyEntry(BerkeleyDBStatementEntity entity)
			{
				super();
				this.entity = entity;
			}

			@Override
			public Identifier getKey()
			{
				return entity.getIdentifier();
			}

			@Override
			public Statement getValue()
			{
				return persistenceManager.entityToStatement(entity);
			}

			@Override
			public Statement setValue(Statement value)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString()
			{
				return getKey() + "=" + getValue();
			}

		}
		;

		class EntrySet extends AbstractSet<Map.Entry<Identifier, Statement>>implements SortedSet<Map.Entry<Identifier, Statement>>
		{

			@Override
			public boolean contains(Object o)
			{
				if (!(o instanceof Entry<?, ?>))
					return false;
				Entry<?, ?> e = (Entry<?, ?>) o;
				Statement st = get(e.getKey());
				if (st == null)
					return false;
				return st.equals(e.getValue());
			}

			@Override
			public CloseableIterator<Map.Entry<Identifier, Statement>> iterator()
			{
				final EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityContextIdentifierSecondaryIndex,
						uuidContextIdentifierMin, true, uuidContextIdentifierMax, false);
				return new CloseableIterator<Map.Entry<Identifier, Statement>>()
				{
					BerkeleyDBStatementEntity next;

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
					public MyEntry next()
					{
						if (!hasNext())
							throw new NoSuchElementException();
						BerkeleyDBStatementEntity entity = next;
						next = transaction.next(cursor);
						return new MyEntry(entity);
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
				int n = 0;
				Iterator<?> i = iterator();
				while (i.hasNext())
				{
					i.next();
					n++;
				}
				return n;
			}

			@Override
			public Comparator<Entry<Identifier, Statement>> comparator()
			{
				final Comparator<Identifier> comp = BerkeleyDBLocalIdentifierToStatement.this.comparator();
				return new Comparator<Entry<Identifier, Statement>>()
				{

					@Override
					public int compare(Entry<Identifier, Statement> e0, Entry<Identifier, Statement> e1)
					{
						return comp.compare(e0.getKey(), e1.getKey());
					}

				};
			}

			@Override
			public MyEntry first()
			{
				EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityContextIdentifierSecondaryIndex, uuidContextIdentifierMin,
						true, uuidContextIdentifierMax, false);
				try
				{
					BerkeleyDBStatementEntity e = transaction.first(cursor);
					if (e == null)
						throw new NoSuchElementException();
					return new MyEntry(e);
				}
				finally
				{
					transaction.close(cursor);
				}
			}

			@Override
			public MyEntry last()
			{
				EntityCursor<BerkeleyDBStatementEntity> cursor = transaction.entities(statementEntityContextIdentifierSecondaryIndex, uuidContextIdentifierMin,
						true, uuidContextIdentifierMax, false);
				try
				{
					BerkeleyDBStatementEntity e = transaction.last(cursor);
					if (e == null)
						throw new NoSuchElementException();
					return new MyEntry(e);
				}
				finally
				{
					transaction.close(cursor);
				}
			}

			@Override
			public SortedSet<Map.Entry<Identifier, Statement>> headSet(Map.Entry<Identifier, Statement> from)
			{
				return BerkeleyDBLocalIdentifierToStatement.this.headMap(from.getKey()).entrySet();
			}

			@Override
			public SortedSet<Map.Entry<Identifier, Statement>> subSet(Map.Entry<Identifier, Statement> from, Map.Entry<Identifier, Statement> to)
			{
				return BerkeleyDBLocalIdentifierToStatement.this.subMap(from.getKey(), to.getKey()).entrySet();
			}

			@Override
			public SortedSet<Map.Entry<Identifier, Statement>> tailSet(Map.Entry<Identifier, Statement> to)
			{
				return BerkeleyDBLocalIdentifierToStatement.this.tailMap(to.getKey()).entrySet();
			}

			@Override
			public boolean isEmpty()
			{
				EntityCursor<UUIDContextIdentifier> cursor = transaction.keys(statementEntityContextIdentifierSecondaryIndex, uuidContextIdentifierMin, true,
						uuidContextIdentifierMax, false);
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
		;

		return new EntrySet();

	}

	@Override
	public Comparator<Identifier> comparator()
	{
		return null;
	}

	@Override
	public Identifier firstKey()
	{
		return entrySet().first().getKey();
	}

	@Override
	public Identifier lastKey()
	{
		return entrySet().last().getKey();
	}

	protected abstract BerkeleyDBLocalIdentifierToStatement newBerkeleyDBIdentifierToStatementBounds(NodeNamespace fromKey, NodeNamespace toKey);

	@Override
	public BerkeleyDBLocalIdentifierToStatement headMap(Identifier toKey)
	{
		return newBerkeleyDBIdentifierToStatementBounds(from, toKey.min(to));
	}

	@Override
	public BerkeleyDBLocalIdentifierToStatement tailMap(Identifier fromKey)
	{
		return newBerkeleyDBIdentifierToStatementBounds(fromKey.max(from), to);
	}

	@Override
	public BerkeleyDBLocalIdentifierToStatement subMap(Identifier fromKey, Identifier toKey)
	{
		return newBerkeleyDBIdentifierToStatementBounds(fromKey.max(from), toKey.min(to));
	}

	@Override
	public boolean isEmpty()
	{
		return entrySet().isEmpty();
	}

}
