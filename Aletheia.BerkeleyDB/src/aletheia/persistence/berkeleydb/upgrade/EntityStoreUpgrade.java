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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEntityStore;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.utilities.BerkeleyDBMiscUtilities;
import aletheia.persistence.exceptions.PersistenceException;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.aborter.Aborter;
import aletheia.utilities.aborter.Aborter.AbortException;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.utilint.IdentityHashMap;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.model.PersistentProxy;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawStore;
import com.sleepycat.persist.raw.RawType;

public abstract class EntityStoreUpgrade
{
	private final static Logger logger = LoggerManager.logger();

	@SuppressWarnings("unchecked")
	// @formatter:off
	private static final Class<? extends EntityStoreUpgrade>[] upgradeClasses = (Class<? extends EntityStoreUpgrade>[]) new Class<?>[]
			{
		EntityStoreUpgrade_000.class,
		EntityStoreUpgrade_001.class,
		EntityStoreUpgrade_002.class,
		EntityStoreUpgrade_003.class,
		EntityStoreUpgrade_004.class,
		EntityStoreUpgrade_005.class,
		EntityStoreUpgrade_014.class,
		EntityStoreUpgrade_016.class,
		EntityStoreUpgrade_017.class,
		EntityStoreUpgrade_018.class,
			};
	// @formatter:on

	private static final Map<Integer, EntityStoreUpgrade> upgradeMap;

	static
	{
		try
		{
			upgradeMap = new HashMap<Integer, EntityStoreUpgrade>();
			for (Class<? extends EntityStoreUpgrade> upgradeClass : upgradeClasses)
			{
				EntityStoreUpgrade upgrade = upgradeClass.newInstance();
				for (int version : upgrade.versions())
					if (upgradeMap.put(version, upgrade) != null)
						throw new Error();
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new Error(e);
		}
		finally
		{

		}
	}

	protected EntityStoreUpgrade()
	{

	}

	public abstract Collection<Integer> versions();

	public static EntityStoreUpgrade getEntityStoreUpgrade(int version)
	{
		return upgradeMap.get(version);
	}

	public class UpgradeException extends PersistenceException
	{
		private static final long serialVersionUID = 1733246537553898003L;

		public UpgradeException()
		{
			super();
		}

		public UpgradeException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UpgradeException(String message)
		{
			super(message);
		}

		public UpgradeException(Throwable cause)
		{
			super(cause);
		}

	}

	public class AbortUpgradeException extends UpgradeException
	{
		private static final long serialVersionUID = -443244686401334637L;

	}

	protected abstract class UpgradeInstance
	{
		private final BerkeleyDBAletheiaEnvironment environment;
		private final String storeName;

		private boolean abort;

		private class ShutdownHook extends Thread
		{
			private boolean terminate = false;

			@Override
			public void run()
			{
				logger.debug("UpgradeInstance ShutdownHook");
				synchronized (this)
				{
					try
					{
						UpgradeInstance.this.abort = true;
						while (!terminate)
							wait();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}

			public synchronized void terminate() throws InterruptedException
			{
				terminate = true;
				notifyAll();
				join();
			}
		}

		private final ShutdownHook shutdownHook;

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			this.environment = environment;
			this.storeName = storeName;
			this.abort = false;
			this.shutdownHook = new ShutdownHook();
		}

		protected BerkeleyDBAletheiaEnvironment getEnvironment()
		{
			return environment;
		}

		protected String getStoreName()
		{
			return storeName;
		}

		protected boolean isAbort()
		{
			return abort;
		}

		protected void upgrade() throws UpgradeException
		{
			logger.debug("Upgrading");
			String tmpStoreName = environment.unusedStoreName();
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			boolean generated = false;
			try
			{
				generateStore(tmpStoreName);
				generated = true;
				environment.removeStore(storeName);
				environment.renameStore(tmpStoreName, storeName);
			}
			catch (AbortUpgradeException e)
			{
				logger.error("Upgrade aborted!");
				if (!generated)
					environment.removeStore(tmpStoreName);
				getEnvironment().sync();
				getEnvironment().close();
				try
				{
					shutdownHook.terminate();
				}
				catch (InterruptedException e_)
				{
					e_.printStackTrace();
				}
			}
			catch (Exception e)
			{
				logger.error("Upgrade exception caught");
				if (!generated)
					environment.removeStore(tmpStoreName);
				throw e;
			}
			finally
			{
				try
				{
					environment.sync();
					shutdownHook.terminate();
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
				catch (IllegalStateException e)
				{

				}
			}
			logger.debug("Upgraded");
		}

		protected void postConversion(BerkeleyDBAletheiaEntityStore newStore)
		{

		}

		protected void postProcessing(final BerkeleyDBAletheiaEnvironment environment, final BerkeleyDBAletheiaEntityStore entityStore)
		{
			class MyPersistenceManager extends BerkeleyDBPersistenceManager
			{
				protected MyPersistenceManager()
				{
					super(environment, entityStore);
				}

				@Override
				public void close()
				{
					try
					{
						getPersistenceSchedulerThread().shutdown();
					}
					catch (InterruptedException e)
					{
						throw new PersistenceException(e);
					}
				}
			}

			MyPersistenceManager persistenceManager = new MyPersistenceManager();
			try
			{
				postProcessing(persistenceManager);
			}
			finally
			{
				persistenceManager.close();
			}

		}

		protected void postProcessing(BerkeleyDBPersistenceManager persistenceManager)
		{

		}

		protected void generateStore(String newStoreName) throws UpgradeException
		{
			logger.debug("Generating new entity store");
			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setTransactional(true);
			RawStore rawStore = new RawStore(environment, storeName, storeConfig);
			BerkeleyDBAletheiaEntityStore newStore = BerkeleyDBAletheiaEntityStore.open(environment, newStoreName, false, true);
			try
			{
				convertStore(rawStore, newStore);
				postConversion(newStore);
				createSecondaryIndices(newStore);
				logger.debug("Generated new entity store");
				postProcessing(environment, newStore);
			}
			finally
			{
				rawStore.close();
				newStore.close();
			}
		}

		protected void createSecondaryIndices(BerkeleyDBAletheiaEntityStore aletheiaStore) throws UpgradeException
		{
			logger.debug("Creating secondary indices");
			try
			{
				aletheiaStore.touchSecondaryIndices(new Aborter()
				{

					@Override
					public void checkAbort() throws AbortException
					{
						if (abort)
							throw new Aborter.AbortException();
					}

				});
			}
			catch (AbortException e)
			{
				throw new AbortUpgradeException();
			}
			logger.debug("Created secondary indices");
		}

		protected void convertStore(RawStore oldStore, BerkeleyDBAletheiaEntityStore aletheiaStore) throws UpgradeException
		{
			logger.debug("Converting entity store");
			EntityModel oldModel = oldStore.getModel();
			final Transaction tx = environment.beginTransaction(null, null);
			try
			{
				for (String className : oldModel.getKnownClasses())
				{
					if (abort)
						throw new AbortUpgradeException();
					RawType oldRawType = oldModel.getRawType(className);
					if (oldRawType.getClassMetadata().isEntityClass())
						convertClass(oldStore, aletheiaStore, tx, className);
				}
				logger.debug("Converted entity store");
				tx.commit();
			}
			finally
			{
				tx.abort();
			}

		}

		protected void convertClass(RawStore oldStore, BerkeleyDBAletheiaEntityStore aletheiaStore, Transaction tx, String className) throws UpgradeException
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
					int n = 0;
					try
					{
						for (RawObject oldRawObject : cursor)
						{
							if (abort)
								throw new AbortUpgradeException();
							putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
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

		protected void putConvertedRawObject(Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass, Class<Object> primaryKeyClass,
				PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			Object object = aletheiaModel.convertRawObject(oldRawObject);
			newPrimaryIndex.put(tx, object);
		}

		protected Object partialConvertRawObjectDefaultType(EntityModel model, RawObject rawObject, RawType rawType, Map<RawObject, Object> converted)
				throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
				IllegalArgumentException, InvocationTargetException
		{
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(rawType.getClassName());
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			Object object = constructor.newInstance();
			RawObject rawObject_ = rawObject;
			RawType rawType_ = rawType;
			while (rawObject_ != null && rawType_ != null)
			{
				Class<?> clazz_ = ClassLoader.getSystemClassLoader().loadClass(rawType_.getClassName());
				for (Map.Entry<String, Object> e : rawObject_.getValues().entrySet())
				{
					String sfield = e.getKey();
					Object value = e.getValue();
					if (value instanceof RawObject)
					{
						RawObject rawObject__ = (RawObject) value;
						value = partialConvertRawObject(model, rawObject__, converted);
					}
					try
					{
						Field field = clazz_.getDeclaredField(sfield);
						field.setAccessible(true);
						field.set(object, value);
					}
					catch (Exception ex2)
					{
					}
				}
				rawObject_ = rawObject_.getSuper();
				rawType_ = rawType_.getSuperType();
			}
			if (object instanceof PersistentProxy<?>)
				return ((PersistentProxy<?>) object).convertProxy();
			else
				return object;

		}

		protected Object partialConvertRawObjectArrayType(EntityModel model, RawObject rawObject, RawType rawType, Map<RawObject, Object> converted)
				throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
				IllegalArgumentException, InvocationTargetException
		{
			RawType componentType = rawType.getComponentType();
			int length = rawObject.getElements().length;
			Class<?> clazz;
			if (componentType.isPrimitive())
				clazz = MiscUtilities.resolvePrimitiveTypeClass(componentType.getClassName());
			else
				clazz = ClassLoader.getSystemClassLoader().loadClass(componentType.getClassName());
			Object a = Array.newInstance(clazz, length);
			for (int i = 0; i < length; i++)
			{
				Object value = rawObject.getElements()[i];
				if (value instanceof RawObject)
				{
					RawObject rawObject__ = (RawObject) value;
					value = partialConvertRawObject(model, rawObject__, converted);
				}
				Array.set(a, i, value);
			}
			return a;
		}

		protected Object partialConvertRawObject(EntityModel model, RawObject rawObject, Map<RawObject, Object> converted) throws ClassNotFoundException,
				NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
		{
			Object o = converted.get(rawObject);
			if (o == null)
			{
				RawType rawType = rawObject.getType();
				if (rawType.isArray())
					o = partialConvertRawObjectArrayType(model, rawObject, rawType, converted);
				else
					o = partialConvertRawObjectDefaultType(model, rawObject, rawType, converted);
				converted.put(rawObject, o);
			}
			return o;
		}

		protected Object partialConvertRawObject(EntityModel model, RawObject rawObject) throws ClassNotFoundException, NoSuchMethodException,
				SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
		{
			return partialConvertRawObject(model, rawObject, new IdentityHashMap<RawObject, Object>());
		}

		protected Object partialConvertRawObjectIfException(EntityModel model, RawObject rawObject) throws ClassNotFoundException, InstantiationException,
				IllegalAccessException, NoSuchFieldException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
		{
			try
			{
				return model.convertRawObject(rawObject);
			}
			catch (Exception ex)
			{
				return partialConvertRawObject(model, rawObject);
			}
		}

	}

	protected abstract UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName);

	public void upgrade(BerkeleyDBAletheiaEnvironment environment, String storeName) throws UpgradeException
	{
		instance(environment, storeName).upgrade();
	}

}
