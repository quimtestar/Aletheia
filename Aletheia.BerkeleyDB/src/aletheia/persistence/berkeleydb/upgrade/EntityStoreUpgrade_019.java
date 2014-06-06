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

import java.util.Arrays;
import java.util.Collection;

import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPlainPrivateSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPrivateSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatoryEntity;

import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawType;

public class EntityStoreUpgrade_019 extends EntityStoreUpgrade
{

	public EntityStoreUpgrade_019()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(19);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void putConvertedRawObject(com.sleepycat.je.Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass,
				Class<Object> primaryKeyClass, PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			if (entityClass.equals(BerkeleyDBSignatoryEntity.class))
			{
				if (oldRawObject.getType().getClassName().equals(BerkeleyDBPrivateSignatoryEntity.class.getName()))
				{
					RawType type = aletheiaModel.getRawType(BerkeleyDBPlainPrivateSignatoryEntity.class.getName());
					RawObject object = new RawObject(type, oldRawObject.getValues(), oldRawObject);
					super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, object);
				}
				else
					super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
			}
			else
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
		}
	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
