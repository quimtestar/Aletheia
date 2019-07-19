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
package aletheia.persistence.berkeleydb.collections.statement;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity.IdentifierKey;
import aletheia.persistence.collections.statement.GenericRootContextsMap;
import aletheia.persistence.collections.statement.IdentifierToRootContexts;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSortedSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSortedSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBIdentifierToRootContexts extends AbstractCloseableMap<Identifier, GenericRootContextsMap> implements IdentifierToRootContexts
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<IdentifierKey, UUIDKey, BerkeleyDBRootContextEntity> rootContextEntityIdentifierSecondaryIndex;
	private final BerkeleyDBTransaction transaction;
	private final NodeNamespace from;
	private final NodeNamespace to;
	private final IdentifierKey identifierKeyMin;
	private final IdentifierKey identifierKeyMax;

	public BerkeleyDBIdentifierToRootContexts(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		this(persistenceManager, transaction, null, null);
	}

	protected BerkeleyDBIdentifierToRootContexts(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, NodeNamespace from,
			NodeNamespace to)
	{
		super();
		try
		{
			this.persistenceManager = persistenceManager;
			this.rootContextEntityIdentifierSecondaryIndex = persistenceManager.getEntityStore().rootContextEntityIdentifierSecondaryIndex();
			this.transaction = transaction;
			this.from = from;
			this.to = to;
			if (from == null)
				this.identifierKeyMin = IdentifierKey.minValue();
			else
				this.identifierKeyMin = IdentifierKey.minValue(from);
			if (to == null)
				this.identifierKeyMax = IdentifierKey.maxValue();
			else
				this.identifierKeyMax = IdentifierKey.maxValue(to);
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

	private EntityIndex<UUIDKey, BerkeleyDBRootContextEntity> myRootContextsMapSubIndex(Identifier identifier)
	{
		return persistenceManager.getEntityStore().rootContextEntityIdentifierSubIndex(identifier);
	}

	private class MyRootContextsMap extends BerkeleyDBGenericRootContextsMap
	{

		public MyRootContextsMap(Identifier identifier)
		{
			super(persistenceManager, myRootContextsMapSubIndex(identifier), transaction);
		}

	}

	@Override
	public GenericRootContextsMap get(Object o)
	{
		if (!(o instanceof Identifier))
			return null;
		Identifier id = (Identifier) o;
		if (!containsKey(id))
			return null;
		return new MyRootContextsMap(id);
	}

	@Override
	public boolean containsKey(Object o)
	{
		if (!(o instanceof Identifier))
			return false;
		Identifier identifier = (Identifier) o;
		IdentifierKey identifierKey = new IdentifierKey(identifier);
		return transaction.contains(rootContextEntityIdentifierSecondaryIndex, identifierKey);
	}

	private final static Comparator<Identifier> comparator = new Comparator<>()
	{
		@Override
		public int compare(Identifier o1, Identifier o2)
		{
			return o1.compareTo(o2);
		}
	};

	@Override
	public Comparator<Identifier> comparator()
	{
		return comparator;
	}

	protected BerkeleyDBIdentifierToRootContexts newBerkeleyDBIdentifierToRootContextsBounds(NodeNamespace fromKey, NodeNamespace toKey)
	{
		return new BerkeleyDBIdentifierToRootContexts(persistenceManager, transaction, fromKey, toKey);
	}

	@Override
	public BerkeleyDBIdentifierToRootContexts headMap(Identifier toKey)
	{
		return newBerkeleyDBIdentifierToRootContextsBounds(from, toKey.min(to));
	}

	@Override
	public BerkeleyDBIdentifierToRootContexts tailMap(Identifier fromKey)
	{
		return newBerkeleyDBIdentifierToRootContextsBounds(fromKey.max(from), to);
	}

	@Override
	public BerkeleyDBIdentifierToRootContexts subMap(Identifier fromKey, Identifier toKey)
	{
		return newBerkeleyDBIdentifierToRootContextsBounds(fromKey.max(from), toKey.min(to));
	}

	@Override
	public CloseableSortedSet<Entry<Identifier, GenericRootContextsMap>> entrySet()
	{
		class MyEntry implements Entry<Identifier, GenericRootContextsMap>
		{
			private final Identifier identifier;

			public MyEntry(Identifier identifier)
			{
				super();
				this.identifier = identifier;
			}

			@Override
			public Identifier getKey()
			{
				return identifier;
			}

			@Override
			public GenericRootContextsMap getValue()
			{
				return new MyRootContextsMap(identifier);
			}

			@Override
			public GenericRootContextsMap setValue(GenericRootContextsMap value)
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

		class EntrySet extends AbstractCloseableSortedSet<Map.Entry<Identifier, GenericRootContextsMap>>
		{

			@Override
			public boolean contains(Object o)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public CloseableIterator<Map.Entry<Identifier, GenericRootContextsMap>> iterator()
			{
				final EntityCursor<IdentifierKey> cursor = transaction.keys(rootContextEntityIdentifierSecondaryIndex, identifierKeyMin, true, identifierKeyMax,
						false);
				return new CloseableIterator<>()
				{
					IdentifierKey next;

					{
						next = transaction.nextNoDup(cursor);
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
						IdentifierKey key = next;
						next = transaction.nextNoDup(cursor);
						return new MyEntry(key.getIdentifier());
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
			public Comparator<Entry<Identifier, GenericRootContextsMap>> comparator()
			{
				final Comparator<Identifier> comp = BerkeleyDBIdentifierToRootContexts.this.comparator();
				return new Comparator<>()
				{

					@Override
					public int compare(Entry<Identifier, GenericRootContextsMap> e0, Entry<Identifier, GenericRootContextsMap> e1)
					{
						return comp.compare(e0.getKey(), e1.getKey());
					}

				};
			}

			@Override
			public MyEntry first()
			{
				EntityCursor<IdentifierKey> cursor = transaction.keys(rootContextEntityIdentifierSecondaryIndex, identifierKeyMin, true, identifierKeyMax,
						false);
				try
				{
					IdentifierKey k = transaction.first(cursor);
					if (k == null)
						throw new NoSuchElementException();
					return new MyEntry(k.getIdentifier());
				}
				finally
				{
					transaction.close(cursor);
				}
			}

			@Override
			public MyEntry last()
			{
				EntityCursor<IdentifierKey> cursor = transaction.keys(rootContextEntityIdentifierSecondaryIndex, identifierKeyMin, true, identifierKeyMax,
						false);
				try
				{
					IdentifierKey k = transaction.last(cursor);
					if (k == null)
						throw new NoSuchElementException();
					return new MyEntry(k.getIdentifier());
				}
				finally
				{
					transaction.close(cursor);
				}
			}

			@Override
			public CloseableSortedSet<Map.Entry<Identifier, GenericRootContextsMap>> headSet(Map.Entry<Identifier, GenericRootContextsMap> from)
			{
				return BerkeleyDBIdentifierToRootContexts.this.headMap(from.getKey()).entrySet();
			}

			@Override
			public CloseableSortedSet<Map.Entry<Identifier, GenericRootContextsMap>> subSet(Map.Entry<Identifier, GenericRootContextsMap> from,
					Map.Entry<Identifier, GenericRootContextsMap> to)
			{
				return BerkeleyDBIdentifierToRootContexts.this.subMap(from.getKey(), to.getKey()).entrySet();
			}

			@Override
			public CloseableSortedSet<Map.Entry<Identifier, GenericRootContextsMap>> tailSet(Map.Entry<Identifier, GenericRootContextsMap> to)
			{
				return BerkeleyDBIdentifierToRootContexts.this.tailMap(to.getKey()).entrySet();
			}

			@Override
			public boolean isEmpty()
			{
				EntityCursor<IdentifierKey> cursor = transaction.keys(rootContextEntityIdentifierSecondaryIndex, identifierKeyMin, true, identifierKeyMax,
						false);
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
	public Identifier firstKey()
	{
		return entrySet().first().getKey();
	}

	@Override
	public Identifier lastKey()
	{
		return entrySet().last().getKey();
	}

	@Override
	public boolean isEmpty()
	{
		return entrySet().isEmpty();
	}

}
