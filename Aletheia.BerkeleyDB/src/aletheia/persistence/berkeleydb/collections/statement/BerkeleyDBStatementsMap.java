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

import aletheia.model.statement.Statement;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.StatementsMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.PrimaryIndex;

public class BerkeleyDBStatementsMap extends BerkeleyDBGenericStatementsMap<Statement, BerkeleyDBStatementEntity> implements StatementsMap
{
	private static PrimaryIndex<UUIDKey, BerkeleyDBStatementEntity> index(BerkeleyDBPersistenceManager persistenceManager)
	{
		try
		{
			return persistenceManager.getEntityStore().statementEntityPrimaryIndex();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBStatementsMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction)
	{
		super(persistenceManager, index(persistenceManager), transaction);
	}

	@Override
	public PrimaryIndex<UUIDKey, BerkeleyDBStatementEntity> getIndex()
	{
		return (PrimaryIndex<UUIDKey, BerkeleyDBStatementEntity>) super.getIndex();
	}

}
