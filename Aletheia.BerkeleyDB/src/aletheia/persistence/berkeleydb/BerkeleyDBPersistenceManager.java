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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.Person;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Namespace;
import aletheia.model.local.ContextLocal;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.SimpleTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBDelegateAuthorizerByAuthorizerMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBDelegateAuthorizerSetByDelegate;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBDelegateTreeRootNodeSetBySuccessor;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBLocalDelegateAuthorizerByAuthorizerMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBLocalDelegateAuthorizerMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBLocalDelegateTreeSubNodeMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBLocalStatementAuthoritySet;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPackedSignatureRequestContextPackingDateCollection;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPackedSignatureRequestSetByContextPath;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPersonsByNick;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPersonsMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPersonsOrphanSinceSortedSet;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPrivatePersonsByNick;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPrivatePersonsMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBPrivateSignatoriesMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBRootContextAuthorityBySignatureUuid;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBSignatoriesMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBSignatureRequestContextCreationDateCollection;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBSignatureRequestContextSubContextUuidsCollection;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBSignatureRequestMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBSignedDependenciesLocalStatementAuthoritySet;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBSignedProofLocalStatementAuthoritySet;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBStatementAuthoritySet;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBStatementAuthoritySetByAuthor;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBStatementAuthoritySignatureDateSortedSet;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBStatementAuthoritySignatureMap;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBStatementAuthoritySignatureSetByAuthorizer;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBStatementAuthoritySignatureSetByAuthorizerAndSignatureUuid;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBUnpackedSignatureRequestSetByContextPath;
import aletheia.persistence.berkeleydb.collections.authority.BerkeleyDBUnpackedSignatureRequestSetByStatementAuthority;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBStatementLocalSet;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBStatementLocalSetMap;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBSubscribeProofRootContextLocalSet;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBSubscribeProofStatementLocalSet;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBSubscribeProofStatementLocalSetMap;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBSubscribeStatementsContextLocalSet;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBSubscribeStatementsContextLocalSetMap;
import aletheia.persistence.berkeleydb.collections.local.BerkeleyDBSubscribeStatementsRootContextLocalSet;
import aletheia.persistence.berkeleydb.collections.peertopeer.BerkeleyDBHookList;
import aletheia.persistence.berkeleydb.collections.peertopeer.BerkeleyDBNodeDeferredMessagesByNodeMap;
import aletheia.persistence.berkeleydb.collections.peertopeer.BerkeleyDBNodeDeferredMessagesByRecipientCollection;
import aletheia.persistence.berkeleydb.collections.peertopeer.BerkeleyDBNodeDeferredMessagesMap;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBAssumptionList;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBContextLocalIdentifierToStatement;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBContextLocalStatementToIdentifier;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBDependentsSet;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBDescendantContextsByConsequent;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBIdentifierToRootContexts;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBLocalSortedStatements;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBLocalStatementsByTerm;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBLocalStatementsMap;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBRootContextToIdentifier;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBRootContextsMap;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBSortedRootContexts;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBStatementsMap;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBSubContextsSet;
import aletheia.persistence.berkeleydb.collections.statement.BerkeleyDBUnfoldingContextsByDeclaration;
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
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBRootContextAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatoryEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthorityEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBStatementAuthoritySignatureEntity;
import aletheia.persistence.berkeleydb.entities.authority.BerkeleyDBUnpackedSignatureRequestEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBContextLocalEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBRootContextLocalEntity;
import aletheia.persistence.berkeleydb.entities.local.BerkeleyDBStatementLocalEntity;
import aletheia.persistence.berkeleydb.entities.misc.BerkeleyDBPersistenceSecretKeySingletonEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBHookEntity;
import aletheia.persistence.berkeleydb.entities.peertopeer.BerkeleyDBNodeDeferredMessageEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBAssumptionEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBDeclarationEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBRootContextEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBSpecializationEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBStatementEntity;
import aletheia.persistence.berkeleydb.entities.statement.BerkeleyDBUnfoldingContextEntity;
import aletheia.persistence.berkeleydb.exceptions.BerkeleyDBPersistenceException;
import aletheia.persistence.berkeleydb.exceptions.BerkeleyDBPersistenceLockTimeoutException;
import aletheia.persistence.berkeleydb.lowlevelbackuprestore.LowLevelBackupRestoreEntityStore;
import aletheia.persistence.berkeleydb.upgrade.EntityStoreUpgrade.UpgradeException;
import aletheia.persistence.collections.authority.DelegateAuthorizerByAuthorizerMap;
import aletheia.persistence.collections.authority.DelegateAuthorizerSetByDelegate;
import aletheia.persistence.collections.authority.DelegateTreeRootNodeSetBySuccessor;
import aletheia.persistence.collections.authority.LocalDelegateAuthorizerByAuthorizerMap;
import aletheia.persistence.collections.authority.LocalDelegateAuthorizerMap;
import aletheia.persistence.collections.authority.LocalDelegateTreeSubNodeMap;
import aletheia.persistence.collections.authority.LocalStatementAuthoritySet;
import aletheia.persistence.collections.authority.PackedSignatureRequestContextPackingDateCollection;
import aletheia.persistence.collections.authority.PackedSignatureRequestSetByContextPath;
import aletheia.persistence.collections.authority.PersonsByNick;
import aletheia.persistence.collections.authority.PersonsOrphanSinceSortedSet;
import aletheia.persistence.collections.authority.PrivatePersonsByNick;
import aletheia.persistence.collections.authority.PrivatePersonsMap;
import aletheia.persistence.collections.authority.PrivateSignatoriesMap;
import aletheia.persistence.collections.authority.RootContextAuthorityBySignatureUuid;
import aletheia.persistence.collections.authority.SignatoriesMap;
import aletheia.persistence.collections.authority.SignatureRequestContextCreationDateCollection;
import aletheia.persistence.collections.authority.SignatureRequestContextSubContextUuidsCollection;
import aletheia.persistence.collections.authority.SignatureRequestMap;
import aletheia.persistence.collections.authority.SignedDependenciesLocalStatementAuthoritySet;
import aletheia.persistence.collections.authority.SignedProofLocalStatementAuthoritySet;
import aletheia.persistence.collections.authority.StatementAuthoritySet;
import aletheia.persistence.collections.authority.StatementAuthoritySetByAuthor;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureDateSortedSet;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSetByAuthorizer;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid;
import aletheia.persistence.collections.authority.UnpackedSignatureRequestSetByContextPath;
import aletheia.persistence.collections.authority.UnpackedSignatureRequestSetByStatementAuthority;
import aletheia.persistence.collections.local.StatementLocalSet;
import aletheia.persistence.collections.local.StatementLocalSetMap;
import aletheia.persistence.collections.local.SubscribeProofRootContextLocalSet;
import aletheia.persistence.collections.local.SubscribeProofStatementLocalSet;
import aletheia.persistence.collections.local.SubscribeProofStatementLocalSetMap;
import aletheia.persistence.collections.local.SubscribeStatementsContextLocalSet;
import aletheia.persistence.collections.local.SubscribeStatementsContextLocalSetMap;
import aletheia.persistence.collections.local.SubscribeStatementsRootContextLocalSet;
import aletheia.persistence.collections.peertopeer.HookList;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesByNodeMap;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesByRecipientCollection;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesMap;
import aletheia.persistence.collections.statement.RootContextToIdentifier;
import aletheia.persistence.entities.authority.DelegateAuthorizerEntity;
import aletheia.persistence.entities.authority.DelegateTreeNodeEntity;
import aletheia.persistence.entities.authority.DelegateTreeRootNodeEntity;
import aletheia.persistence.entities.authority.DelegateTreeSubNodeEntity;
import aletheia.persistence.entities.authority.PersonEntity;
import aletheia.persistence.entities.authority.RootContextAuthorityEntity;
import aletheia.persistence.entities.authority.SignatoryEntity;
import aletheia.persistence.entities.authority.SignatureRequestEntity;
import aletheia.persistence.entities.authority.StatementAuthorityEntity;
import aletheia.persistence.entities.authority.StatementAuthoritySignatureEntity;
import aletheia.persistence.entities.local.StatementLocalEntity;
import aletheia.persistence.entities.misc.PersistenceSecretKeySingletonEntity;
import aletheia.persistence.entities.peertopeer.DeferredMessageEntity;
import aletheia.persistence.entities.peertopeer.HookEntity;
import aletheia.persistence.entities.peertopeer.NodeDeferredMessageEntity;
import aletheia.persistence.entities.statement.StatementEntity;
import aletheia.persistence.exceptions.PersistenceDeleteConstraintException;
import aletheia.persistence.exceptions.PersistenceException;
import aletheia.persistence.exceptions.PersistenceUniqueConstraintException;
import aletheia.protocol.ProtocolException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DeleteConstraintException;
import com.sleepycat.je.EnvironmentNotFoundException;
import com.sleepycat.je.LockTimeoutException;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.UniqueConstraintException;

/**
 * Implementation of the {@link PersistenceManager} that makes use of the
 * <a href =
 * "http://www.oracle.com/technetwork/products/berkeleydb/overview/index.html" >
 * Berkeley DB</a> Software (Java edition).
 *
 */
public class BerkeleyDBPersistenceManager extends PersistenceManager
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final static String storeName = "aletheia";
	private final static String temporaryStoreName = "aletheiaTemp";

	public static class Configuration extends PersistenceManager.Configuration
	{
		private final static boolean defaultAllowCreate = false;
		private final static boolean defaultReadOnly = true;
		private final static boolean defaultAllowUpgrade = false;
		private final static int defaultCachePercent = 0;

		private File dbFile;
		private boolean allowCreate;
		private boolean readOnly;
		private boolean allowUpgrade;
		private int cachePercent;

		public Configuration()
		{
			super();
			this.allowCreate = defaultAllowCreate;
			this.readOnly = defaultReadOnly;
			this.allowUpgrade = defaultAllowUpgrade;
			this.cachePercent = defaultCachePercent;
		}

		public File getDbFile()
		{
			return dbFile;
		}

		public void setDbFile(File dbFile)
		{
			this.dbFile = dbFile;
		}

		public boolean isAllowCreate()
		{
			return allowCreate;
		}

		public void setAllowCreate(boolean allowCreate)
		{
			this.allowCreate = allowCreate;
		}

		public boolean isReadOnly()
		{
			return readOnly;
		}

		public void setReadOnly(boolean readOnly)
		{
			this.readOnly = readOnly;
		}

		public boolean isAllowUpgrade()
		{
			return allowUpgrade;
		}

		public void setAllowUpgrade(boolean allowUpgrade)
		{
			this.allowUpgrade = allowUpgrade;
		}

		public int getCachePercent()
		{
			return cachePercent;
		}

		public void setCachePercent(int cachePercent)
		{
			this.cachePercent = cachePercent;
		}

		@Override
		public String toString()
		{
			return super.toString() + "[dbFile=" + dbFile + ", allowCreate=" + allowCreate + ", readOnly=" + readOnly + ", allowUpgrade=" + allowUpgrade
					+ ", cachePercent=" + cachePercent + "]";
		}

	}

	public static class RegisterException extends PersistenceException
	{
		private static final long serialVersionUID = -5757166696145694126L;

		protected RegisterException()
		{
			super();
		}

		protected RegisterException(String message)
		{
			super(message);
		}

		protected RegisterException(Throwable cause)
		{
			super(cause);
		}
	}

	public static class ExistingActiveManagerRegistered extends RegisterException
	{
		private static final long serialVersionUID = 706609053105874527L;

		protected ExistingActiveManagerRegistered(String path)
		{
			super("Existing active Berkeley DB persistence manager registered on path: " + path);
		}
	}

	private final static class Registry
	{
		private final Set<String> pathSet = new HashSet<>();

		private synchronized void register(File dbFile) throws RegisterException
		{
			try
			{
				String path = dbFile.getCanonicalPath();
				if (!pathSet.add(path))
					throw new ExistingActiveManagerRegistered(path);
			}
			catch (IOException e)
			{
				throw new RegisterException(e);
			}
		}

		private synchronized void unregister(File dbFile)
		{
			try
			{
				pathSet.remove(dbFile.getCanonicalPath());
			}
			catch (IOException e)
			{
				throw new RegisterException(e);
			}
		}
	}

	private final static Registry registry = new Registry();

	private final BerkeleyDBAletheiaEnvironment environment;
	private final BerkeleyDBAletheiaEntityStore entityStore;
	private final BerkeleyDBAletheiaTemporaryEntityStore temporaryEntityStore;
	private final Map<Long, BerkeleyDBTransaction> openedTransactionsMap;
	private final TransactionConfig defaultTransactionConfig;
	private final TransactionConfig dirtyTransactionConfig;

	public static class MustAllowCreateException extends PersistenceException
	{
		private static final long serialVersionUID = -5412888039106717133L;

		public MustAllowCreateException(EnvironmentNotFoundException e)
		{
			super(e);
		}
	}

	public static class EntityStoreVersionException extends PersistenceException
	{
		private static final long serialVersionUID = 1775467149862553798L;

		private final int storeVersion;
		private final int codeStoreVersion;

		protected EntityStoreVersionException(Exception exception, int storeVersion, int codeStoreVersion)
		{
			super(exception);
			this.storeVersion = storeVersion;
			this.codeStoreVersion = codeStoreVersion;
		}

		protected EntityStoreVersionException(String message, int storeVersion, int codeStoreVersion)
		{
			super(message);
			this.storeVersion = storeVersion;
			this.codeStoreVersion = codeStoreVersion;
		}

		public int getStoreVersion()
		{
			return storeVersion;
		}

		public int getCodeStoreVersion()
		{
			return codeStoreVersion;
		}
	}

	public static class UnsupportedEntityStoreVersionException extends EntityStoreVersionException
	{
		private static final long serialVersionUID = -3862615279692540213L;

		protected UnsupportedEntityStoreVersionException(int storeVersion, int codeStoreVersion)
		{
			super("Database entity store version " + storeVersion + " not supported", storeVersion, codeStoreVersion);
		}

	}

	private static class InitializationData
	{
		private final BerkeleyDBAletheiaEnvironment environment;
		private final BerkeleyDBAletheiaEntityStore entityStore;
		private final BerkeleyDBAletheiaTemporaryEntityStore temporaryEntityStore;

		private InitializationData(BerkeleyDBAletheiaEnvironment environment, BerkeleyDBAletheiaEntityStore entityStore,
				BerkeleyDBAletheiaTemporaryEntityStore temporaryEntityStore)
		{
			super();
			this.environment = environment;
			this.entityStore = entityStore;
			this.temporaryEntityStore = temporaryEntityStore;
		}

		private InitializationData(Configuration configuration)
		{
			registry.register(configuration.getDbFile());
			logger.info("Berkeley DB Persistence Manager starting (" + configuration + ")");
			configuration.getStartupProgressListener().updateProgress(0);
			try
			{
				try
				{
					this.environment = new BerkeleyDBAletheiaEnvironment(configuration);
				}
				catch (Exception e)
				{
					registry.unregister(configuration.getDbFile());
					throw e;
				}

			}
			catch (EnvironmentNotFoundException e)
			{
				throw new MustAllowCreateException(e);
			}
			try
			{
				this.entityStore = BerkeleyDBAletheiaEntityStore.open(environment, storeName, configuration.isAllowUpgrade());
				if (!configuration.isReadOnly())
				{
					try
					{
						this.temporaryEntityStore = BerkeleyDBAletheiaTemporaryEntityStore.open(environment, temporaryStoreName);
					}
					catch (Exception e)
					{
						this.entityStore.close();
						throw e;
					}
				}
				else
					this.temporaryEntityStore = null;
			}
			catch (Exception e)
			{
				this.environment.close();
				registry.unregister(configuration.getDbFile());
				throw e;
			}

		}
	}

	private BerkeleyDBPersistenceManager(PersistenceManager.Configuration configuration, InitializationData initializationData)
	{
		super(configuration);
		this.environment = initializationData.environment;
		this.entityStore = initializationData.entityStore;
		this.temporaryEntityStore = initializationData.temporaryEntityStore;

		this.openedTransactionsMap = Collections.synchronizedMap(new HashMap<Long, BerkeleyDBTransaction>());
		this.defaultTransactionConfig = new TransactionConfig();
		this.defaultTransactionConfig.setReadCommitted(true);
		this.dirtyTransactionConfig = new TransactionConfig();
		this.dirtyTransactionConfig.setReadUncommitted(true);

		getPersistenceSchedulerThread().start();

		logger.info("Berkeley DB Persistence Manager started (storeVersion: " + entityStore.storeVersion() + ")");
		configuration.getStartupProgressListener().updateProgress(1);
	}

	protected BerkeleyDBPersistenceManager(BerkeleyDBAletheiaEnvironment environment, BerkeleyDBAletheiaEntityStore entityStore,
			BerkeleyDBAletheiaTemporaryEntityStore temporaryEntityStore)
	{
		this(new PersistenceManager.Configuration()
		{
		}, new InitializationData(environment, entityStore, temporaryEntityStore));
	}

	protected BerkeleyDBPersistenceManager(BerkeleyDBAletheiaEnvironment environment, BerkeleyDBAletheiaEntityStore entityStore)
	{
		this(environment, entityStore, null);
	}

	public BerkeleyDBPersistenceManager(Configuration configuration) throws MustAllowCreateException, UpgradeException
	{
		this(configuration, new InitializationData(configuration));
	}

	public File getDbFile()
	{
		return environment.getHome();
	}

	public boolean isReadOnly()
	{
		return environment.getConfig().getReadOnly();
	}

	public BerkeleyDBAletheiaEnvironment getEnvironment()
	{
		return environment;
	}

	public BerkeleyDBAletheiaEntityStore getEntityStore()
	{
		return entityStore;
	}

	public class NoTemporaryEntityStorePersistenceException extends PersistenceException
	{
		private static final long serialVersionUID = 7208313253028782190L;

		private NoTemporaryEntityStorePersistenceException()
		{
			super("No temporary entity store");
		}
	}

	public BerkeleyDBAletheiaTemporaryEntityStore getTemporaryEntityStore()
	{
		if (temporaryEntityStore == null)
			throw new NoTemporaryEntityStorePersistenceException();
		return temporaryEntityStore;
	}

	public static void lowLevelBackup(File backupFile, File dbFile) throws IOException
	{
		Configuration configuration = new Configuration();
		configuration.setDbFile(dbFile);
		configuration.setAllowCreate(false);
		configuration.setReadOnly(true);
		configuration.setAllowUpgrade(false);
		BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
		try
		{
			DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(backupFile)));
			try
			{
				new LowLevelBackupRestoreEntityStore(environment).backup(out, storeName);
			}
			finally
			{
				out.close();
			}
		}
		finally
		{
			environment.close();
		}
	}

	public static void lowLevelRestore(File backupFile, File dbFile) throws IOException, ProtocolException
	{
		Configuration configuration = new Configuration();
		configuration.setDbFile(dbFile);
		configuration.setAllowCreate(true);
		configuration.setReadOnly(false);
		configuration.setAllowUpgrade(false);
		BerkeleyDBAletheiaEnvironment environment = new BerkeleyDBAletheiaEnvironment(configuration);
		try
		{
			DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(backupFile)));
			try
			{
				new LowLevelBackupRestoreEntityStore(environment).restore(in, storeName);
			}
			finally
			{
				in.close();
			}
		}
		finally
		{
			environment.close();

		}
	}

	public static void lowLevelCopy(File srcDbFile, File dstDbFile) throws ProtocolException
	{
		Configuration srcConfiguration = new Configuration();
		srcConfiguration.setDbFile(srcDbFile);
		srcConfiguration.setAllowCreate(false);
		srcConfiguration.setReadOnly(true);
		srcConfiguration.setAllowUpgrade(false);
		final BerkeleyDBAletheiaEnvironment srcEnvironment = new BerkeleyDBAletheiaEnvironment(srcConfiguration);
		try
		{
			PipedOutputStream pos = new PipedOutputStream();
			final DataOutputStream dos = new DataOutputStream(pos);
			Thread srcThread = new Thread()
			{

				@Override
				public void run()
				{
					try
					{
						new LowLevelBackupRestoreEntityStore(srcEnvironment).backup(dos, storeName);
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}

			};

			Configuration dstConfiguration = new Configuration();
			dstConfiguration.setDbFile(dstDbFile);
			dstConfiguration.setAllowCreate(true);
			dstConfiguration.setReadOnly(false);
			dstConfiguration.setAllowUpgrade(false);
			BerkeleyDBAletheiaEnvironment dstEnvironment = new BerkeleyDBAletheiaEnvironment(dstConfiguration);
			try
			{
				PipedInputStream pis = new PipedInputStream(pos);
				srcThread.start();
				final DataInputStream dis = new DataInputStream(pis);
				try
				{
					new LowLevelBackupRestoreEntityStore(dstEnvironment).restore(dis, storeName);
				}
				finally
				{
					dis.close();
					srcThread.join();
					dos.close();
				}
			}
			catch (IOException | InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				dstEnvironment.close();
			}
		}
		finally
		{
			srcEnvironment.close();
		}

	}

	/**
	 * Registers a transaction as open in the system.
	 *
	 * @param transaction
	 *            The transaction.
	 */
	protected void registerTransaction(BerkeleyDBTransaction transaction)
	{
		openedTransactionsMap.put(transaction.getDbTransactionId(), transaction);
	}

	/**
	 * Unregisters a transaction as open in the system
	 *
	 * @param transaction
	 *            The transaction.
	 */
	protected void unregisterTransaction(BerkeleyDBTransaction transaction)
	{
		openedTransactionsMap.remove(transaction.getDbTransactionId());
	}

	public Map<Long, BerkeleyDBTransaction> openedTransactionsMap()
	{
		return Collections.unmodifiableMap(openedTransactionsMap);
	}

	public PersistenceException convertDatabaseException(DatabaseException databaseException)
	{
		if (databaseException instanceof LockTimeoutException)
			return new BerkeleyDBPersistenceLockTimeoutException(this, (LockTimeoutException) databaseException);
		else if (databaseException instanceof DeleteConstraintException)
			return new PersistenceDeleteConstraintException(databaseException);
		else if (databaseException instanceof UniqueConstraintException)
			return new PersistenceUniqueConstraintException(databaseException);
		else
			return new BerkeleyDBPersistenceException(databaseException);
	}

	private StatementEntity getStatementEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().statementEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public StatementEntity getStatementEntity(Transaction transaction, UUID uuid)
	{
		return getStatementEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	@Override
	public void putStatementEntity(Transaction transaction, StatementEntity entity)
	{
		putStatementEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBStatementEntity) entity);
	}

	private void putStatementEntity(BerkeleyDBTransaction transaction, BerkeleyDBStatementEntity entity)
	{
		transaction.putNoReturn(getEntityStore().statementEntityPrimaryIndex(), entity);
	}

	@Override
	public void deleteStatementEntity(Transaction transaction, UUID uuid)
	{
		deleteStatementEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteStatementEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().statementEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public boolean lockStatementEntity(Transaction transaction, UUID uuid)
	{
		return lockStatementEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private boolean lockStatementEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.lock(getEntityStore().statementEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public BerkeleyDBDependentsSet dependents(Transaction transaction, Statement statement)
	{
		return new BerkeleyDBDependentsSet(this, (BerkeleyDBTransaction) transaction, statement);
	}

	@Override
	public BerkeleyDBLocalStatementsMap localStatements(Transaction transaction, Context context)
	{
		return new BerkeleyDBLocalStatementsMap(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public BerkeleyDBStatementsMap statements(Transaction transaction)
	{
		return new BerkeleyDBStatementsMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public BerkeleyDBLocalStatementsByTerm localStatementsByTerm(Transaction transaction, Context context)
	{
		return new BerkeleyDBLocalStatementsByTerm(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public BerkeleyDBAssumptionList assumptionList(Transaction transaction, Context context)
	{
		return new BerkeleyDBAssumptionList(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public BerkeleyDBSubContextsSet subContexts(Transaction transaction, Context context)
	{
		return new BerkeleyDBSubContextsSet(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public BerkeleyDBContextLocalStatementToIdentifier contextLocalStatementToIdentifier(Transaction transaction, Context context)
	{
		return new BerkeleyDBContextLocalStatementToIdentifier(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public RootContextToIdentifier rootContextToIdentifier(Transaction transaction)
	{
		return new BerkeleyDBRootContextToIdentifier(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public BerkeleyDBContextLocalIdentifierToStatement contextLocalIdentifierToStatement(Transaction transaction, Context context)
	{
		return new BerkeleyDBContextLocalIdentifierToStatement(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public BerkeleyDBIdentifierToRootContexts identifierToRootContexts(Transaction transaction)
	{
		return new BerkeleyDBIdentifierToRootContexts(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public BerkeleyDBDescendantContextsByConsequent descendantContextsByConsequent(Transaction transaction, Context context, SimpleTerm consequent)
	{
		return new BerkeleyDBDescendantContextsByConsequent(this, (BerkeleyDBTransaction) transaction, context, consequent);
	}

	@Override
	public BerkeleyDBUnfoldingContextsByDeclaration unfoldingContextsByDeclaration(Transaction transaction, Declaration declaration)
	{
		return new BerkeleyDBUnfoldingContextsByDeclaration(this, (BerkeleyDBTransaction) transaction, declaration);
	}

	@Override
	public BerkeleyDBLocalSortedStatements localSortedStatements(Transaction transaction, Context context)
	{
		return new BerkeleyDBLocalSortedStatements(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public void close()
	{
		try
		{
			super.close();
			for (Transaction t : openedTransactionsMap.values().toArray(new Transaction[0]))
			{
				logger.warn("Opened transaction:" + t);
				if (t.getStackTraceList() != null)
					for (StackTraceElement ste : t.getStackTraceList())
						logger.debug("\tat " + ste.getClassName() + "." + ste.getMethodName() + "(" + ste.getFileName() + ":" + ste.getLineNumber() + ")");
				t.abort();
			}
			entityStore.close();
			if (temporaryEntityStore != null)
				temporaryEntityStore.close();
		}
		catch (DatabaseException e)
		{
			throw convertDatabaseException(e);
		}
		finally
		{
			File dbFile = getDbFile();
			environment.close();
			logger.info("Berkeley DB Persistence Manager closed");
			registry.unregister(dbFile);
		}
	}

	@Override
	public BerkeleyDBRootContextsMap rootContexts(Transaction transaction)
	{
		return new BerkeleyDBRootContextsMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public BerkeleyDBSortedRootContexts sortedRootContexts(Transaction transaction)
	{
		return new BerkeleyDBSortedRootContexts(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public void sync()
	{
		if (!isReadOnly())
		{
			try
			{
				environment.sync();
				logger.trace("Sync");
			}
			catch (DatabaseException e)
			{
				throw convertDatabaseException(e);
			}
		}
	}

	public void evictMemory()
	{
		try
		{
			environment.evictMemory();
		}
		catch (DatabaseException e)
		{
			throw convertDatabaseException(e);
		}
	}

	public void compress()
	{
		try
		{
			environment.compress();
		}
		catch (DatabaseException e)
		{
			throw convertDatabaseException(e);
		}
	}

	@Override
	public BerkeleyDBTransaction beginTransaction()
	{
		return new BerkeleyDBTransaction(this, defaultTransactionConfig);
	}

	@Override
	public BerkeleyDBTransaction beginDirtyTransaction()
	{
		return new BerkeleyDBTransaction(this, dirtyTransactionConfig);
	}

	@Override
	public BerkeleyDBTransaction beginTransaction(long timeOut)
	{
		return new BerkeleyDBTransaction(this, defaultTransactionConfig, timeOut);
	}

	@Override
	public BerkeleyDBTransaction beginDirtyTransaction(long timeOut)
	{
		return new BerkeleyDBTransaction(this, dirtyTransactionConfig, timeOut);
	}

	@Override
	public StatementEntity instantiateStatementEntity(Class<? extends StatementEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(BerkeleyDBAssumptionEntity.class))
			return new BerkeleyDBAssumptionEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBSpecializationEntity.class))
			return new BerkeleyDBSpecializationEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBContextEntity.class))
			return new BerkeleyDBContextEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBRootContextEntity.class))
			return new BerkeleyDBRootContextEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBUnfoldingContextEntity.class))
			return new BerkeleyDBUnfoldingContextEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBDeclarationEntity.class))
			return new BerkeleyDBDeclarationEntity();
		else
			throw new Error();
	}

	@Override
	public SignatoryEntity instantiateSignatoryEntity(Class<? extends SignatoryEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(BerkeleyDBSignatoryEntity.class))
			return new BerkeleyDBSignatoryEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBPlainPrivateSignatoryEntity.class))
			return new BerkeleyDBPlainPrivateSignatoryEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBEncryptedPrivateSignatoryEntity.class))
			return new BerkeleyDBEncryptedPrivateSignatoryEntity();
		else
			throw new Error();
	}

	@Override
	public SignatoryEntity getSignatoryEntity(Transaction transaction, UUID uuid)
	{
		return getSignatoryEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private SignatoryEntity getSignatoryEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().signatoryEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public void putSignatoryEntity(Transaction transaction, SignatoryEntity entity)
	{
		putSignatoryEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBSignatoryEntity) entity);
	}

	private void putSignatoryEntity(BerkeleyDBTransaction transaction, BerkeleyDBSignatoryEntity entity)
	{
		transaction.putNoReturn(getEntityStore().signatoryEntityPrimaryIndex(), entity);
	}

	@Override
	public void deleteSignatoryEntity(Transaction transaction, UUID uuid)
	{
		deleteSignatoryEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteSignatoryEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().signatoryEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public SignatoriesMap signatories(Transaction transaction)
	{
		return new BerkeleyDBSignatoriesMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PrivateSignatoriesMap privateSignatories(Transaction transaction)
	{
		return new BerkeleyDBPrivateSignatoriesMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PersonEntity instantiatePersonEntity(Class<? extends PersonEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(BerkeleyDBPersonEntity.class))
			return new BerkeleyDBPersonEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBPrivatePersonEntity.class))
			return new BerkeleyDBPrivatePersonEntity();
		else
			throw new Error();
	}

	@Override
	public void putPersonEntity(Transaction transaction, PersonEntity entity)
	{
		putPersonEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBPersonEntity) entity);
	}

	private void putPersonEntity(BerkeleyDBTransaction transaction, BerkeleyDBPersonEntity entity)
	{
		transaction.putNoReturn(getEntityStore().personEntityPrimaryIndex(), entity);
	}

	@Override
	public BerkeleyDBPersonsMap persons(Transaction transaction)
	{
		return new BerkeleyDBPersonsMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PersonsByNick personsByNick(Transaction transaction)
	{
		return new BerkeleyDBPersonsByNick(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PersonsOrphanSinceSortedSet personsOrphanSinceSortedSet(Transaction transaction)
	{
		return new BerkeleyDBPersonsOrphanSinceSortedSet(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PrivatePersonsMap privatePersons(Transaction transaction)
	{
		return new BerkeleyDBPrivatePersonsMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PrivatePersonsByNick privatePersonsByNick(Transaction transaction)
	{
		return new BerkeleyDBPrivatePersonsByNick(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public void deletePersonEntity(Transaction transaction, UUID uuid)
	{
		deletePersonEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deletePersonEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().personEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public BerkeleyDBPersonEntity getPersonEntity(Transaction transaction, UUID uuid)
	{
		return getPersonEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private BerkeleyDBPersonEntity getPersonEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().personEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public StatementAuthorityEntity instantiateStatementAuthorityEntity(Class<? extends StatementAuthorityEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(BerkeleyDBStatementAuthorityEntity.class))
			return new BerkeleyDBStatementAuthorityEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBContextAuthorityEntity.class))
			return new BerkeleyDBContextAuthorityEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBRootContextAuthorityEntity.class))
			return new BerkeleyDBRootContextAuthorityEntity();
		else
			throw new Error();
	}

	@Override
	public void putStatementAuthorityEntity(Transaction transaction, StatementAuthorityEntity entity)
	{
		putStatementAuthorityEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBStatementAuthorityEntity) entity);
	}

	public void putStatementAuthorityEntity(BerkeleyDBTransaction transaction, BerkeleyDBStatementAuthorityEntity entity)
	{
		transaction.putNoReturn(getEntityStore().statementAuthorityEntityPrimaryIndex(), entity);
	}

	@Override
	public boolean putStatementAuthorityEntityNoOverwrite(Transaction transaction, StatementAuthorityEntity entity)
	{
		return putStatementAuthorityEntityNoOverwrite((BerkeleyDBTransaction) transaction, (BerkeleyDBStatementAuthorityEntity) entity);
	}

	public boolean putStatementAuthorityEntityNoOverwrite(BerkeleyDBTransaction transaction, BerkeleyDBStatementAuthorityEntity entity)
	{
		return transaction.putNoOverwrite(getEntityStore().statementAuthorityEntityPrimaryIndex(), entity);
	}

	@Override
	public StatementAuthorityEntity getStatementAuthorityEntity(Transaction transaction, UUID uuid)
	{
		return getStatementAuthorityEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private StatementAuthorityEntity getStatementAuthorityEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().statementAuthorityEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public void deleteStatementAuthorityEntity(Transaction transaction, UUID uuid)
	{
		deleteStatementAuthorityEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteStatementAuthorityEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().statementAuthorityEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public boolean lockStatementAuthorityEntity(Transaction transaction, UUID uuid)
	{
		return lockStatementAuthorityEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private boolean lockStatementAuthorityEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.lock(getEntityStore().statementAuthorityEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public StatementAuthoritySet statementAuthoritySet(Transaction transaction)
	{
		return new BerkeleyDBStatementAuthoritySet(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public LocalStatementAuthoritySet localStatementAuthoritySet(Transaction transaction, StatementAuthority contextAuthority)
	{
		return new BerkeleyDBLocalStatementAuthoritySet(this, (BerkeleyDBTransaction) transaction, contextAuthority);
	}

	@Override
	public SignedDependenciesLocalStatementAuthoritySet signedDependenciesLocalStatementAuthoritySet(Transaction transaction, ContextAuthority contextAuthority)
	{
		return new BerkeleyDBSignedDependenciesLocalStatementAuthoritySet(this, (BerkeleyDBTransaction) transaction, contextAuthority);
	}

	@Override
	public SignedProofLocalStatementAuthoritySet signedProofLocalStatementAuthoritySet(Transaction transaction, ContextAuthority contextAuthority)
	{
		return new BerkeleyDBSignedProofLocalStatementAuthoritySet(this, (BerkeleyDBTransaction) transaction, contextAuthority);
	}

	@Override
	public StatementAuthoritySetByAuthor statementAuthoritySetByAuthor(Transaction transaction, Person author)
	{
		return new BerkeleyDBStatementAuthoritySetByAuthor(this, (BerkeleyDBTransaction) transaction, author);
	}

	@Override
	public RootContextAuthorityEntity getRootContextAuthorityEntityBySignatureUuid(Transaction transaction, UUID uuid)
	{
		return getRootContextAuthorityEntityBySignatureUuid((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private RootContextAuthorityEntity getRootContextAuthorityEntityBySignatureUuid(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().rootContextAuthorityEntitySignatureUuidSecondaryIndex(), uuidKey);
	}

	@Override
	public RootContextAuthorityBySignatureUuid rootContextAuthorityBySignatureUuid(Transaction transaction)
	{
		return new BerkeleyDBRootContextAuthorityBySignatureUuid(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public StatementAuthoritySignatureEntity instantiateStatementAuthoritySignatureEntity(Class<? extends StatementAuthoritySignatureEntity> entityClass)
	{
		return new BerkeleyDBStatementAuthoritySignatureEntity();
	}

	@Override
	public void putStatementAuthoritySignatureEntity(Transaction transaction, StatementAuthoritySignatureEntity entity)
	{
		putStatementAuthoritySignatureEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBStatementAuthoritySignatureEntity) entity);
	}

	private void putStatementAuthoritySignatureEntity(BerkeleyDBTransaction transaction, BerkeleyDBStatementAuthoritySignatureEntity entity)
	{
		transaction.putNoReturn(getEntityStore().statementAuthoritySignatureEntityPrimaryIndex(), entity);
	}

	@Override
	public StatementAuthoritySignatureEntity getStatementAuthoritySignatureEntity(Transaction transaction, UUID statementUuid, UUID authorizerUuid)
	{
		return getStatementAuthoritySignatureEntity((BerkeleyDBTransaction) transaction,
				new BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData(statementUuid, authorizerUuid));
	}

	private StatementAuthoritySignatureEntity getStatementAuthoritySignatureEntity(BerkeleyDBTransaction transaction,
			BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData primaryKeyData)
	{
		return transaction.get(getEntityStore().statementAuthoritySignatureEntityPrimaryIndex(), primaryKeyData);
	}

	@Override
	public void deleteStatementAuthoritySignatureEntity(Transaction transaction, UUID statementUuid, UUID authorizerUuid)
	{
		deleteStatementAuthoritySignatureEntity((BerkeleyDBTransaction) transaction,
				new BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData(statementUuid, authorizerUuid));
	}

	private void deleteStatementAuthoritySignatureEntity(BerkeleyDBTransaction transaction,
			BerkeleyDBStatementAuthoritySignatureEntity.PrimaryKeyData primaryKey)
	{
		transaction.delete(getEntityStore().statementAuthoritySignatureEntityPrimaryIndex(), primaryKey);
	}

	@Override
	public BerkeleyDBStatementAuthoritySignatureMap statementAuthoritySignatureMap(Transaction transaction, StatementAuthority statementAuthority)
	{
		return new BerkeleyDBStatementAuthoritySignatureMap(this, (BerkeleyDBTransaction) transaction, statementAuthority);
	}

	@Override
	public StatementAuthoritySignatureDateSortedSet statementAuthoritySignatureDateSortedSet(Transaction transaction, StatementAuthority statementAuthority)
	{
		return new BerkeleyDBStatementAuthoritySignatureDateSortedSet(this, (BerkeleyDBTransaction) transaction, statementAuthority);
	}

	@Override
	public StatementAuthoritySignatureSetByAuthorizer statementAuthoritySignatureSetByAuthorizer(Transaction transaction, Signatory authorizer)
	{
		return new BerkeleyDBStatementAuthoritySignatureSetByAuthorizer(this, (BerkeleyDBTransaction) transaction, authorizer);
	}

	@Override
	public StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid statementAuthoritySignatureSetByAuthorizerAndSignatureUuid(Transaction transaction,
			Signatory authorizer, UUID signatureUuid)
	{
		return new BerkeleyDBStatementAuthoritySignatureSetByAuthorizerAndSignatureUuid(this, (BerkeleyDBTransaction) transaction, authorizer, signatureUuid);
	}

	@Override
	public DelegateTreeNodeEntity instantiateDelegateTreeNodeEntity(Class<? extends DelegateTreeNodeEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(DelegateTreeRootNodeEntity.class))
			return new BerkeleyDBDelegateTreeRootNodeEntity();
		if (entityClass.isAssignableFrom(DelegateTreeSubNodeEntity.class))
			return new BerkeleyDBDelegateTreeSubNodeEntity();
		else
			throw new Error();

	}

	@Override
	public void putDelegateTreeNodeEntity(Transaction transaction, DelegateTreeNodeEntity entity)
	{
		putDelegateTreeNodeEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBDelegateTreeNodeEntity) entity);
	}

	private void putDelegateTreeNodeEntity(BerkeleyDBTransaction transaction, BerkeleyDBDelegateTreeNodeEntity entity)
	{
		transaction.putNoReturn(getEntityStore().delegateTreeNodeEntityPrimaryIndex(), entity);
	}

	@Override
	public DelegateTreeNodeEntity getDelegateTreeNodeEntity(Transaction transaction, UUID contextUuid, Namespace prefix)
	{
		return getDelegateTreeNodeEntity((BerkeleyDBTransaction) transaction, new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(contextUuid, prefix));
	}

	private DelegateTreeNodeEntity getDelegateTreeNodeEntity(BerkeleyDBTransaction transaction, BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData primaryKey)
	{
		return transaction.get(getEntityStore().delegateTreeNodeEntityPrimaryIndex(), primaryKey);
	}

	@Override
	public void deleteDelegateTreeNodeEntity(Transaction transaction, UUID contextUuid, Namespace prefix)
	{
		deleteDelegateTreeNodeEntity((BerkeleyDBTransaction) transaction, new BerkeleyDBDelegateTreeNodeEntity.PrimaryKeyData(contextUuid, prefix));
	}

	private void deleteDelegateTreeNodeEntity(BerkeleyDBTransaction transaction, PrimaryKeyData primaryKeyData)
	{
		transaction.delete(getEntityStore().delegateTreeNodeEntityPrimaryIndex(), primaryKeyData);
	}

	@Override
	public LocalDelegateTreeSubNodeMap localDelegateTreeSubNodeMap(Transaction transaction, DelegateTreeNode parent)
	{
		return new BerkeleyDBLocalDelegateTreeSubNodeMap(this, (BerkeleyDBTransaction) transaction, parent);
	}

	@Override
	public DelegateTreeRootNodeSetBySuccessor delegateTreeRootNodeSetBySuccessor(Transaction transaction, Person successor)
	{
		return new BerkeleyDBDelegateTreeRootNodeSetBySuccessor(this, (BerkeleyDBTransaction) transaction, successor);
	}

	@Override
	public DelegateAuthorizerEntity instantiateDelegateAuthorizerEntity(Class<? extends DelegateAuthorizerEntity> entityClass)
	{
		return new BerkeleyDBDelegateAuthorizerEntity();
	}

	@Override
	public void putDelegateAuthorizerEntity(Transaction transaction, DelegateAuthorizerEntity entity)
	{
		putDelegateAuthorizerEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBDelegateAuthorizerEntity) entity);
	}

	private void putDelegateAuthorizerEntity(BerkeleyDBTransaction transaction, BerkeleyDBDelegateAuthorizerEntity entity)
	{
		transaction.putNoReturn(getEntityStore().delegateAuthorizerEntityPrimaryIndex(), entity);
	}

	@Override
	public DelegateAuthorizerEntity getDelegateAuthorizerEntity(Transaction transaction, UUID contextUuid, Namespace prefix, UUID delegateUuid)
	{
		return getDelegateAuthorizerEntity((BerkeleyDBTransaction) transaction,
				new BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData(contextUuid, prefix, delegateUuid));
	}

	private DelegateAuthorizerEntity getDelegateAuthorizerEntity(BerkeleyDBTransaction transaction,
			BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData primaryKey)
	{
		return transaction.get(getEntityStore().delegateAuthorizerEntityPrimaryIndex(), primaryKey);
	}

	@Override
	public void deleteDelegateAuthorizerEntity(Transaction transaction, UUID contextUuid, Namespace prefix, UUID delegateUuid)
	{
		deleteDelegateAuthorizerEntity((BerkeleyDBTransaction) transaction,
				new BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData(contextUuid, prefix, delegateUuid));
	}

	private void deleteDelegateAuthorizerEntity(BerkeleyDBTransaction transaction, BerkeleyDBDelegateAuthorizerEntity.PrimaryKeyData primaryKeyData)
	{
		transaction.delete(getEntityStore().delegateAuthorizerEntityPrimaryIndex(), primaryKeyData);
	}

	@Override
	public LocalDelegateAuthorizerMap localDelegateAuthorizerMap(Transaction transaction, DelegateTreeNode delegateTreeNode)
	{
		return new BerkeleyDBLocalDelegateAuthorizerMap(this, (BerkeleyDBTransaction) transaction, delegateTreeNode);
	}

	@Override
	public LocalDelegateAuthorizerByAuthorizerMap localDelegateAuthorizerByAuthorizerMap(Transaction transaction, StatementAuthority statementAuthority,
			Namespace prefix)
	{
		return new BerkeleyDBLocalDelegateAuthorizerByAuthorizerMap(this, (BerkeleyDBTransaction) transaction, statementAuthority, prefix);
	}

	@Override
	public DelegateAuthorizerByAuthorizerMap delegateAuthorizerByAuthorizerMap(Transaction transaction)
	{
		return new BerkeleyDBDelegateAuthorizerByAuthorizerMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public DelegateAuthorizerSetByDelegate delegateAuthorizerSetByDelegate(Transaction transaction, Person delegate)
	{
		return new BerkeleyDBDelegateAuthorizerSetByDelegate(this, (BerkeleyDBTransaction) transaction, delegate);
	}

	@Override
	public SignatureRequestEntity instantiateSignatureRequestEntity(Class<? extends SignatureRequestEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(BerkeleyDBUnpackedSignatureRequestEntity.class))
			return new BerkeleyDBUnpackedSignatureRequestEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBPackedSignatureRequestEntity.class))
			return new BerkeleyDBPackedSignatureRequestEntity();
		else
			throw new Error();
	}

	private SignatureRequestEntity getSignatureRequestEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().signatureRequestEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public SignatureRequestEntity getSignatureRequestEntity(Transaction transaction, UUID uuid)
	{
		return getSignatureRequestEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	@Override
	public void putSignatureRequestEntity(Transaction transaction, SignatureRequestEntity entity)
	{
		putSignatureRequestEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBSignatureRequestEntity) entity);
	}

	private void putSignatureRequestEntity(BerkeleyDBTransaction transaction, BerkeleyDBSignatureRequestEntity entity)
	{
		transaction.putNoReturn(getEntityStore().signatureRequestEntityPrimaryIndex(), entity);
	}

	@Override
	public boolean putSignatureRequestEntityNoOverwrite(Transaction transaction, SignatureRequestEntity entity)
	{
		return putSignatureRequestEntityNoOverwrite((BerkeleyDBTransaction) transaction, (BerkeleyDBSignatureRequestEntity) entity);
	}

	private boolean putSignatureRequestEntityNoOverwrite(BerkeleyDBTransaction transaction, BerkeleyDBSignatureRequestEntity entity)
	{
		return transaction.putNoOverwrite(getEntityStore().signatureRequestEntityPrimaryIndex(), entity);
	}

	@Override
	public void deleteSignatureRequestEntity(Transaction transaction, UUID uuid)
	{
		deleteSignatureRequestEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteSignatureRequestEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().signatureRequestEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public SignatureRequestMap signatureRequestMap(Transaction transaction)
	{
		return new BerkeleyDBSignatureRequestMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public UnpackedSignatureRequestSetByContextPath unpackedSignatureRequestSetByContextPath(Transaction transaction, Context context)
	{
		return new BerkeleyDBUnpackedSignatureRequestSetByContextPath(this, (BerkeleyDBTransaction) transaction, context);
	}

	@Override
	public PackedSignatureRequestSetByContextPath packedSignatureRequestSetByContextPath(Transaction transaction, UUID contextUuid)
	{
		return new BerkeleyDBPackedSignatureRequestSetByContextPath(this, (BerkeleyDBTransaction) transaction, contextUuid);
	}

	@Override
	public PackedSignatureRequestContextPackingDateCollection packedSignatureRequestContextPackingDateCollection(Transaction transaction, UUID contextUuid)
	{
		return new BerkeleyDBPackedSignatureRequestContextPackingDateCollection(this, (BerkeleyDBTransaction) transaction, contextUuid);
	}

	@Override
	public SignatureRequestContextCreationDateCollection signatureRequestContextCreationDateCollection(Transaction transaction, UUID contextUuid)
	{
		return new BerkeleyDBSignatureRequestContextCreationDateCollection(this, (BerkeleyDBTransaction) transaction, contextUuid);
	}

	@Override
	public SignatureRequestContextSubContextUuidsCollection signatureRequestContextSubContextUuidsCollection(Transaction transaction, UUID contextUuid)
	{
		return new BerkeleyDBSignatureRequestContextSubContextUuidsCollection(this, (BerkeleyDBTransaction) transaction, contextUuid);
	}

	@Override
	public SignatureRequestContextSubContextUuidsCollection signatureRequestContextSubContextUuidsCollection(Transaction transaction)
	{
		return new BerkeleyDBSignatureRequestContextSubContextUuidsCollection(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public UnpackedSignatureRequestSetByStatementAuthority unpackedSignatureRequestSetByStatementAuthority(Transaction transaction,
			StatementAuthority statementAuthority)
	{
		return new BerkeleyDBUnpackedSignatureRequestSetByStatementAuthority(this, (BerkeleyDBTransaction) transaction, statementAuthority);
	}

	@Override
	public StatementLocalEntity instantiateStatementLocalEntity(Class<? extends StatementLocalEntity> entityClass)
	{
		if (entityClass.isAssignableFrom(BerkeleyDBStatementLocalEntity.class))
			return new BerkeleyDBStatementLocalEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBContextLocalEntity.class))
			return new BerkeleyDBContextLocalEntity();
		else if (entityClass.isAssignableFrom(BerkeleyDBRootContextLocalEntity.class))
			return new BerkeleyDBRootContextLocalEntity();
		else
			throw new Error();
	}

	@Override
	public void putStatementLocalEntity(Transaction transaction, StatementLocalEntity entity)
	{
		putStatementLocalEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBStatementLocalEntity) entity);
	}

	public void putStatementLocalEntity(BerkeleyDBTransaction transaction, BerkeleyDBStatementLocalEntity entity)
	{
		transaction.putNoReturn(getEntityStore().statementLocalEntityPrimaryIndex(), entity);
	}

	private StatementLocalEntity getStatementLocalEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().statementLocalEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public StatementLocalEntity getStatementLocalEntity(Transaction transaction, UUID uuid)
	{
		return getStatementLocalEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	@Override
	public void deleteStatementLocalEntity(Transaction transaction, UUID uuid)
	{
		deleteStatementLocalEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteStatementLocalEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().statementLocalEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public SubscribeProofStatementLocalSet subscribeProofStatementLocalSet(Transaction transaction, ContextLocal contextLocal)
	{
		return new BerkeleyDBSubscribeProofStatementLocalSet(this, (BerkeleyDBTransaction) transaction, contextLocal);
	}

	@Override
	public SubscribeStatementsContextLocalSet subscribeStatementsContextLocalSet(Transaction transaction, ContextLocal contextLocal)
	{
		return new BerkeleyDBSubscribeStatementsContextLocalSet(this, (BerkeleyDBTransaction) transaction, contextLocal);
	}

	@Override
	public SubscribeProofRootContextLocalSet subscribeProofRootContextLocalSet(Transaction transaction)
	{
		return new BerkeleyDBSubscribeProofRootContextLocalSet(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public SubscribeStatementsRootContextLocalSet subscribeStatementsRootContextLocalSet(Transaction transaction)
	{
		return new BerkeleyDBSubscribeStatementsRootContextLocalSet(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public SubscribeProofStatementLocalSetMap subscribeProofStatementLocalSetMap(Transaction transaction)
	{
		return new BerkeleyDBSubscribeProofStatementLocalSetMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public SubscribeStatementsContextLocalSetMap subscribeStatementsContextLocalSetMap(Transaction transaction)
	{
		return new BerkeleyDBSubscribeStatementsContextLocalSetMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public StatementLocalSet statementLocalSet(Transaction transaction, ContextLocal contextLocal)
	{
		return new BerkeleyDBStatementLocalSet(this, (BerkeleyDBTransaction) transaction, contextLocal);
	}

	@Override
	public StatementLocalSetMap statementLocalSetMap(Transaction transaction)
	{
		return new BerkeleyDBStatementLocalSetMap(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public DeferredMessageEntity instantiateDeferredMessageEntity(Class<? extends DeferredMessageEntity> entityClass)
	{
		return new BerkeleyDBDeferredMessageEntity();
	}

	@Override
	public BerkeleyDBDeferredMessageEntity getDeferredMessageEntity(Transaction transaction, UUID uuid)
	{
		return getDeferredMessageEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private BerkeleyDBDeferredMessageEntity getDeferredMessageEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getTemporaryEntityStore().deferredMessageEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public void putDeferredMessageEntity(Transaction transaction, DeferredMessageEntity entity)
	{
		putDeferredMessageEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBDeferredMessageEntity) entity);
	}

	private void putDeferredMessageEntity(BerkeleyDBTransaction transaction, BerkeleyDBDeferredMessageEntity entity)
	{
		transaction.putNoReturn(getTemporaryEntityStore().deferredMessageEntityPrimaryIndex(), entity);
	}

	@Override
	public void deleteDeferredMessageEntity(Transaction transaction, UUID uuid)
	{
		deleteDeferredMessageEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteDeferredMessageEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getTemporaryEntityStore().deferredMessageEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public boolean lockDeferredMessageEntity(Transaction transaction, UUID uuid)
	{
		return lockDeferredMessageEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private boolean lockDeferredMessageEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.lock(getTemporaryEntityStore().deferredMessageEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public NodeDeferredMessageEntity instantiateNodeDeferredMessageEntity(Class<? extends NodeDeferredMessageEntity> entityClass)
	{
		return new BerkeleyDBNodeDeferredMessageEntity();
	}

	@Override
	public BerkeleyDBNodeDeferredMessageEntity getNodeDeferredMessageEntity(Transaction transaction, UUID nodeUuid, UUID recipientUuid)
	{
		return getNodeDeferredMessageEntity((BerkeleyDBTransaction) transaction,
				new BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData(nodeUuid, recipientUuid));
	}

	private BerkeleyDBNodeDeferredMessageEntity getNodeDeferredMessageEntity(BerkeleyDBTransaction transaction,
			BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData primaryKeyData)
	{
		return transaction.get(getTemporaryEntityStore().nodeDeferredMessageEntityPrimaryIndex(), primaryKeyData);
	}

	@Override
	public void putNodeDeferredMessageEntity(Transaction transaction, NodeDeferredMessageEntity entity)
	{
		putNodeDeferredMessageEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBNodeDeferredMessageEntity) entity);
	}

	private void putNodeDeferredMessageEntity(BerkeleyDBTransaction transaction, BerkeleyDBNodeDeferredMessageEntity entity)
	{
		transaction.putNoReturn(getTemporaryEntityStore().nodeDeferredMessageEntityPrimaryIndex(), entity);
	}

	@Override
	public boolean putNodeDeferredMessageEntityNoOverwrite(Transaction transaction, NodeDeferredMessageEntity entity)
	{
		return putNodeDeferredMessageEntityNoOverwrite((BerkeleyDBTransaction) transaction, (BerkeleyDBNodeDeferredMessageEntity) entity);
	}

	private boolean putNodeDeferredMessageEntityNoOverwrite(BerkeleyDBTransaction transaction, BerkeleyDBNodeDeferredMessageEntity entity)
	{
		return transaction.putNoOverwrite(getTemporaryEntityStore().nodeDeferredMessageEntityPrimaryIndex(), entity);
	}

	@Override
	public void deleteNodeDeferredMessageEntity(Transaction transaction, UUID nodeUuid, UUID recipientUuid)
	{
		deleteNodeDeferredMessageEntity((BerkeleyDBTransaction) transaction, new BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData(nodeUuid, recipientUuid));
	}

	private void deleteNodeDeferredMessageEntity(BerkeleyDBTransaction transaction, BerkeleyDBNodeDeferredMessageEntity.PrimaryKeyData primaryKeyData)
	{
		transaction.delete(getTemporaryEntityStore().nodeDeferredMessageEntityPrimaryIndex(), primaryKeyData);
	}

	@Override
	public NodeDeferredMessagesMap nodeDeferredMessagesMap(Transaction transaction, DeferredMessage deferredMessage)
	{
		return new BerkeleyDBNodeDeferredMessagesMap(this, (BerkeleyDBTransaction) transaction, deferredMessage);
	}

	@Override
	public NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID nodeUuid, UUID recipientUuid)
	{
		return new BerkeleyDBNodeDeferredMessagesByRecipientCollection(this, (BerkeleyDBTransaction) transaction, nodeUuid, recipientUuid);
	}

	@Override
	public NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID nodeUuid, UUID recipientUuid,
			Date fromDate)
	{
		return new BerkeleyDBNodeDeferredMessagesByRecipientCollection(this, (BerkeleyDBTransaction) transaction, nodeUuid, recipientUuid, fromDate);
	}

	@Override
	public NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID nodeUuid, UUID recipientUuid,
			Date fromDate, Date toDate)
	{
		return new BerkeleyDBNodeDeferredMessagesByRecipientCollection(this, (BerkeleyDBTransaction) transaction, nodeUuid, recipientUuid, fromDate, toDate);
	}

	@Override
	public NodeDeferredMessagesByNodeMap nodeDeferredMessagesByNodeMap(Transaction transaction, UUID nodeUuid)
	{
		return new BerkeleyDBNodeDeferredMessagesByNodeMap(this, (BerkeleyDBTransaction) transaction, nodeUuid);
	}

	@Override
	public HookEntity instantiateHookEntity(Class<? extends HookEntity> entityClass)
	{
		return new BerkeleyDBHookEntity();
	}

	@Override
	public HookEntity getHookEntity(Transaction transaction, UUID uuid)
	{
		return getHookEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private HookEntity getHookEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		return transaction.get(getEntityStore().hookEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public void putHookEntity(Transaction transaction, HookEntity entity)
	{
		putHookEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBHookEntity) entity);
	}

	private void putHookEntity(BerkeleyDBTransaction transaction, BerkeleyDBHookEntity entity)
	{
		transaction.put(getEntityStore().hookEntityPrimaryIndex(), entity);
	}

	@Override
	public boolean putHookEntityNoOverwrite(Transaction transaction, HookEntity entity)
	{
		return putHookEntityNoOverwrite((BerkeleyDBTransaction) transaction, (BerkeleyDBHookEntity) entity);
	}

	private boolean putHookEntityNoOverwrite(BerkeleyDBTransaction transaction, BerkeleyDBHookEntity entity)
	{
		return transaction.putNoOverwrite(getEntityStore().hookEntityPrimaryIndex(), entity);
	}

	@Override
	public void deleteHookEntity(Transaction transaction, UUID uuid)
	{
		deleteHookEntity((BerkeleyDBTransaction) transaction, new UUIDKey(uuid));
	}

	private void deleteHookEntity(BerkeleyDBTransaction transaction, UUIDKey uuidKey)
	{
		transaction.delete(getEntityStore().hookEntityPrimaryIndex(), uuidKey);
	}

	@Override
	public HookList hookList(Transaction transaction)
	{
		return new BerkeleyDBHookList(this, (BerkeleyDBTransaction) transaction);
	}

	@Override
	public PersistenceSecretKeySingletonEntity instantiatePersistenceSecretKeySingletonEntity(Class<? extends PersistenceSecretKeySingletonEntity> entityClass)
	{
		return new BerkeleyDBPersistenceSecretKeySingletonEntity();
	}

	@Override
	public PersistenceSecretKeySingletonEntity getPersistenceSecretKeySingletonEntity(Transaction transaction)
	{
		return ((BerkeleyDBTransaction) transaction).get(getEntityStore().persistenceSecretKeySingletonPrimaryIndex(), true);
	}

	@Override
	public void putPersistenceSecretKeySingletonEntity(Transaction transaction, PersistenceSecretKeySingletonEntity entity)
	{
		putPersistenceSecretKeySingletonEntity((BerkeleyDBTransaction) transaction, (BerkeleyDBPersistenceSecretKeySingletonEntity) entity);
	}

	private void putPersistenceSecretKeySingletonEntity(BerkeleyDBTransaction transaction, BerkeleyDBPersistenceSecretKeySingletonEntity entity)
	{
		transaction.putNoReturn(getEntityStore().persistenceSecretKeySingletonPrimaryIndex(), entity);
	}

	@Override
	public void deletePersistenceSecretKeySingletonEntity(Transaction transaction)
	{
		((BerkeleyDBTransaction) transaction).delete(getEntityStore().persistenceSecretKeySingletonPrimaryIndex(), true);
	}

	@Override
	public boolean lockPersistenceSecretKeySingletonEntity(Transaction transaction)
	{
		return ((BerkeleyDBTransaction) transaction).lock(getEntityStore().persistenceSecretKeySingletonPrimaryIndex(), true);
	}

}
