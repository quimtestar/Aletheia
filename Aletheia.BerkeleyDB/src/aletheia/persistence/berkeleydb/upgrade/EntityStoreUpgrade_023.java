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

import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;

import org.apache.logging.log4j.Logger;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawStore;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.proxies.identifier.AbstractNodeNamespaceProxy;

public class EntityStoreUpgrade_023 extends EntityStoreUpgrade
{
	private static final Logger logger = LoggerManager.instance.logger();

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(23);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void upgrade() throws UpgradeException
		{
			unidentifyTauStatements();
			getEnvironment().putStoreVersion(getStoreName(), 24);
		}

		private void unidentifyTauStatements() throws UpgradeException
		{
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
							RawObject statementRawObject = rawObject;
							while (statementRawObject != null && !statementRawObject.getType().getClassName().equals(className))
								statementRawObject = statementRawObject.getSuper();
							if (statementRawObject == null)
								throw new UpgradeException();
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
											{
												unidentify(rawObject);
												cursor.update(rawObject);
												break;
											}
										}
										Object oParent = roAbstractNodeNamespace.getValues().get("parent");
										if (oParent instanceof RawObject)
											roAbstractNodeNamespace = (RawObject) oParent;
										else
											roAbstractNodeNamespace = null;
									}
								}

							}

							n++;
							if (n % 1000 == 0)
								logger.debug("Processed " + n + " entities");
						}
					}
					finally
					{
						cursor.close();
					}
					tx.commit();
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

		private void unidentify(RawObject rawObject)
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

	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
