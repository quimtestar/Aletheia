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
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeRootNodeEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPersonEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;

public class EntityStoreUpgrade_018 extends EntityStoreUpgrade_019
{

	public EntityStoreUpgrade_018()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(18);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_019.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void putConvertedRawObject(Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass, Class<Object> primaryKeyClass,
				PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			if (entityClass.equals(BerkeleyDBStatementAuthoritySignatureEntity.class))
			{
				Object osv = oldRawObject.getValues().get("signatureVersion");
				if (osv == null)
					oldRawObject.getValues().put("signatureVersion", 0);
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
			}
			else if (entityClass.equals(BerkeleyDBDelegateTreeNodeEntity.class))
			{
				Object osv = oldRawObject.getValues().get("signatureVersion");
				if (osv == null)
				{
					RawObject ro = oldRawObject;
					while (ro != null && !ro.getType().getClassName().equals(BerkeleyDBDelegateTreeRootNodeEntity.class.getName()))
						ro = ro.getSuper();
					if (ro != null)
						ro.getValues().put("signatureVersion", 0);
				}
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
			}
			else if (entityClass.equals(BerkeleyDBPersonEntity.class))
			{
				Object osv = oldRawObject.getValues().get("signatureVersion");
				if (osv == null)
				{
					RawObject ro = oldRawObject;
					while (ro != null && !ro.getType().getClassName().equals(BerkeleyDBPersonEntity.class.getName()))
						ro = ro.getSuper();
					if (ro != null)
						ro.getValues().put("signatureVersion", 0);
				}
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
			}
			else if (entityClass.equals(BerkeleyDBDelegateAuthorizerEntity.class))
			{
				Object osv = oldRawObject.getValues().get("signatureVersion");
				if (osv == null)
					oldRawObject.getValues().put("signatureVersion", 0);
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
