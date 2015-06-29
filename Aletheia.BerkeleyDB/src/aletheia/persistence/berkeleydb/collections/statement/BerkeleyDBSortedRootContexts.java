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

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.LocalSortKey;
import aletheia.persistence.collections.statement.SortedRootContexts;

public class BerkeleyDBSortedRootContexts extends BerkeleyDBSortedStatements<RootContext>implements SortedRootContexts
{

	protected BerkeleyDBSortedRootContexts(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from,
			boolean fromInclusive, LocalSortKey to, boolean toInclusive)
	{
		super(persistenceManager, transaction, from, fromInclusive, to, toInclusive);
	}

	protected BerkeleyDBSortedRootContexts(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from,
			LocalSortKey to)
	{
		super(persistenceManager, transaction, from, to);
	}

	public BerkeleyDBSortedRootContexts(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		this(persistenceManager, transaction, LocalSortKey.minValue(), LocalSortKey.maxValue());
	}

	@Override
	protected RootContext entitytoStatement(BerkeleyDBStatementEntity entity)
	{
		return (RootContext) getPersistenceManager().entityToStatement(entity);
	}

	@Override
	protected BerkeleyDBSortedRootContexts newBerkeleyDBSortedStatementsBounds(LocalSortKey from, boolean fromInclusive, LocalSortKey to, boolean toInclusive)
	{
		return new BerkeleyDBSortedRootContexts(getPersistenceManager(), getTransaction(), from, fromInclusive, to, toInclusive);
	}

	@Override
	public BerkeleyDBSortedRootContexts subSet(RootContext fromElement, RootContext toElement)
	{
		return (BerkeleyDBSortedRootContexts) super.subSet(fromElement, toElement);
	}

	@Override
	public BerkeleyDBSortedRootContexts headSet(RootContext toElement)
	{
		return (BerkeleyDBSortedRootContexts) super.headSet(toElement);
	}

	@Override
	public BerkeleyDBSortedRootContexts tailSet(RootContext fromElement)
	{
		return (BerkeleyDBSortedRootContexts) super.tailSet(fromElement);
	}

	@Override
	public BerkeleyDBSortedRootContexts subSet(Identifier from, Identifier to)
	{
		return (BerkeleyDBSortedRootContexts) super.subSet(from, to);
	}

	@Override
	public BerkeleyDBSortedRootContexts headSet(Identifier to)
	{
		return (BerkeleyDBSortedRootContexts) super.headSet(to);
	}

	@Override
	public BerkeleyDBSortedRootContexts tailSet(Identifier from)
	{
		return (BerkeleyDBSortedRootContexts) super.tailSet(from);
	}

	@Override
	public BerkeleyDBSortedRootContexts identifierSet(Identifier identifier)
	{
		return (BerkeleyDBSortedRootContexts) super.identifierSet(identifier);
	}

	@Override
	public BerkeleyDBSortedRootContexts postIdentifierSet(Identifier identifier)
	{
		return (BerkeleyDBSortedRootContexts) super.postIdentifierSet(identifier);
	}

}
