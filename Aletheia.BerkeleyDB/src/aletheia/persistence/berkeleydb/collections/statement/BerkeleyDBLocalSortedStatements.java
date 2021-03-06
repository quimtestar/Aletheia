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

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.LocalSortKey;
import aletheia.persistence.collections.statement.LocalSortedStatements;

public class BerkeleyDBLocalSortedStatements extends BerkeleyDBSortedStatements<Statement> implements LocalSortedStatements
{

	protected BerkeleyDBLocalSortedStatements(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from,
			boolean fromInclusive, LocalSortKey to, boolean toInclusive)
	{
		super(persistenceManager, transaction, from, fromInclusive, to, toInclusive);
	}

	protected BerkeleyDBLocalSortedStatements(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, LocalSortKey from,
			LocalSortKey to)
	{
		super(persistenceManager, transaction, from, to);
	}

	public BerkeleyDBLocalSortedStatements(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context)
	{
		this(persistenceManager, transaction, LocalSortKey.minValue(context.getUuid()), LocalSortKey.maxValue(context.getUuid()));
	}

	@Override
	protected Statement entitytoStatement(BerkeleyDBStatementEntity entity)
	{
		return getPersistenceManager().entityToStatement(entity);
	}

	@Override
	protected BerkeleyDBLocalSortedStatements newBerkeleyDBSortedStatementsBounds(LocalSortKey from, boolean fromInclusive, LocalSortKey to,
			boolean toInclusive)
	{
		return new BerkeleyDBLocalSortedStatements(getPersistenceManager(), getTransaction(), from, fromInclusive, to, toInclusive);
	}

	@Override
	public BerkeleyDBLocalSortedStatements subSet(Statement fromElement, Statement toElement)
	{
		return (BerkeleyDBLocalSortedStatements) super.subSet(fromElement, toElement);
	}

	@Override
	public BerkeleyDBLocalSortedStatements headSet(Statement toElement)
	{
		return (BerkeleyDBLocalSortedStatements) super.headSet(toElement);
	}

	@Override
	public BerkeleyDBLocalSortedStatements tailSet(Statement fromElement)
	{
		return (BerkeleyDBLocalSortedStatements) super.tailSet(fromElement);
	}

	@Override
	public BerkeleyDBLocalSortedStatements subSet(Identifier from, Identifier to)
	{
		return (BerkeleyDBLocalSortedStatements) super.subSet(from, to);
	}

	@Override
	public BerkeleyDBLocalSortedStatements headSet(Identifier to)
	{
		return (BerkeleyDBLocalSortedStatements) super.headSet(to);
	}

	@Override
	public BerkeleyDBLocalSortedStatements tailSet(Identifier from)
	{
		return (BerkeleyDBLocalSortedStatements) super.tailSet(from);
	}

	@Override
	public BerkeleyDBLocalSortedStatements identifierSet(Identifier identifier)
	{
		return (BerkeleyDBLocalSortedStatements) super.identifierSet(identifier);
	}

	@Override
	public BerkeleyDBLocalSortedStatements postIdentifierSet(Identifier identifier)
	{
		return (BerkeleyDBLocalSortedStatements) super.postIdentifierSet(identifier);
	}

}
