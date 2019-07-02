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
package aletheia.model.catalog;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

/**
 * <p>
 * An abstract class representation of a catalog node linked to a context and a
 * {@linkplain Namespace name space} prefix.
 * </p>
 * <p>
 * A catalog is also associated to a persistence transaction that will be used
 * on every operation with this data structure. Note that the catalog will only
 * be usable while the transaction is alive.
 * </p>
 *
 * @see aletheia.model.catalog
 */
public abstract class Catalog
{
	private final PersistenceManager persistenceManager;
	private final Context context;

	/**
	 * Creates a new catalog associated to a persistence transaction and a
	 * context.
	 *
	 * @param persistenceManager
	 * @param context
	 *            The context.
	 */
	protected Catalog(PersistenceManager persistenceManager, Context context)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.context = context;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	/**
	 * @return The context.
	 */
	public Context getContext()
	{
		return context;
	}

	/**
	 * The name space prefix associated to this catalog.
	 *
	 * @return The prefix.
	 */
	public abstract Namespace prefix();

	/**
	 * The depth of this node of the catalog tree. The same as the number of
	 * components of {@link #prefix()}.
	 *
	 * @return The depth.
	 */
	public abstract int depth();

	/**
	 * The mapping between identifiers and statements in this catalog (and
	 * subcatalogs).
	 *
	 * @return The map.
	 */
	protected abstract SortedMap<Identifier, Statement> map(Transaction transaction);

	public Collection<SubCatalog> subCatalogs(final Transaction transaction)
	{
		return new AbstractCollection<>()
		{

			@Override
			public Iterator<SubCatalog> iterator()
			{
				final SortedMap<Identifier, Statement> map = map(transaction);
				if (map == null)
					return Collections.emptyIterator();
				return new Iterator<>()
				{

					NodeNamespace nextNamespace = null;

					{
						try
						{
							nextNamespace = map.firstKey().prefixList().get(depth());
						}
						catch (NoSuchElementException e)
						{
						}
					}

					@Override
					public boolean hasNext()
					{
						return nextNamespace != null;
					}

					@Override
					public SubCatalog next()
					{
						if (nextNamespace == null)
							throw new NoSuchElementException();
						NodeNamespace namespace = nextNamespace;
						nextNamespace = null;
						try
						{
							nextNamespace = map.tailMap(namespace.terminator()).firstKey().prefixList().get(depth());
						}
						catch (NoSuchElementException e)
						{
						}
						return new SubCatalog(Catalog.this, namespace);
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

				};
			}

			@Override
			public int size()
			{
				SortedMap<Identifier, Statement> map = map(transaction);
				if (map == null)
					return 0;
				return map.size();
			}

			@Override
			public boolean isEmpty()
			{
				SortedMap<Identifier, Statement> map = map(transaction);
				if (map == null)
					return true;
				return map.isEmpty();
			}

		};
	}

	public SubCatalog subCatalog(String name) throws InvalidNameException
	{
		return new SubCatalog(this, new NodeNamespace(prefix(), name));
	}

}
