/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
package aletheia.persistence;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.DelegateAuthorizer;
import aletheia.model.authority.DelegateTreeNode;
import aletheia.model.authority.DelegateTreeRootNode;
import aletheia.model.authority.DelegateTreeSubNode;
import aletheia.model.authority.EncryptedPrivateSignatory;
import aletheia.model.authority.EncryptedPrivateSignatory.EncryptedException;
import aletheia.model.authority.PackedSignatureRequest;
import aletheia.model.authority.Person;
import aletheia.model.authority.PlainPrivateSignatory;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.Signatory;
import aletheia.model.authority.SignatureRequest;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthoritySignature;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.RootContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.misc.PersistenceSecretKeySingleton;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.Hook;
import aletheia.model.peertopeer.NodeDeferredMessage;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.model.term.SimpleTerm;
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
import aletheia.persistence.collections.authority.PersonsMap;
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
import aletheia.persistence.collections.authority.StatementAuthoritySignatureMap;
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
import aletheia.persistence.collections.statement.AssumptionList;
import aletheia.persistence.collections.statement.ContextLocalIdentifierToStatement;
import aletheia.persistence.collections.statement.ContextLocalStatementToIdentifier;
import aletheia.persistence.collections.statement.DependentsSet;
import aletheia.persistence.collections.statement.DescendantContextsByConsequent;
import aletheia.persistence.collections.statement.IdentifierToRootContexts;
import aletheia.persistence.collections.statement.LocalSortedStatements;
import aletheia.persistence.collections.statement.LocalStatementsByTerm;
import aletheia.persistence.collections.statement.LocalStatementsMap;
import aletheia.persistence.collections.statement.RootContextToIdentifier;
import aletheia.persistence.collections.statement.RootContextsMap;
import aletheia.persistence.collections.statement.SortedRootContexts;
import aletheia.persistence.collections.statement.SpecializationsByGeneral;
import aletheia.persistence.collections.statement.StatementsMap;
import aletheia.persistence.collections.statement.SubContextsSet;
import aletheia.persistence.collections.statement.UnfoldingContextsByDeclaration;
import aletheia.persistence.entities.authority.ContextAuthorityEntity;
import aletheia.persistence.entities.authority.DelegateAuthorizerEntity;
import aletheia.persistence.entities.authority.DelegateTreeNodeEntity;
import aletheia.persistence.entities.authority.DelegateTreeRootNodeEntity;
import aletheia.persistence.entities.authority.DelegateTreeSubNodeEntity;
import aletheia.persistence.entities.authority.EncryptedPrivateSignatoryEntity;
import aletheia.persistence.entities.authority.PackedSignatureRequestEntity;
import aletheia.persistence.entities.authority.PersonEntity;
import aletheia.persistence.entities.authority.PlainPrivateSignatoryEntity;
import aletheia.persistence.entities.authority.PrivatePersonEntity;
import aletheia.persistence.entities.authority.PrivateSignatoryEntity;
import aletheia.persistence.entities.authority.RootContextAuthorityEntity;
import aletheia.persistence.entities.authority.SignatoryEntity;
import aletheia.persistence.entities.authority.SignatureRequestEntity;
import aletheia.persistence.entities.authority.StatementAuthorityEntity;
import aletheia.persistence.entities.authority.StatementAuthoritySignatureEntity;
import aletheia.persistence.entities.authority.UnpackedSignatureRequestEntity;
import aletheia.persistence.entities.local.ContextLocalEntity;
import aletheia.persistence.entities.local.RootContextLocalEntity;
import aletheia.persistence.entities.local.StatementLocalEntity;
import aletheia.persistence.entities.misc.PersistenceSecretKeySingletonEntity;
import aletheia.persistence.entities.peertopeer.DeferredMessageEntity;
import aletheia.persistence.entities.peertopeer.HookEntity;
import aletheia.persistence.entities.peertopeer.NodeDeferredMessageEntity;
import aletheia.persistence.entities.statement.AssumptionEntity;
import aletheia.persistence.entities.statement.ContextEntity;
import aletheia.persistence.entities.statement.DeclarationEntity;
import aletheia.persistence.entities.statement.RootContextEntity;
import aletheia.persistence.entities.statement.SpecializationEntity;
import aletheia.persistence.entities.statement.StatementEntity;
import aletheia.persistence.entities.statement.UnfoldingContextEntity;
import aletheia.persistence.exceptions.PersistenceException;
import aletheia.protocol.ProtocolException;
import aletheia.security.utilities.PassphraseEncryptedStreamer.BadPassphraseException;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.collections.AdaptedCloseableSet;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CombinedCloseableSet;

/**
 * Base class for the persistence manager. This class does not depend on the
 * actual implementation of the persistence environment. Any new implementation
 * of the persistence should start subclassing this class.
 */
public abstract class PersistenceManager implements AutoCloseable
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerManager.instance.logger();

	public interface StartupProgressListener
	{
		public default void updateProgress(float progress, String text)
		{
		}

		public default void updateProgress(float progress)
		{
			updateProgress(progress, null);
		}

		public static StartupProgressListener silent = new StartupProgressListener()
		{
		};
	}

	private final PersistenceSchedulerThread persistenceSchedulerThread;

	private final PersistenceListenerManager persistenceListenerManager;

	private final PersistenceSecretKeyManager persistenceSecretKeyManager;

	private final PersistenceUndeleteManager persistenceUndeleteManager;

	private final boolean debug;

	private boolean open;

	/**
	 * Creates a new persistence manager.
	 */
	public PersistenceManager(Configuration configuration)
	{
		this.persistenceSchedulerThread = new PersistenceSchedulerThread(this);
		this.persistenceListenerManager = new PersistenceListenerManager();
		this.persistenceSecretKeyManager = new PersistenceSecretKeyManager(this);
		this.persistenceUndeleteManager = new PersistenceUndeleteManager();
		this.debug = configuration.isDebug();
		this.open = true;
	}

	protected PersistenceSchedulerThread getPersistenceSchedulerThread()
	{
		return persistenceSchedulerThread;
	}

	public PersistenceListenerManager getListenerManager()
	{
		return persistenceListenerManager;
	}

	public PersistenceSecretKeyManager getSecretKeyManager()
	{
		return persistenceSecretKeyManager;
	}

	public PersistenceUndeleteManager getUndeleteManager()
	{
		return persistenceUndeleteManager;
	}

	public boolean isDebug()
	{
		return debug;
	}

	/**
	 * Obtains a {@link Statement} given a {@link Transaction} and the
	 * {@link UUID} that identifies the {@link Statement}. Calls to
	 * {@link #getStatementEntity(Transaction, UUID)} and then to
	 * {@link #entityToStatement(StatementEntity)};
	 *
	 * @param transaction
	 *            The transaction.
	 * @param uuid
	 *            The UUID of the statement to get.
	 * @return The statement.
	 *
	 * @see #getStatementEntity(Transaction, UUID)
	 * @see #entityToStatement(StatementEntity)
	 */
	public Statement getStatement(Transaction transaction, UUID uuid)
	{
		StatementEntity entity = getStatementEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToStatement(entity);
	}

	/**
	 * Obtains an {@link StatementEntity} from the persistence environment given
	 * a {@link Transaction} and the {@link UUID} that identifies the entity. In
	 * the resulting {@link StatementEntity}, the
	 * {@link StatementEntity#getUuid()} must coincide with the parameter uuid.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param uuid
	 *            The UUID of the entity to get.
	 * @return The statement.
	 */
	public abstract StatementEntity getStatementEntity(Transaction transaction, UUID uuid);

	/**
	 * Returns a {@link Context} statement given it's identifying {@link UUID}.
	 * If that UUID doesn't correspond to any context, return null.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param uuid
	 *            The UUID of the context to get.
	 * @return The context.
	 */
	public Context getContext(Transaction transaction, UUID uuid)
	{
		Statement statement = getStatement(transaction, uuid);
		if (!(statement instanceof Context))
			return null;
		return (Context) statement;
	}

	/**
	 * Returns a {@link RootContext} statement given it's identifying
	 * {@link UUID}. If that UUID doesn't correspond to any context, return
	 * null.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param uuid
	 *            The UUID of the context to get.
	 * @return The root context.
	 */
	public RootContext getRootContext(Transaction transaction, UUID uuid)
	{
		Statement statement = getStatement(transaction, uuid);
		if (!(statement instanceof RootContext))
			return null;
		return (RootContext) statement;
	}

	/**
	 * Returns a {@link Declaration} statement given it's identifying
	 * {@link UUID}. If that UUID doesn't correspond to any declaration, return
	 * null.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param uuid
	 *            The UUID of the declaration to get.
	 * @return The declaration.
	 */
	public Declaration getDeclaration(Transaction transaction, UUID uuid)
	{
		Statement statement = getStatement(transaction, uuid);
		if (!(statement instanceof Declaration))
			return null;
		return (Declaration) statement;
	}

	/**
	 * Inserts or updates (by {@link UUID}) a statement to the persistence
	 * environment. Calls to
	 * {@link #putStatementEntity(Transaction, StatementEntity)} with the result
	 * of {@link Statement#getEntity()}.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param statement
	 *            The statement to put.
	 */
	public void putStatement(Transaction transaction, Statement statement)
	{
		putStatementEntity(transaction, statement.getEntity());
	}

	/**
	 * Inserts or updates (by {@link UUID}) a {@link StatementEntity} to the
	 * persistence environment. The entity is identified by the
	 * {@link StatementEntity#getUuid()}
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param entity
	 *            The entity to put.
	 */
	public abstract void putStatementEntity(Transaction transaction, StatementEntity entity);

	/**
	 * Deletes a statement from the persistence environment whose UUID is the
	 * same as this one.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param statement
	 *            The statement to delete.
	 */
	public void deleteStatement(Transaction transaction, Statement statement)
	{
		deleteStatement(transaction, statement.getUuid());
		getUndeleteManager().push(transaction, statement);
	}

	/**
	 * Deletes a statement from the persistence environment by UUID.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param uuid
	 *            The UUID whose statement is to be deleted.
	 */
	public void deleteStatement(Transaction transaction, UUID uuid)
	{
		deleteStatementEntity(transaction, uuid);
	}

	/**
	 * Deletes a statement entity from the persistence environment by UUID.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param uuid
	 *            The UUID whose statement is to be deleted.
	 */
	public abstract void deleteStatementEntity(Transaction transaction, UUID uuid);

	public boolean lockStatement(Transaction transaction, Statement statement)
	{
		return lockStatementEntity(transaction, statement.getUuid());
	}

	public boolean lockStatement(Transaction transaction, UUID uuid)
	{
		return lockStatementEntity(transaction, uuid);
	}

	public abstract boolean lockStatementEntity(Transaction transaction, UUID uuid);

	/**
	 * Cretes a set view of the statements that depend on another one.
	 *
	 * @param transaction
	 *            The transaction to be used on the operations on the resulting
	 *            set.
	 * @param statement
	 *            The statement.
	 * @return The dependets set of the statement.
	 *
	 * @see DependentsSet
	 */
	public abstract DependentsSet dependents(Transaction transaction, Statement statement);

	/**
	 * Creates the map view of local statements (variables -> statements) of a
	 * {@link Context}.
	 *
	 * @param transaction
	 *            The transaction transactions to be used in the operations on
	 *            the map.
	 * @param context
	 *            The context.
	 * @return The local statements map.
	 *
	 * @see LocalStatementsMap
	 */
	public abstract LocalStatementsMap localStatements(Transaction transaction, Context context);

	/**
	 * Creates the map view of all the statements (variables -> statements) of
	 * the system.
	 *
	 * @param transaction
	 *            The transaction transactions to be used in the operations on
	 *            the map.
	 * @return The statements map.
	 *
	 * @see StatementsMap
	 */
	public abstract StatementsMap statements(Transaction transaction);

	/**
	 * Creates a mapping view from terms to sets of statements that are local to
	 * this context.
	 *
	 * @param transaction
	 *            The transaction transactions to be used in the operations on
	 *            the map.
	 * @param context
	 *            The context.
	 * @return The local statements by term map.
	 *
	 * @see LocalStatementsByTerm
	 */
	public abstract LocalStatementsByTerm localStatementsByTerm(Transaction transaction, Context context);

	/**
	 * Creates a list view of the assumptions of a context.
	 *
	 * @param transaction
	 *            The transaction transactions to be used in the operations on
	 *            the list.
	 * @param context
	 *            The context.
	 * @return The list of assumptions.
	 *
	 * @see AssumptionList
	 */
	public abstract AssumptionList assumptionList(Transaction transaction, Context context);

	/**
	 * The set of direct subcontexts of this context.
	 *
	 * @param transaction
	 *            The persistence transaction across which the operations on
	 *            this set will be performed.
	 * @return The set.
	 *
	 */
	public abstract SubContextsSet subContexts(Transaction transaction, Context context);

	/**
	 * Creates a map view from statements to their identifiers for the local
	 * statements for a given context.
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @param context
	 *            The context.
	 * @return The map.
	 *
	 * @see ContextLocalStatementToIdentifier
	 */
	public abstract ContextLocalStatementToIdentifier contextLocalStatementToIdentifier(Transaction transaction, Context context);

	public abstract RootContextToIdentifier rootContextToIdentifier(Transaction transaction);

	/**
	 * Creates a map view from identifiers to the statements assigned to them
	 * locally to a given context.
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @param context
	 *            The context.
	 * @return The map.
	 *
	 * @see ContextLocalIdentifierToStatement
	 */
	public abstract ContextLocalIdentifierToStatement contextLocalIdentifierToStatement(Transaction transaction, Context context);

	public abstract IdentifierToRootContexts identifierToRootContexts(Transaction transaction);

	/**
	 * Creates a set view of all context that descent from a given one that
	 * match a given consequent term. That is, essentially the answer to the
	 * question "what contexts may this statement prove?". This view should
	 * operate reasonably efficient.
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @param context
	 *            The context.
	 * @param consequent
	 *            The consequent to be matched.
	 * @return The set view.
	 *
	 * @see DescendantContextsByConsequent
	 */
	public abstract DescendantContextsByConsequent descendantContextsByConsequent(Transaction transaction, Context context, SimpleTerm consequent);

	/**
	 * Creates a set view of all the {@linkplain UnfoldingContext unfolding
	 * contexts} that unfolds the given {@link Declaration}.
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the set.
	 * @param declaration
	 *            The declaration.
	 * @return The set view.
	 *
	 * @see UnfoldingContextsByDeclaration
	 */
	public abstract UnfoldingContextsByDeclaration unfoldingContextsByDeclaration(Transaction transaction, Declaration declaration);

	public abstract SpecializationsByGeneral specializationsByGeneral(Transaction transaction, Statement general);

	public abstract LocalSortedStatements localSortedStatements(Transaction transaction, Context context);

	/**
	 * Closes this persistence manager. Aborts any pending transaction. Any
	 * further operations will result in an error.
	 */
	@Override
	public void close()
	{
		try
		{
			sync();
			try
			{
				persistenceSchedulerThread.shutdown();
			}
			catch (InterruptedException e)
			{
				throw new PersistenceException(e);
			}
			persistenceSecretKeyManager.close();
		}
		finally
		{
			open = false;
		}
	}

	public boolean isOpen()
	{
		return open;
	}

	public abstract RootContextsMap rootContexts(Transaction transaction);

	public abstract SortedRootContexts sortedRootContexts(Transaction transaction);

	public RootContext getRootContextByHexRef(Transaction transaction, String hexRef)
	{
		CloseableIterator<RootContext> iterator = rootContexts(transaction).values().iterator();
		try
		{
			while (iterator.hasNext())
			{
				RootContext rc = iterator.next();
				if (hexRef.equals(rc.hexRef()))
					return rc;
			}
		}
		finally
		{
			iterator.close();
		}
		return null;
	}

	/**
	 * Synchronizes any buffering done in the persistence environment. In other
	 * words, no information previously entered to the system will be lost if
	 * the system crashes after this function returns.
	 */
	public abstract void sync();

	/**
	 * Creates a new {@link Transaction} that can be used on the operations of
	 * this persistence environment.
	 *
	 * @return The transaction.
	 */
	public abstract Transaction beginTransaction();

	/**
	 * Creates a new dirty {@link Transaction}. The precise meaning of what is a
	 * dirty transaction will depend on the concrete implementation of the
	 * persistence environment. Usually, from a dirty transaction the operation
	 * will be see the modified data in other uncommited transactions.
	 *
	 * @return The transaction.
	 */
	public abstract Transaction beginDirtyTransaction();

	/**
	 * Creates a new {@link Transaction} with a lock timeout in milliseconds.
	 * Any operation using this transaction will fail if it remains locked this
	 * amount of time.
	 *
	 * @param timeOut
	 *            The timeout in milliseconds.
	 * @return The transaction.
	 */
	public abstract Transaction beginTransaction(long timeOut);

	/**
	 * Creates a new dirty {@link Transaction} with a lock timeout in
	 * milliseconds.
	 *
	 * @param timeOut
	 *            The timeout in milliseconds.
	 * @return The transaction.
	 *
	 * @see #beginDirtyTransaction()
	 * @see #beginTransaction(long)
	 */
	public abstract Transaction beginDirtyTransaction(long timeOut);

	public void backupPrivate(Transaction transaction, File file, char[] passphrase) throws IOException
	{
		PrivateSignatoryBackupRestore privateSignatoryBackupRestore = new PrivateSignatoryBackupRestore(this, transaction);
		privateSignatoryBackupRestore.backup(passphrase, file);
	}

	public void restorePrivate(Transaction transaction, File file, char[] passphrase) throws IOException, ProtocolException, BadPassphraseException
	{
		PrivateSignatoryBackupRestore privateSignatoryBackupRestore = new PrivateSignatoryBackupRestore(this, transaction);
		privateSignatoryBackupRestore.restore(passphrase, file);
	}

	/**
	 * Envelopes a {@link StatementEntity} with an {@link Statement} depending
	 * on the kind of entity it is.
	 *
	 * @param e
	 *            The entity.
	 * @return The enveloping entity.
	 */
	public Statement entityToStatement(StatementEntity e)
	{
		if (e instanceof AssumptionEntity)
			return assumptionEntityToStatement((AssumptionEntity) e);
		else if (e instanceof ContextEntity)
			return contextEntityToStatement((ContextEntity) e);
		else if (e instanceof SpecializationEntity)
			return specializationEntityToStatement((SpecializationEntity) e);
		else if (e instanceof DeclarationEntity)
			return declarationEntityToStatement((DeclarationEntity) e);
		else
			throw new Error();
	}

	/**
	 * Envelopes an {@link AssumptionEntity} with an {@link Assumption}
	 *
	 * @param e
	 *            The assumption entity.
	 * @return The enveloping assumption.
	 */
	public Assumption assumptionEntityToStatement(AssumptionEntity e)
	{
		return new Assumption(this, e);
	}

	/**
	 * Envelopes a {@link ContextEntity} with a {@link Context} depending on the
	 * kind of entity it is.
	 *
	 * @param e
	 *            The context entity.
	 * @return The enveloping context.
	 */
	public Context contextEntityToStatement(ContextEntity e)
	{
		if (e instanceof RootContextEntity)
			return rootContextEntityToStatement((RootContextEntity) e);
		else if (e instanceof UnfoldingContextEntity)
			return unfoldingContextEntityToStatement((UnfoldingContextEntity) e);
		else
			return new Context(this, e);
	}

	/**
	 * Envelope a {@link RootContextEntity} with a {@link RootContext}.
	 *
	 * @param e
	 *            The root context entity.
	 * @return The enveloping root context.
	 */
	public RootContext rootContextEntityToStatement(RootContextEntity e)
	{
		return new RootContext(this, e);
	}

	/**
	 * Envelope a {@link UnfoldingContextEntity} with a {@link UnfoldingContext}
	 * .
	 *
	 * @param e
	 *            The unfolding context entity.
	 * @return The enveloping unfolding context.
	 */
	public UnfoldingContext unfoldingContextEntityToStatement(UnfoldingContextEntity e)
	{
		return new UnfoldingContext(this, e);
	}

	/**
	 * Envelope a {@link DeclarationEntity} with a {@link Declaration}.
	 *
	 * @param e
	 *            The declaration entity.
	 * @return The enveloping declaration.
	 */
	public Declaration declarationEntityToStatement(DeclarationEntity e)
	{
		return new Declaration(this, e);
	}

	/**
	 * Envelope a {@link SpecializationEntity} with a {@link Specialization}.
	 *
	 * @param e
	 *            The specialization entity.
	 * @return The enveloping specialization.
	 */
	public Specialization specializationEntityToStatement(SpecializationEntity e)
	{
		return new Specialization(this, e);
	}

	/**
	 * Creates a new instance of a entity class suitable for this persistence
	 * manager.
	 *
	 * @param entityClass
	 *            The entity class.
	 * @return The entity.
	 */
	public abstract StatementEntity instantiateStatementEntity(Class<? extends StatementEntity> entityClass);

	/**
	 * Looks up the entity interface that a concrete entity is implementing.
	 *
	 * @param e
	 *            The entity.
	 * @return The entity interface's class object.
	 */
	private Class<? extends StatementEntity> statementEntityClass(StatementEntity e)
	{
		if (e instanceof AssumptionEntity)
			return AssumptionEntity.class;
		else if (e instanceof ContextEntity)
		{
			if (e instanceof UnfoldingContextEntity)
				return UnfoldingContextEntity.class;
			else if (e instanceof RootContextEntity)
				return RootContextEntity.class;
			else if (e instanceof DeclarationEntity)
				return DeclarationEntity.class;
			else
				return ContextEntity.class;
		}
		else if (e instanceof SpecializationEntity)
			return SpecializationEntity.class;
		else
			throw new PersistenceException();

	}

	/**
	 * Adapts a generic statement entity to a freshly created statement entity
	 * that is suitable for this persistence manager. With this method an entity
	 * from another persistence manager might be copied to this one in a way
	 * that doesn't depend on the implementation.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param in
	 *            The statement entity to convert.
	 * @return The converted statement entity.
	 */
	public StatementEntity convertStatementEntity(Transaction transaction, StatementEntity in)
	{
		StatementEntity out = instantiateStatementEntity(statementEntityClass(in));
		out.setUuid(in.getUuid());
		out.setContextUuid(in.getContextUuid());
		out.setVariable(in.getVariable());
		out.setProved(in.isProved());
		out.setIdentifier(in.getIdentifier());
		out.getUuidDependencies().clear();
		out.getUuidDependencies().addAll(in.getUuidDependencies());
		if (in instanceof AssumptionEntity)
		{
			AssumptionEntity in_ = (AssumptionEntity) in;
			AssumptionEntity out_ = (AssumptionEntity) out;
			out_.setOrder(in_.getOrder());
		}
		else if (in instanceof ContextEntity)
		{
			ContextEntity in_ = (ContextEntity) in;
			ContextEntity out_ = (ContextEntity) out;
			ContextEntity parent = (ContextEntity) getStatementEntity(transaction, in.getContextUuid());
			out_.initializeContextData(parent);
			out_.setConsequent(in_.getConsequent());
			if (in instanceof UnfoldingContextEntity)
			{
				UnfoldingContextEntity in__ = (UnfoldingContextEntity) in;
				UnfoldingContextEntity out__ = (UnfoldingContextEntity) out;
				out__.setDeclarationUuid(in__.getDeclarationUuid());
			}
			else if (in instanceof RootContextEntity)
			{
			}
			else if (in instanceof DeclarationEntity)
			{
				DeclarationEntity in__ = (DeclarationEntity) in;
				DeclarationEntity out__ = (DeclarationEntity) out;
				out__.setValue(in__.getValue());
			}
		}
		else if (in instanceof SpecializationEntity)
		{
			SpecializationEntity in_ = (SpecializationEntity) in;
			SpecializationEntity out_ = (SpecializationEntity) out;
			out_.setGeneralUuid(in_.getGeneralUuid());
			out_.setInstance(in_.getInstance());
		}
		else
			throw new PersistenceException();

		return out;
	}

	public abstract SignatoryEntity instantiateSignatoryEntity(Class<? extends SignatoryEntity> entityClass);

	public Signatory getSignatory(Transaction transaction, UUID uuid)
	{
		SignatoryEntity entity = getSignatoryEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToSignatory(entity);
	}

	public abstract SignatoryEntity getSignatoryEntity(Transaction transaction, UUID uuid);

	public void putSignatory(Transaction transaction, Signatory signatory)
	{
		putSignatoryEntity(transaction, signatory.getEntity());
	}

	public abstract void putSignatoryEntity(Transaction transaction, SignatoryEntity entity);

	public abstract SignatoriesMap signatories(Transaction transaction);

	public abstract PrivateSignatoriesMap privateSignatories(Transaction transaction);

	public Signatory entityToSignatory(SignatoryEntity e)
	{
		if (e instanceof PrivateSignatoryEntity)
		{
			if (e instanceof PlainPrivateSignatoryEntity)
				return new PlainPrivateSignatory(this, (PlainPrivateSignatoryEntity) e);
			else if (e instanceof EncryptedPrivateSignatoryEntity)
				try
				{
					return new EncryptedPrivateSignatory(this, (EncryptedPrivateSignatoryEntity) e);
				}
				catch (EncryptedException e1)
				{
					return new Signatory(this, e);
				}
			else
				throw new Error();
		}
		else
			return new Signatory(this, e);
	}

	public void deleteSignatory(Transaction transaction, Signatory signatory)
	{
		deleteSignatory(transaction, signatory.getUuid());
	}

	public void deleteSignatory(Transaction transaction, UUID uuid)
	{
		deleteSignatoryEntity(transaction, uuid);
	}

	public abstract void deleteSignatoryEntity(Transaction transaction, UUID uuid);

	public abstract PersonEntity instantiatePersonEntity(Class<? extends PersonEntity> entityClass);

	public Person getPerson(Transaction transaction, UUID uuid)
	{
		PersonEntity entity = getPersonEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToPerson(entity);
	}

	public abstract PersonEntity getPersonEntity(Transaction transaction, UUID uuid);

	public void putPerson(Transaction transaction, Person person)
	{
		putPersonEntity(transaction, person.getEntity());
	}

	public abstract void putPersonEntity(Transaction transaction, PersonEntity entity);

	public Person entityToPerson(PersonEntity e)
	{
		if (e instanceof PrivatePersonEntity)
			return new PrivatePerson(this, (PrivatePersonEntity) e);
		else
			return new Person(this, e);
	}

	public void deletePerson(Transaction transaction, Person person)
	{
		deletePerson(transaction, person.getUuid());
	}

	public void deletePerson(Transaction transaction, UUID uuid)
	{
		deletePersonEntity(transaction, uuid);
	}

	public PrivatePerson getPrivatePerson(Transaction transaction, UUID uuid)
	{
		Person person = getPerson(transaction, uuid);
		if (!(person instanceof PrivatePerson))
			return null;
		return (PrivatePerson) person;
	}

	public abstract void deletePersonEntity(Transaction transaction, UUID uuid);

	public abstract PersonsMap persons(Transaction transaction);

	public abstract PersonsByNick personsByNick(Transaction transaction);

	public abstract PersonsOrphanSinceSortedSet personsOrphanSinceSortedSet(Transaction transaction);

	public abstract PrivatePersonsMap privatePersons(Transaction transaction);

	public abstract PrivatePersonsByNick privatePersonsByNick(Transaction transaction);

	public abstract StatementAuthorityEntity instantiateStatementAuthorityEntity(Class<? extends StatementAuthorityEntity> entityClass);

	public void putStatementAuthority(Transaction transaction, StatementAuthority statementAuthority)
	{
		putStatementAuthorityEntity(transaction, statementAuthority.getEntity());
	}

	public abstract void putStatementAuthorityEntity(Transaction transaction, StatementAuthorityEntity entity);

	public boolean putStatementAuthorityNoOverwrite(Transaction transaction, StatementAuthority statementAuthority)
	{
		return putStatementAuthorityEntityNoOverwrite(transaction, statementAuthority.getEntity());
	}

	public abstract boolean putStatementAuthorityEntityNoOverwrite(Transaction transaction, StatementAuthorityEntity entity);

	public StatementAuthority getStatementAuthority(Transaction transaction, UUID uuid)
	{
		StatementAuthorityEntity entity = getStatementAuthorityEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToStatementAuthority(entity);
	}

	public ContextAuthority getContextAuthority(Transaction transaction, UUID uuid)
	{
		StatementAuthority statementAuthority = getStatementAuthority(transaction, uuid);
		if (!(statementAuthority instanceof ContextAuthority))
			return null;
		return (ContextAuthority) statementAuthority;
	}

	public RootContextAuthority getRootContextAuthority(Transaction transaction, UUID uuid)
	{
		StatementAuthority statementAuthority = getStatementAuthority(transaction, uuid);
		if (!(statementAuthority instanceof RootContextAuthority))
			return null;
		return (RootContextAuthority) statementAuthority;
	}

	public abstract StatementAuthorityEntity getStatementAuthorityEntity(Transaction transaction, UUID uuid);

	public void deleteStatementAuthority(Transaction transaction, StatementAuthority statementAuthority)
	{
		deleteStatementAuthority(transaction, statementAuthority.getStatementUuid());
	}

	public void deleteStatementAuthority(Transaction transaction, UUID uuid)
	{
		deleteStatementAuthorityEntity(transaction, uuid);
	}

	public abstract void deleteStatementAuthorityEntity(Transaction transaction, UUID uuid);

	public boolean lockStatementAuthority(Transaction transaction, StatementAuthority statementAuthority)
	{
		return lockStatementAuthorityEntity(transaction, statementAuthority.getStatementUuid());
	}

	public boolean lockStatementAuthority(Transaction transaction, UUID uuid)
	{
		return lockStatementAuthorityEntity(transaction, uuid);
	}

	public abstract boolean lockStatementAuthorityEntity(Transaction transaction, UUID uuid);

	public RootContextAuthority entityToRootContextAuthority(RootContextAuthorityEntity entity)
	{
		return new RootContextAuthority(this, entity);
	}

	public ContextAuthority entityToContextAuthority(ContextAuthorityEntity entity)
	{
		return new ContextAuthority(this, entity);
	}

	public StatementAuthority entityToStatementAuthority(StatementAuthorityEntity entity)
	{
		if (entity instanceof RootContextAuthorityEntity)
			return entityToRootContextAuthority((RootContextAuthorityEntity) entity);
		else if (entity instanceof ContextAuthorityEntity)
			return entityToContextAuthority((ContextAuthorityEntity) entity);
		else
			return new StatementAuthority(this, entity);
	}

	public StatementAuthoritySignature entityToStatementAuthoritySignature(StatementAuthoritySignatureEntity entity)
	{
		return new StatementAuthoritySignature(this, entity);
	}

	public abstract StatementAuthoritySet statementAuthoritySet(Transaction transaction);

	public abstract LocalStatementAuthoritySet localStatementAuthoritySet(Transaction transaction, StatementAuthority contextAuthority);

	public abstract SignedDependenciesLocalStatementAuthoritySet signedDependenciesLocalStatementAuthoritySet(Transaction transaction,
			ContextAuthority contextAuthority);

	public abstract SignedProofLocalStatementAuthoritySet signedProofLocalStatementAuthoritySet(Transaction transaction, ContextAuthority contextAuthority);

	public abstract StatementAuthoritySetByAuthor statementAuthoritySetByAuthor(Transaction transaction, Person author);

	public RootContextAuthority getRootContextAuthorityBySignatureUuid(Transaction transaction, UUID uuid)
	{
		RootContextAuthorityEntity entity = getRootContextAuthorityEntityBySignatureUuid(transaction, uuid);
		if (entity == null)
			return null;
		return entityToRootContextAuthority(entity);
	}

	public abstract RootContextAuthorityEntity getRootContextAuthorityEntityBySignatureUuid(Transaction transaction, UUID uuid);

	public abstract RootContextAuthorityBySignatureUuid rootContextAuthorityBySignatureUuid(Transaction transaction);

	public abstract StatementAuthoritySignatureEntity instantiateStatementAuthoritySignatureEntity(
			Class<? extends StatementAuthoritySignatureEntity> entityClass);

	public void putStatementAuthoritySignature(Transaction transaction, StatementAuthoritySignature statementAuthoritySignature)
	{
		putStatementAuthoritySignatureEntity(transaction, statementAuthoritySignature.getEntity());
	}

	public abstract void putStatementAuthoritySignatureEntity(Transaction transaction, StatementAuthoritySignatureEntity entity);

	public StatementAuthoritySignature getStatementAuthoritySignature(Transaction transaction, UUID statementUuid, UUID authorizerUuid)
	{
		StatementAuthoritySignatureEntity entity = getStatementAuthoritySignatureEntity(transaction, statementUuid, authorizerUuid);
		if (entity == null)
			return null;
		return entityToStatementAuthoritySignature(entity);
	}

	public abstract StatementAuthoritySignatureEntity getStatementAuthoritySignatureEntity(Transaction transaction, UUID statementUuid, UUID authorizerUuid);

	public void deleteStatementAuthoritySignature(Transaction transaction, StatementAuthoritySignature statementAuthoritySignature)
	{
		deleteStatementAuthoritySignature(transaction, statementAuthoritySignature.getStatementUuid(), statementAuthoritySignature.getAuthorizerUuid());
	}

	public void deleteStatementAuthoritySignature(Transaction transaction, UUID statementUuid, UUID authorizerUuid)
	{
		deleteStatementAuthoritySignatureEntity(transaction, statementUuid, authorizerUuid);
	}

	public abstract void deleteStatementAuthoritySignatureEntity(Transaction transaction, UUID statementUuid, UUID authorizerUuid);

	public abstract StatementAuthoritySignatureMap statementAuthoritySignatureMap(Transaction transaction, StatementAuthority statementAuthority);

	public abstract StatementAuthoritySignatureDateSortedSet statementAuthoritySignatureDateSortedSet(Transaction transaction,
			StatementAuthority statementAuthority);

	public abstract StatementAuthoritySignatureSetByAuthorizer statementAuthoritySignatureSetByAuthorizer(Transaction transaction, Signatory authorizer);

	public abstract StatementAuthoritySignatureSetByAuthorizerAndSignatureUuid statementAuthoritySignatureSetByAuthorizerAndSignatureUuid(
			Transaction transaction, Signatory authorizer, UUID signatureUuid);

	public abstract DelegateTreeNodeEntity instantiateDelegateTreeNodeEntity(Class<? extends DelegateTreeNodeEntity> entityClass);

	public void putDelegateTreeNode(Transaction transaction, DelegateTreeNode delegateTreeNode)
	{
		putDelegateTreeNodeEntity(transaction, delegateTreeNode.getEntity());
	}

	public abstract void putDelegateTreeNodeEntity(Transaction transaction, DelegateTreeNodeEntity entity);

	public DelegateTreeNode getDelegateTreeNode(Transaction transaction, UUID statementUuid, Namespace prefix)
	{
		DelegateTreeNodeEntity entity = getDelegateTreeNodeEntity(transaction, statementUuid, prefix);
		if (entity == null)
			return null;
		return entityToDelegateTreeNode(entity);
	}

	public abstract DelegateTreeNodeEntity getDelegateTreeNodeEntity(Transaction transaction, UUID statementUuid, Namespace prefix);

	public DelegateTreeRootNode getDelegateTreeRootNode(Transaction transaction, UUID statementUuid)
	{
		return (DelegateTreeRootNode) getDelegateTreeNode(transaction, statementUuid, RootNamespace.instance);
	}

	public DelegateTreeSubNode getDelegateTreeSubNode(Transaction transaction, UUID statementUuid, NodeNamespace prefix)
	{
		return (DelegateTreeSubNode) getDelegateTreeNode(transaction, statementUuid, prefix);
	}

	public void deleteDelegateTreeNode(Transaction transaction, DelegateTreeNode delegateTreeNode)
	{
		deleteDelegateTreeNode(transaction, delegateTreeNode.getStatementUuid(), delegateTreeNode.getPrefix());
	}

	public void deleteDelegateTreeNode(Transaction transaction, UUID contextUuid, Namespace prefix)
	{
		deleteDelegateTreeNodeEntity(transaction, contextUuid, prefix);
	}

	public abstract void deleteDelegateTreeNodeEntity(Transaction transaction, UUID contextUuid, Namespace prefix);

	public DelegateTreeNode entityToDelegateTreeNode(DelegateTreeNodeEntity entity)
	{
		if (entity instanceof DelegateTreeRootNodeEntity)
			return new DelegateTreeRootNode(this, (DelegateTreeRootNodeEntity) entity);
		else if (entity instanceof DelegateTreeSubNodeEntity)
			return new DelegateTreeSubNode(this, (DelegateTreeSubNodeEntity) entity);
		else
			throw new Error();
	}

	public abstract LocalDelegateTreeSubNodeMap localDelegateTreeSubNodeMap(Transaction transaction, DelegateTreeNode delegateTreeNode);

	public abstract DelegateTreeRootNodeSetBySuccessor delegateTreeRootNodeSetBySuccessor(Transaction transaction, Person successor);

	public abstract DelegateAuthorizerEntity instantiateDelegateAuthorizerEntity(Class<? extends DelegateAuthorizerEntity> entityClass);

	public void putDelegateAuthorizer(Transaction transaction, DelegateAuthorizer delegateAuthorizer)
	{
		putDelegateAuthorizerEntity(transaction, delegateAuthorizer.getEntity());
	}

	public abstract void putDelegateAuthorizerEntity(Transaction transaction, DelegateAuthorizerEntity entity);

	public DelegateAuthorizer getDelegateAuthorizer(Transaction transaction, UUID contextUuid, Namespace prefix, UUID delegateUuid)
	{
		DelegateAuthorizerEntity entity = getDelegateAuthorizerEntity(transaction, contextUuid, prefix, delegateUuid);
		if (entity == null)
			return null;
		return entityToDelegateAuthorizer(entity);
	}

	public abstract DelegateAuthorizerEntity getDelegateAuthorizerEntity(Transaction transaction, UUID contextUuid, Namespace prefix, UUID delegateUuid);

	public void deleteDelegateAuthorizer(Transaction transaction, DelegateAuthorizer delegateAuthorizer)
	{
		deleteDelegateAuthorizer(transaction, delegateAuthorizer.getStatementUuid(), delegateAuthorizer.getPrefix(), delegateAuthorizer.getDelegateUuid());
	}

	public void deleteDelegateAuthorizer(Transaction transaction, UUID contextUuid, Namespace prefix, UUID delegateUuid)
	{
		deleteDelegateAuthorizerEntity(transaction, contextUuid, prefix, delegateUuid);
	}

	public abstract void deleteDelegateAuthorizerEntity(Transaction transaction, UUID contextUuid, Namespace prefix, UUID delegateUuid);

	public DelegateAuthorizer entityToDelegateAuthorizer(DelegateAuthorizerEntity entity)
	{
		return new DelegateAuthorizer(this, entity);
	}

	public abstract LocalDelegateAuthorizerMap localDelegateAuthorizerMap(Transaction transaction, DelegateTreeNode delegateTreeNode);

	public abstract LocalDelegateAuthorizerByAuthorizerMap localDelegateAuthorizerByAuthorizerMap(Transaction transaction,
			StatementAuthority statementAuthorityAuthority, Namespace prefix);

	public abstract DelegateAuthorizerByAuthorizerMap delegateAuthorizerByAuthorizerMap(Transaction transaction);

	public abstract DelegateAuthorizerSetByDelegate delegateAuthorizerSetByDelegate(Transaction transaction, Person delegate);

	public abstract SignatureRequestEntity instantiateSignatureRequestEntity(Class<? extends SignatureRequestEntity> entityClass);

	public SignatureRequest getSignatureRequest(Transaction transaction, UUID uuid)
	{
		SignatureRequestEntity entity = getSignatureRequestEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToSignatureRequest(entity);
	}

	public <S extends SignatureRequest> S getSignatureRequest(Transaction transaction, UUID uuid, Class<? extends S> clazz)
	{
		SignatureRequest signatureRequest = getSignatureRequest(transaction, uuid);
		if (!clazz.isInstance(signatureRequest))
			return null;
		return clazz.cast(signatureRequest);
	}

	public UnpackedSignatureRequest getUnpackedSignatureRequest(Transaction transaction, UUID uuid)
	{
		return getSignatureRequest(transaction, uuid, UnpackedSignatureRequest.class);
	}

	public PackedSignatureRequest getPackedSignatureRequest(Transaction transaction, UUID uuid)
	{
		return getSignatureRequest(transaction, uuid, PackedSignatureRequest.class);
	}

	public abstract SignatureRequestEntity getSignatureRequestEntity(Transaction transaction, UUID uuid);

	public void putSignatureRequest(Transaction transaction, SignatureRequest signatureRequest)
	{
		putSignatureRequestEntity(transaction, signatureRequest.getEntity());
	}

	public abstract void putSignatureRequestEntity(Transaction transaction, SignatureRequestEntity entity);

	public boolean putSignatureRequestNoOverwrite(Transaction transaction, SignatureRequest signatureRequest)
	{
		return putSignatureRequestEntityNoOverwrite(transaction, signatureRequest.getEntity());
	}

	public abstract boolean putSignatureRequestEntityNoOverwrite(Transaction transaction, SignatureRequestEntity entity);

	public UnpackedSignatureRequest entityToUnpackedSignatureRequest(UnpackedSignatureRequestEntity e)
	{
		return new UnpackedSignatureRequest(this, e);
	}

	public PackedSignatureRequest entityToPackedSignatureRequest(PackedSignatureRequestEntity e)
	{
		return new PackedSignatureRequest(this, e);
	}

	public SignatureRequest entityToSignatureRequest(SignatureRequestEntity e)
	{
		if (e instanceof UnpackedSignatureRequestEntity)
			return entityToUnpackedSignatureRequest((UnpackedSignatureRequestEntity) e);
		else if (e instanceof PackedSignatureRequestEntity)
			return entityToPackedSignatureRequest((PackedSignatureRequestEntity) e);
		else
			throw new Error();
	}

	public abstract void deleteSignatureRequestEntity(Transaction transaction, UUID uuid);

	public void deleteSignatureRequest(Transaction transaction, UUID uuid)
	{
		deleteSignatureRequestEntity(transaction, uuid);
	}

	public void deleteSignatureRequest(Transaction transaction, SignatureRequest signatureRequest)
	{
		deleteSignatureRequest(transaction, signatureRequest.getUuid());
	}

	public abstract SignatureRequestMap signatureRequestMap(Transaction transaction);

	public abstract UnpackedSignatureRequestSetByContextPath unpackedSignatureRequestSetByContextPath(Transaction transaction, Context context);

	public abstract PackedSignatureRequestSetByContextPath packedSignatureRequestSetByContextPath(Transaction transaction, UUID contextUuid);

	public CloseableSet<SignatureRequest> signatureRequestSetByContextPath(Transaction transaction, UUID contextUuid)
	{
		CloseableSet<SignatureRequest> packedRequests = new AdaptedCloseableSet<>(packedSignatureRequestSetByContextPath(transaction, contextUuid));
		Context context = getContext(transaction, contextUuid);
		if (context == null)
			return packedRequests;
		else
		{
			CloseableSet<SignatureRequest> unpackedRequests = new AdaptedCloseableSet<>(unpackedSignatureRequestSetByContextPath(transaction, context));
			return new CombinedCloseableSet<>(packedRequests, unpackedRequests);
		}
	}

	public abstract PackedSignatureRequestContextPackingDateCollection packedSignatureRequestContextPackingDateCollection(Transaction transaction,
			UUID contextUuid);

	public abstract SignatureRequestContextCreationDateCollection signatureRequestContextCreationDateCollection(Transaction transaction, UUID contextUuid);

	public abstract SignatureRequestContextSubContextUuidsCollection signatureRequestContextSubContextUuidsCollection(Transaction transaction,
			UUID contextUuid);

	public abstract SignatureRequestContextSubContextUuidsCollection signatureRequestContextSubContextUuidsCollection(Transaction transaction);

	public abstract UnpackedSignatureRequestSetByStatementAuthority unpackedSignatureRequestSetByStatementAuthority(Transaction transaction,
			StatementAuthority statementAuthority);

	public abstract StatementLocalEntity instantiateStatementLocalEntity(Class<? extends StatementLocalEntity> entityClass);

	public void putStatementLocal(Transaction transaction, StatementLocal statementLocal)
	{
		putStatementLocalEntity(transaction, statementLocal.getEntity());
	}

	public abstract void putStatementLocalEntity(Transaction transaction, StatementLocalEntity entity);

	public StatementLocal getStatementLocal(Transaction transaction, UUID uuid)
	{
		StatementLocalEntity entity = getStatementLocalEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToStatementLocal(entity);
	}

	public ContextLocal getContextLocal(Transaction transaction, UUID uuid)
	{
		StatementLocal statementLocal = getStatementLocal(transaction, uuid);
		if (!(statementLocal instanceof ContextLocal))
			return null;
		return (ContextLocal) statementLocal;
	}

	public RootContextLocal getRootContextLocal(Transaction transaction, UUID uuid)
	{
		StatementLocal statementLocal = getStatementLocal(transaction, uuid);
		if (!(statementLocal instanceof RootContextLocal))
			return null;
		return (RootContextLocal) statementLocal;
	}

	public abstract StatementLocalEntity getStatementLocalEntity(Transaction transaction, UUID uuid);

	public void deleteStatementLocal(Transaction transaction, StatementLocal statementLocal)
	{
		deleteStatementLocal(transaction, statementLocal.getStatementUuid());
	}

	public void deleteStatementLocal(Transaction transaction, UUID uuid)
	{
		deleteStatementLocalEntity(transaction, uuid);
	}

	public abstract void deleteStatementLocalEntity(Transaction transaction, UUID uuid);

	public StatementLocal entityToStatementLocal(StatementLocalEntity entity)
	{
		if (entity instanceof ContextLocalEntity)
		{
			if (entity instanceof RootContextLocalEntity)
				return new RootContextLocal(this, (RootContextLocalEntity) entity);
			else
				return new ContextLocal(this, (ContextLocalEntity) entity);
		}
		else
			return new StatementLocal(this, entity);
	}

	public abstract SubscribeProofStatementLocalSet subscribeProofStatementLocalSet(Transaction transaction, ContextLocal contextLocal);

	public abstract SubscribeStatementsContextLocalSet subscribeStatementsContextLocalSet(Transaction transaction, ContextLocal contextLocal);

	public abstract SubscribeProofRootContextLocalSet subscribeProofRootContextLocalSet(Transaction transaction);

	public abstract SubscribeStatementsRootContextLocalSet subscribeStatementsRootContextLocalSet(Transaction transaction);

	public abstract SubscribeProofStatementLocalSetMap subscribeProofStatementLocalSetMap(Transaction transaction);

	public abstract SubscribeStatementsContextLocalSetMap subscribeStatementsContextLocalSetMap(Transaction transaction);

	public abstract StatementLocalSet statementLocalSet(Transaction transaction, ContextLocal contextLocal);

	public abstract StatementLocalSetMap statementLocalSetMap(Transaction transaction);

	public abstract DeferredMessageEntity instantiateDeferredMessageEntity(Class<? extends DeferredMessageEntity> entityClass);

	public DeferredMessage getDeferredMessage(Transaction transaction, UUID uuid)
	{
		DeferredMessageEntity entity = getDeferredMessageEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToDeferredMessage(entity);
	}

	public abstract DeferredMessageEntity getDeferredMessageEntity(Transaction transaction, UUID uuid);

	public void putDeferredMessage(Transaction transaction, DeferredMessage deferredMessage)
	{
		putDeferredMessageEntity(transaction, deferredMessage.getEntity());
	}

	public abstract void putDeferredMessageEntity(Transaction transaction, DeferredMessageEntity entity);

	public DeferredMessage entityToDeferredMessage(DeferredMessageEntity e)
	{
		return new DeferredMessage(this, e);
	}

	public abstract void deleteDeferredMessageEntity(Transaction transaction, UUID uuid);

	public void deleteDeferredMessage(Transaction transaction, UUID uuid)
	{
		deleteDeferredMessageEntity(transaction, uuid);
	}

	public void deleteDeferredMessage(Transaction transaction, DeferredMessage deferredMessage)
	{
		deleteDeferredMessage(transaction, deferredMessage.getUuid());
	}

	public abstract boolean lockDeferredMessageEntity(Transaction transaction, UUID uuid);

	public boolean lockDeferredMessage(Transaction transaction, UUID uuid)
	{
		return lockDeferredMessageEntity(transaction, uuid);
	}

	public boolean lockDeferredMessage(Transaction transaction, DeferredMessage deferredMessage)
	{
		return lockDeferredMessage(transaction, deferredMessage.getUuid());
	}

	public abstract NodeDeferredMessageEntity instantiateNodeDeferredMessageEntity(Class<? extends NodeDeferredMessageEntity> entityClass);

	public NodeDeferredMessage getNodeDeferredMessage(Transaction transaction, UUID nodeUuid, UUID deferredMessageUuid)
	{
		NodeDeferredMessageEntity entity = getNodeDeferredMessageEntity(transaction, nodeUuid, deferredMessageUuid);
		if (entity == null)
			return null;
		return entityToNodeDeferredMessage(entity);
	}

	public abstract NodeDeferredMessageEntity getNodeDeferredMessageEntity(Transaction transaction, UUID nodeUuid, UUID deferredMessageUuid);

	public void putNodeDeferredMessage(Transaction transaction, NodeDeferredMessage nodeDeferredMessage)
	{
		putNodeDeferredMessageEntity(transaction, nodeDeferredMessage.getEntity());
	}

	public abstract void putNodeDeferredMessageEntity(Transaction transaction, NodeDeferredMessageEntity entity);

	public boolean putNodeDeferredMessageNoOverwrite(Transaction transaction, NodeDeferredMessage nodeDeferredMessage)
	{
		return putNodeDeferredMessageEntityNoOverwrite(transaction, nodeDeferredMessage.getEntity());
	}

	public abstract boolean putNodeDeferredMessageEntityNoOverwrite(Transaction transaction, NodeDeferredMessageEntity entity);

	public NodeDeferredMessage entityToNodeDeferredMessage(NodeDeferredMessageEntity e)
	{
		return new NodeDeferredMessage(this, e);
	}

	public abstract void deleteNodeDeferredMessageEntity(Transaction transaction, UUID nodeUuid, UUID deferredMessageUuid);

	public void deleteNodeDeferredMessage(Transaction transaction, UUID nodeUuid, UUID deferredMessageUuid)
	{
		deleteNodeDeferredMessageEntity(transaction, nodeUuid, deferredMessageUuid);
	}

	public void deleteNodeDeferredMessage(Transaction transaction, NodeDeferredMessage nodeDeferredMessage)
	{
		deleteNodeDeferredMessage(transaction, nodeDeferredMessage.getNodeUuid(), nodeDeferredMessage.getDeferredMessageUuid());
	}

	public abstract NodeDeferredMessagesMap nodeDeferredMessagesMap(Transaction transaction, DeferredMessage deferredMessage);

	public abstract NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID nodeUuid,
			UUID recipientUuid);

	public abstract NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID nodeUuid,
			UUID recipientUuid, Date fromDate);

	public abstract NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID nodeUuid,
			UUID recipientUuid, Date fromDate, Date toDate);

	public abstract NodeDeferredMessagesByNodeMap nodeDeferredMessagesByNodeMap(Transaction transaction, UUID nodeUuid);

	public abstract HookEntity instantiateHookEntity(Class<? extends HookEntity> entityClass);

	public Hook getHook(Transaction transaction, UUID uuid)
	{
		HookEntity entity = getHookEntity(transaction, uuid);
		if (entity == null)
			return null;
		return entityToHook(entity);
	}

	public abstract HookEntity getHookEntity(Transaction transaction, UUID uuid);

	public void putHook(Transaction transaction, Hook hook)
	{
		putHookEntity(transaction, hook.getEntity());
	}

	public abstract void putHookEntity(Transaction transaction, HookEntity entity);

	public boolean putHookNoOverwrite(Transaction transaction, Hook hook)
	{
		return putHookEntityNoOverwrite(transaction, hook.getEntity());
	}

	public abstract boolean putHookEntityNoOverwrite(Transaction transaction, HookEntity entity);

	public Hook entityToHook(HookEntity e)
	{
		return new Hook(this, e);
	}

	public abstract void deleteHookEntity(Transaction transaction, UUID uuid);

	public void deleteHook(Transaction transaction, UUID uuid)
	{
		deleteHookEntity(transaction, uuid);
	}

	public void deleteHook(Transaction transaction, Hook hook)
	{
		deleteHook(transaction, hook.getUuid());
	}

	public abstract HookList hookList(Transaction transaction);

	public void clearHookList(Transaction transaction)
	{
		for (Hook hook : hookList(transaction))
			hook.delete(transaction);
	}

	public abstract PersistenceSecretKeySingletonEntity instantiatePersistenceSecretKeySingletonEntity(
			Class<? extends PersistenceSecretKeySingletonEntity> entityClass);

	public PersistenceSecretKeySingleton getPersistenceSecretKeySingleton(Transaction transaction)
	{
		PersistenceSecretKeySingletonEntity entity = getPersistenceSecretKeySingletonEntity(transaction);
		if (entity == null)
			return null;
		return entityToPersistenceSecretKeySingleton(entity);
	}

	public abstract PersistenceSecretKeySingletonEntity getPersistenceSecretKeySingletonEntity(Transaction transaction);

	public void putPersistenceSecretKeySingleton(Transaction transaction, PersistenceSecretKeySingleton persistenceSecretKeySingleton)
	{
		putPersistenceSecretKeySingletonEntity(transaction, persistenceSecretKeySingleton.getEntity());
	}

	public abstract void putPersistenceSecretKeySingletonEntity(Transaction transaction, PersistenceSecretKeySingletonEntity entity);

	public PersistenceSecretKeySingleton entityToPersistenceSecretKeySingleton(PersistenceSecretKeySingletonEntity e)
	{
		return new PersistenceSecretKeySingleton(this, e);
	}

	public void deletePersistenceSecretKeySingleton(Transaction transaction)
	{
		deletePersistenceSecretKeySingletonEntity(transaction);
	}

	public abstract void deletePersistenceSecretKeySingletonEntity(Transaction transaction);

	public abstract boolean lockPersistenceSecretKeySingletonEntity(Transaction transaction);

	public boolean lockPersistenceSecretKeySingleton(Transaction transaction)
	{
		return lockPersistenceSecretKeySingletonEntity(transaction);
	}

	public void export(File file, Transaction transaction, Collection<Statement> statements, boolean signed, boolean skipSignedProof) throws IOException
	{
		new ExportImport(this).export(file, transaction, statements, signed, skipSignedProof);
	}

	public void import_(DataInput in) throws IOException, ProtocolException
	{
		new ExportImport(this).import_(in);
	}

	public void import_(File file, ListenableAborter aborter) throws IOException, ProtocolException, AbortException
	{
		new ExportImport(this).import_(file, aborter);
	}

	public void import_(File file) throws IOException, ProtocolException
	{
		new ExportImport(this).import_(file);
	}

	public void export(DataOutput out, Transaction transaction, Collection<Statement> statements, boolean signed, boolean skipSignedProof) throws IOException
	{
		new ExportImport(this).export(out, transaction, statements, signed, skipSignedProof);
	}

	public void import_(DataInput in, ListenableAborter aborter) throws IOException, ProtocolException, AbortException
	{
		new ExportImport(this).import_(in, aborter);
	}

}
