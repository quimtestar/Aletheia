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
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;

/**
 * A catalog associated to the {@linkplain RootNamespace root name space}.
 */
public class RootCatalog extends Catalog
{

	public RootCatalog(PersistenceManager persistenceManager, Context context)
	{
		super(persistenceManager, context);
	}

	/**
	 * The prefix is the unique {@link RootNamespace} instance.
	 */
	@Override
	public Namespace prefix()
	{
		return RootNamespace.instance;
	}

	/**
	 * The depth is 0.
	 */
	@Override
	public int depth()
	{
		return 0;
	}

	@Override
	protected SortedMap<Identifier, Statement> map(Transaction transaction)
	{
		return getContext().identifierToStatement(transaction);
	}

}
