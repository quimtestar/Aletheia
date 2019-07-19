/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
import java.util.Set;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.collections.statement.LocalStatementToIdentifier;

public abstract class BerkeleyDBLocalStatementToIdentifier extends AbstractMap<Statement, Identifier> implements LocalStatementToIdentifier
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final BerkeleyDBTransaction transaction;

	public BerkeleyDBLocalStatementToIdentifier(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
	}

	@Override
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	@Override
	public Identifier get(Object o)
	{
		if (!(o instanceof Statement))
			return null;
		Statement st = ((Statement) o).refresh(transaction);
		if (st == null)
			return null;
		return st.getEntity().getIdentifier();
	}

	@Override
	public boolean containsKey(Object o)
	{
		return get(o) != null;
	}

	@Override
	public Set<Entry<Statement, Identifier>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

}
