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
package aletheia.persistence.berkeleydb.collections.statement;

import java.util.AbstractList;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.SecondaryIndex;

import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity.UUIDKeyOrder;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.collections.statement.AssumptionList;

public class BerkeleyDBAssumptionList extends AbstractList<Assumption> implements AssumptionList
{
	private final BerkeleyDBPersistenceManager persistenceManager;
	private final SecondaryIndex<UUIDKeyOrder, UUIDKey, BerkeleyDBAssumptionEntity> assumptionEntityKeyOrderIndex;
	private final BerkeleyDBTransaction transaction;
	private final Context context;
	private final UUIDKey uuidKeyContext;

	public BerkeleyDBAssumptionList(BerkeleyDBPersistenceManager persistenceManager, BerkeleyDBTransaction transaction, Context context)
	{
		try
		{
			this.persistenceManager = persistenceManager;
			this.assumptionEntityKeyOrderIndex = persistenceManager.getEntityStore().assumptionEntityKeyOrderIndex();
			this.transaction = transaction;
			this.context = context;
			this.uuidKeyContext = ((BerkeleyDBStatementEntity) context.getEntity()).getUuidKey();
		}
		catch (DatabaseException e)
		{
			throw persistenceManager.convertDatabaseException(e);
		}
	}

	@Override
	public BerkeleyDBPersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	@Override
	public BerkeleyDBTransaction getTransaction()
	{
		return transaction;
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	@Override
	public Assumption get(int i)
	{
		UUIDKeyOrder uuidKeyOrder = new UUIDKeyOrder();
		uuidKeyOrder.setUUIDKey(uuidKeyContext);
		uuidKeyOrder.setOrder(i);
		BerkeleyDBAssumptionEntity entity = transaction.get(assumptionEntityKeyOrderIndex, uuidKeyOrder);
		if (entity == null)
			throw new IndexOutOfBoundsException("Invalid assumption index");
		return persistenceManager.assumptionEntityToStatement(entity);
	}

	@Override
	public int size()
	{
		UUIDKeyOrder uuidKeyOrderMin = new UUIDKeyOrder();
		uuidKeyOrderMin.setUUIDKey(uuidKeyContext);
		uuidKeyOrderMin.setOrder(Integer.MIN_VALUE);
		UUIDKeyOrder uuidKeyOrderMax = new UUIDKeyOrder();
		uuidKeyOrderMax.setUUIDKey(uuidKeyContext);
		uuidKeyOrderMax.setOrder(Integer.MAX_VALUE);
		EntityCursor<UUIDKeyOrder> cursor = transaction.keys(assumptionEntityKeyOrderIndex, uuidKeyOrderMin, true, uuidKeyOrderMax, true);
		try
		{
			UUIDKeyOrder last = transaction.last(cursor);
			if (last == null)
				return 0;
			return last.getOrder() + 1;
		}
		finally
		{
			transaction.close(cursor);
		}
	}

}
