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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawStore;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEntityStore;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager;
import aletheia.persistence.berkeleydb.BerkeleyDBTransaction;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.proxies.identifier.AbstractNodeNamespaceProxy;
import aletheia.persistence.exceptions.PersistenceException;

/*
 * TODO This upgrade has been proven hazardous.
 * This should be replaced with just a check that there is no "Tau" keyword
 * in the database with no modification or just delete it.
 * 
 * For now it is just dropped from the upgrade catalog in EntityStoreUpgrade.java.
 * 
 * UPDATE: Should be enough to delete and re-create the secondary indices that
 * store an identifier in its key.
 */
public class EntityStoreUpgrade_023 extends EntityStoreUpgrade
{
	private static final Logger logger = LoggerManager.instance.logger();

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(23);
	}

	protected abstract class AbstractUpgradeInstance extends EntityStoreUpgrade.UpgradeInstance
	{

		protected AbstractUpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		protected RawObject statementRawObject(RawObject rawObject)
		{
			RawObject statementRawObject = rawObject;
			while (statementRawObject != null && !statementRawObject.getType().getClassName().equals(BerkeleyDBStatementEntity.class.getName()))
				statementRawObject = statementRawObject.getSuper();
			return statementRawObject;
		}

		protected UUID obtainUuidFromStatementRawObject(RawObject rawObject)
		{
			RawObject statementRawObject = statementRawObject(rawObject);
			if (statementRawObject == null)
				return null;
			Object oUUIDKey = statementRawObject.getValues().get("uuidKey");
			if (oUUIDKey instanceof RawObject)
			{
				RawObject uuidKey = (RawObject) oUUIDKey;
				Object oLeastSigBits = uuidKey.getValues().get("leastSigBits");
				Object oMostSigBits = uuidKey.getValues().get("mostSigBits");
				if (oLeastSigBits instanceof Long && oMostSigBits instanceof Long)
				{
					long leastSigBits = ((Long) oLeastSigBits).longValue();
					long mostSigBits = ((Long) oMostSigBits).longValue();
					return new UUID(mostSigBits, leastSigBits);
				}
				else
					return null;
			}
			else
				return null;
		}

		protected boolean isTauInIdentifier(RawObject rawObject)
		{
			RawObject statementRawObject = statementRawObject(rawObject);
			if (statementRawObject == null)
				return false;
			Object oIdentifier = statementRawObject.getValues().get("identifier");
			if (oIdentifier instanceof RawObject)
			{
				RawObject roAbstractNodeNamespace = (RawObject) oIdentifier;
				while (roAbstractNodeNamespace != null)
				{
					while (roAbstractNodeNamespace != null
							&& !roAbstractNodeNamespace.getType().getClassName().equals(AbstractNodeNamespaceProxy.class.getName()))
						roAbstractNodeNamespace = roAbstractNodeNamespace.getSuper();
					if (roAbstractNodeNamespace != null)
					{
						Object oName = roAbstractNodeNamespace.getValues().get("name");
						if (oName instanceof String)
						{
							String name = (String) oName;
							if (name.equals("Tau"))
								return true;
						}
						Object oParent = roAbstractNodeNamespace.getValues().get("parent");
						if (oParent instanceof RawObject)
							roAbstractNodeNamespace = (RawObject) oParent;
						else
							roAbstractNodeNamespace = null;
					}
				}

			}
			return false;

		}

		protected void unidentify(RawObject rawObject)
		{
			Stack<RawObject> hierarchyStack = new Stack<>();
			while (rawObject != null)
			{
				hierarchyStack.push(rawObject);
				rawObject = rawObject.getSuper();
			}
			while (!hierarchyStack.isEmpty())
			{
				rawObject = hierarchyStack.pop();
				if (rawObject.getType().getClassName().equals(BerkeleyDBStatementEntity.class.getName()))
				{
					rawObject.getValues().put("identifier", null);
					rawObject.getValues().put("uuidContextIdentifier", null);
					Object oLocalSortKey = rawObject.getValues().get("localSortKey");
					if (oLocalSortKey instanceof RawObject)
					{
						RawObject localSortKey = (RawObject) oLocalSortKey;
						localSortKey.getValues().put("strIdentifier", "");
					}
				}
				else if (rawObject.getType().getClassName().equals(BerkeleyDBRootContextEntity.class.getName()))
					rawObject.getValues().put("identifierKey", null);
			}

		}

		protected void clearSignatures(BerkeleyDBPersistenceManager persistenceManager, Collection<UUID> uuids)
		{
			BerkeleyDBTransaction transaction = persistenceManager.beginTransaction();
			try
			{
				for (UUID uuid : uuids)
				{
					StatementAuthority stAuth = persistenceManager.getStatementAuthority(transaction, uuid);
					if (stAuth != null)
						stAuth.clearSignatures(transaction);
				}
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}

	}

	protected class UpgradeInstance extends AbstractUpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void upgrade() throws UpgradeException
		{
			Collection<UUID> uuids = unidentifyTauStatements();
			if (!uuids.isEmpty())
			{
				clearSecondaryIndices();
				getEnvironment().putStoreVersion(getStoreName(), 24);
				BerkeleyDBAletheiaEntityStore aletheiaStore = BerkeleyDBAletheiaEntityStore.open(getEnvironment(), getStoreName(), false);
				try
				{
					createSecondaryIndices(aletheiaStore);
					clearSignatures(aletheiaStore, uuids);
				}
				finally
				{
					aletheiaStore.close();
				}
			}
			else
				getEnvironment().putStoreVersion(getStoreName(), 24);
		}

		protected void clearSignatures(BerkeleyDBAletheiaEntityStore aletheiaStore, Collection<UUID> uuids)
		{
			class MyPersistenceManager extends BerkeleyDBPersistenceManager
			{
				protected MyPersistenceManager()
				{
					super(UpgradeInstance.this.getEnvironment(), aletheiaStore);
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
				clearSignatures(persistenceManager, uuids);
			}
			finally
			{
				persistenceManager.close();
			}

		}

		private void clearSecondaryIndices()
		{
			Collection<String> databaseNames = new ArrayList<>();
			RawStore rawStore = new RawStore(getEnvironment(), getStoreName(), new StoreConfig());
			try
			{
				Collection<SecondaryIndex<?, ?, ?>> indices = Arrays.asList(
						rawStore.getSecondaryIndex(BerkeleyDBStatementEntity.class.getName(), BerkeleyDBStatementEntity.localSortKey_FieldName),
						rawStore.getSecondaryIndex(BerkeleyDBStatementEntity.class.getName(), BerkeleyDBStatementEntity.uuidContextIdentifier_FieldName),
						rawStore.getSecondaryIndex(BerkeleyDBStatementEntity.class.getName(), BerkeleyDBRootContextEntity.identifierKey_FieldName));

				for (SecondaryIndex<?, ?, ?> index : indices)
				{
					Database db = index.getDatabase();
					if (db != null)
						databaseNames.add(db.getDatabaseName());
					Database kdb = index.getKeysDatabase();
					if (kdb != null)
						databaseNames.add(kdb.getDatabaseName());
				}
			}
			finally
			{
				rawStore.close();
			}
			Transaction tx = getEnvironment().beginTransaction(null, null);
			try
			{
				for (String dbn : databaseNames)
					getEnvironment().removeDatabase(tx, dbn);
				tx.commit();
			}
			finally
			{
				tx.abort();
			}

		}

		private Collection<UUID> unidentifyTauStatements() throws UpgradeException
		{
			Collection<UUID> uuids = new ArrayList<>();
			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setTransactional(true);
			RawStore rawStore = new RawStore(getEnvironment(), getStoreName(), storeConfig);
			try
			{
				final Transaction tx = getEnvironment().beginTransaction(null, null);
				try
				{
					String className = BerkeleyDBStatementEntity.class.getName();
					PrimaryIndex<Object, RawObject> primaryIndex = rawStore.getPrimaryIndex(className);
					EntityCursor<RawObject> cursor = primaryIndex.entities(tx, CursorConfig.DEFAULT);
					int n = 0;
					try
					{
						for (RawObject rawObject : cursor)
						{
							if (isAbort())
								throw new AbortUpgradeException();
							if (isTauInIdentifier(rawObject))
							{
								UUID uuid = obtainUuidFromStatementRawObject(rawObject);
								logger.debug("Clearing identifier of statement: " + uuid);
								throw new UpgradeException(
										"This routine can't be trusted and needs further work. Try to search and modify the identifiers with *.Tau.* by yourself. Sorry.");
								/*
								 * TODO Uncomment and analyze this code
								 * 
								unidentify(rawObject);
								cursor.update(rawObject);
								if (uuid != null)
									uuids.add(uuid);
								*/
							}
							n++;
							if (n % 1000 == 0)
								logger.debug("Processed " + n + " statement entities");
						}
						logger.debug("Processed " + n + " statement entities");
					}
					finally
					{
						cursor.close();
					}
					tx.commit();
					return uuids;
				}
				finally
				{
					tx.abort();
				}

			}
			finally
			{
				rawStore.close();
			}

		}

	}

	@Override
	protected AbstractUpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
