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
package aletheia.persistence.berkeleydb.lowlevelbackuprestore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEntityStore;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.VersionProtocol;
import aletheia.protocol.primitive.BooleanProtocol;
import aletheia.protocol.primitive.StringProtocol;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseNotFoundException;

public class LowLevelBackupRestoreEntityStore
{
	private final static Logger logger = LoggerManager.logger();
	private static final int backupVersion = 0;

	private final BerkeleyDBAletheiaEnvironment environment;

	public LowLevelBackupRestoreEntityStore(BerkeleyDBAletheiaEnvironment environment)
	{
		this.environment = environment;
	}

	public void backup(DataOutput out, String storeName) throws IOException
	{
		VersionProtocol versionProtocol = new VersionProtocol();
		versionProtocol.send(out, backupVersion);
		BooleanProtocol booleanProtocol = new BooleanProtocol(0);
		StringProtocol stringProtocol = new StringProtocol(0);
		{
			String dbName = "storeVersion#" + storeName;
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setReadOnly(true);
			Database db = environment.openDatabase(null, dbName, dbConfig);
			DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
			try
			{
				databaseContentProtocol.send(out);
			}
			finally
			{
				db.close();
			}
		}
		{
			String dbName = "persist#" + storeName + "#" + "com.sleepycat.persist.formats";
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setReadOnly(true);
			Database db = environment.openDatabase(null, dbName, dbConfig);
			DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
			try
			{
				databaseContentProtocol.send(out);
			}
			finally
			{
				db.close();
			}
		}
		{
			String dbName = "persist#" + storeName + "#" + "com.sleepycat.persist.sequences";
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setReadOnly(true);
			try
			{
				Database db = environment.openDatabase(null, dbName, dbConfig);
				booleanProtocol.send(out, true);
				DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
				try
				{
					databaseContentProtocol.send(out);
				}
				finally
				{
					db.close();
				}
			}
			catch (DatabaseNotFoundException e)
			{
				booleanProtocol.send(out, false);
			}
		}
		BerkeleyDBAletheiaEntityStore entityStore = BerkeleyDBAletheiaEntityStore.open(environment, storeName, false);
		try
		{
			for (String className : entityStore.getModel().getKnownClasses())
			{
				if (entityStore.getModel().getClassMetadata(className).isEntityClass())
				{
					String dbName = "persist#" + entityStore.getStoreName() + "#" + className;
					DatabaseConfig dbConfig = entityStore.getPrimaryConfig(entityStore.resolveClass(className));
					dbConfig.setAllowCreate(false);
					try
					{
						Database db = environment.openDatabase(null, dbName, dbConfig);
						stringProtocol.send(out, className);
						DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
						try
						{
							databaseContentProtocol.send(out);
						}
						finally
						{
							db.close();
						}
					}
					catch (DatabaseNotFoundException e)
					{
						logger.warn(e.getMessage(), e);
					}
				}
			}
			stringProtocol.send(out, "");
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			entityStore.close();
		}
	}

	public class VersionException extends ProtocolException
	{
		private static final long serialVersionUID = 8038390795353591072L;

		protected VersionException(int version)
		{
			super("Backup version " + version + " not supported :(");
		}

	}

	public void restore(DataInput in, String storeName) throws IOException, ProtocolException
	{
		VersionProtocol versionProtocol = new VersionProtocol();
		int version = versionProtocol.recv(in);
		if (version != 0)
			throw new VersionException(version);
		BooleanProtocol booleanProtocol = new BooleanProtocol(0);
		StringProtocol stringProtocol = new StringProtocol(0);
		String oldStoreName = environment.renameStore(storeName);
		{
			String dbName = "storeVersion#" + storeName;
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			Database db = environment.openDatabase(null, dbName, dbConfig);
			DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
			try
			{
				databaseContentProtocol.recv(in);
			}
			finally
			{
				db.close();
			}
		}
		{
			String dbName = "persist#" + storeName + "#" + "com.sleepycat.persist.formats";
			DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			Database db = environment.openDatabase(null, dbName, dbConfig);
			DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
			try
			{
				databaseContentProtocol.recv(in);
			}
			finally
			{
				db.close();
			}
		}
		{
			boolean sequencesDatabaseSent = booleanProtocol.recv(in);
			if (sequencesDatabaseSent)
			{
				String dbName = "persist#" + storeName + "#" + "com.sleepycat.persist.sequences";
				DatabaseConfig dbConfig = new DatabaseConfig();
				dbConfig.setAllowCreate(true);
				Database db = environment.openDatabase(null, dbName, dbConfig);
				DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
				try
				{
					databaseContentProtocol.recv(in);
				}
				finally
				{
					db.close();
				}
			}
		}
		BerkeleyDBAletheiaEntityStore entityStore = BerkeleyDBAletheiaEntityStore.open(environment, storeName, false, true);
		try
		{
			while (true)
			{
				String className = stringProtocol.recv(in);
				if (className.isEmpty())
					break;
				if (!entityStore.getModel().getClassMetadata(className).isEntityClass())
					throw new ProtocolException();
				String dbName = "persist#" + entityStore.getStoreName() + "#" + className;
				DatabaseConfig dbConfig = entityStore.getPrimaryConfig(entityStore.resolveClass(className));
				Database db = environment.openDatabase(null, dbName, dbConfig);
				DatabaseContentProtocol databaseContentProtocol = new DatabaseContentProtocol(0, db);
				try
				{
					databaseContentProtocol.recv(in);
				}
				finally
				{
					db.close();
				}
			}
			entityStore.touchSecondaryIndices();
		}
		catch (Exception e)
		{
			entityStore.close();
			environment.removeStore(entityStore.getStoreName());
			environment.renameStore(oldStoreName, entityStore.getStoreName());
			try
			{
				throw e;
			}
			catch (ClassNotFoundException e_)
			{
				throw new RuntimeException(e_);
			}
		}
		finally
		{
			entityStore.close();
			environment.removeStore(oldStoreName);
		}
	}

}
