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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.identifier.Identifier;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager.EntityStoreVersionException;
import aletheia.persistence.berkeleydb.BerkeleyDBPersistenceManager.UnsupportedEntityStoreVersionException;
import aletheia.persistence.berkeleydb.entities.UUIDKey;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBContextAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateAuthorizerEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeRootNodeEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBDelegateTreeSubNodeEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBEncryptedPrivateSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPackedSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPersonEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPlainPrivateSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPrivatePersonEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBPrivateSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBRootContextAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity.ContextSubContextSecondaryKeyData;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBUnpackedSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBContextLocalEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBRootContextLocalEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBStatementLocalEntity;
import aletheia.persistence.berkeleydb.entities.misc.BerkeleyDBPersistenceSecretKeySingletonEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBHookEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity.UUIDKeyOrder;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBDeclarationEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity.IdentifierKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBSpecializationEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.LocalSortKey;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.UUIDContextIdentifier;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity.UUIDKeyTermHash;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBUnfoldingContextEntity;
import aletheia.persistence.berkeleydb.proxies.UUIDProxy;
import aletheia.persistence.berkeleydb.proxies.identifier.IdentifierProxy;
import aletheia.persistence.berkeleydb.proxies.identifier.NamespaceProxy;
import aletheia.persistence.berkeleydb.proxies.identifier.NodeNamespaceProxy;
import aletheia.persistence.berkeleydb.proxies.identifier.RootNamespaceProxy;
import aletheia.persistence.berkeleydb.proxies.net.InetAddressProxy;
import aletheia.persistence.berkeleydb.proxies.net.InetSocketAddressProxy;
import aletheia.persistence.berkeleydb.proxies.parameteridentification.CompositionParameterIdentificationProxy;
import aletheia.persistence.berkeleydb.proxies.parameteridentification.FunctionParameterIdentificationProxy;
import aletheia.persistence.berkeleydb.proxies.parameteridentification.ParameterIdentificationProxy;
import aletheia.persistence.berkeleydb.proxies.security.MessageDigestDataProxy;
import aletheia.persistence.berkeleydb.proxies.security.PrivateKeyProxy;
import aletheia.persistence.berkeleydb.proxies.security.PublicKeyProxy;
import aletheia.persistence.berkeleydb.proxies.security.SignatureDataProxy;
import aletheia.persistence.berkeleydb.proxies.term.CastTypeTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.CompositionTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.FoldingCastTypeTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.FunctionTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.IdentifiableVariableTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.ParameterVariableTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.ProjectedCastTypeTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.ProjectionCastTypeTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.ProjectionTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.SimpleTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.TauTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.TermProxy;
import aletheia.persistence.berkeleydb.proxies.term.UnprojectedCastTypeTermProxy;
import aletheia.persistence.berkeleydb.proxies.term.VariableTermProxy;
import aletheia.persistence.berkeleydb.upgrade.EntityStoreUpgrade;
import aletheia.persistence.berkeleydb.upgrade.EntityStoreUpgrade.UpgradeException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class BerkeleyDBAletheiaEntityStore extends BerkeleyDBAletheiaAbstractEntityStore
{
	private static final Logger logger = LoggerManager.instance.logger();

	private static final int storeVersion = 26;
	private static final int minimalStoreVersion = 25;

	private static final Collection<Class<?>> registerClasses = Arrays.<Class<?>> asList(
	// @formatter:off
			TermProxy.class,
			TauTermProxy.class,
			FunctionTermProxy.class,
			ProjectionTermProxy.class,
			SimpleTermProxy.class,
			CompositionTermProxy.class,
			VariableTermProxy.class,
			ParameterVariableTermProxy.class,
			IdentifiableVariableTermProxy.class,
			CastTypeTermProxy.class,
			ProjectionCastTypeTermProxy.class,
			ProjectedCastTypeTermProxy.class,
			UnprojectedCastTypeTermProxy.class,
			FoldingCastTypeTermProxy.class,
			UUIDProxy.class,
			
			ParameterIdentificationProxy.class,
			CompositionParameterIdentificationProxy.class,
			FunctionParameterIdentificationProxy.class,

			NamespaceProxy.class,
			RootNamespaceProxy.class,
			NodeNamespaceProxy.class,
			IdentifierProxy.class,

			UUIDKey.class,

			BerkeleyDBStatementEntity.class,
			BerkeleyDBContextEntity.class,
			BerkeleyDBAssumptionEntity.class,
			BerkeleyDBSpecializationEntity.class,
			BerkeleyDBUnfoldingContextEntity.class,
			BerkeleyDBRootContextEntity.class,
			BerkeleyDBDeclarationEntity.class,

			BerkeleyDBSignatoryEntity.class,
			BerkeleyDBPrivateSignatoryEntity.class,
			BerkeleyDBPlainPrivateSignatoryEntity.class,
			BerkeleyDBEncryptedPrivateSignatoryEntity.class,
			BerkeleyDBPersonEntity.class,
			BerkeleyDBPrivatePersonEntity.class,
			BerkeleyDBStatementAuthorityEntity.class,
			BerkeleyDBContextAuthorityEntity.class,
			BerkeleyDBRootContextAuthorityEntity.class,
			BerkeleyDBStatementAuthoritySignatureEntity.class,
			BerkeleyDBDelegateTreeNodeEntity.class,
			BerkeleyDBDelegateTreeRootNodeEntity.class,
			BerkeleyDBDelegateTreeSubNodeEntity.class,
			BerkeleyDBDelegateAuthorizerEntity.class,
			BerkeleyDBSignatureRequestEntity.class,
			BerkeleyDBPackedSignatureRequestEntity.class,
			BerkeleyDBUnpackedSignatureRequestEntity.class,

			PublicKeyProxy.class,
			PrivateKeyProxy.class,
			SignatureDataProxy.class,
			MessageDigestDataProxy.class,

			BerkeleyDBStatementLocalEntity.class,
			BerkeleyDBContextLocalEntity.class,
			BerkeleyDBRootContextLocalEntity.class,

			InetAddressProxy.class,
			InetAddressProxy.Inet4AddressProxy.class,
			InetAddressProxy.Inet6AddressProxy.class,
			InetSocketAddressProxy.class,

			BerkeleyDBHookEntity.class,

			BerkeleyDBPersistenceSecretKeySingletonEntity.class
	// @formatter:on
	);

	protected BerkeleyDBAletheiaEntityStore(BerkeleyDBAletheiaEnvironment env, String storeName, boolean bulkLoad) throws DatabaseException
	{
		super(env, storeName, makeConfig(env, bulkLoad, registerClasses));
	}

	protected BerkeleyDBAletheiaEntityStore(BerkeleyDBAletheiaEnvironment env, String storeName) throws DatabaseException
	{
		this(env, storeName, false);
	}

	@Override
	public int storeVersion()
	{
		return storeVersion;
	}

	@Override
	public int minimalStoreVersion()
	{
		return minimalStoreVersion;
	}

	public static BerkeleyDBAletheiaEntityStore open(BerkeleyDBAletheiaEnvironment environment, String storeName, boolean allowUpgrade, boolean bulkLoad)
			throws UpgradeException
	{
		int currentStoreVersion = environment.getStoreVersion(storeName);
		if (currentStoreVersion >= 0 && (currentStoreVersion < minimalStoreVersion || currentStoreVersion > storeVersion))
			throw new UnsupportedEntityStoreVersionException(currentStoreVersion, storeVersion);
		try
		{
			return new BerkeleyDBAletheiaEntityStore(environment, storeName, bulkLoad);
		}
		catch (Exception e)
		{
			logger.error("Error caught opening store", e);
			EntityStoreUpgrade upgrade = EntityStoreUpgrade.getEntityStoreUpgrade(currentStoreVersion);
			if (upgrade == null)
				throw e;
			if (environment.getConfig().getReadOnly() || !allowUpgrade)
				throw new EntityStoreVersionException(e, currentStoreVersion, storeVersion);
			logger.info("Trying to upgrade from store version:" + currentStoreVersion);
			upgrade.upgrade(environment, storeName);
			logger.info("Upgrade success!!! :D");
			return new BerkeleyDBAletheiaEntityStore(environment, storeName);
		}
	}

	public static BerkeleyDBAletheiaEntityStore open(BerkeleyDBAletheiaEnvironment environment, String storeName, boolean allowUpgrade) throws UpgradeException
	{
		return open(environment, storeName, allowUpgrade, false);
	}

	public static BerkeleyDBAletheiaEntityStore open(BerkeleyDBAletheiaEnvironment environment, String storeName) throws UpgradeException
	{
		return open(environment, storeName, true);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBStatementEntity> statementEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBStatementEntity.class);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementEntity> statementEntityContextSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBStatementEntity.uuidKeyContext_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBStatementEntity> statementEntityContextSubIndex(UUIDKey uuidKeyContext) throws DatabaseException
	{
		return statementEntityContextSecondaryIndex().subIndex(uuidKeyContext);
	}

	public SecondaryIndex<Boolean, UUIDKey, BerkeleyDBRootContextEntity> rootContextEntityMarkSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBRootContextEntity.class, Boolean.class, BerkeleyDBRootContextEntity.mark_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBRootContextEntity> rootContextEntityMarkSubIndex(boolean mark) throws DatabaseException
	{
		return rootContextEntityMarkSecondaryIndex().subIndex(mark);
	}

	public SecondaryIndex<IdentifierKey, UUIDKey, BerkeleyDBRootContextEntity> rootContextEntityIdentifierSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBRootContextEntity.class, BerkeleyDBRootContextEntity.IdentifierKey.class,
				BerkeleyDBRootContextEntity.identifierKey_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBRootContextEntity> rootContextEntityIdentifierSubIndex(Identifier identifier) throws DatabaseException
	{
		return rootContextEntityIdentifierSecondaryIndex().subIndex(new BerkeleyDBRootContextEntity.IdentifierKey(identifier));
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementEntity> statementEntityDependenciesSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBStatementEntity.uuidKeyDependencies_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBStatementEntity> statementEntityDependenciesSubIndex(UUIDKey uuidKey) throws DatabaseException
	{
		return statementEntityDependenciesSecondaryIndex().subIndex(uuidKey);
	}

	public SecondaryIndex<BerkeleyDBStatementEntity.UUIDKeyTermHash, UUIDKey, BerkeleyDBStatementEntity> statementEntityTermHashSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementEntityPrimaryIndex(), BerkeleyDBStatementEntity.UUIDKeyTermHash.class,
				BerkeleyDBStatementEntity.uuidKeyTermHash_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBStatementEntity> statementEntityTermHashSubIndex(UUIDKey uuidKeyContext, int termHash) throws DatabaseException
	{
		BerkeleyDBStatementEntity.UUIDKeyTermHash uuidKeyTermHash = new BerkeleyDBStatementEntity.UUIDKeyTermHash();
		uuidKeyTermHash.setUUIDKey(uuidKeyContext);
		uuidKeyTermHash.setTermHash(termHash);
		return statementEntityTermHashSecondaryIndex().subIndex(uuidKeyTermHash);
	}

	public SecondaryIndex<UUIDKeyOrder, UUIDKey, BerkeleyDBAssumptionEntity> assumptionEntityKeyOrderIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBAssumptionEntity.class, BerkeleyDBAssumptionEntity.UUIDKeyOrder.class, "uuidKeyOrder");
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBContextEntity> contextEntityContextSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBContextEntity.class, UUIDKey.class, BerkeleyDBContextEntity.uuidKeyContext__FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBContextEntity> contextEntityContextSubIndex(UUIDKey uuidKeyContext) throws DatabaseException
	{
		return contextEntityContextSecondaryIndex().subIndex(uuidKeyContext);
	}

	public SecondaryIndex<UUIDContextIdentifier, UUIDKey, BerkeleyDBStatementEntity> statementEntityContextIdentifierSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementEntityPrimaryIndex(), UUIDContextIdentifier.class, BerkeleyDBStatementEntity.uuidContextIdentifier_FieldName);
	}

	public SecondaryIndex<UUIDKeyTermHash, UUIDKey, BerkeleyDBContextEntity> contextEntityConsequentHashSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBContextEntity.class, BerkeleyDBStatementEntity.UUIDKeyTermHash.class,
				BerkeleyDBContextEntity.uuidKeyAncestorsConsequentHash_FieldName);
	}

	public SecondaryIndex<LocalSortKey, UUIDKey, BerkeleyDBStatementEntity> statementEntityLocalSortKeySecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementEntityPrimaryIndex(), BerkeleyDBStatementEntity.LocalSortKey.class, BerkeleyDBStatementEntity.localSortKey_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBUnfoldingContextEntity> unfoldingContextEntityDeclarationSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBUnfoldingContextEntity.class, UUIDKey.class,
				BerkeleyDBUnfoldingContextEntity.uuidKeyDeclaration_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBSpecializationEntity> specializationEntityGeneralSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(statementEntityPrimaryIndex(), BerkeleyDBSpecializationEntity.class, UUIDKey.class,
				BerkeleyDBSpecializationEntity.uuidKeyGeneral_FieldName);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBSignatoryEntity> signatoryEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBSignatoryEntity.class);
	}

	public SecondaryIndex<Boolean, UUIDKey, BerkeleyDBPrivateSignatoryEntity> privateSignatoryEntityMarkSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(signatoryEntityPrimaryIndex(), BerkeleyDBPrivateSignatoryEntity.class, Boolean.class,
				BerkeleyDBPrivateSignatoryEntity.mark_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBPrivateSignatoryEntity> privateSignatoryEntityMarkSubIndex(boolean mark) throws DatabaseException
	{
		return privateSignatoryEntityMarkSecondaryIndex().subIndex(mark);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBPersonEntity> personEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBPersonEntity.class);
	}

	public SecondaryIndex<String, UUIDKey, BerkeleyDBPersonEntity> personEntityNickSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(personEntityPrimaryIndex(), String.class, BerkeleyDBPersonEntity.nick_FieldName);
	}

	public SecondaryIndex<Date, UUIDKey, BerkeleyDBPersonEntity> personEntityOrphanSinceSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(personEntityPrimaryIndex(), Date.class, BerkeleyDBPersonEntity.orphanSince_FieldName);
	}

	public SecondaryIndex<Boolean, UUIDKey, BerkeleyDBPrivatePersonEntity> privatePersonEntityMarkSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(personEntityPrimaryIndex(), BerkeleyDBPrivatePersonEntity.class, Boolean.class, BerkeleyDBPrivatePersonEntity.mark_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBPrivatePersonEntity> privatePersonEntityMarkSubIndex(boolean mark) throws DatabaseException
	{
		return privatePersonEntityMarkSecondaryIndex().subIndex(mark);
	}

	public SecondaryIndex<String, UUIDKey, BerkeleyDBPrivatePersonEntity> privatePersonEntityNickSecondaryIndex() throws DatabaseException
	{
		return getSubclassIndex(personEntityPrimaryIndex(), BerkeleyDBPrivatePersonEntity.class, String.class, BerkeleyDBPrivatePersonEntity.nick__FieldName);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBStatementAuthorityEntity> statementAuthorityEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBStatementAuthorityEntity.class);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementAuthorityEntity> statementAuthorityEntityContextSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementAuthorityEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBStatementAuthorityEntity.contextUuidKey_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementAuthorityEntity> statementAuthorityEntityAuthorSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementAuthorityEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBStatementAuthorityEntity.authorUuidKey_FieldName);
	}

	public SecondaryIndex<BerkeleyDBStatementAuthorityEntity.ContextFlagSecondaryKeyData, UUIDKey, BerkeleyDBStatementAuthorityEntity> statementAuthorityEntityContextSignedDependenciesSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementAuthorityEntityPrimaryIndex(), BerkeleyDBStatementAuthorityEntity.ContextFlagSecondaryKeyData.class,
				BerkeleyDBStatementAuthorityEntity.contextSignedDependenciesSecondaryKeyData_FieldName);
	}

	public SecondaryIndex<BerkeleyDBStatementAuthorityEntity.ContextFlagSecondaryKeyData, UUIDKey, BerkeleyDBStatementAuthorityEntity> statementAuthorityEntityContextSignedProofSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementAuthorityEntityPrimaryIndex(), BerkeleyDBStatementAuthorityEntity.ContextFlagSecondaryKeyData.class,
				BerkeleyDBStatementAuthorityEntity.contextSignedProofSecondaryKeyData_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBRootContextAuthorityEntity> rootContextAuthorityEntitySignatureUuidSecondaryIndex()
	{
		return getSubclassIndex(statementAuthorityEntityPrimaryIndex(), BerkeleyDBRootContextAuthorityEntity.class, UUIDKey.class,
				BerkeleyDBRootContextAuthorityEntity.signatureUuidKey_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBRootContextAuthorityEntity> rootContextAuthorityEntitySignatureUuidSubIndex(UUIDKey signatureUuidKey)
	{
		return rootContextAuthorityEntitySignatureUuidSecondaryIndex().subIndex(signatureUuidKey);
	}

	public PrimaryIndex<BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> statementAuthoritySignatureEntityPrimaryIndex()
			throws DatabaseException
	{
		return getPrimaryIndex(BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData.class, BerkeleyDBStatementAuthoritySignatureEntity.class);
	}

	public SecondaryIndex<UUIDKey, BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> statementAuthoritySignatureEntityStatementSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementAuthoritySignatureEntityPrimaryIndex(), UUIDKey.class,
				BerkeleyDBStatementAuthoritySignatureEntity.statementUuidKey_FieldName);
	}

	public SecondaryIndex<UUIDKey, BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> statementAuthoritySignatureEntityAuthorizerSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementAuthoritySignatureEntityPrimaryIndex(), UUIDKey.class,
				BerkeleyDBStatementAuthoritySignatureEntity.authorizerUuidKey_FieldName);
	}

	public SecondaryIndex<BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData, BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> statementAuthoritySignatureEntityStatementSignatureDateSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementAuthoritySignatureEntityPrimaryIndex(),
				BerkeleyDBStatementAuthoritySignatureEntity.StatementSignatureDateKeyData.class,
				BerkeleyDBStatementAuthoritySignatureEntity.statementSignatureDateKeyData_FieldName);
	}

	public SecondaryIndex<BerkeleyDBStatementAuthoritySignatureEntity.AuthorizerSignatureUuidKeyData, BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData, BerkeleyDBStatementAuthoritySignatureEntity> statementAuthoritySignatureEntityAuthorizerSignatureUuidKeySecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementAuthoritySignatureEntityPrimaryIndex(),
				BerkeleyDBStatementAuthoritySignatureEntity.AuthorizerSignatureUuidKeyData.class,
				BerkeleyDBStatementAuthoritySignatureEntity.authorizerSignatureUuidKeyData_FieldName);
	}

	public PrimaryIndex<BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData, BerkeleyDBDelegateTreeNodeEntity> delegateTreeNodeEntityPrimaryIndex()
			throws DatabaseException
	{
		return getPrimaryIndex(BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData.class, BerkeleyDBDelegateTreeNodeEntity.class);
	}

	public SecondaryIndex<BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData, BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData, BerkeleyDBDelegateTreeNodeEntity> delegateTreeSubNodeEntityParentSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(delegateTreeNodeEntityPrimaryIndex(), BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData.class,
				BerkeleyDBDelegateTreeSubNodeEntity.parent_FieldName);
	}

	public EntityIndex<BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData, BerkeleyDBDelegateTreeNodeEntity> delegateTreeSubNodeEntityParentSubindex(
			BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData parentPkd) throws DatabaseException
	{
		return delegateTreeSubNodeEntityParentSecondaryIndex().subIndex(parentPkd);
	}

	public SecondaryIndex<UUIDKey, PrimaryKeyData, BerkeleyDBDelegateTreeRootNodeEntity> delegateTreeRootNodeEntitySuccessorUuidKeysSecondaryIndex()
	{
		return getSubclassIndex(delegateTreeNodeEntityPrimaryIndex(), BerkeleyDBDelegateTreeRootNodeEntity.class, UUIDKey.class,
				BerkeleyDBDelegateTreeRootNodeEntity.successorUuidKeys_FieldName);
	}

	public EntityIndex<PrimaryKeyData, BerkeleyDBDelegateTreeRootNodeEntity> delegateTreeRootNodeEntitySuccessorUuidKeysSubIndex(UUIDKey successorUuidKey)
	{
		return delegateTreeRootNodeEntitySuccessorUuidKeysSecondaryIndex().subIndex(successorUuidKey);
	}

	public PrimaryIndex<BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityPrimaryIndex()
			throws DatabaseException
	{
		return getPrimaryIndex(BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData.class, BerkeleyDBDelegateAuthorizerEntity.class);
	}

	public SecondaryIndex<BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityTreeNodeSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(delegateAuthorizerEntityPrimaryIndex(), BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData.class,
				BerkeleyDBDelegateAuthorizerEntity.delegateTreeNodePrimaryKeyData_FieldName);
	}

	public EntityIndex<BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityTreeNodeSubindex(
			BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData parentPkd) throws DatabaseException
	{
		return delegateAuthorizerEntityTreeNodeSecondaryIndex().subIndex(parentPkd);
	}

	public SecondaryIndex<BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityContextAuthorizerSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(delegateAuthorizerEntityPrimaryIndex(), BerkeleyDBDelegateAuthorizerEntity.StatementAuthorizerKeyData.class,
				BerkeleyDBDelegateAuthorizerEntity.statementAuthorizerKeyData_FieldName);
	}

	public SecondaryIndex<UUIDKey, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityAuthorizerSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(delegateAuthorizerEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBDelegateAuthorizerEntity.authorizerUuidKey_FieldName);
	}

	public SecondaryIndex<UUIDKey, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityDelegateSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(delegateAuthorizerEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBDelegateAuthorizerEntity.delegateUuidKey_FieldName);
	}

	public EntityIndex<BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData, BerkeleyDBDelegateAuthorizerEntity> delegateAuthorizerEntityDelegateSubindex(
			UUIDKey delegateUuidKey) throws DatabaseException
	{
		return delegateAuthorizerEntityDelegateSecondaryIndex().subIndex(delegateUuidKey);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBSignatureRequestEntity> signatureRequestEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBSignatureRequestEntity.class);
	}

	public SecondaryIndex<Date, UUIDKey, BerkeleyDBSignatureRequestEntity> signatureRequestEntityCreationDateSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(signatureRequestEntityPrimaryIndex(), Date.class, BerkeleyDBSignatureRequestEntity.creationDate_FieldName);
	}

	public SecondaryIndex<BerkeleyDBSignatureRequestEntity.ContextCreationDateSecondaryKeyData, UUIDKey, BerkeleyDBSignatureRequestEntity> signatureRequestEntityContextCreationDateSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBSignatureRequestEntity.ContextCreationDateSecondaryKeyData.class,
				BerkeleyDBSignatureRequestEntity.contextCreationDateSecondaryKeyData_FieldName);
	}

	public SecondaryIndex<ContextSubContextSecondaryKeyData, UUIDKey, BerkeleyDBSignatureRequestEntity> signatureRequestEntityContextSubContextSecondaryIndex()
	{
		return getSecondaryIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBSignatureRequestEntity.ContextSubContextSecondaryKeyData.class,
				BerkeleyDBSignatureRequestEntity.contextSubContextSecondaryKeyDataList_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBUnpackedSignatureRequestEntity> unpackedSignatureRequestEntityContextPathSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBUnpackedSignatureRequestEntity.class, UUIDKey.class,
				BerkeleyDBUnpackedSignatureRequestEntity.contextUuidKeyPath_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBUnpackedSignatureRequestEntity> unpackedSignatureRequestEntityContextPathSubIndex(UUIDKey contextUuidKey)
			throws DatabaseException
	{
		return unpackedSignatureRequestEntityContextPathSecondaryIndex().subIndex(contextUuidKey);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBUnpackedSignatureRequestEntity> unpackedSignatureRequestEntityStatementListSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBUnpackedSignatureRequestEntity.class, UUIDKey.class,
				BerkeleyDBUnpackedSignatureRequestEntity.statementUuidKeys_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBUnpackedSignatureRequestEntity> unpackedSignatureRequestEntityStatementListSubIndex(UUIDKey contextUuidKey)
			throws DatabaseException
	{
		return unpackedSignatureRequestEntityStatementListSecondaryIndex().subIndex(contextUuidKey);
	}

	public SecondaryIndex<BerkeleyDBPackedSignatureRequestEntity.ContextPackingDateSecondaryKeyData, UUIDKey, BerkeleyDBPackedSignatureRequestEntity> packedSignatureRequestEntityContextPackingDateSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBPackedSignatureRequestEntity.class,
				BerkeleyDBPackedSignatureRequestEntity.ContextPackingDateSecondaryKeyData.class,
				BerkeleyDBPackedSignatureRequestEntity.contextPackingDateSecondaryKeyDataList_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBPackedSignatureRequestEntity> packedSignatureRequestEntityRootContextSignatureUuidSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBPackedSignatureRequestEntity.class, UUIDKey.class,
				BerkeleyDBPackedSignatureRequestEntity.rootContextSignatureUuidKey_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBPackedSignatureRequestEntity> packedSignatureRequestEntityContextPathSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBPackedSignatureRequestEntity.class, UUIDKey.class,
				BerkeleyDBPackedSignatureRequestEntity.contextUuidKeyPath_FieldName);
	}

	public EntityIndex<UUIDKey, BerkeleyDBPackedSignatureRequestEntity> packedSignatureRequestEntityContextPathSubIndex(UUIDKey contextUuidKey)
			throws DatabaseException
	{
		return packedSignatureRequestEntityContextPathSecondaryIndex().subIndex(contextUuidKey);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBPackedSignatureRequestEntity> packedSignatureRequestEntityDependencyUuidsSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(signatureRequestEntityPrimaryIndex(), BerkeleyDBPackedSignatureRequestEntity.class, UUIDKey.class,
				BerkeleyDBPackedSignatureRequestEntity.dependencyUuidKeys_FieldName);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBStatementLocalEntity> statementLocalEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBStatementLocalEntity.class);
	}

	public SecondaryIndex<BerkeleyDBStatementLocalEntity.ContextSubscribeProofSecondaryKeyData, UUIDKey, BerkeleyDBStatementLocalEntity> statementLocalEntitySubscribeProofSecondaryIndex()
			throws DatabaseException
	{
		return getSecondaryIndex(statementLocalEntityPrimaryIndex(), BerkeleyDBStatementLocalEntity.ContextSubscribeProofSecondaryKeyData.class,
				BerkeleyDBStatementLocalEntity.contextSubscribeProofSecondaryKeyData_FieldName);
	}

	public SecondaryIndex<BerkeleyDBContextLocalEntity.ContextSubscribeStatementsSecondaryKeyData, UUIDKey, BerkeleyDBContextLocalEntity> contextLocalEntitySubscribeStatementSecondaryIndex()
			throws DatabaseException
	{
		return getSubclassIndex(statementLocalEntityPrimaryIndex(), BerkeleyDBContextLocalEntity.class,
				BerkeleyDBContextLocalEntity.ContextSubscribeStatementsSecondaryKeyData.class,
				BerkeleyDBContextLocalEntity.contextSubscribeStatementsSecondaryKeyData_FieldName);
	}

	public SecondaryIndex<UUIDKey, UUIDKey, BerkeleyDBStatementLocalEntity> statementLocalEntityContextSecondaryIndex() throws DatabaseException
	{
		return getSecondaryIndex(statementLocalEntityPrimaryIndex(), UUIDKey.class, BerkeleyDBStatementLocalEntity.contextUuidKey_FieldName);
	}

	public PrimaryIndex<UUIDKey, BerkeleyDBHookEntity> hookEntityPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(UUIDKey.class, BerkeleyDBHookEntity.class);
	}

	public SecondaryIndex<Long, UUIDKey, BerkeleyDBHookEntity> hookEntityPrioritySecondaryIndex()
	{
		return getSecondaryIndex(hookEntityPrimaryIndex(), Long.class, BerkeleyDBHookEntity.priority_FieldName);
	}

	public PrimaryIndex<Boolean, BerkeleyDBPersistenceSecretKeySingletonEntity> persistenceSecretKeySingletonPrimaryIndex() throws DatabaseException
	{
		return getPrimaryIndex(Boolean.class, BerkeleyDBPersistenceSecretKeySingletonEntity.class);
	}

}
