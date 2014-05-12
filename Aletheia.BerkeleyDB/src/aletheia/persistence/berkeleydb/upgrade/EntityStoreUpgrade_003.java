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
package aletheia.persistence.berkeleydb.upgrade;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;

public class EntityStoreUpgrade_003 extends EntityStoreUpgrade_004
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.logger();

	public EntityStoreUpgrade_003()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(3);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_004.UpgradeInstance
	{
		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void putConvertedRawObject(Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass, Class<Object> primaryKeyClass,
				PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			if (entityClass.equals(BerkeleyDBStatementEntity.class))
			{
				try
				{
					BerkeleyDBStatementEntity entity = (BerkeleyDBStatementEntity) partialConvertRawObject(aletheiaModel, oldRawObject);
					entity.getLocalSortKey().setUuidKeyContext(entity.getUuidKeyContext());
					entity.getLocalSortKey().setIdentifier(entity.getIdentifier());
					if (entity instanceof BerkeleyDBAssumptionEntity)
						entity.getLocalSortKey().setAssumptionOrder(((BerkeleyDBAssumptionEntity) entity).getOrder());
					newPrimaryIndex.put(tx, entity);
				}
				catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException | NoSuchMethodException
						| IllegalArgumentException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
			}
			else
			{
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
			}
		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
