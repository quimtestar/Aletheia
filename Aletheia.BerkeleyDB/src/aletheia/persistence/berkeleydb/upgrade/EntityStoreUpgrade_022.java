/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawType;

import aletheia.log4j.LoggerManager;
import aletheia.model.term.TauTerm;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;

public class EntityStoreUpgrade_022 extends EntityStoreUpgrade_023
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerManager.instance.logger();

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(22);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_023.AbstractUpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		final Collection<UUID> uuids = new ArrayList<>();

		@Override
		protected void putConvertedRawObject(com.sleepycat.je.Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass,
				Class<Object> primaryKeyClass, PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			if (BerkeleyDBStatementEntity.class.equals(entityClass))
			{
				if (isTauInIdentifier(oldRawObject))
				{
					unidentify(oldRawObject);
					uuids.add(obtainUuidFromStatementRawObject(oldRawObject));
				}
				try
				{
					Object object = partialConvertRawObject(aletheiaModel, oldRawObject);
					newPrimaryIndex.put(tx, object);
				}
				catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
			}
			else
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
		}

		@Override
		protected Object partialConvertRawObjectDefaultType(EntityModel model, RawObject rawObject, RawType rawType, Map<RawObject, Object> converted)
				throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
				IllegalArgumentException, InvocationTargetException
		{
			if ("aletheia.persistence.berkeleydb.proxies.term.TTermProxy".equals(rawType.getClassName()))
				return TauTerm.instance;
			else
				return super.partialConvertRawObjectDefaultType(model, rawObject, rawType, converted);
		}

		@Override
		protected void postProcessing(BerkeleyDBPersistenceManager persistenceManager)
		{
			clearSignatures(persistenceManager, uuids);
		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
