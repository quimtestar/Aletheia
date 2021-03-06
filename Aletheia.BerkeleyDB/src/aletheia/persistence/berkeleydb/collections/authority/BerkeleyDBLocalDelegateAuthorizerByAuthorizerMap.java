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
package aletheia.persistence.berkeleydb.collections.authority;

import java.util.UUID;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.collections.authority.LocalDelegateAuthorizerByAuthorizerMap;

public class BerkeleyDBLocalDelegateAuthorizerByAuthorizerMap
		extends BerkeleyDBGenericDelegateAuthorizerByAuthorizerMap<BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData>
		implements LocalDelegateAuthorizerByAuthorizerMap
{
	private final StatementAuthority statementAuthority;
	private final Namespace prefix;

	private static SecondaryIndex<BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> makeIndex(
			BerkeleyDBPersistenceManager persistenceManager)
	{
		try
		{
			return persistenceManager.getEntityStore().delegateAuthorizerEntityContextAuthorizerSecondaryIndex();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	public BerkeleyDBLocalDelegateAuthorizerByAuthorizerMap(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction,
			StatementAuthority statementAuthority, Namespace prefix)
	{
		super(persistenceManager, transaction, makeIndex(persistenceManager),
				BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData.first(statementAuthority.getStatementUuid(), prefix),
				BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData.last(statementAuthority.getStatementUuid(), prefix));
		this.statementAuthority = statementAuthority;
		this.prefix = prefix;
	}

	public StatementAuthority getStatementAuthority()
	{
		return statementAuthority;
	}

	public Namespace getPrefix()
	{
		return prefix;
	}

	@Override
	protected BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData uuidToKey(UUID authorizerUuid)
	{
		return new BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData(statementAuthority.getStatementUuid(), prefix, authorizerUuid);
	}

}
