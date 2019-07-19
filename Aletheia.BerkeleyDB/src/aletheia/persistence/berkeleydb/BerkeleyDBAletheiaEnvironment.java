/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCollection;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseNotFoundException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.ProgressListener;
import com.sleepycat.je.RecoveryProgress;
import com.sleepycat.je.Transaction;

public class BerkeleyDBAletheiaEnvironment extends Environment
{
	private final static Logger logger = LoggerManager.instance.logger();

	public BerkeleyDBAletheiaEnvironment(BerkeleyDBPersistenceManager.Configuration configuration)
	{
		super(makeDbFile(configuration), makeConfig(configuration));
	}

	private static File makeDbFile(BerkeleyDBPersistenceManager.Configuration configuration)
	{
		File dbFile = configuration.getDbFile();
		if (configuration.isAllowCreate())
			dbFile.mkdirs();
		return dbFile;
	}

	private static EnvironmentConfig makeConfig(final BerkeleyDBPersistenceManager.Configuration configuration)
	{
		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(configuration.isAllowCreate());
		environmentConfig.setReadOnly(configuration.isReadOnly());
		environmentConfig.setTransactional(true);
		environmentConfig.setDurability(Durability.COMMIT_NO_SYNC);
		environmentConfig.setLockTimeout(0, TimeUnit.SECONDS);
		if (configuration.getCachePercent() > 0)
			environmentConfig.setCachePercent(configuration.getCachePercent());
		environmentConfig.setSharedCache(configuration.isSharedCache());
		environmentConfig.setRecoveryProgressListener(new ProgressListener<RecoveryProgress>()
		{
			@Override
			public boolean progress(RecoveryProgress phase, long n, long total)
			{
				{
					if (n >= 0)
						logger.debug("Recovering environment: " + phase + ": " + n + "/" + total);
					else
						logger.debug("Recovering environment: " + phase);
				}
				{
					float progress = ((float) phase.ordinal()) / RecoveryProgress.values().length;
					if (n >= 0)
						progress += ((float) n) / total / RecoveryProgress.values().length;
					configuration.getStartupProgressListener().updateProgress(progress);
				}
				return true;
			}
		});
		return environmentConfig;
	}

	private String storeVersionDbName(String storeName)
	{
		return "storeVersion#" + storeName;
	}

	public int getStoreVersion(String storeName)
	{
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setReadOnly(getConfig().getReadOnly());
		try
		{
			Database db = openDatabase(null, storeVersionDbName(storeName), dbConfig);
			try
			{
				DatabaseEntry key = new DatabaseEntry();
				key.setData(new byte[0]);
				DatabaseEntry value = new DatabaseEntry();
				OperationStatus status = db.get(null, key, value, null);
				if (status != OperationStatus.SUCCESS)
					return -1;
				byte[] data = value.getData();
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				DataInputStream dis = new DataInputStream(bais);
				try
				{
					return dis.readInt();
				}
				finally
				{
					dis.close();
				}
			}
			catch (IOException e)
			{
				return -1;
			}
			finally
			{
				db.close();
			}
		}
		catch (DatabaseNotFoundException e)
		{
			return -1;
		}
	}

	public void removeStoreVersion(String storeName)
	{
		try
		{
			removeDatabase(null, storeVersionDbName(storeName));
		}
		catch (DatabaseNotFoundException e)
		{

		}
	}

	public class PutStoreVersionException extends RuntimeException
	{
		private static final long serialVersionUID = 6953997225373225159L;

		public PutStoreVersionException()
		{
			super();
		}

		public PutStoreVersionException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public PutStoreVersionException(String message)
		{
			super(message);
		}

		public PutStoreVersionException(Throwable cause)
		{
			super(cause);
		}

	}

	public void putStoreVersion(String storeName, int version) throws PutStoreVersionException
	{
		DatabaseConfig dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setReadOnly(getConfig().getReadOnly());
		Database db = openDatabase(null, storeVersionDbName(storeName), dbConfig);
		try
		{
			DatabaseEntry key = new DatabaseEntry();
			key.setData(new byte[0]);
			DatabaseEntry value = new DatabaseEntry();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(version);
			dos.close();
			value.setData(baos.toByteArray());
			OperationStatus status = db.put(null, key, value);
			if (status != OperationStatus.SUCCESS)
				throw new PutStoreVersionException();
		}
		catch (IOException e)
		{
			throw new PutStoreVersionException(e);
		}
		finally
		{
			db.close();
		}
	}

	public static boolean databaseNameMatchesStore(String storeName, String dbName)
	{
		return dbName.matches("(persist#" + Pattern.quote(storeName) + "#.*)|(storeVersion#" + Pattern.quote(storeName) + ")");
	}

	public Collection<String> storeDatabaseNames(final String storeName)
	{
		return new FilteredCollection<>(new Filter<String>()
		{

			@Override
			public boolean filter(String dbName)
			{
				return databaseNameMatchesStore(storeName, dbName);
			}
		}, getDatabaseNames());
	}

	public void renameStore(String oldStoreName, String newStoreName)
	{
		Transaction tx = beginTransaction(null, null);
		try
		{
			for (String dbName : storeDatabaseNames(oldStoreName))
			{
				String dbNameNew;
				if (dbName.equals("storeVersion#" + oldStoreName))
					dbNameNew = "storeVersion#" + newStoreName;
				else
					dbNameNew = dbName.replaceFirst("^persist#" + Pattern.quote(oldStoreName) + "#", "persist#" + newStoreName + "#");
				renameDatabase(tx, dbName, dbNameNew);
			}
			tx.commit();
		}
		finally
		{
			tx.abort();
		}
	}

	public String unusedStoreName()
	{
		SortedSet<String> nameSet = new TreeSet<>(getDatabaseNames());
		for (int i = 0;; i++)
		{
			String storeName = "tmpStore" + i;
			if (nameSet.subSet("persist#" + storeName + "#", "persist#" + storeName + "~").isEmpty() && !nameSet.contains("storeVersion#" + storeName))
				return storeName;
		}
	}

	public String renameStore(String storeName)
	{
		String oldStoreName = unusedStoreName();
		renameStore(storeName, oldStoreName);
		return oldStoreName;
	}

	public void removeStore(String oldStoreName)
	{
		Transaction tx = beginTransaction(null, null);
		try
		{
			for (String dbName : storeDatabaseNames(oldStoreName))
				try
				{
					removeDatabase(tx, dbName);
				}
				catch (DatabaseNotFoundException e)
				{
					logger.error(e.getMessage(), e);
				}
			tx.commit();
		}
		finally
		{
			tx.abort();
		}
	}

}
