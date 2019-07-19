/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.persistence.berkeleydb;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.berkeleydb.mutations.BerkeleyDBAletheiaMutations;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBMiscUtilities;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.aborter.Aborter;
import aletheia.utilities.aborter.Aborter.AbortException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.AnnotationModel;
import com.sleepycat.persist.model.EntityMetadata;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.model.SecondaryKeyMetadata;
import com.sleepycat.persist.raw.RawType;

public abstract class BerkeleyDBAletheiaAbstractEntityStore extends EntityStore
{
	private static final Logger logger = LoggerManager.instance.logger();

	protected BerkeleyDBAletheiaAbstractEntityStore(BerkeleyDBAletheiaEnvironment env, String storeName, StoreConfig storeConfig) throws DatabaseException
	{
		super(env, storeName, storeConfig);
		try
		{
			updateStoreVersion();
		}
		catch (Exception e)
		{
			close();
			throw e;
		}
	}

	@Override
	public BerkeleyDBAletheiaEnvironment getEnvironment()
	{
		return (BerkeleyDBAletheiaEnvironment) super.getEnvironment();
	}

	protected static StoreConfig makeConfig(Environment env, boolean bulkLoad, Collection<Class<?>> registerClasses)
	{
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(env.getConfig().getTransactional());
		storeConfig.setReadOnly(env.getConfig().getReadOnly());
		storeConfig.setSecondaryBulkLoad(bulkLoad);
		EntityModel model = new AnnotationModel();
		for (Class<?> persistentClass : registerClasses)
			model.registerClass(persistentClass);
		storeConfig.setModel(model);
		storeConfig.setMutations(new BerkeleyDBAletheiaMutations());
		return storeConfig;
	}

	public int storeVersion()
	{
		return 0;
	}

	public int minimalStoreVersion()
	{
		return Integer.MIN_VALUE;
	}

	public class UnsupportedBerkeleyDBStoreVersion extends RuntimeException
	{
		private static final long serialVersionUID = 8090100720123016127L;

		private UnsupportedBerkeleyDBStoreVersion(int currentStoreVersion)
		{
			super("Unsupported store version: " + currentStoreVersion);
		}
	}

	private void updateStoreVersion()
	{
		BerkeleyDBAletheiaEnvironment environment = getEnvironment();
		String storeName = getStoreName();
		int currentStoreVersion = environment.getStoreVersion(storeName);
		if (currentStoreVersion >= 0 && (currentStoreVersion < minimalStoreVersion() || currentStoreVersion > storeVersion()))
			throw new UnsupportedBerkeleyDBStoreVersion(currentStoreVersion);
		if (currentStoreVersion != storeVersion())
			environment.putStoreVersion(storeName, storeVersion());
	}

	private static Class<?> resolveClass(EntityModel model, String className) throws ClassNotFoundException
	{
		try
		{
			return model.resolveClass(className);
		}
		catch (ClassNotFoundException e)
		{
			Class<?> c = MiscUtilities.resolvePrimitiveTypeWrapperClass(className);
			if (c == null)
				throw e;
			return c;
		}
	}

	public Class<?> resolveClass(String className) throws ClassNotFoundException
	{
		return resolveClass(getModel(), className);
	}

	protected void truncateEntityClasses()
	{
		EntityModel model = getModel();
		Transaction tx = getEnvironment().beginTransaction(null, null);
		try
		{

			for (String className : model.getKnownClasses())
			{
				EntityMetadata entityMetadata = model.getEntityMetadata(className);
				if (entityMetadata != null)
				{
					try
					{
						Class<?> entityClass = resolveClass(className);
						truncateClass(tx, entityClass);
					}
					catch (ClassNotFoundException e)
					{
					}
				}
			}
			tx.commit();
		}
		finally
		{
			tx.abort();
		}
	}

	public void touchSecondaryIndices()
	{
		try
		{
			touchSecondaryIndices(Aborter.nullAborter);
		}
		catch (AbortException e)
		{
			throw new Error(e);
		}
	}

	public void touchSecondaryIndices(Aborter aborter) throws DatabaseException, AbortException
	{
		try
		{
			EntityModel aletheiaModel = getModel();
			for (String className : aletheiaModel.getKnownClasses())
			{
				aborter.checkAbort();
				RawType newRawType = aletheiaModel.getRawType(className);
				if (newRawType != null && newRawType.getClassMetadata().isEntityClass())
				{
					@SuppressWarnings("unchecked")
					Class<Object> entityClass = (Class<Object>) resolveClass(className);
					@SuppressWarnings("unchecked")
					Class<Object> primaryKeyClass = (Class<Object>) resolveClass(BerkeleyDBMiscUtilities.primaryKeyMetadata(newRawType).getClassName());
					PrimaryIndex<Object, Object> newPrimaryIndex = getPrimaryIndex(primaryKeyClass, entityClass);
					Map<String, SecondaryKeyMetadata> skMap = newRawType.getEntityMetadata().getSecondaryKeys();
					if (skMap != null)
					{
						for (SecondaryKeyMetadata skmd : skMap.values())
						{
							aborter.checkAbort();
							String skClassName;
							switch (skmd.getRelationship())
							{
							case MANY_TO_ONE:
							case ONE_TO_ONE:
								skClassName = skmd.getClassName();
								break;
							case MANY_TO_MANY:
							case ONE_TO_MANY:
								skClassName = skmd.getElementClassName();
								break;
							default:
								throw new Error();
							}
							@SuppressWarnings("unchecked")
							Class<Object> secondaryKeyClass = (Class<Object>) resolveClass(skClassName);
							logger.debug("Touching secondary key: " + className + " " + skmd.getKeyName());
							getSecondaryIndex(newPrimaryIndex, secondaryKeyClass, skmd.getKeyName());
						}
					}
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

}
