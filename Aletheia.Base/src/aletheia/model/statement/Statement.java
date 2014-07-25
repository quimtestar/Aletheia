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
package aletheia.model.statement;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthority.AlreadyAuthoredStatementException;
import aletheia.model.authority.StatementAuthority.AuthorityCreationException;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.authority.StatementAuthority.DependentUnpackedSignatureRequests;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.local.StatementLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.nomenclator.Nomenclator.UnknownIdentifierException;
import aletheia.model.statement.Context.CantDeleteAssumptionException;
import aletheia.model.statement.Context.StatementHasDependentsException;
import aletheia.model.statement.Context.StatementNotInContextException;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.UnprojectException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.DependentsSet;
import aletheia.persistence.entities.statement.StatementEntity;
import aletheia.protocol.Exportable;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCloseableSet;
import aletheia.utilities.collections.FilteredSet;
import aletheia.utilities.collections.NotNullFilter;

/**
 * Abstract representation of a statement.
 *
 * <p>
 * A statement is a {@link IdentifiableVariableTerm} which type's term
 * represents a mathematical expression which might be found valid or not
 * according with the inference rules of the system.
 * </p>
 * <p>
 * It will be associated to a {@link PersistenceManager} and a
 * {@link StatementEntity}, which will manage the persistent state of the
 * statement.
 * </p>
 * <p>
 * Also, it will also be associated to a {@link Context}, which is itself a kind
 * of {@link Statement}. The statement-context relationship is i,mutable and
 * acyclic, so a context might be considered as an aggregation of statements.
 * Every statement is associated to a context except for a special statement,
 * namely the singleton {@link RootContext}. In other words, the full set of
 * statements conform a tree which every node is a {@link Context} and the root
 * is a {@link RootContext}.
 * </p>
 * <p>
 * A statement's term can't have projections pending to unproject.
 * </p>
 * <p>
 * Another important relation between statements is the <b>dependency</b>
 * relation. Informally, an statement depends on another one if its existence is
 * necessary to it to make sense. When the proven status changes on a statement,
 * it also might change on the statements that depend on it.
 * </p>
 *
 */
public abstract class Statement implements Exportable
{
	private static final Logger logger = LoggerManager.logger();

	private final PersistenceManager persistenceManager;
	private final StatementEntity entity;

	/**
	 * Any exception thrown during a statement operation extends from this.
	 *
	 */
	public static abstract class StatementException extends Exception
	{
		private static final long serialVersionUID = 1661670150538609097L;

		public StatementException()
		{
			super();
		}

		public StatementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
		{
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public StatementException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public StatementException(String message)
		{
			super(message);
		}

		public StatementException(Throwable cause)
		{
			super(cause);
		}

	}

	public class UndefinedVariableStatementException extends StatementException
	{
		private static final long serialVersionUID = 1516758744068889334L;

		private final Set<VariableTerm> undefined;
		private final Set<String> undefinedIds;

		public UndefinedVariableStatementException(Context context, Transaction transaction, Set<VariableTerm> undefined)
		{
			super();
			this.undefined = undefined;
			this.undefinedIds = new HashSet<String>();
			Map<IdentifiableVariableTerm, Identifier> var2id = context.variableToIdentifier(transaction);
			for (VariableTerm var : undefined)
			{
				Identifier id = var2id.get(var);
				if (id != null)
					undefinedIds.add(id.qualifiedName());
				else
					undefinedIds.add(var.toString());
			}

		}

		public Set<VariableTerm> getUndefined()
		{
			return undefined;
		}

		@Override
		public String getMessage()
		{
			return "Undefined variable(s): " + undefinedIds;
		}

	}

	public class FreeVariableNotIdentifiableStatementException extends StatementException
	{
		private static final long serialVersionUID = 5370054707607618960L;

		public FreeVariableNotIdentifiableStatementException()
		{
			super();
		}

		public FreeVariableNotIdentifiableStatementException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public FreeVariableNotIdentifiableStatementException(String message)
		{
			super(message);
		}

		public FreeVariableNotIdentifiableStatementException(Throwable cause)
		{
			super(cause);
		}

	}

	public class ProjectStatementException extends StatementException
	{
		private static final long serialVersionUID = 8843888002013040800L;

		public ProjectStatementException()
		{
			super();
		}

		public ProjectStatementException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ProjectStatementException(String message)
		{
			super(message);
		}

		public ProjectStatementException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Creates a new statement from scratch.
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction associated to the persistence manager that
	 *            will be used in the creation of this statement.
	 * @param entityClass
	 *            The type object of the persistent entity (the generic
	 *            interface, not the actual implementation of persistence) that
	 *            will be created for storing the persistent state of this
	 *            statement. Will depend on the actual subclass of
	 *            {@link Statement} that is actually being created.
	 * @param uuid
	 *            The UUID associated to this statement (i.e. the variable that
	 *            identifies this statement). Used as unique identifier of a
	 *            statement. If null, a new one will be generated.
	 * @param context
	 *            The context.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @throws StatementException
	 */
	protected Statement(PersistenceManager persistenceManager, Transaction transaction, Class<? extends StatementEntity> entityClass, UUID uuid,
			Context context, Term term) throws StatementException
	{
		super();
		Set<VariableTerm> undefined = (context == null ? term.freeVariables() : context.undefinedVariables(transaction, term));
		if (!undefined.isEmpty())
			throw new UndefinedVariableStatementException(context, transaction, undefined);
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateStatementEntity(entityClass);

		Term term_;
		try
		{
			term_ = term.unproject();
		}
		catch (UnprojectException e1)
		{
			throw new ProjectStatementException(e1);
		}
		IdentifiableVariableTerm variable = (uuid == null) ? new IdentifiableVariableTerm(term_) : new IdentifiableVariableTerm(term_, uuid);
		this.entity.setUuid(variable.getUuid());
		this.entity.setVariable(variable);
		this.entity.setProved(false);
		if (this instanceof RootContext)
			this.entity.setContextUuid(null);
		else
			this.entity.setContextUuid(context.getUuid());
		Set<UUID> uuidDependencies = this.entity.getUuidDependencies();
		try
		{
			for (IdentifiableVariableTerm v : getTerm().freeIdentifiableVariables())
				uuidDependencies.add(v.getUuid());
		}
		catch (ClassCastException e)
		{
			throw new FreeVariableNotIdentifiableStatementException(e);
		}
		if (context == null)
			this.entity.initializeContextData(null);
		else
			this.entity.initializeContextData(context.getEntity());
	}

	/**
	 * Calls to
	 * {@link #Statement(PersistenceManager, Transaction, Class, UUID, Context, Term)}
	 * with a null UUID.
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param entityClass
	 *            The type object of the persistent entity (the generic
	 *            interface, not the actual implementation of persistence) that
	 *            will be created for storing the persistent state of this
	 *            statement. Will depend on the actual subclass of
	 *            {@link Statement} that is actually being created.
	 * @param context
	 *            The context
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @throws StatementException
	 */
	protected Statement(PersistenceManager persistenceManager, Transaction transaction, Class<? extends StatementEntity> entityClass, Context context, Term term)
			throws StatementException
	{
		this(persistenceManager, transaction, entityClass, null, context, term);
	}

	/**
	 * Creates a statement that envelopes an already existing
	 * {@link StatementEntity}
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The persistence entity that will be enveloped in this
	 *            statement.
	 */
	protected Statement(PersistenceManager persistenceManager, StatementEntity entity)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	/**
	 * @return The persistence manager associated to this statement.
	 */
	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	/**
	 * @return The persistence entity associated to this statement.
	 */
	public StatementEntity getEntity()
	{
		return entity;
	}

	/**
	 * Saves the persistent state using the transaction.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 */
	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putStatement(transaction, this);
	}

	/**
	 * Returns an updated version of this very statement.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @return The updated statement.
	 */
	public Statement refresh(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getUuid());
	}

	public boolean persists(Transaction transaction)
	{
		return persistenceManager.getStatement(transaction, getUuid()) != null;
	}

	/**
	 * Checks the proof status of a set of statements. When the status of a
	 * statement is changed, the status of all the statements that depend on it
	 * must be checked too, so this method can trigger the checking of a huge
	 * set of statements.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param checkContext
	 *            If true, the checking can be propagated to a context which
	 *            consequent matches this statement. This flag must be set to
	 *            false when we are checking the status of the assumptions of a
	 *            context which we are creating (if not, that would result into
	 *            a infinite loop).
	 * @param statements
	 *            The collection of statements to be checked.
	 */
	protected static void checkProved(Transaction transaction, boolean checkContext, Collection<Statement> statements)
	{
		checkProvedUuids(transaction, checkContext, new BijectionCollection<>(new Bijection<Statement, UUID>()
		{

			@Override
			public UUID forward(Statement input)
			{
				return input.getUuid();
			}

			@Override
			public Statement backward(UUID output)
			{
				throw new UnsupportedOperationException();
			}
		}, statements));
	}

	/**
	 * Checks the proof status of a set of statements. When the status of a
	 * statement is changed, the status of all the statements that depend on it
	 * must be checked too, so this method can trigger the checking of a huge
	 * set of statements.
	 *
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @param checkContext
	 *            If true, the checking can be propagated to a context which
	 *            consequent matches this statement. This flag must be set to
	 *            false when we are checking the status of the assumptions of a
	 *            context which we are creating (if not, that would result into
	 *            a infinite loop).
	 * @param statementsUuids
	 *            The collection of statement to be checked uuid's.
	 */
	protected static void checkProvedUuids(Transaction transaction, boolean checkContext, Collection<UUID> statementUuids)
	{
		PersistenceManager persistenceManager = transaction.getPersistenceManager();
		int changes = 0;
		Set<UUID> enqueued = new HashSet<UUID>();
		enqueued.addAll(statementUuids);
		Queue<UUID> queue = new ArrayDeque<UUID>(enqueued);
		while (!queue.isEmpty())
		{
			logger.trace("--> checkProved:" + queue.size() + " " + changes);
			UUID uuid = queue.poll();
			enqueued.remove(uuid);
			Statement st = persistenceManager.getStatement(transaction, uuid);
			if (st != null)
			{
				boolean proved = st.calcProved(transaction);
				if (st.isProved() != proved)
				{
					changes++;
					st.setProved(transaction, proved);
					Iterable<StateListener> listeners = st.stateListeners();
					synchronized (listeners)
					{
						for (StateListener listener : listeners)
							listener.provedStateChanged(transaction, st, st.isProved());
					}
					for (Statement st_ : st.dependents(transaction))
					{
						if (st_.isProved() != proved && !enqueued.contains(st_.getUuid()))
						{
							queue.add(st_.getUuid());
							enqueued.add(st_.getUuid());
						}
					}
					if (checkContext && !(st instanceof RootContext))
					{
						for (Context ctx : st.getContext(transaction).descendantContextsByConsequent(transaction, st.getTerm()))
						{
							if (ctx.isProved() != proved && !enqueued.contains(ctx.getUuid()))
							{
								queue.add(ctx.getUuid());
								enqueued.add(ctx.getUuid());
							}
						}
					}

				}
			}
		}
	}

	protected boolean clearProved(Transaction transaction)
	{
		if (isProved())
		{
			setProved(transaction, false);
			Iterable<StateListener> listeners = stateListeners();
			synchronized (listeners)
			{
				for (StateListener listener : listeners)
					listener.provedStateChanged(transaction, this, isProved());
			}
			return true;
		}
		else
			return false;
	}

	/**
	 * Equivalent to {@link #checkProved(Transaction, boolean, Collection)} with
	 * checkContext set to true.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The collection of statements to be checked.
	 *
	 * @see #checkProved(Transaction, boolean, Collection)
	 */
	protected static void checkProved(Transaction transaction, Collection<Statement> statements)
	{
		checkProved(transaction, true, statements);
	}

	/**
	 * Equivalent to {@link #checkProved(Transaction, boolean, Collection)} with
	 * statements set to the singleton set of <b>this</b> statement.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param checkContext
	 *            If true, the checking can be propagated to a context which
	 *            consequent matches this statement. This flag must be set to
	 *            false when we are checking the status of the assumptions of a
	 *            context which we are creating (if not, that would result into
	 *            a infinite loop).
	 */
	protected void checkProved(Transaction transaction, boolean checkContext)
	{
		checkProved(transaction, checkContext, Collections.singletonList(this));
	}

	/**
	 * Equivalent to {@link #checkProved(Transaction, boolean)} with
	 * checkContext set to true.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 */
	protected void checkProved(Transaction transaction)
	{
		checkProved(transaction, true);
	}

	/**
	 * @return The UUID of the parent context.
	 */
	public UUID getContextUuid()
	{
		return entity.getContextUuid();
	}

	/**
	 * @param transaction
	 *            The transaction to use in the operation.
	 * @return The context statement associated to this statement.
	 */
	public Context getContext(Transaction transaction)
	{
		return persistenceManager.getContext(transaction, getContextUuid());
	}

	public Nomenclator getParentNomenclator(Transaction transaction)
	{
		return getContext(transaction).getNomenclator(transaction);
	}

	/**
	 * @return The variable associated to this statement.
	 */
	public IdentifiableVariableTerm getVariable()
	{
		return entity.getVariable();
	}

	/**
	 * Creates a set of statements that depend on this statement.
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the set.
	 * @return The set.
	 *
	 * @see DependentsSet
	 */
	protected DependentsSet getDependents(Transaction transaction)
	{
		return persistenceManager.dependents(transaction, this);
	}

	/**
	 * The set of statements that depend on this statement.
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the set.
	 * @return The set.
	 *
	 * @see #getDependents(Transaction)
	 */
	public DependentsSet dependents(Transaction transaction)
	{
		return getDependents(transaction);
	}

	/**
	 * @return The term that this statement expresses, i.e.the type of the
	 *         variable associated to this statement.
	 */
	public Term getTerm()
	{
		return getVariable().getType();
	}

	/**
	 * @return The proven status of this statement (might be unsynchronized with
	 *         the persistence layer, if the actual present value is needed, the
	 *         method {@link #refresh(Transaction)} must be used).
	 */
	public boolean isProved()
	{
		return entity.isProved();
	}

	/**
	 * Sets the proven status of this statement AND saves it to the persistence
	 * layer.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param proved
	 *            The proven status value.
	 */
	protected void setProved(Transaction transaction, boolean proved)
	{
		entity.setProved(proved);
		persistenceUpdate(transaction);
	}

	/**
	 * Sets the {@link Identifier} of this statement in its context.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param identifier
	 *            The identifier.
	 */
	public void setIdentifier(Transaction transaction, Identifier identifier)
	{
		Identifier old = identifier(transaction);
		if ((old == null) != (identifier == null) || (!old.equals(identifier)))
		{
			StatementAuthority statementAuthority = getAuthority(transaction);
			if (statementAuthority != null)
				statementAuthority.clearSignatures(transaction);
			entity.setIdentifier(identifier);
			persistenceUpdate(transaction);
		}
	}

	/**
	 * Calculates the proved status based on (the conjunction of) the statuses
	 * of statements associated to the free variables of the term. Must be
	 * overridden by the subclasses to add the specific logic on this
	 * computation. This method doesn't set the proven flag of this class.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The proven status
	 */
	protected boolean calcProved(Transaction transaction)
	{
		for (VariableTerm var : getTerm().freeVariables())
		{
			Statement st = getPersistenceManager().statements(transaction).get(var);
			if (!st.isProved())
				return false;
		}
		return true;
	}

	public Set<UUID> getUuidDependencies()
	{
		return entity.getUuidDependencies();
	}

	/**
	 * Returns the set of statements on which this statement depends. This is
	 * the opposite relation of {@link #dependents(Transaction)}.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The set.
	 */
	public Set<Statement> dependencies(final Transaction transaction)
	{
		return new BijectionSet<UUID, Statement>(new Bijection<UUID, Statement>()
		{

			@Override
			public Statement forward(UUID uuid)
			{
				return persistenceManager.getStatement(transaction, uuid);
			}

			@Override
			public UUID backward(Statement statement)
			{
				return statement.getUuid();
			}
		}, getUuidDependencies());
	}

	public Set<Statement> localDependencies(Transaction transaction)
	{
		return new FilteredSet<Statement>(new Filter<Statement>()
		{
			@Override
			public boolean filter(Statement statement)
			{
				return getContextUuid().equals(statement.getContextUuid());
			}
		}, dependencies(transaction));
	}

	public Set<Statement> dependenciesThisAndDescendents(Transaction transaction)
	{
		return dependencies(transaction);
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The {@link Context#variableToIdentifier(Transaction)} map of the
	 *         context of this statement.
	 */
	public Map<IdentifiableVariableTerm, Identifier> parentVariableToIdentifier(Transaction transaction)
	{
		return getParentNomenclator(transaction).variableToIdentifier();
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The {@link Context#statementToIdentifier(Transaction)} map of the
	 *         context of this statement.
	 */
	public Map<Statement, Identifier> parentStatementToIdentifier(Transaction transaction)
	{
		return getParentNomenclator(transaction).statementToIdentifier();
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The {@link Context#localStatementToIdentifier(Transaction)} map
	 *         of the context of this statement.
	 */
	public Map<Statement, Identifier> parentLocalStatementToIdentifier(Transaction transaction)
	{
		return getContext(transaction).localStatementToIdentifier(transaction);
	}

	public Identifier getIdentifier()
	{
		return getEntity().getIdentifier();
	}

	public Namespace prefix()
	{
		return Namespace.identifierToPrefix(getIdentifier());
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The identifier associated to this statement on the parent
	 *         context.
	 */
	public Identifier identifier(Transaction transaction)
	{
		Statement st = refresh(transaction);
		if (st == null)
			return null;
		return st.getIdentifier();
	}

	public Namespace prefix(Transaction transaction)
	{
		return Namespace.identifierToPrefix(identifier(transaction));
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The list of statements you need to traverse to reach this
	 *         statement from the {@link RootContext} via the context-statement
	 *         relation.
	 */
	public List<? extends Statement> statementPath(Transaction transaction)
	{
		return statementPath(transaction, null);
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return A string representation of the
	 *         {@link #statementPath(Transaction)}
	 */
	public String statementPathString(Transaction transaction)
	{
		return statementPathString(transaction, null);
	}

	/**
	 * Same as {@link #statementPath(Transaction)}, but relative to a specified
	 * context. i.e. the list of stataments you need to traverse to reach this
	 * statement from the specified context to this statement. If this statement
	 * is not a descendent of that context, the absolute path is returned.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param from
	 *            The context relative to which the path will be computed.
	 * @return The list.
	 */
	public List<? extends Statement> statementPath(Transaction transaction, Context from)
	{
		Set<Statement> set = new HashSet<Statement>();
		if (from != null)
			set.addAll(from.statementPath(transaction));
		LinkedList<Statement> path = new LinkedList<Statement>();
		Statement st = this;
		while (!set.contains(st))
		{
			path.addFirst(st);
			if (st instanceof RootContext)
				break;
			st = st.getContext(transaction);
		}
		return Collections.unmodifiableList(path);
	}

	/**
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param from
	 *            The context relative to which the path will be computed.
	 * @return A string representation of the
	 *         {@link #statementPath(Transaction, Context)}
	 */
	public String statementPathString(Transaction transaction, Context from)
	{
		List<? extends Statement> path = statementPath(transaction, from);
		StringBuffer pathString = new StringBuffer();
		boolean first = true;
		for (Statement st : path)
		{
			if (!first || st instanceof RootContext)
				pathString.append("/");
			first = false;
			Identifier id = st.identifier(transaction);
			if (id != null)
				pathString.append(id.toString());
			else
				pathString.append(st.getVariable().toString());
		}
		return pathString.toString();
	}

	/**
	 * Calls to {@link #toString(Transaction)} using a persistence's dirty
	 * transaction created ad hoc for this operation.
	 *
	 * @see PersistenceManager#beginDirtyTransaction()
	 */
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

	/**
	 * A string representation of this statement. Calls to
	 * {@link #toString(Map)} with the
	 * {@link #parentVariableToIdentifier(Transaction)} of this statement.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The string.
	 */
	public String toString(Transaction transaction)
	{
		return toString(parentVariableToIdentifier(transaction));
	}

	/**
	 * A string representation of this statement using a specific map between
	 * variables and identifiers.
	 *
	 * @param variableToIdentifier
	 *            The map to use in the construction of the representation.
	 * @return The string.
	 */
	private String toString(Map<IdentifiableVariableTerm, Identifier> variableToIdentifier)
	{
		return getVariable().toString(variableToIdentifier) + ":" + getTerm().toString(variableToIdentifier) + " [proved:" + isProved() + "]";
	}

	protected void trace(Transaction transaction, PrintStream out, String indent)
	{
		out.println(indent + toString());
	}

	public void trace(Transaction transaction, PrintStream out)
	{
		trace(transaction, out, "");
	}

	/**
	 * Listener interface for keeping track of the state changes on a statement.
	 * Any object implementing this interface and registered as a listener of
	 * this statement will receive update notifications. A simple observer
	 * pattern is implemented.
	 */
	public interface StateListener extends PersistenceListener
	{
		/**
		 * The statement's proven status has changed. This listener must be
		 * registered on that statement's listeners poll.
		 *
		 * @param transaction
		 *            The transaction under which the modification has been
		 *            done. Possibly the data change is not visible outside of
		 *            this transaction if it hasn't been commited.
		 * @param statement
		 *            The statement that has been proven or unproven.
		 * @param proved
		 *            The new proven status.
		 *
		 * @see Statement#addStateListener(StateListener)
		 */
		public void provedStateChanged(Transaction transaction, Statement statement, boolean proved);

		/**
		 * A statement has been added to a listened context.
		 *
		 * @param transaction
		 *            The transaction under which the modification has been
		 *            done. Possibly the data change is not visible outside of
		 *            this transaction if it hasn't been commited.
		 * @param context
		 *            The context on which a statement has been added.
		 * @param statement
		 *            The added statement
		 */
		public void statementAddedToContext(Transaction transaction, Context context, Statement statement);

		/**
		 * A statement has been deleted from a listened context.
		 *
		 * @param transaction
		 *            The transaction under which the modification has been
		 *            done. Possibly the data change is not visible outside of
		 *            this transaction if it hasn't been commited.
		 * @param context
		 *            The context on which a statement has been deleted.
		 * @param statement
		 *            The deleted statement.
		 * @param identifier
		 *            The identifier that the deleted statement had on this
		 *            context, or null if it was an unidentified statement.
		 */
		public void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier);

		/**
		 * @param transaction
		 * @param statement
		 * @param statementAuthority
		 */
		public void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority);

		public void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority);
	}

	/**
	 * Register a listener to this statement.
	 *
	 * @param listener
	 *            The listener to register.
	 */
	public void addStateListener(StateListener listener)
	{
		persistenceManager.getListenerManager().getStatementStateListeners().add(getUuid(), listener);
	}

	/**
	 * Unregister a listener from this statement.
	 *
	 * @param listener
	 *            The listener to unregister.
	 */
	public void removeStateListener(StateListener listener)
	{
		persistenceManager.getListenerManager().getStatementStateListeners().remove(getUuid(), listener);
	}

	public void addAuthorityStateListener(StatementAuthority.StateListener listener)
	{
		persistenceManager.getListenerManager().getStatementAuthorityStateListeners().add(getUuid(), listener);
	}

	public void removeAuthorityStateListener(StatementAuthority.StateListener listener)
	{
		persistenceManager.getListenerManager().getStatementAuthorityStateListeners().remove(getUuid(), listener);
	}

	/**
	 * Return the set of listeners of this statement.
	 *
	 * @return The set of listeners.
	 */
	protected Iterable<StateListener> stateListeners()
	{
		return persistenceManager.getListenerManager().getStatementStateListeners().iterable(getUuid());
	}

	/**
	 * The UUID of the variable assigned to this statement. Identifies uniquely
	 * this statement.
	 *
	 * @return The UUID.
	 */
	public UUID getUuid()
	{
		return getVariable().getUuid();
	}

	/**
	 * The hashcode of the variable is used as a hashcode of the statement.
	 */
	@Override
	public int hashCode()
	{
		return getVariable().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Statement other = (Statement) obj;
		return getVariable().equals(other.getVariable());
	}

	public StatementAuthority createAuthorityOverwrite(Transaction transaction, Person author, Date creationDate) throws AuthorityWithNoParentException
	{
		while (true)
		{
			if (lockAuthority(transaction))
			{
				StatementAuthority statementAuthority = getAuthority(transaction);
				statementAuthority.overwrite(transaction, author, creationDate);
				return statementAuthority;
			}
			else
			{
				try
				{
					return createAuthority(transaction, author, creationDate);
				}
				catch (AuthorityCreationException e)
				{
				}
			}

		}
	}

	private StatementAuthority createAuthority(Transaction transaction, Person author, Date creationDate) throws AuthorityCreationException
	{
		return StatementAuthority.create(persistenceManager, transaction, this, author, creationDate);
	}

	public StatementAuthority createAuthority(Transaction transaction, Person author) throws AuthorityCreationException
	{
		return StatementAuthority.create(persistenceManager, transaction, this, author);
	}

	public StatementAuthority getOrCreateAuthority(Transaction transaction, Person author) throws AuthorityWithNoParentException
	{
		while (true)
		{
			StatementAuthority statementAuthority = getAuthority(transaction);
			if (statementAuthority != null)
				return statementAuthority;
			try
			{
				return createAuthority(transaction, author);
			}
			catch (AlreadyAuthoredStatementException e)
			{
			}
			catch (AuthorityWithNoParentException e)
			{
				throw e;
			}
			catch (AuthorityCreationException e)
			{
				throw new Error(e);
			}
		}
	}

	public StatementAuthority getAuthority(Transaction transaction)
	{
		return persistenceManager.getStatementAuthority(transaction, getUuid());
	}

	public boolean lockAuthority(Transaction transaction)
	{
		return persistenceManager.lockStatementAuthority(transaction, getUuid());
	}

	protected void deleteAuthorityNoCheckSignedProof(Transaction transaction) throws DependentUnpackedSignatureRequests
	{
		StatementAuthority old = getAuthority(transaction);
		if (old != null)
			old.deleteNoCheckSignedProof(transaction);
	}

	public void deleteAuthority(Transaction transaction) throws DependentUnpackedSignatureRequests
	{
		StatementAuthority old = getAuthority(transaction);
		if (old != null)
			old.delete(transaction);
	}

	protected StatementLocal createLocal(Transaction transaction)
	{
		if (!(this instanceof RootContext))
			getContext(transaction).getOrCreateLocal(transaction);
		return StatementLocal.create(persistenceManager, transaction, this);
	}

	public StatementLocal getOrCreateLocal(Transaction transaction)
	{
		StatementLocal statementLocal = getLocal(transaction);
		if (statementLocal == null)
			statementLocal = createLocal(transaction);
		return statementLocal;
	}

	public StatementLocal getLocal(Transaction transaction)
	{
		return persistenceManager.getStatementLocal(transaction, getUuid());
	}

	public void deleteLocal(Transaction transaction)
	{
		StatementLocal statementLocal = getLocal(transaction);
		if (statementLocal != null)
			statementLocal.delete(transaction);
	}

	public boolean isValidSignature(Transaction transaction)
	{
		StatementAuthority authority = getAuthority(transaction);
		if (authority == null)
			return false;
		return authority.isValidSignature();
	}

	public CloseableSet<StatementAuthority> dependentAuthorities(final Transaction transaction)
	{
		return new FilteredCloseableSet<>(new NotNullFilter<StatementAuthority>(), new BijectionCloseableSet<>(new Bijection<Statement, StatementAuthority>()
		{
			@Override
			public StatementAuthority forward(Statement statement)
			{
				return statement.getAuthority(transaction);
			}

			@Override
			public Statement backward(StatementAuthority output)
			{
				throw new UnsupportedOperationException();
			}
		}, dependents(transaction)));
	}

	public void identify(Transaction transaction, Identifier identifier) throws NomenclatorException
	{
		getParentNomenclator(transaction).identifyStatement(identifier, this);
	}

	public void unidentify(Transaction transaction)
	{
		try
		{
			Identifier identifier = identifier(transaction);
			if (identifier != null)
				getParentNomenclator(transaction).unidentifyStatement(identifier);
		}
		catch (UnknownIdentifierException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void delete(Transaction transaction) throws StatementNotInContextException, StatementHasDependentsException, CantDeleteAssumptionException,
			DependentUnpackedSignatureRequests
	{
		getContext(transaction).deleteStatement(transaction, this);
	}

	public void deleteWithRemovalFromDependentUnpackedSignatureRequests(Transaction transaction) throws StatementNotInContextException,
			StatementHasDependentsException, CantDeleteAssumptionException
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

	public void deleteCascade(Transaction transaction) throws StatementNotInContextException
	{
		getContext(transaction).deleteStatementCascade(transaction, this);
	}

	public RootContext rootContext(Transaction transaction)
	{
		Statement statement = this;
		while (!(statement instanceof RootContext))
			statement = statement.getContext(transaction);
		return (RootContext) statement;
	}

	/**
	 * Rebuild the proven statuses of all the descendants of this context (and
	 * the statements that depend on them).
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 */
	public void rebuildProved(Transaction transaction)
	{
		Stack<Statement> stack = new Stack<Statement>();
		stack.push(this);
		Set<UUID> visited = new HashSet<UUID>();
		Collection<UUID> statements = new ArrayList<UUID>();
		Collection<UUID> statementAuthorities = new ArrayList<UUID>();
		int changes = 0;
		while (!stack.isEmpty())
		{
			Statement st = stack.pop();
			if (!visited.contains(st.getUuid()))
			{
				visited.add(st.getUuid());
				Stack<Statement> stack2 = new Stack<Statement>();
				stack2.add(st);
				while (!stack2.isEmpty())
				{
					Statement st_ = stack2.pop();
					st_.clearProved(transaction);
					changes++;
					logger.debug("--> clearProved:" + changes);
					statements.add(st_.getUuid());
					StatementAuthority stAuth = st_.getAuthority(transaction);
					if (stAuth != null)
					{
						stAuth.clearSignedProof(transaction);
						statementAuthorities.add(stAuth.getStatementUuid());
					}
					if (st_ instanceof Context)
						stack2.addAll(((Context) st_).localStatements(transaction).values());
				}
				stack.addAll(st.dependents(transaction));
				if (!(st instanceof RootContext))
					stack.addAll(st.getContext(transaction).descendantContextsByConsequent(transaction, st.getTerm()));
			}
		}
		checkProvedUuids(transaction, true, statements);
		StatementAuthority.checkSignedProofUuids(transaction, statementAuthorities);
	}

	public void removeFromDependentUnpackedSignatureRequests(Transaction transaction)
	{
		StatementAuthority statementAuthority = getAuthority(transaction);
		if (statementAuthority != null)
			statementAuthority.removeFromDependentUnpackedSignatureRequests(transaction);
	}

	public String hexRef()
	{
		return getVariable().hexRef();
	}

}
