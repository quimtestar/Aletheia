/*******************************************************************************
 * Copyright (c) 2014, 2017 Quim Testar.
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

import aletheia.model.identifier.NodeNamespace;
import aletheia.model.statement.Context;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.collections.statement.ContextLocalIdentifierToStatement;

public class BerkeleyDBContextLocalIdentifierToStatement extends BerkeleyDBLocalIdentifierToStatement implements ContextLocalIdentifierToStatement
{
	private final Context context;

	public BerkeleyDBContextLocalIdentifierToStatement(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context)
	{
		this(persistenceManager, transaction, context, null, null);
	}

	protected BerkeleyDBContextLocalIdentifierToStatement(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context,
			NodeNamespace from, NodeNamespace to)
	{
		super(persistenceManager, transaction, context.getUuid(), from, to);
		this.context = context;
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	protected BerkeleyDBContextLocalIdentifierToStatement newBerkeleyDBIdentifierToStatementBounds(NodeNamespace fromKey, NodeNamespace toKey)
	{
		return new BerkeleyDBContextLocalIdentifierToStatement(getPersistenceManager(), getTransaction(), context, fromKey, toKey);
	}

}
