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
package aletheia.persistence.berkeleydb;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.NodeDeferredMessageRecipientDateSecondaryKeyData;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.NodeDeferredMessageRecipientSecondaryKeyData;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent.CipheredDeferredMessageContentProxy;
import aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent.DeferredMessageContentProxy;
import aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent.PersonsDeferredMessageContentProxy;
import aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent.SignatureRequestDeferredMessageContentProxy;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBAletheiaTemporaryEntityStore extends BerkeleyDBAletheiaAbstractEntityStore
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerManager.logger();

	@SuppressWarnings("deprecation")
	private static final Collection<Class<?>> registerClasses = Arrays.<Class<?>> asList(
			// @formatter:off
			DeferredMessageContentProxy.class,
			CipheredDeferredMessageContentProxy.class,
			SignatureRequestDeferredMessageContentProxy.class,
			PersonsDeferredMessageContentProxy.class,
			aletheia.persistence.berkeleydb.proxies.peertopeer.deferredmessagecontent.DummyDeferredMessageContentProxy.class,
			BerkeleyDBDeferredMessageEntity.class
			// @formatter:on
			);

	protected BerkeleyDBAletheiaTemporaryEntityStore(BerkeleyDBAletheiaEnvironment env, String storeName, boolean bulkLoad) throws DatabaseException
	{
		super(env, storeName, makeConfig(env, bulkLoad, registerClasses));
		try
		{
			truncateEntityClasses();
		}
		catch (Exception e)
		{
			close();
			throw e;
		}
	}

	@Override
	public int storeVersion()
	{
		return 0;
	}

	public static BerkeleyDBAletheiaTemporaryEntityStore open(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		try
		{
			return new BerkeleyDBAletheiaTemporaryEntityStore(environment, storeName, false);
		}
		catch (Exception e)
		{
			if (environment.getConfig().getReadOnly())
				throw e;
			environment.removeStore(storeName);
			return new BerkeleyDBAletheiaTemporaryEntityStore(environment, storeName, false);
		}

	}

	public PrimaryIndex<UUIDKey, BerkeleyDBDeferredMessageEntity> deferredMessageEntityPrimaryIndex()
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBDeferredMessageEntity.class);
	}

	public PrimaryIndex<PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> nodeDeferredMessageEntityPrimaryIndex()
	{
		return getPrimaryIndex(BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData.class, BerkeleyDBNodeDeferredMessageEntity.class);
	}

	public SecondaryIndex<UUIDKey, PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> nodeDeferredMessageEntityDeferredMessageSecondaryIndex()
	{
		return getSecondaryIndex(nodeDeferredMessageEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBNodeDeferredMessageEntity.deferredMessageUuidKey_FieldName);
	}

	public EntityIndex<PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> nodeDeferredMessageEntityDeferredMessageSubIndex(UUIDKey deferredMessageUuidKey)
	{
		return nodeDeferredMessageEntityDeferredMessageSecondaryIndex().subIndex(deferredMessageUuidKey);
	}

	public SecondaryIndex<NodeDeferredMessageRecipientSecondaryKeyData, PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> nodeDeferredMessageEntityNodeRecipientSecondaryIndex()
	{
		return getSecondaryIndex(nodeDeferredMessageEntityPrimaryIndex(),
				BerkeleyDBNodeDeferredMessageEntity.NodeDeferredMessageRecipientSecondaryKeyData.class,
				BerkeleyDBNodeDeferredMessageEntity.nodeDeferredMessageRecipientSecondaryKeyData_FieldName);
	}

	public SecondaryIndex<NodeDeferredMessageRecipientDateSecondaryKeyData, PrimaryKeyData, BerkeleyDBNodeDeferredMessageEntity> nodeDeferredMessageEntityNodeRecipientDateSecondaryIndex()
	{
		return getSecondaryIndex(nodeDeferredMessageEntityPrimaryIndex(),
				BerkeleyDBNodeDeferredMessageEntity.NodeDeferredMessageRecipientDateSecondaryKeyData.class,
				BerkeleyDBNodeDeferredMessageEntity.nodeDeferredMessageRecipientDateSecondaryKeyData_FieldName);
	}

}
