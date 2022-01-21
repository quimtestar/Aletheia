/*******************************************************************************
 * Copyright (c) 2014, 2022 Quim Testar.
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
package aletheia.model.authority;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.DelegateTreeRootNode.DateConsistenceException;
import aletheia.model.authority.DelegateTreeRootNode.NoPrivateDataForAuthorException;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.identifier.RootNamespace;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.statement.protocol.StatementProtocol;
import aletheia.model.term.Term;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.LocalDelegateAuthorizerByAuthorizerMap;
import aletheia.persistence.collections.authority.LocalStatementAuthoritySet;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureDateSortedSet;
import aletheia.persistence.collections.authority.StatementAuthoritySignatureMap;
import aletheia.persistence.collections.authority.UnpackedSignatureRequestSetByStatementAuthority;
import aletheia.persistence.collections.statement.DescendantContextsByConsequent;
import aletheia.persistence.entities.authority.StatementAuthorityEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.model.SignatureData;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CloseableSortedSet;
import aletheia.utilities.collections.CombinedCloseableCollection;
import aletheia.utilities.collections.CombinedCloseableIterable;
import aletheia.utilities.collections.CombinedCloseableMap;
import aletheia.utilities.collections.EmptyCloseableMap;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCloseableMap;
import aletheia.utilities.collections.FilteredCloseableSet;
import aletheia.utilities.collections.FilteredCloseableSortedSet;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.TrivialCloseableCollection;
import aletheia.utilities.collections.TrivialCloseableIterable;

public class StatementAuthority implements Exportable
{
	private static final Logger logger = LoggerManager.instance.logger();

	private final PersistenceManager persistenceManager;
	private final StatementAuthorityEntity entity;

	private UUID oldAuthorUuid;

	public StatementAuthority(PersistenceManager persistenceManager, StatementAuthorityEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;

		this.oldAuthorUuid = entity.getAuthorUuid();
	}

	protected StatementAuthority(PersistenceManager persistenceManager, Class<? extends StatementAuthorityEntity> entityClass, Statement statement,
			UUID authorUuid, Date creationDate)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateStatementAuthorityEntity(entityClass);
		setStatementUuid(statement.getUuid());
		setContextUuid(statement.getContextUuid());
		setAuthorUuid(authorUuid);
		setCreationDate(creationDate);

		this.oldAuthorUuid = null;
	}

	protected StatementAuthority(PersistenceManager persistenceManager, Statement statement, UUID authorUuid, Date creationDate)
	{
		this(persistenceManager, StatementAuthorityEntity.class, statement, authorUuid, creationDate);
	}

	protected StatementAuthority(PersistenceManager persistenceManager, Statement statement, Person author, Date creationDate)
	{
		this(persistenceManager, statement, author.getUuid(), creationDate);
	}

	public static StatementAuthority create(PersistenceManager persistenceManager, Transaction transaction, Statement statement, Person author)
			throws AuthorityCreationException
	{
		return create(persistenceManager, transaction, statement, author, new Date());
	}

	public static abstract class AuthorityCreationException extends AuthorityException
	{
		private static final long serialVersionUID = 4653634423621814396L;

		public AuthorityCreationException()
		{
			super();
		}

		public AuthorityCreationException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public AuthorityCreationException(String message)
		{
			super(message);
		}

		public AuthorityCreationException(Throwable cause)
		{
			super(cause);
		}
	}

	public static class AuthorityWithNoParentException extends AuthorityCreationException
	{
		private static final long serialVersionUID = 5718796193137314535L;

		public AuthorityWithNoParentException()
		{
			super("Statement's parent context has no authority");
		}
	}

	public static class AlreadyAuthoredStatementException extends AuthorityCreationException
	{
		private static final long serialVersionUID = -181488453259328354L;

		public AlreadyAuthoredStatementException()
		{
			super("Statement is already authored");
		}
	}

	public static StatementAuthority create(PersistenceManager persistenceManager, Transaction transaction, Statement statement, Person author,
			Date creationDate) throws AuthorityCreationException
	{
		StatementAuthority statementAuthority;
		if (statement instanceof RootContext)
			statementAuthority = new RootContextAuthority(persistenceManager, (RootContext) statement, author, creationDate);
		else
		{
			Context ctx = statement.getContext(transaction);
			if (ctx.getAuthority(transaction) == null)
				throw new AuthorityWithNoParentException();
			if (statement instanceof Context)
				statementAuthority = new ContextAuthority(persistenceManager, (Context) statement, author, creationDate);
			else
				statementAuthority = new StatementAuthority(persistenceManager, statement, author, creationDate);
		}
		if (!statementAuthority.persistenceUpdateNoOverwrite(transaction))
			throw new AlreadyAuthoredStatementException();
		{
			Iterable<aletheia.model.statement.Statement.StateListener> listeners = statementAuthority.statementStateListeners();
			synchronized (listeners)
			{
				for (Statement.StateListener stateListener : listeners)
					stateListener.statementAuthorityCreated(transaction, statement, statementAuthority);
			}
		}
		return statementAuthority;
	}

	private Iterable<aletheia.model.statement.Statement.StateListener> statementStateListeners()
	{
		return persistenceManager.getListenerManager().getStatementStateListeners().iterable(getStatementUuid());
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public StatementAuthorityEntity getEntity()
	{
		return entity;
	}

	private synchronized void checkAuthorOrphanityInsert(Transaction transaction)
	{
		if (oldAuthorUuid == null || !oldAuthorUuid.equals(getAuthorUuid()))
		{
			getAuthor(transaction).checkOrphanity(transaction);
			if (oldAuthorUuid != null)
			{
				Person author = getPersistenceManager().getPerson(transaction, oldAuthorUuid);
				if (author != null)
					author.checkOrphanity(transaction);
			}
			oldAuthorUuid = getAuthorUuid();
		}
	}

	private synchronized void checkAuthorOrphanityDelete(Transaction transaction)
	{
		if (oldAuthorUuid != null)
		{
			Person author = getPersistenceManager().getPerson(transaction, oldAuthorUuid);
			if (author != null)
				author.checkOrphanity(transaction);
			oldAuthorUuid = null;
		}
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putStatementAuthority(transaction, this);
		checkAuthorOrphanityInsert(transaction);
	}

	protected boolean persistenceUpdateNoOverwrite(Transaction transaction)
	{
		boolean put = persistenceManager.putStatementAuthorityNoOverwrite(transaction, this);
		if (put)
			checkAuthorOrphanityInsert(transaction);
		return put;
	}

	public UUID getStatementUuid()
	{
		return entity.getStatementUuid();
	}

	private void setStatementUuid(UUID statementUuid)
	{
		entity.setStatementUuid(statementUuid);
	}

	public UUID getContextUuid()
	{
		return entity.getContextUuid();
	}

	private void setContextUuid(UUID contextUuid)
	{
		entity.setContextUuid(contextUuid);
	}

	public UUID getAuthorUuid()
	{
		return entity.getAuthorUuid();
	}

	private void setAuthorUuid(UUID authorUuid)
	{
		entity.setAuthorUuid(authorUuid);
	}

	public Statement getStatement(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getStatementUuid());
	}

	public ContextAuthority getContextAuthority(Transaction transaction)
	{
		return persistenceManager.getContextAuthority(transaction, getContextUuid());
	}

	public Context getContext(Transaction transaction)
	{
		return persistenceManager.getContext(transaction, getContextUuid());
	}

	public Person getAuthor(Transaction transaction)
	{
		return persistenceManager.getPerson(transaction, getAuthorUuid());
	}

	public Date getCreationDate()
	{
		return entity.getCreationDate();
	}

	private void setCreationDate(Date creationDate)
	{
		entity.setCreationDate(creationDate);
	}

	void signatureDataOutStatementAuthoritySignature(DataOutput out, Transaction transaction, int signatureVersion)
	{
		try
		{
			UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			DateProtocol dateProtocol = new DateProtocol(0);
			StatementProtocol statementProtocol = new StatementProtocol(4, persistenceManager, transaction);
			Statement statement = getStatement(transaction);
			statementProtocol.send(out, statement);
			uuidProtocol.send(out, getAuthorUuid());
			dateProtocol.send(out, getCreationDate());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

	public StatementAuthoritySignatureMap signatureMap(Transaction transaction)
	{
		return persistenceManager.statementAuthoritySignatureMap(transaction, this);
	}

	public StatementAuthoritySignatureDateSortedSet signatureDateSortedSet(Transaction transaction)
	{
		return persistenceManager.statementAuthoritySignatureDateSortedSet(transaction, this);
	}

	public StatementAuthoritySignature createSignature(Transaction transaction, PrivateSignatory authorizer)
	{
		return StatementAuthoritySignature.create(getPersistenceManager(), transaction, this, authorizer);
	}

	public StatementAuthoritySignature createSignature(Transaction transaction, Signatory authorizer, Date signatureDate, int signatureVersion,
			SignatureData signatureData) throws SignatureVerifyException, SignatureVersionException
	{
		return StatementAuthoritySignature.create(getPersistenceManager(), transaction, this, authorizer.getUuid(), signatureDate, signatureVersion,
				signatureData);
	}

	private void deleteSignatureNoCheckValidSignature(Transaction transaction, UUID authorizerUuid)
	{
		StatementAuthoritySignature signature = persistenceManager.getStatementAuthoritySignature(transaction, getStatementUuid(), authorizerUuid);
		if (signature != null)
		{
			persistenceManager.deleteStatementAuthoritySignature(transaction, getStatementUuid(), authorizerUuid);
			Iterable<StatementAuthority.StateListener> stateListeners = stateListeners();
			synchronized (stateListeners)
			{
				for (StatementAuthority.StateListener l : stateListeners)
					l.signatureDeleted(transaction, this, signature);
			}
		}
	}

	private void deleteSignatureNoCheckValidSignature(Transaction transaction, Signatory authorizer)
	{
		deleteSignatureNoCheckValidSignature(transaction, authorizer.getUuid());
	}

	protected void deleteSignature(Transaction transaction, UUID authorizerUuid)
	{
		deleteSignatureNoCheckValidSignature(transaction, authorizerUuid);
		checkValidSignature(transaction);
	}

	public void deleteSignature(Transaction transaction, Signatory authorizer)
	{
		deleteSignature(transaction, authorizer.getUuid());
	}

	public CloseableMap<Signatory, StatementAuthoritySignature> validSignatureMap(Transaction transaction)
	{
		return new FilteredCloseableMap<>(new Filter<StatementAuthoritySignature>()
		{

			@Override
			public boolean filter(StatementAuthoritySignature sas)
			{
				return sas.isValid();
			}
		}, signatureMap(transaction));
	}

	public boolean isValidSignature()
	{
		return getEntity().isValidSignature();
	}

	private void setValidSignature(Transaction transaction, boolean validSignature)
	{
		getEntity().setValidSignature(validSignature);
		persistenceUpdate(transaction);
	}

	private boolean calcValidSignature(Transaction transaction)
	{
		return !validSignatureMap(transaction).isEmpty();
	}

	public void checkValidSignature(Transaction transaction)
	{
		boolean oldValidSignature = isValidSignature();
		boolean newValidSignature = calcValidSignature(transaction);
		if (newValidSignature != oldValidSignature)
		{
			setValidSignature(transaction, newValidSignature);
			Iterable<StateListener> listeners = stateListeners();
			synchronized (listeners)
			{
				for (StateListener listener : listeners)
					listener.validSignatureStateChanged(transaction, this, newValidSignature);
			}
			checkSignedDependencies(transaction);
			StatementAuthority stAuth = refresh(transaction);
			setSignedDependencies(stAuth.isSignedDependencies());
			if (newValidSignature)
				checkSignedProof(transaction);
			else
				checkSignedProofUuids(transaction, stAuth.resetSignedProof(transaction));
			stAuth = refresh(transaction);
			setSignedProof(stAuth.isSignedProof());
		}
	}

	public CloseableSortedSet<StatementAuthoritySignature> validSignatureDateSortedSet(Transaction transaction)
	{
		return new FilteredCloseableSortedSet<>(new Filter<StatementAuthoritySignature>()
		{

			@Override
			public boolean filter(StatementAuthoritySignature sas)
			{
				return sas.isValid();
			}
		}, signatureDateSortedSet(transaction))
		{

			@Override
			protected StatementAuthoritySignatureDateSortedSet getInner()
			{
				return (StatementAuthoritySignatureDateSortedSet) super.getInner();
			}

			@Override
			public StatementAuthoritySignature last()
			{
				CloseableIterator<StatementAuthoritySignature> iterator = getInner().reverseIterator();
				try
				{
					while (true)
					{
						StatementAuthoritySignature sas = iterator.next();
						if (getFilter().filter(sas))
							return sas;
					}
				}
				finally
				{
					iterator.close();
				}
			}
		};
	}

	public StatementAuthoritySignature lastValidSignature(Transaction transaction)
	{
		try
		{
			return validSignatureDateSortedSet(transaction).last();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		Transaction transaction = persistenceManager.beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

	public String toString(Transaction transaction)
	{
		Statement statement = getStatement(transaction);
		if (statement == null)
			return "*null*";
		Person author = getAuthor(transaction);
		if (author == null)
			return "*null*";
		return statement.getVariable().toString(statement.parentVariableToIdentifier(transaction)) + ": " + "[ " + author.toString() + "]" + ": "
				+ getCreationDate() + ": " + signatureStatus();
	}

	public DelegateTreeRootNode getDelegateTreeRootNode(Transaction transaction)
	{
		return (DelegateTreeRootNode) getPersistenceManager().getDelegateTreeNode(transaction, getStatementUuid(), RootNamespace.instance);
	}

	private DelegateTreeRootNode createDelegateTreeRootNode(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		return DelegateTreeRootNode.create(persistenceManager, transaction, this);
	}

	public DelegateTreeRootNode getOrCreateDelegateTreeRootNode(Transaction transaction) throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		DelegateTreeRootNode delegateTreeRootNode = getDelegateTreeRootNode(transaction);
		if (delegateTreeRootNode == null)
			delegateTreeRootNode = createDelegateTreeRootNode(transaction);
		return delegateTreeRootNode;
	}

	private DelegateTreeRootNode createDelegateTreeRootNodeNoSign(Transaction transaction)
	{
		return DelegateTreeRootNode.createNoSign(persistenceManager, transaction, this);
	}

	public DelegateTreeRootNode getOrCreateDelegateTreeRootNodeNoSign(Transaction transaction)
	{
		DelegateTreeRootNode delegateTreeRootNode = getDelegateTreeRootNode(transaction);
		if (delegateTreeRootNode == null)
			delegateTreeRootNode = createDelegateTreeRootNodeNoSign(transaction);
		return delegateTreeRootNode;
	}

	public DelegateTreeNode getDelegateTreeNode(Transaction transaction, Namespace namespace)
	{
		DelegateTreeNode node = getDelegateTreeRootNode(transaction);
		for (String name : namespace.nameList())
		{
			if (node == null)
				return null;
			try
			{
				node = node.getSubNode(transaction, name);
			}
			catch (InvalidNameException e)
			{
				throw new Error();
			}
		}
		return node;
	}

	public DelegateTreeNode getClosestDelegateTreeNode(Transaction transaction, Namespace namespace)
	{
		DelegateTreeNode node = getDelegateTreeRootNode(transaction);
		if (node == null)
			return null;
		DelegateTreeNode prev = node;
		for (String name : namespace.nameList())
		{
			prev = node;
			try
			{
				node = node.getSubNode(transaction, name);
				if (node == null)
					return prev;
			}
			catch (InvalidNameException e)
			{
				throw new Error();
			}
		}
		return node;
	}

	protected DelegateTreeNode getOrCreateDelegateTreeNode(Transaction transaction, Namespace namespace) throws NoPrivateDataForAuthorException
	{
		DelegateTreeNode node = getOrCreateDelegateTreeRootNodeNoSign(transaction);
		for (String name : namespace.nameList())
		{
			try
			{
				node = node.getOrCreateSubNodeNoSign(transaction, name);
			}
			catch (InvalidNameException e)
			{
				throw new Error();
			}
		}
		node.persistenceUpdate(transaction);
		return node;
	}

	public DelegateAuthorizer getOrCreateDelegateAuthorizer(Transaction transaction, Namespace namespace, Person delegate)
			throws NoPrivateDataForAuthorException, DateConsistenceException
	{
		return getOrCreateDelegateTreeNode(transaction, namespace).getOrCreateDelegateAuthorizer(transaction, delegate);
	}

	public DelegateAuthorizer getDelegateAuthorizer(Transaction transaction, Namespace namespace, Person delegate)
	{
		DelegateTreeNode node = getDelegateTreeNode(transaction, namespace);
		if (node == null)
			return null;
		return node.getDelegateAuthorizer(transaction, delegate);
	}

	protected CloseableMap<Person, DelegateAuthorizer> localDelegateAuthorizerMap(Transaction transaction, Namespace namespace)
	{
		DelegateTreeNode delegateTreeNode = getClosestDelegateTreeNode(transaction, namespace);
		if (delegateTreeNode == null)
			return new EmptyCloseableMap<>();
		return delegateTreeNode.delegateAuthorizerMap(transaction);
	}

	public CloseableMap<Person, DelegateAuthorizer> delegateAuthorizerMap(Transaction transaction, Namespace namespace)
	{
		return new CombinedCloseableMap<>(localDelegateAuthorizerMap(transaction, namespace),
				getContextAuthority(transaction).delegateAuthorizerMap(transaction, namespace));
	}

	public void deleteDelegateTree(Transaction transaction)
	{
		DelegateTreeRootNode delegateTreeRootNode = getDelegateTreeRootNode(transaction);
		if (delegateTreeRootNode != null)
		{
			delegateTreeRootNode.deleteDelegateSubTree(transaction);
			Iterable<StateListener> listeners = stateListeners();
			synchronized (listeners)
			{
				for (StateListener l : listeners)
					l.delegateTreeChanged(transaction, this);
			}
		}

	}

	private LocalDelegateAuthorizerByAuthorizerMap localDelegateAuthorizerByAuthorizerMap(Transaction transaction, Namespace prefix)
	{
		return getPersistenceManager().localDelegateAuthorizerByAuthorizerMap(transaction, this, prefix);
	}

	protected CloseableMap<Signatory, DelegateAuthorizer> localDelegateAuthorizerByAuthorizerMapPrefixes(Transaction transaction, Namespace prefix)
	{
		CloseableMap<Signatory, DelegateAuthorizer> map = localDelegateAuthorizerByAuthorizerMap(transaction, RootNamespace.instance);
		for (NodeNamespace nns : prefix.prefixList())
		{
			LocalDelegateAuthorizerByAuthorizerMap front = localDelegateAuthorizerByAuthorizerMap(transaction, nns);
			map = new CombinedCloseableMap<>(front, map);
		}
		return map;
	}

	public CloseableMap<Signatory, DelegateAuthorizer> delegateAuthorizerByAuthorizerMap(Transaction transaction, Namespace prefix)
	{
		return new CombinedCloseableMap<>(localDelegateAuthorizerByAuthorizerMapPrefixes(transaction, prefix),
				getContextAuthority(transaction).delegateAuthorizerByAuthorizerMap(transaction, prefix));
	}

	public boolean isDelegateTreeRootNodeSigned(Transaction transaction)
	{
		DelegateTreeRootNode delegateTreeRootNode = getDelegateTreeRootNode(transaction);
		if (delegateTreeRootNode == null)
			return true;
		return delegateTreeRootNode.isSigned();
	}

	public StatementAuthoritySignature getSignature(Transaction transaction, UUID authorizerUuid)
	{
		return persistenceManager.getStatementAuthoritySignature(transaction, getStatementUuid(), authorizerUuid);
	}

	public CloseableCollection<Signatory> signatoryDependencies(final Transaction transaction)
	{
		CloseableCollection<Signatory> deps = signatureMap(transaction).keySet();
		DelegateTreeRootNode delegateTreeRootNode = getDelegateTreeRootNode(transaction);
		if (delegateTreeRootNode != null)
		{
			Bijection<DelegateAuthorizer, Signatory> bijection = new Bijection<>()
			{

				@Override
				public Signatory forward(DelegateAuthorizer delegateAuthorizer)
				{
					return delegateAuthorizer.getAuthorizer(transaction);
				}

				@Override
				public DelegateAuthorizer backward(Signatory output)
				{
					throw new UnsupportedOperationException();
				}
			};
			deps = new CombinedCloseableCollection<>(new BijectionCloseableCollection<>(bijection,
					new TrivialCloseableCollection<>(new BufferedList<>(delegateTreeRootNode.delegateAuthorizersRecursive(transaction)))), deps);
		}
		return deps;
	}

	public void clearSignatures(Transaction transaction)
	{
		clearSignaturesNoCheckValidSignature(transaction);
		checkValidSignature(transaction);
	}

	protected void clearSignaturesNoCheckValidSignature(Transaction transaction)
	{
		for (Signatory s : signatureMap(transaction).keySet())
			deleteSignatureNoCheckValidSignature(transaction, s);
	}

	public boolean isSignedDependencies()
	{
		return getEntity().isSignedDependencies();
	}

	private void setSignedDependencies(boolean signedDependencies)
	{
		getEntity().setSignedDependencies(signedDependencies);
	}

	private void setSignedDependencies(Transaction transaction, boolean signedDependencies)
	{
		setSignedDependencies(signedDependencies);
		persistenceUpdate(transaction);
	}

	private boolean calcSignedDependencies(Transaction transaction)
	{
		if (!isValidSignature())
			return false;
		Statement st = getStatement(transaction);
		for (Statement dep : st.dependencies(transaction))
		{
			StatementAuthority da = dep.getAuthority(transaction);
			if (da == null || !da.isSignedDependencies())
				return false;
		}
		return true;
	}

	private static void checkSignedDependenciesUuids(Transaction transaction, Collection<UUID> statementAuthorityUuids)
	{
		PersistenceManager persistenceManager = transaction.getPersistenceManager();
		int changes = 0;
		Set<UUID> enqueued = new HashSet<>();
		enqueued.addAll(statementAuthorityUuids);
		Queue<UUID> queue = new ArrayDeque<>(enqueued);
		while (!queue.isEmpty())
		{
			logger.trace("--> checkSignedDependencies:" + queue.size() + " " + changes);
			UUID uuid = queue.poll();
			enqueued.remove(uuid);
			StatementAuthority sa = persistenceManager.getStatementAuthority(transaction, uuid);
			if (sa != null)
			{
				boolean signedDependencies = sa.calcSignedDependencies(transaction);
				if (sa.isSignedDependencies() != signedDependencies)
				{
					changes++;
					sa.setSignedDependencies(transaction, signedDependencies);
					Iterable<StateListener> stateListeners = sa.stateListeners();
					synchronized (stateListeners)
					{
						for (StateListener sl : stateListeners)
							sl.signedDependenciesStateChanged(transaction, sa, signedDependencies);
					}
					Statement st = sa.getStatement(transaction);
					for (Statement st_ : st.dependents(transaction))
					{
						StatementAuthority sa_ = st_.getAuthority(transaction);

						if (sa_ != null && sa_.isSignedDependencies() != signedDependencies && !enqueued.contains(sa_.getStatementUuid()))
						{
							queue.add(sa_.getStatementUuid());
							enqueued.add(sa_.getStatementUuid());
						}
					}

				}
			}

		}

	}

	public static void checkSignedDependencies(Transaction transaction, Collection<StatementAuthority> statementAuthorities)
	{
		checkSignedDependenciesUuids(transaction, new BijectionCollection<>(new Bijection<StatementAuthority, UUID>()
		{

			@Override
			public UUID forward(StatementAuthority input)
			{
				return input.getStatementUuid();
			}

			@Override
			public StatementAuthority backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		}, statementAuthorities));

	}

	private void checkSignedDependencies(Transaction transaction)
	{
		StatementAuthority.checkSignedDependenciesUuids(transaction, Collections.singleton(getStatementUuid()));
	}

	public boolean isSignedProof()
	{
		return getEntity().isSignedProof();
	}

	private void setSignedProof(boolean signedProof)
	{
		getEntity().setSignedProof(signedProof);
	}

	private void setSignedProof(Transaction transaction, boolean signedProof)
	{
		setSignedProof(signedProof);
		persistenceUpdate(transaction);
	}

	private boolean calcSignedProof(Transaction transaction)
	{
		if (!isSignedDependencies())
			return false;
		Statement st = getStatement(transaction);
		for (Statement dep : st.dependencies(transaction))
		{
			StatementAuthority da = dep.getAuthority(transaction);
			if (da == null || !da.isSignedProof())
				return false;
		}
		if (st instanceof Context)
		{
			Context ctx = (Context) st;
			boolean signedSolver = false;
			CloseableIterator<Statement> it = ctx.solvers(transaction).iterator();
			try
			{
				while (it.hasNext())
				{
					Statement sol = it.next();
					StatementAuthority sola = sol.getAuthority(transaction);
					if (sola != null && sola.isSignedProof())
					{
						signedSolver = true;
						break;
					}
				}
			}
			finally
			{
				it.close();
			}
			if (!signedSolver)
				return false;
		}
		return true;
	}

	public static void checkSignedProof(Transaction transaction, Collection<StatementAuthority> statementAuthorities)
	{
		checkSignedProofUuids(transaction, new BijectionCollection<>(new Bijection<StatementAuthority, UUID>()
		{

			@Override
			public UUID forward(StatementAuthority input)
			{
				return input.getStatementUuid();
			}

			@Override
			public StatementAuthority backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		}, statementAuthorities));

	}

	public static void checkSignedProofUuids(Transaction transaction, Collection<UUID> statementAuthorityUuids)
	{
		PersistenceManager persistenceManager = transaction.getPersistenceManager();
		int changes = 0;
		Set<UUID> enqueued = new HashSet<>();
		enqueued.addAll(statementAuthorityUuids);
		Queue<UUID> queue = new ArrayDeque<>(enqueued);
		while (!queue.isEmpty())
		{
			logger.trace("--> checkSignedProof:" + queue.size() + " " + changes);
			UUID uuid = queue.poll();
			enqueued.remove(uuid);
			StatementAuthority sa = persistenceManager.getStatementAuthority(transaction, uuid);
			if (sa != null)
			{
				boolean signedProof = sa.calcSignedProof(transaction);
				if (sa.isSignedProof() != signedProof)
				{
					changes++;
					sa.setSignedProof(transaction, signedProof);
					Iterable<StateListener> stateListeners = sa.stateListeners();
					synchronized (stateListeners)
					{
						for (StateListener sl : stateListeners)
							sl.signedProofStateChanged(transaction, sa, signedProof);
					}
					Statement st = sa.getStatement(transaction);
					if (signedProof && !st.isProved())
						throw new RuntimeException();
					for (Statement st_ : st.dependents(transaction))
					{
						StatementAuthority sa_ = st_.getAuthority(transaction);

						if (sa_ != null && sa_.isSignedProof() != signedProof && !enqueued.contains(sa_.getStatementUuid()))
						{
							queue.add(sa_.getStatementUuid());
							enqueued.add(sa_.getStatementUuid());
						}
					}

					if (!(st instanceof RootContext))
					{
						for (Context ctx : st.getContext(transaction).descendantContextsByConsequent(transaction, st.getTerm()))
						{

							StatementAuthority sa_ = ctx.getAuthority(transaction);

							if (sa_ != null && sa_.isSignedProof() != signedProof && !enqueued.contains(sa_.getStatementUuid()))
							{
								queue.add(sa_.getStatementUuid());
								enqueued.add(sa_.getStatementUuid());
							}
						}
					}

				}
			}

		}

	}

	private void checkSignedProof(Transaction transaction)
	{
		StatementAuthority.checkSignedProofUuids(transaction, Collections.singleton(getStatementUuid()));
	}

	public LocalStatementAuthoritySet localAuthoritiesSet(Transaction transaction)
	{
		return persistenceManager.localStatementAuthoritySet(transaction, this);
	}

	public CloseableSet<StatementAuthority> dependentAuthorities(Transaction transaction)
	{
		return getStatement(transaction).dependentAuthorities(transaction);
	}

	private Set<UUID> resetSignedProof(Transaction transaction)
	{
		Set<UUID> set = new HashSet<>();
		Stack<StatementAuthority> stack = new Stack<>();
		stack.push(this);
		while (!stack.isEmpty())
		{
			logger.trace("---> resetSignedProof:" + stack.size() + " " + set.size());
			StatementAuthority stAuth = stack.pop();
			if (stAuth.isSignedProof() && !set.contains(stAuth.getStatementUuid()))
			{
				stAuth.setSignedProof(transaction, false);
				Iterable<StateListener> stateListeners = stAuth.stateListeners();
				synchronized (stateListeners)
				{
					for (StateListener listener : stateListeners)
						listener.signedProofStateChanged(transaction, stAuth, false);
				}

				set.add(stAuth.getStatementUuid());
				Statement st = stAuth.getStatement(transaction);
				stack.addAll(st.dependentAuthorities(transaction));
				Context stCtx = st instanceof RootContext ? (RootContext) st : st.getContext(transaction);
				stack.addAll(safelySignedProofDescendantContextAuthoritiesToResetByTerm(transaction, stCtx, st.getTerm()));
			}
		}
		return set;
	}

	private static CloseableSet<StatementAuthority> safelySignedProofDescendantContextAuthoritiesToResetByTerm(Transaction transaction, Context context,
			Term term)
	{
		return new FilteredCloseableSet<>(new NotNullFilter<StatementAuthority>(), new BijectionCloseableSet<>(new Bijection<Context, StatementAuthority>()
		{
			@Override
			public StatementAuthority forward(Context context)
			{
				return context.getAuthority(transaction);
			}

			@Override
			public Context backward(StatementAuthority output)
			{
				throw new UnsupportedOperationException();
			}
		}, safelySignedProofDescendantContextsToResetByTerm(transaction, context, term)));

	}

	/**
	 * Returns the set of descendant contexts of a context that match the given
	 * consequent to propagate the resetting of the signed proof status. If the
	 * size of that set is found too big (>=100) we first look for a safe
	 * alternative in the same context that might satisfy all that subcontexts
	 * and if so, no resetting will be necessary so the empty set is returned.
	 */
	private static DescendantContextsByConsequent safelySignedProofDescendantContextsToResetByTerm(Transaction transaction, Context context, Term term)
	{
		DescendantContextsByConsequent descendants = context.descendantContextsByConsequent(transaction, term);
		if (!descendants.smaller(70))
		{
			boolean safe = false;
			CloseableIterator<Statement> iterator = context.statementsByTerm(transaction).get(term).iterator();
			try
			{
				while (iterator.hasNext())
				{
					Statement alt = iterator.next();
					if (checkSafelySignedProof(alt, transaction))
					{
						safe = true;
						break;
					}
				}
			}
			finally
			{
				iterator.close();
			}
			if (safe)
				return new DescendantContextsByConsequent.Empty(transaction, context);
			else
				return descendants;
		}
		else
			return descendants;
	}

	/**
	 * A statement "safely" has a signed proof when:
	 * 
	 * *) Its signed proof status is true.
	 * 
	 * *) All its dependents safely have a signed proof.
	 * 
	 * *) If a context, at least one of its solvers is a strict descendant and
	 * safely have a signed proof or is out of the initial considered
	 * statement's context.
	 * 
	 * (Warning: This function might return some false negatives according to
	 * the previous definition but no false positives)
	 * 
	 */
	private static boolean checkSafelySignedProof(Statement statement, Transaction transaction)
	{
		Context context = statement.getContext(transaction);
		Set<UUID> visited = new HashSet<>();
		Stack<Statement> stack = new Stack<>();
		stack.push(statement);
		while (!stack.isEmpty())
		{
			Statement st = stack.pop();
			if (!visited.contains(st.getUuid()))
			{
				visited.add(st.getUuid());
				StatementAuthority stAuth = st.getAuthority(transaction);
				if (stAuth == null || !stAuth.isSignedProof())
					return false;
				if (context.isDescendent(transaction, st))
				{
					if (st instanceof Context)
					{
						Context ctx = (Context) st;
						boolean solver = false;
						CloseableIterator<Statement> iterator = ctx.solvers(transaction).iterator();
						try
						{
							while (iterator.hasNext())
							{
								Statement sol = iterator.next();
								if ((!ctx.equals(sol) && ctx.isDescendent(transaction, sol)) || !context.isDescendent(transaction, sol))
								{
									stack.push(sol);
									solver = true;
									break;
								}
							}
						}
						finally
						{
							iterator.close();
						}
						if (!solver)
							return false;
					}
					stack.addAll(st.dependencies(transaction));
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		return getStatementUuid().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof StatementAuthority))
			return false;
		StatementAuthority other = (StatementAuthority) obj;
		return getStatementUuid().equals(other.getStatementUuid());
	}

	public class DependentUnpackedSignatureRequests extends AuthorityException
	{
		private static final long serialVersionUID = -6858023217961551438L;

		private DependentUnpackedSignatureRequests()
		{
			super("There are unpacked signature requests depending on this authority");
		}
	}

	public void deleteNoCheckSignedProof(Transaction transaction) throws DependentUnpackedSignatureRequests
	{
		if (!dependentUnpackedSignatureRequests(transaction).isEmpty())
			throw new DependentUnpackedSignatureRequests();
		deleteDelegateTree(transaction);
		clearSignaturesNoCheckValidSignature(transaction);
		persistenceManager.deleteStatementAuthority(transaction, getStatementUuid());
		checkAuthorOrphanityDelete(transaction);
		Iterable<Statement.StateListener> listeners = statementStateListeners();
		synchronized (listeners)
		{
			Statement statement = null;
			boolean first = true;
			for (Statement.StateListener listener : statementStateListeners())
			{
				if (first)
				{
					statement = getStatement(transaction);
					first = false;
				}
				listener.statementAuthorityDeleted(transaction, statement, this);
			}
		}
	}

	public void delete(Transaction transaction) throws DependentUnpackedSignatureRequests
	{
		if (!dependentUnpackedSignatureRequests(transaction).isEmpty())
			throw new DependentUnpackedSignatureRequests();
		Set<UUID> reseted = resetSignedProof(transaction);
		Stack<StatementAuthority> stack = new Stack<>();
		stack.push(this);
		Stack<StatementAuthority> stack2 = new Stack<>();
		while (!stack.isEmpty())
		{
			logger.trace("---> deleting statement authority: " + stack.size());
			StatementAuthority stAuth = stack.peek();
			stack.addAll(stAuth.localAuthoritiesSet(transaction));
			while (!stack2.isEmpty() && stAuth.equals(stack2.peek()))
			{
				stAuth.deleteNoCheckSignedProof(transaction);
				stAuth = stack2.pop();
				stack.pop();
			}
			stack2.push(stAuth);
		}
		checkSignedDependencies(transaction, dependentAuthorities(transaction));
		checkSignedProofUuids(transaction, reseted);
	}

	public void removeFromDependentUnpackedSignatureRequests(Transaction transaction)
	{
		for (UnpackedSignatureRequest unpackedSignatureRequest : dependentUnpackedSignatureRequests(transaction))
			unpackedSignatureRequest.removeStatementAuthority(transaction, this);
	}

	public void deleteWithRemovalFromDependentUnpackedSignatureRequests(Transaction transaction)
	{
		removeFromDependentUnpackedSignatureRequests(transaction);
		try
		{
			delete(transaction);
		}
		catch (DependentUnpackedSignatureRequests e)
		{
			throw new Error(e);
		}
	}

	public static interface StateListener extends PersistenceListener
	{
		public default void validSignatureStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean validSignature)
		{
		}

		public default void signedDependenciesStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedDependencies)
		{
		}

		public default void signedProofStateChanged(Transaction transaction, StatementAuthority statementAuthority, boolean signedProof)
		{
		}

		public default void signatureAdded(Transaction transaction, StatementAuthority statementAuthority,
				StatementAuthoritySignature statementAuthoritySignature)
		{
		}

		public default void signatureDeleted(Transaction transaction, StatementAuthority statementAuthority,
				StatementAuthoritySignature statementAuthoritySignature)
		{
		}

		public default void delegateTreeChanged(Transaction transaction, StatementAuthority statementAuthority)
		{
		}

		public default void successorEntriesChanged(Transaction transaction, StatementAuthority statementAuthority)
		{
		}

		public default void delegateAuthorizerChanged(Transaction transaction, StatementAuthority statementAuthority, Namespace prefix, Person delegate,
				Signatory authorizer)
		{
		}
	}

	public void addStateListener(StateListener stateListener)
	{
		getPersistenceManager().getListenerManager().getStatementAuthorityStateListeners().add(getStatementUuid(), stateListener);
	}

	public void removeStateListener(StateListener stateListener)
	{
		getPersistenceManager().getListenerManager().getStatementAuthorityStateListeners().remove(getStatementUuid(), stateListener);
	}

	/**
	 * Return the set of listeners of this {@link StatementAuthority}.
	 *
	 * @return The set of listeners.
	 */
	protected Iterable<StateListener> stateListeners()
	{
		return getPersistenceManager().getListenerManager().getStatementAuthorityStateListeners().iterable(getStatementUuid());
	}

	public boolean clearSignedProof(Transaction transaction)
	{
		if (isSignedProof())
		{
			setSignedProof(transaction, false);
			Iterable<StateListener> listeners = stateListeners();
			synchronized (listeners)
			{
				for (StateListener listener : listeners)
					listener.signedProofStateChanged(transaction, this, isSignedProof());
			}
			return true;
		}
		else
			return false;
	}

	/**
	 * Returns an updated version of this very statement authority.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @return The updated statement authority.
	 */
	public StatementAuthority refresh(Transaction transaction)
	{
		return persistenceManager.getStatementAuthority(transaction, getStatementUuid());
	}

	public static enum SignatureStatus
	{
		NotValid("Not valid"), Valid("Valid"), Dependencies("Signed dependencies"), Proof("Signed proof"),;

		private final String text;

		private SignatureStatus(String text)
		{
			this.text = text;
		}

		public String getText()
		{
			return text;
		}
	}

	public SignatureStatus signatureStatus()
	{
		if (isSignedProof())
			return SignatureStatus.Proof;
		else if (isSignedDependencies())
			return SignatureStatus.Dependencies;
		else if (isValidSignature())
			return SignatureStatus.Valid;
		else
			return SignatureStatus.NotValid;
	}

	public UnpackedSignatureRequestSetByStatementAuthority dependentUnpackedSignatureRequests(Transaction transaction)
	{
		return getPersistenceManager().unpackedSignatureRequestSetByStatementAuthority(transaction, this);
	}

	public boolean lock(Transaction transaction)
	{
		return getPersistenceManager().lockStatementAuthority(transaction, this);
	}

	public void overwrite(Transaction transaction, Person author, Date creationDate)
	{
		UUID authorUuid = author.getUuid();
		if (getAuthorUuid().equals(authorUuid) && getCreationDate().equals(creationDate))
			return;
		setAuthorUuid(author.getUuid());
		setCreationDate(creationDate);
		persistenceUpdate(transaction);
		clearSignatures(transaction);
		deleteDelegateTree(transaction);
	}

	public CloseableIterable<Person> personDependencies(Transaction transaction)
	{
		Person author = getAuthor(transaction);
		CloseableIterable<Person> authorIterable = new TrivialCloseableIterable<>(Collections.singleton(author));
		DelegateTreeRootNode dtrn = getDelegateTreeRootNode(transaction);
		if (dtrn == null)
			return authorIterable;
		else
			return new CombinedCloseableIterable<>(authorIterable, dtrn.personDependencies(transaction));
	}

}
