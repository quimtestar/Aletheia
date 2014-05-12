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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import aletheia.model.security.SignatureData;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEntityStore;
import aletheia.persistence.berkeleydb.BerkeleyDBAletheiaEnvironment;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBContextAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBRootContextAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;

public class EntityStoreUpgrade_014 extends EntityStoreUpgrade_016
{

	public EntityStoreUpgrade_014()
	{
	}

	@Override
	public Collection<Integer> versions()
	{
		return Arrays.asList(14, 15);
	}

	protected class UpgradeInstance extends EntityStoreUpgrade_016.UpgradeInstance
	{

		protected UpgradeInstance(BerkeleyDBAletheiaEnvironment environment, String storeName)
		{
			super(environment, storeName);
		}

		@Override
		protected void putConvertedRawObject(Transaction tx, EntityModel aletheiaModel, Class<Object> entityClass, Class<Object> primaryKeyClass,
				PrimaryIndex<Object, Object> newPrimaryIndex, RawObject oldRawObject)
		{
			if (entityClass.equals(BerkeleyDBStatementAuthorityEntity.class))
			{
				try
				{
					Object object = partialConvertRawObjectIfException(aletheiaModel, oldRawObject);
					newPrimaryIndex.put(tx, object);
				}
				catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException
						| NoSuchMethodException | IllegalArgumentException | InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}
			}
			else
				super.putConvertedRawObject(tx, aletheiaModel, entityClass, primaryKeyClass, newPrimaryIndex, oldRawObject);
		}

		@Override
		protected void postConversion(BerkeleyDBAletheiaEntityStore newStore)
		{
			super.postConversion(newStore);
			PrimaryIndex<UUIDKey, BerkeleyDBStatementAuthorityEntity> statementAuthorityEntityPrimaryIndex = newStore.statementAuthorityEntityPrimaryIndex();
			PrimaryIndex<UUIDKey, BerkeleyDBStatementEntity> statementEntityPrimaryIndex = newStore.statementEntityPrimaryIndex();
			PrimaryIndex<BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> statementAuthoritySignatureEntityPrimaryIndex = newStore
					.statementAuthoritySignatureEntityPrimaryIndex();
			Transaction tx = getEnvironment().beginTransaction(null, null);
			try
			{
				EntityCursor<BerkeleyDBStatementAuthorityEntity> cursor = statementAuthorityEntityPrimaryIndex.entities(tx, CursorConfig.DEFAULT);
				try
				{
					while (true)
					{
						BerkeleyDBStatementAuthorityEntity statementAuthorityEntity = cursor.next();
						if (statementAuthorityEntity == null)
							break;
						BerkeleyDBStatementEntity statementEntity = statementEntityPrimaryIndex.get(new UUIDKey(statementAuthorityEntity.getStatementUuid()));
						if (statementEntity instanceof BerkeleyDBRootContextEntity)
						{
							if (!statementAuthorityEntity.getClass().equals(BerkeleyDBRootContextAuthorityEntity.class))
							{
								BerkeleyDBRootContextAuthorityEntity e_ = new BerkeleyDBRootContextAuthorityEntity();
								e_.setAuthorUuid(statementAuthorityEntity.getAuthorUuid());
								e_.setContextUuid(statementAuthorityEntity.getContextUuid());
								e_.setCreationDate(statementAuthorityEntity.getCreationDate());
								e_.setSignedDependencies(statementAuthorityEntity.isSignedDependencies());
								e_.setSignedProof(statementAuthorityEntity.isSignedProof());
								e_.setStatementUuid(statementAuthorityEntity.getStatementUuid());
								e_.setValidSignature(statementAuthorityEntity.isValidSignature());

								BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData pkd = new BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData(
										e_.getStatementUuid(), e_.getAuthorUuid());
								BerkeleyDBStatementAuthoritySignatureEntity sase = statementAuthoritySignatureEntityPrimaryIndex.get(pkd);
								if (sase != null)
								{
									SignatureData signatureData = sase.getSignatureData();
									if (signatureData != null)
										e_.setSignatureUuid(signatureData.uuid());
								}
								cursor.update(e_);
							}
						}
						else if (statementEntity instanceof BerkeleyDBContextEntity)
						{
							if (!statementAuthorityEntity.getClass().equals(BerkeleyDBContextAuthorityEntity.class))
							{
								BerkeleyDBContextAuthorityEntity e_ = new BerkeleyDBContextAuthorityEntity();
								e_.setAuthorUuid(statementAuthorityEntity.getAuthorUuid());
								e_.setContextUuid(statementAuthorityEntity.getContextUuid());
								e_.setCreationDate(statementAuthorityEntity.getCreationDate());
								e_.setSignedDependencies(statementAuthorityEntity.isSignedDependencies());
								e_.setSignedProof(statementAuthorityEntity.isSignedProof());
								e_.setStatementUuid(statementAuthorityEntity.getStatementUuid());
								e_.setValidSignature(statementAuthorityEntity.isValidSignature());
								cursor.update(e_);
							}
						}
						else
						{
							if (!statementAuthorityEntity.getClass().equals(BerkeleyDBStatementAuthorityEntity.class))
							{
								BerkeleyDBContextAuthorityEntity e_ = new BerkeleyDBContextAuthorityEntity();
								e_.setAuthorUuid(statementAuthorityEntity.getAuthorUuid());
								e_.setContextUuid(statementAuthorityEntity.getContextUuid());
								e_.setCreationDate(statementAuthorityEntity.getCreationDate());
								e_.setSignedDependencies(statementAuthorityEntity.isSignedDependencies());
								e_.setSignedProof(statementAuthorityEntity.isSignedProof());
								e_.setStatementUuid(statementAuthorityEntity.getStatementUuid());
								e_.setValidSignature(statementAuthorityEntity.isValidSignature());
								cursor.update(e_);
							}
						}
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
	}

	@Override
	protected UpgradeInstance instance(BerkeleyDBAletheiaEnvironment environment, String storeName)
	{
		return new UpgradeInstance(environment, storeName);
	}

}
