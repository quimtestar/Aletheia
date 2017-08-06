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

import java.util.SortedMap;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

/**
 * A catalog associated to a {@linkplain Namespace name space} distinct than the
 * {@linkplain RootNamespace root name space}. These subatalogs cannot be
 * created directly, but obtained via their parent's {@link Catalog#children()}
 * method. A subcatalog can have a correspondence to one or zero statements, if
 * there is a statement in the context that is identified exactly by the
 * subcatalog's prefix taken as a identifier.
 */
public class SubCatalog extends Catalog
{

	private final Catalog parent;
	private final NodeNamespace prefix;
	private final int depth;

	/**
	 * Creates a new subcatalog with the specified parent and prefix. The prefix
	 * must be consistent with parent's prefix.
	 *
	 * @param parent
	 *            The parent catalog.
	 * @param prefix
	 *            The prefix.
	 */
	protected SubCatalog(Catalog parent, NodeNamespace prefix)
	{
		super(parent.getPersistenceManager(), parent.getContext());
		this.parent = parent;
		this.prefix = prefix;
		if (!parent.prefix().isPrefixOf(prefix))
			throw new Error();
		this.depth = parent.depth() + 1;
	}

	@Override
	public NodeNamespace prefix()
	{
		return prefix;
	}

	/**
	 * The prefix's name.
	 *
	 * @return The name.
	 */
	public String name()
	{
		return prefix.getName();
	}

	@Override
	public int depth()
	{
		return depth;
	}

	@Override
	protected SortedMap<Identifier, Statement> map(Transaction transaction)
	{
		SortedMap<Identifier, Statement> parentMap = parent.map(transaction);
		if (parentMap == null)
			return null;
		return parentMap.subMap(prefix.initiator(), prefix.terminator());
	}

	/**
	 * The statement associated with this catalog, or null if there isn't one. I.e.
	 * the one that is identified by the catalog's prefix.
	 *
	 * @return The statement.
	 */
	public Statement statement(Transaction transaction)
	{
		return getContext().identifierToStatement(transaction).get(prefix());
	}

}
