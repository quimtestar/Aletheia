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

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.LocalStatementsMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBLocalStatementsMap extends BerkeleyDBStatementsMap implements LocalStatementsMap
{
	private final SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementEntity> statementContextSecondaryIndex;
	private final Context context;
	private final UUIDKey uuidKeyContext;

	public BerkeleyDBLocalStatementsMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context)
	{
		super(persistenceManager, transaction);
		try
		{
			this.statementContextSecondaryIndex = persistenceManager.getEntityStore().statementEntityContextSecondaryIndex();
			this.context = context;
			this.uuidKeyContext = ((BerkeleyDBStatementEntity) context.getEntity()).getUuidKey();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}

	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	public Statement get(Object key)
	{
		Statement statement = super.get(key);
		if (statement == null)
			return null;
		if (!((BerkeleyDBStatementEntity) context.getEntity()).getUuidKey().equals(((BerkeleyDBStatementEntity) statement.getEntity()).getUuidKeyContext()))
			return null;
		return statement;
	}

	@Override
	public boolean containsKey(Object key)
	{
		return get(key) != null;
	}

	@Override
	public int size()
	{
		EntityCursor<BerkeleyDBStatementEntity> cursor = getTransaction().entities(statementContextSecondaryIndex, uuidKeyContext, true, uuidKeyContext, true);
		try
		{
			if (getTransaction().first(cursor) == null)
				return 0;
			return getTransaction().count(cursor);
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

	@Override
	public boolean isEmpty()
	{
		EntityCursor<UUIDKey> cursor = getTransaction().keys(statementContextSecondaryIndex, uuidKeyContext, true, uuidKeyContext, true);
		try
		{
			return getTransaction().first(cursor) == null;
		}
		finally
		{
			getTransaction().close(cursor);
		}
	}

	@Override
	public EntrySet entrySet()
	{
		return new EntrySet(statementContextSecondaryIndex, uuidKeyContext, uuidKeyContext);
	}

}
