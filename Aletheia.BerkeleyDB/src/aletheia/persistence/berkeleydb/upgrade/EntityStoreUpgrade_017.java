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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEntityStore;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBMiscUtilities;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawStore;
import com.sleepycat.persist.raw.RawType;

public class EntityStoreUpgrade_017 extends EntityStoreUpgrade_018
{
	private final static Logger logger = LoggerManager.instance.logger();

	public EntityStoreUpgrade_017()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(17);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_018.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void convertClass(RawStore oldStore, BerkeleyDBAletheiaEntityStore aletheiaStore, com.sleepycat.je.Transaction tx, String className)
				throws UpgradeException
		{
			if (BerkeleyDBDelegateAuthorizerEntity.class.getName().equals(className))
			{
				logger.debug("Converting class: " + className);
				try
				{
					EntityModel aletheiaModel = aletheiaStore.getModel();
					RawType newRawType = aletheiaModel.getRawType(className);
					if (newRawType != null)
					{
						@SuppressWarnings("unchecked")
						Class<Object> entityClass = aletheiaModel.resolveClass(className);
						@SuppressWarnings("unchecked")
						Class<Object> primaryKeyClass = aletheiaModel.resolveClass(BerkeleyDBMiscUtilities.primaryKeyMetadata(newRawType).getClassName());
						PrimaryIndex<Object, Object> newPrimaryIndex = aletheiaStore.getPrimaryIndex(primaryKeyClass, entityClass);
						PrimaryIndex<Object, RawObject> oldPrimaryIndex = oldStore.getPrimaryIndex(className);
						EntityCursor<RawObject> cursor = oldPrimaryIndex.entities(tx, CursorConfig.DEFAULT);
						Set<UUID> authorizerUuids = new HashSet<UUID>();
						int n = 0;
						try
						{
							for (RawObject oldRawObject : cursor)
							{
								if (isAbort())
									throw new AbortUpgradeException();
								BerkeleyDBDelegateAuthorizerEntity entity = (BerkeleyDBDelegateAuthorizerEntity) aletheiaModel.convertRawObject(oldRawObject);
								if (authorizerUuids.add(entity.getAuthorizerUuid()))
									newPrimaryIndex.put(tx, entity);
								n++;
								if (n % 1000 == 0)
									logger.debug("Converted " + n + " entities");
							}
						}
						finally
						{
							cursor.close();
						}
						logger.debug("Converted class: " + className + " (" + n + " entities)");
					}
					else
						throw new UpgradeException("Class: " + className + " not in the new model");
				}
				catch (ClassNotFoundException e)
				{
					throw new UpgradeException(e);
				}
			}
			else
				super.convertClass(oldStore, aletheiaStore, tx, className);
		}

		@Override
		protected void postProcessing(BerkeleyDBPersistenceManager persistenceManager)
		{
			logger.debug("Post-processing");
			super.postProcessing(persistenceManager);
			Transaction transaction = persistenceManager.beginTransaction();
			try
			{
				int n = 0;
				for (StatementAuthority stAuth : persistenceManager.statementAuthoritySet(transaction))
				{
					for (StatementAuthoritySignature sas : stAuth.signatureMap(transaction).values())
					{
						sas.checkValidSignature(transaction);
						n++;
						if (n % 1000 == 0)
							logger.debug("Checked " + n + " statement authority signatures");
					}
				}
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
			logger.debug("Post-processed");
		}

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
