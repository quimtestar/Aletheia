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
package aletheia.model.nomenclator;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.LocalIdentifierToStatement;
import aletheia.utilities.collections.AbstractCloseableMap;
import aletheia.utilities.collections.AbstractCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSortedSet;

public class RootLocalIdentifierToStatement extends AbstractCloseableMap<Identifier, Statement> implements LocalIdentifierToStatement
{
	private final PersistenceManager persistenceManager;
	private final Transaction transaction;
	private final RootContext rootContext;
	private final NodeNamespace from;
	private final NodeNamespace to;
	private final NodeNamespace actualFrom;
	private final NodeNamespace actualTo;

	public RootLocalIdentifierToStatement(PersistenceManager persistenceManager, Transaction transaction, RootContext rootContext)
	{
		this(persistenceManager, transaction, rootContext, null, null);
	}

	protected RootLocalIdentifierToStatement(PersistenceManager persistenceManager, Transaction transaction, RootContext rootContext, NodeNamespace from,
			NodeNamespace to)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.rootContext = rootContext;
		this.from = from;
		this.to = to;
		this.actualFrom = from != null ? from : RootNamespace.instance.initiator();
		this.actualTo = to != null ? to : RootNamespace.instance.terminator();
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

	public RootContext getRootContext()
	{
		return rootContext;
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
		if (!containsKey(o))
			return null;
		return rootContext.refresh(transaction);
	}

	@Override
	public boolean containsKey(Object o)
	{
		if (!(o instanceof Identifier))
			return false;
		Identifier id = (Identifier) o;
		if (id.compareTo(actualFrom) < 0 || id.compareTo(actualTo) >= 0)
			return false;
		return id.equals(rootContext.identifier(transaction));
	}

	@Override
	public CloseableSortedSet<Entry<Identifier, Statement>> entrySet()
	{
		class MyEntry implements Entry<Identifier, Statement>
		{
			private final Statement statement;

			public MyEntry(Statement statement)
			{
				super();
				this.statement = statement;
			}

			@Override
			public Identifier getKey()
			{
				return statement.getIdentifier();
			}

			@Override
			public Statement getValue()
			{
				return statement;
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

		class EntrySet extends AbstractCloseableSet<Map.Entry<Identifier, Statement>> implements CloseableSortedSet<Map.Entry<Identifier, Statement>>
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

			public MyEntry myEntry()
			{
				Statement st = rootContext.refresh(transaction);
				if (st == null)
					return null;
				Identifier id = st.getIdentifier();
				if (id == null || id.compareTo(actualFrom) < 0 || id.compareTo(actualTo) >= 0)
					return null;
				else
					return new MyEntry(st);
			}

			@Override
			public CloseableIterator<Map.Entry<Identifier, Statement>> iterator()
			{
				return new CloseableIterator<>()
				{
					private MyEntry next = myEntry();

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public Map.Entry<Identifier, Statement> next()
					{
						if (next == null)
							throw new NoSuchElementException();
						MyEntry e = next;
						next = null;
						return e;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public void close()
					{
					}
				};

			}

			@Override
			public int size()
			{
				return myEntry() == null ? 0 : 1;
			}

			@Override
			public Comparator<Entry<Identifier, Statement>> comparator()
			{
				final Comparator<Identifier> comp = RootLocalIdentifierToStatement.this.comparator();
				return new Comparator<>()
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
				MyEntry e = myEntry();
				if (e == null)
					throw new NoSuchElementException();
				return e;
			}

			@Override
			public MyEntry last()
			{
				MyEntry e = myEntry();
				if (e == null)
					throw new NoSuchElementException();
				return e;
			}

			@Override
			public CloseableSortedSet<Map.Entry<Identifier, Statement>> headSet(Map.Entry<Identifier, Statement> from)
			{
				return RootLocalIdentifierToStatement.this.headMap(from.getKey()).entrySet();
			}

			@Override
			public CloseableSortedSet<Map.Entry<Identifier, Statement>> subSet(Map.Entry<Identifier, Statement> from, Map.Entry<Identifier, Statement> to)
			{
				return RootLocalIdentifierToStatement.this.subMap(from.getKey(), to.getKey()).entrySet();
			}

			@Override
			public CloseableSortedSet<Map.Entry<Identifier, Statement>> tailSet(Map.Entry<Identifier, Statement> to)
			{
				return RootLocalIdentifierToStatement.this.tailMap(to.getKey()).entrySet();
			}

			@Override
			public boolean isEmpty()
			{
				return myEntry() == null;
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

	protected RootLocalIdentifierToStatement newBerkeleyDBRootIdentifierToStatementBounds(NodeNamespace fromKey, NodeNamespace toKey)
	{
		return new RootLocalIdentifierToStatement(persistenceManager, transaction, rootContext, fromKey, toKey);
	}

	@Override
	public RootLocalIdentifierToStatement headMap(Identifier toKey)
	{
		return newBerkeleyDBRootIdentifierToStatementBounds(from, toKey.min(to));
	}

	@Override
	public RootLocalIdentifierToStatement tailMap(Identifier fromKey)
	{
		return newBerkeleyDBRootIdentifierToStatementBounds(fromKey.max(from), to);
	}

	@Override
	public RootLocalIdentifierToStatement subMap(Identifier fromKey, Identifier toKey)
	{
		return newBerkeleyDBRootIdentifierToStatementBounds(fromKey.max(from), toKey.min(to));
	}

	@Override
	public boolean isEmpty()
	{
		return entrySet().isEmpty();
	}

}
