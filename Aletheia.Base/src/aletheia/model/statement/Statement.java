/*******************************************************************************
 * Copyright (c) 2014, 2021 Quim Testar.
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.AuthorityException;
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
import aletheia.model.nomenclator.Nomenclator.AlreadyUsedIdentifierException;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.nomenclator.Nomenclator.SignatureIsValidNomenclatorException;
import aletheia.model.nomenclator.Nomenclator.UnknownIdentifierException;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context.CantDeleteAssumptionException;
import aletheia.model.statement.Context.StatementHasDependentsException;
import aletheia.model.statement.Context.StatementNotInContextException;
import aletheia.model.term.CastTypeTerm.CastTypeException;
import aletheia.model.term.FoldingCastTypeTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionCastTypeTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.model.term.Term.DomainParameterIdentification;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.Term.UnprojectTypeException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.DependentsSet;
import aletheia.persistence.collections.statement.SpecializationsByGeneral;
import aletheia.persistence.entities.statement.StatementEntity;
import aletheia.protocol.Exportable;
import aletheia.utilities.aborter.Aborter;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.collections.AbstractCloseableCollection;
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.BijectionKeyMap;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CombinedCollection;
import aletheia.utilities.collections.CombinedIterable;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCloseableCollection;
import aletheia.utilities.collections.FilteredCloseableSet;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.FilteredSet;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.ReverseList;
import aletheia.utilities.collections.TrivialCloseableCollection;
import aletheia.utilities.collections.UnionCloseableCollection;

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
	private static final Logger logger = LoggerManager.instance.logger();

	private final PersistenceManager persistenceManager;
	private final StatementEntity entity;

	/**
	 * Any exception thrown during a statement operation extends from this.
	 *
	 */
	public static abstract class StatementException extends Exception
	{
		private static final long serialVersionUID = 1661670150538609097L;

		protected StatementException()
		{
			super();
		}

		protected StatementException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected StatementException(String message)
		{
			super(message);
		}

		protected StatementException(Throwable cause)
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
			this.undefinedIds = new HashSet<>();
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

	public class NonCastFreeStatementException extends StatementException
	{
		private static final long serialVersionUID = 2233812895735149173L;

		protected NonCastFreeStatementException()
		{
			super("Term is not cast-free");
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
		if (!term.castFree())
			throw new NonCastFreeStatementException();
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
		catch (UnprojectTypeException e1)
		{
			throw new ProjectStatementException(e1);
		}
		if (uuid == null)
			uuid = UUID.randomUUID();
		IdentifiableVariableTerm variable = new IdentifiableVariableTerm(term_, uuid);
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

	public boolean lock(Transaction transaction)
	{
		return getPersistenceManager().lockStatement(transaction, this);
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
		Set<UUID> enqueued = new HashSet<>();
		enqueued.addAll(statementUuids);
		Queue<UUID> queue = new ArrayDeque<>(enqueued);
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

	protected static void checkProvedUuids(Transaction transaction, Collection<UUID> statementUuids)
	{
		checkProvedUuids(transaction, true, statementUuids);
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
	 * Checks if any of the statements is proved by iteratively checking
	 * dependents and solvers and ignoring any statement true valued proved
	 * status flag.
	 */
	protected static boolean checkProvedIgnoringTrueProvedFlag(Transaction transaction, Collection<Statement> statements, int maxTreeSize)
	{
		return checkProvedIgnoringTrueProvedFlagUuids(transaction, statements.stream().map(Statement::getUuid).collect(Collectors.toList()), maxTreeSize);
	}

	/**
	 * Checks if any of the statementUuids identifies a statement that is proved
	 * by iteratively checking dependents and solvers and ignoring any statement
	 * true valued proved status flag.
	 */
	protected static boolean checkProvedIgnoringTrueProvedFlagUuids(Transaction transaction, Collection<UUID> statementUuids, int maxTreeSize)
	{
		PersistenceManager persistenceManager = transaction.getPersistenceManager();
		Stack<UUID> stack = new Stack<>();
		Map<UUID, Set<UUID>> dependencies = new HashMap<>();
		Map<UUID, Set<UUID>> solvers = new HashMap<>();
		Map<UUID, Set<UUID>> dependents = new HashMap<>();
		stack.addAll(statementUuids);
		while (!stack.isEmpty())
		{
			UUID uuid = stack.pop();
			if (!dependencies.containsKey(uuid))
			{
				if (dependencies.size() >= maxTreeSize)
					return false;
				Statement st = persistenceManager.getStatement(transaction, uuid);
				if (st.isProved())
				{
					dependencies.put(uuid, st.getUuidDependencies());
					stack.addAll(st.getUuidDependencies());
					Iterable<UUID> revertIterable = st.getUuidDependencies();
					if (st instanceof Context)
					{
						Set<UUID> sols = ((Context) st).solvers(transaction).stream().map(Statement::getUuid).collect(Collectors.toSet());
						solvers.put(uuid, sols);
						stack.addAll(sols);
						revertIterable = new CombinedIterable<>(sols, revertIterable);
					}
					for (UUID d : revertIterable)
					{
						Set<UUID> s = dependents.get(d);
						if (s == null)
						{
							s = new HashSet<>();
							dependents.put(d, s);
						}
						s.add(uuid);
					}
				}
			}
		}
		stack.addAll(dependencies.keySet());
		Set<UUID> proved = new HashSet<>();
		while (!stack.isEmpty())
		{
			UUID uuid = stack.pop();
			if (!proved.contains(uuid) && proved.containsAll(dependencies.get(uuid)))
			{
				Set<UUID> sols = solvers.get(uuid);
				if (sols == null || !Collections.disjoint(proved, sols))
				{
					if (statementUuids.contains(uuid))
						return true;
					proved.add(uuid);
					stack.addAll(dependents.getOrDefault(uuid, Collections.emptySet()));
				}
			}
		}
		return false;
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
		Context ctx = getContext(transaction);
		if (ctx == null)
			return null;
		return ctx.getNomenclator(transaction);
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

	public Term getInnerTerm(Transaction transaction)
	{
		return getTerm();
	}

	public ParameterIdentification getTermParameterIdentification()
	{
		return getEntity().getTermParameterIdentification();
	}

	public ParameterIdentification termParameterIdentification(Transaction transaction)
	{
		Statement st = refresh(transaction);
		if (st == null)
			return null;
		return st.getTermParameterIdentification();
	}

	private void setTermParameterIdentification(ParameterIdentification termParameterIdentification)
	{
		getEntity().setTermParameterIdentification(termParameterIdentification);
	}

	protected void setTermParameterIdentification(Transaction transaction, ParameterIdentification termParameterIdentification)
	{
		setTermParameterIdentification(termParameterIdentification);
		persistenceUpdate(transaction);
		Iterable<StateListener> listeners = stateListeners();
		synchronized (listeners)
		{
			for (StateListener listener : listeners)
				listener.termParameterIdentificationUpdated(transaction, this, termParameterIdentification);
		}
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

	public boolean proved(Transaction transaction)
	{
		Statement st = refresh(transaction);
		if (st == null)
			return false;
		else
			return st.isProved();
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
	 * @throws SignatureIsValidException
	 */
	public void setIdentifier(Transaction transaction, Identifier identifier) throws SignatureIsValidException
	{
		setIdentifier(transaction, identifier, false);
	}

	/**
	 * Sets the {@link Identifier} of this statement in its context.
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param identifier
	 *            The identifier.
	 * @param force
	 *            If true, don't check the existence of valid signatures.
	 * @throws SignatureIsValidException
	 */
	public void setIdentifier(Transaction transaction, Identifier identifier, boolean force) throws SignatureIsValidException
	{
		Identifier old = identifier(transaction);
		if ((old == null) != (identifier == null) || (old != null && !old.equals(identifier)))
		{
			lockAuthority(transaction);
			StatementAuthority statementAuthority = getAuthority(transaction);
			if (statementAuthority != null)
			{
				if (force)
					statementAuthority.clearSignatures(transaction);
				else if (statementAuthority.isValidSignature())
					throw new SignatureIsValidException("Can't rename statement with valid signatures");
			}
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
		return new BijectionSet<>(new Bijection<UUID, Statement>()
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
		return new FilteredSet<>(new Filter<Statement>()
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
		Set<Statement> set = new HashSet<>();
		if (from != null)
			set.addAll(from.statementPath(transaction));
		LinkedList<Statement> path = new LinkedList<>();
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
	 * Compares this statement's path to another one for sorting. Assumptions
	 * first.
	 */
	public int pathCompare(Transaction transaction, Statement st)
	{
		Iterator<? extends Statement> i1 = statementPath(transaction).iterator();
		Iterator<? extends Statement> i2 = st.statementPath(transaction).iterator();
		while (i1.hasNext() && i2.hasNext())
		{
			int c;
			Statement cp1 = i1.next();
			Statement cp2 = i2.next();
			if (!cp1.equals(cp2))
			{
				Function<Statement, Integer> ord = (Statement cp) -> (cp instanceof Assumption) ? ((Assumption) cp).getOrder() : Integer.MAX_VALUE;
				int ord1 = ord.apply(cp1);
				int ord2 = ord.apply(cp2);
				c = Integer.compare(ord1, ord2);
				if (c != 0)
					return c;
				Identifier id1 = cp1.getIdentifier();
				Identifier id2 = cp2.getIdentifier();
				c = Boolean.compare(id1 == null, id2 == null);
				if (c != 0)
					return c;
				if (id1 != null)
				{
					c = id1.compareTo(id2);
					if (c != 0)
						return c;
				}
			}
		}
		return Boolean.compare(i1.hasNext(), i2.hasNext());
	}

	public static Comparator<Statement> pathComparator(Transaction transaction)
	{
		class PathComparator implements Comparator<Statement>
		{
			private final Transaction transaction;

			private PathComparator(Transaction transaction)
			{
				this.transaction = transaction;
			}

			@Override
			public int compare(Statement st1, Statement st2)
			{
				return st1.pathCompare(transaction, st2);
			}

		}
		;
		return new PathComparator(transaction);
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
		return getUuid() + ":" + label();
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

	public String label()
	{
		return getIdentifier() != null ? getIdentifier().qualifiedName() : getVariable().hexRef();
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
		public default void provedStateChanged(Transaction transaction, Statement statement, boolean proved)
		{
		}

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
		public default void statementAddedToContext(Transaction transaction, Context context, Statement statement)
		{
		}

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
		public default void statementDeletedFromContext(Transaction transaction, Context context, Statement statement, Identifier identifier)
		{
		}

		public default void statementAuthorityCreated(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
		}

		public default void statementAuthorityDeleted(Transaction transaction, Statement statement, StatementAuthority statementAuthority)
		{
		}

		public default void termParameterIdentificationUpdated(Transaction transaction, Statement statement,
				ParameterIdentification termParameterIdentification)
		{
		}

		public default void valueParameterIdentificationUpdated(Transaction transaction, Declaration declaration,
				ParameterIdentification valueParameterIdentification)
		{
		}

		public default void instanceParameterIdentificationUpdated(Transaction transaction, Specialization specialization,
				ParameterIdentification instanceParameterIdentification)
		{
		}

		public default void consequentParameterIdentificationUpdated(Transaction transaction, Context context,
				ParameterIdentification consequentParameterIdentification)
		{
		}

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
		if ((obj == null) || (getClass() != obj.getClass()))
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
				catch (AuthorityWithNoParentException e)
				{
					throw e;
				}
				catch (AlreadyAuthoredStatementException e)
				{
				}
				catch (AuthorityCreationException e)
				{
					throw new Error(e);
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

	public class SignatureIsValidException extends AuthorityException
	{
		private static final long serialVersionUID = 6776102696391059941L;

		protected SignatureIsValidException(String message)
		{
			super(message);
		}

	}

	public void deleteAuthority(Transaction transaction) throws DependentUnpackedSignatureRequests, SignatureIsValidException
	{
		deleteAuthority(transaction, false);
	}

	public void deleteAuthorityForce(Transaction transaction) throws DependentUnpackedSignatureRequests
	{
		try
		{
			deleteAuthority(transaction, true);
		}
		catch (SignatureIsValidException e)
		{
			throw new Error(e);
		}
	}

	public void deleteAuthority(Transaction transaction, boolean force) throws DependentUnpackedSignatureRequests, SignatureIsValidException
	{
		StatementAuthority old = getAuthority(transaction);
		if (old != null)
		{
			if (!force && old.isValidSignature())
				throw new SignatureIsValidException("Can't delete statement with valid signatures");
			old.delete(transaction);
		}
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

	public boolean isSubscribeProof(Transaction transaction)
	{
		StatementLocal statementLocal = getLocal(transaction);
		if (statementLocal == null)
			return false;
		return statementLocal.isSubscribeProof();
	}

	public boolean isValidSignature(Transaction transaction)
	{
		StatementAuthority authority = getAuthority(transaction);
		if (authority == null)
			return false;
		return authority.isValidSignature();
	}

	public boolean isSignedDependencies(Transaction transaction)
	{
		StatementAuthority authority = getAuthority(transaction);
		if (authority == null)
			return false;
		return authority.isSignedDependencies();
	}

	public boolean isSignedProof(Transaction transaction)
	{
		StatementAuthority authority = getAuthority(transaction);
		if (authority == null)
			return false;
		return authority.isSignedProof();
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
		identify(transaction, identifier, false);
	}

	public void identify(Transaction transaction, Identifier identifier, boolean force) throws NomenclatorException
	{
		getParentNomenclator(transaction).identifyStatement(identifier, this, force);
	}

	public Identifier unidentify(Transaction transaction) throws SignatureIsValidNomenclatorException
	{
		return unidentify(transaction, false);
	}

	public Identifier unidentify(Transaction transaction, boolean force) throws SignatureIsValidNomenclatorException
	{
		try
		{
			Identifier identifier = identifier(transaction);
			if (identifier != null)
				getParentNomenclator(transaction).unidentifyStatement(identifier, force);
			return identifier;
		}
		catch (UnknownIdentifierException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void delete(Transaction transaction) throws StatementNotInContextException, StatementHasDependentsException, CantDeleteAssumptionException,
			DependentUnpackedSignatureRequests, SignatureIsValidException
	{
		getContext(transaction).deleteStatement(transaction, this);
	}

	public void deleteWithRemovalFromDependentUnpackedSignatureRequests(Transaction transaction)
			throws StatementNotInContextException, StatementHasDependentsException, CantDeleteAssumptionException, SignatureIsValidException
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

	public void deleteCascade(Transaction transaction) throws StatementNotInContextException, SignatureIsValidException
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
		Stack<Statement> stack = new Stack<>();
		stack.push(this);
		Set<UUID> visited = new HashSet<>();
		Collection<UUID> statements = new ArrayList<>();
		Collection<UUID> statementAuthorities = new ArrayList<>();
		int changes = 0;
		while (!stack.isEmpty())
		{
			Statement st = stack.pop();
			if (!visited.contains(st.getUuid()))
			{
				visited.add(st.getUuid());
				Stack<Statement> stack2 = new Stack<>();
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

	public static CloseableCollection<Statement> dependencySortedStatements(final Transaction transaction,
			final CloseableCollection<? extends Statement> collection)
	{
		return new UnionCloseableCollection<>(new AbstractCloseableCollection<CloseableCollection<Statement>>()
		{
			@SuppressWarnings("unchecked")
			final Comparator<? super Statement> comparator = collection instanceof SortedSet ? ((SortedSet<? super Statement>) collection).comparator() : null;

			@Override
			public CloseableIterator<CloseableCollection<Statement>> iterator()
			{

				return new CloseableIterator<>()
				{
					final CloseableIterator<? extends Statement> iterator = collection.iterator();
					final Set<Statement> visited = new HashSet<>();

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public CloseableCollection<Statement> next()
					{
						Stack<Statement> stack = new Stack<>();
						stack.push(iterator.next());
						Stack<Statement> stack2 = new Stack<>();
						Map<Statement, List<Statement>> dependencyMap = new HashMap<>();
						while (!stack.isEmpty())
						{
							Statement st = stack.pop();
							stack2.push(st);
							List<Statement> list = dependencyMap.get(st);
							if (list == null)
							{
								Collection<Statement> deps = st.dependencies(transaction);
								if (!(st instanceof RootContext))
								{
									Collection<Statement> path = new AdaptedCollection<>(st.getContext(transaction).statementPath(transaction));
									deps = new CombinedCollection<>(deps, path);
								}
								list = new ArrayList<>(new FilteredCollection<>(new Filter<Statement>()
								{
									@Override
									public boolean filter(Statement e)
									{
										return !visited.contains(e) && collection.contains(e);
									}
								}, deps));

								if (comparator != null)
									Collections.sort(list, comparator);
								dependencyMap.put(st, list);
							}
							stack.addAll(list);
						}
						List<Statement> list = new ArrayList<>();
						while (!stack2.isEmpty())
						{
							Statement st = stack2.pop();
							if (!visited.contains(st))
							{
								visited.add(st);
								list.add(st);
							}
						}
						return new TrivialCloseableCollection<>(list);
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					public void close()
					{
						iterator.close();
					}

				};
			}

			@Override
			public int size()
			{
				return collection.size();
			}

			@Override
			public boolean isEmpty()
			{
				return collection.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				return collection.contains(o);
			}

		});

	}

	public static Collection<Statement> dependencySortedStatements(Transaction transaction, final Collection<? extends Statement> collection)
	{
		return dependencySortedStatements(transaction, new TrivialCloseableCollection<>(new AdaptedCollection<>(collection)));
	}

	public Context highestContext(Transaction transaction)
	{
		Context context = rootContext(transaction);
		for (Statement dep : dependenciesThisAndDescendents(transaction))
		{
			if (!isDescendent(transaction, dep))
			{
				Context ctx = dep.getContext(transaction);
				if (context.isDescendent(transaction, ctx))
					context = ctx;
			}
		}
		return context;
	}

	/**
	 * Dependencies for this statement to get its proof status valid.
	 * 
	 * Caution: When a dependency is disjunctive it includes the full set of
	 * statements and that may generate plenty of dependency cycles. (in fact it
	 * does)
	 */
	public Collection<Statement> proofDependencies(Transaction transaction)
	{
		return dependenciesThisAndDescendents(transaction);
	}

	/**
	 * Decides if a statement descends from this context
	 *
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @param st
	 *            The statement.
	 * @return The decision.
	 */
	public boolean isDescendent(Transaction transaction, Statement st)
	{
		return equals(st);
	}

	public static class UndeleteStatementException extends StatementException
	{
		private static final long serialVersionUID = -3761559131914524392L;

		protected UndeleteStatementException()
		{
			super();
		}

		protected UndeleteStatementException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected UndeleteStatementException(String message)
		{
			super(message);
		}

		protected UndeleteStatementException(Throwable cause)
		{
			super(cause);
		}

	}

	public class NoContextUndeleteStatementException extends UndeleteStatementException
	{
		private static final long serialVersionUID = 4275337487447663083L;

		private NoContextUndeleteStatementException()
		{
			super("Can't undelete statement. Context lost");
		}
	}

	protected abstract Statement undeleteStatement(Transaction transaction, Context context) throws UndeleteStatementException;

	public Statement undelete(Transaction transaction) throws UndeleteStatementException
	{
		Context context = null;
		if (!(this instanceof RootContext))
		{
			context = getContext(transaction);
			if (context == null)
				throw new NoContextUndeleteStatementException();
		}
		Statement undeleted = undeleteStatement(transaction, context);
		if (!equals(undeleted))
			throw new UndeleteStatementException("Not equal undeleted statement.");
		if (getIdentifier() != null)
		{
			try
			{
				undeleted.identify(transaction, getIdentifier());
			}
			catch (AlreadyUsedIdentifierException e)
			{
				logger.debug(e.getMessage());
			}
			catch (NomenclatorException e)
			{
				throw new UndeleteStatementException(e);
			}
		}
		return undeleted;
	}

	public class Match
	{
		private final List<ParameterVariableTerm> assignable;
		private final Term.Match termMatch;

		private Match(List<ParameterVariableTerm> assignable, Term.Match termMatch)
		{
			super();
			this.assignable = assignable;
			this.termMatch = termMatch;
		}

		public Statement getStatement()
		{
			return Statement.this;
		}

		public List<ParameterVariableTerm> getAssignable()
		{
			return assignable;
		}

		public Term.Match getTermMatch()
		{
			return termMatch;
		}

	}

	public Match match(Term target)
	{
		List<ParameterVariableTerm> assignableLeft = new ArrayList<>();
		SimpleTerm t = getTerm().consequent(assignableLeft);

		List<ParameterVariableTerm> assignableRight = new ArrayList<>();
		SimpleTerm c = target.consequent(assignableRight);

		Term.Match termMatch = t.match(assignableLeft, c, assignableRight);
		if (termMatch == null)
			return null;
		return new Match(assignableLeft, termMatch);
	}

	public static CloseableCollection<Match> filterMatches(CloseableCollection<Statement> statements, final Term target)
	{
		Bijection<Statement, Match> bijection = new Bijection<>()
		{

			@Override
			public Match forward(Statement statement)
			{
				Set<ParameterVariableTerm> assignable = new HashSet<>();
				SimpleTerm t = statement.getTerm().consequent(assignable);
				if (assignable.contains(t.head()))
					return null;
				return statement.match(target);
			}

			@Override
			public Statement backward(Match match)
			{
				return match.getStatement();
			}

		};

		return new FilteredCloseableCollection<>(new NotNullFilter<Match>(), new BijectionCloseableCollection<>(bijection, statements));
	}

	protected Map<Statement, Term> descendentProofTerms(Transaction transaction, Aborter aborter) throws AbortException
	{
		Map<Statement, Term> proofs = new HashMap<>();
		final Map<Context, Statement> solvers = new HashMap<>();
		Map<VariableTerm, Term> replaceMap = new BijectionKeyMap<>(new Bijection<Statement, VariableTerm>()
		{

			@Override
			public VariableTerm forward(Statement statement)
			{
				return statement.getVariable();
			}

			@Override
			public Statement backward(VariableTerm variable)
			{
				if (variable instanceof IdentifiableVariableTerm)
					return transaction.getPersistenceManager().getStatement(transaction, ((IdentifiableVariableTerm) variable).getUuid());
				else
					return null;
			}
		}, proofs);
		Function<Statement, Term> stTermFunction = st -> (isDescendent(transaction, st)) ? proofs.get(st) : st.getVariable();
		Stack<Statement> stack = new Stack<>();
		stack.push(this);
		Set<Statement> visited = new HashSet<>();
		while (!stack.isEmpty())
		{
			if (aborter != null)
				aborter.checkAbort();
			Statement st = stack.pop();
			deploop: while (true)
			{
				for (Statement dep : st.dependencies(transaction))
					if (isDescendent(transaction, dep) && !proofs.containsKey(dep))
					{
						st = dep;
						continue deploop;
					}
				if (st instanceof Context)
					for (Assumption ass : ((Context) st).assumptions(transaction))
						if (!proofs.containsKey(ass))
						{
							st = ass;
							continue deploop;
						}
				break;
			}
			if (!visited.contains(st))
			{
				visited.add(st);
				try
				{
					Term term;
					if (st instanceof Assumption)
					{
						Term type = st.getTerm().replace(replaceMap);
						Term old = proofs.get(st);
						Term oldType = old == null ? null : old.getType();
						term = type.equals(oldType) ? old : new ParameterVariableTerm(st.getTerm().replace(replaceMap));
					}
					else if (st instanceof Specialization)
					{
						Specialization spc = (Specialization) st;
						Statement general = spc.getGeneral(transaction);
						Term termGeneral = stTermFunction.apply(general);
						Term instance = spc.getInstance().replace(replaceMap);
						term = termGeneral.compose(instance);
					}
					else if (st instanceof Declaration)
					{
						Declaration dec = (Declaration) st;
						term = dec.getValue().replace(replaceMap);
					}
					else if (st instanceof Context)
					{
						Context ctx = (Context) st;
						Statement solver = solvers.get(ctx);
						int size;
						if (solver == null)
						{
							term = null;
							size = Integer.MAX_VALUE;
						}
						else
						{
							term = stTermFunction.apply(solver);
							size = term.size();
						}
						ArrayList<Statement> solvers_ = new ArrayList<>(ctx.solvers(transaction));
						Collections.reverse(solvers_);
						Collections.sort(solvers_, solverStatementComparator());
						for (Statement solver_ : solvers_)
						{
							if (solver_.isProved())
							{
								Term term_ = stTermFunction.apply(solver_);
								if (term_ != null)
								{
									int size_ = term_.size();
									if (size_ < size)
									{
										term = term_;
										size = size_;
										solver = solver_;
									}
								}
								else if (isDescendent(transaction, solver_))
									stack.push(solver_);
							}
						}
						if (term != null)
							for (Assumption a : new ReverseList<>(ctx.assumptions(transaction)))
								term = new FunctionTerm((ParameterVariableTerm) proofs.get(a), term);
						solvers.put(ctx, solver);
					}
					else
						throw new Error();
					if (term != null)
					{
						term = ProjectionCastTypeTerm.castToTargetType(term, st.getInnerTerm(transaction).replace(replaceMap));
						if (st instanceof UnfoldingContext)
						{
							Declaration dec = ((UnfoldingContext) st).getDeclaration(transaction);
							if (getContext(transaction).statements(transaction).containsKey(dec.getVariable()))
								term = new FoldingCastTypeTerm(term, st.getTerm().replace(replaceMap), dec.getVariable(), dec.getValue());
						}
						Term old = proofs.put(st, term);
						if (!equals(st) && !term.equals(old))
						{
							for (Statement dep : st.dependents(transaction))
								if (isDescendent(transaction, dep))
								{
									visited.remove(dep);
									stack.push(dep);
								}
							for (Statement dep : st.getContext(transaction).descendantContextsByConsequent(transaction, st.getTerm()))
							{
								if (!dep.equals(st))
								{
									visited.remove(dep);
									stack.push(dep);
								}
							}
							if (st instanceof Assumption)
							{
								Statement dep = st.getContext(transaction);
								visited.remove(dep);
								stack.push(dep);
							}
						}
					}
				}
				catch (ReplaceTypeException | ComposeTypeException | CastTypeException e)
				{
					throw new RuntimeException(e);
				}
				finally
				{

				}
			}
		}
		return proofs;
	}

	protected Map<Statement, Term> descendentProofTerms(Transaction transaction)
	{
		try
		{
			return descendentProofTerms(transaction, null);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Term proofTerm(Transaction transaction, Aborter aborter) throws AbortException
	{
		return descendentProofTerms(transaction, aborter).get(this);
	}

	public Term proofTerm(Transaction transaction)
	{
		try
		{
			return proofTerm(transaction, null);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * A statement comparator with the following rules:
	 * @formatter:off
	 * 		*) Assumptions first, declarations last.
	 * 		*) Within the same statement class, identified statements before non-identified statements.
	 * 		*) Within the same statement class, less components in the identifier of the identified statement first.
	 * @formatter:on
	 */

	protected static Comparator<Statement> solverStatementComparator()
	{
		return new Comparator<>()
		{

			private int compareClasses(Statement st1, Statement st2)
			{
				int c;
				c = -Boolean.compare(st1 instanceof Assumption, st2 instanceof Assumption);
				if (c != 0)
					return c;
				c = Boolean.compare(st1 instanceof Declaration, st2 instanceof Declaration);
				if (c != 0)
					return c;
				return c;
			}

			@Override
			public int compare(Statement st1, Statement st2)
			{
				Identifier id1 = st1.getIdentifier();
				Identifier id2 = st2.getIdentifier();
				int c;
				c = compareClasses(st1, st2);
				if (c != 0)
					return c;
				c = Boolean.compare(id1 == null, id2 == null);
				if (c != 0)
					return c;
				if (id1 == null || id2 == null)
					return 0;
				c = Integer.compare(id1.length(), id2.length());
				if (c != 0)
					return c;
				return c;
			}
		};
	}

	public SpecializationsByGeneral specializations(Transaction transaction)
	{
		return getPersistenceManager().specializationsByGeneral(transaction, this);
	}

	protected abstract ParameterIdentification calcTermParameterIdentification(Transaction transaction);

	private static void checkTermParameterIdentificationUuids(Transaction transaction, Collection<UUID> statementUuids)
	{
		PersistenceManager persistenceManager = transaction.getPersistenceManager();
		Stack<UUID> stack = new Stack<>();
		stack.addAll(statementUuids);
		while (!stack.isEmpty())
		{
			logger.trace("--> checkTermParameterIdentification:" + stack.size());
			UUID uuid = stack.pop();
			Statement st = persistenceManager.getStatement(transaction, uuid);
			ParameterIdentification oldTermParameterIdentification = st.getTermParameterIdentification();
			ParameterIdentification newTermParameterIdentification = st.calcTermParameterIdentification(transaction);

			if ((oldTermParameterIdentification == null) != (newTermParameterIdentification == null)
					|| (oldTermParameterIdentification != null && !oldTermParameterIdentification.equals(newTermParameterIdentification)))
			{
				st.setTermParameterIdentification(transaction, newTermParameterIdentification);
				if (st instanceof Assumption)
					stack.push(st.getContextUuid());
				stack.addAll(new BijectionCollection<>(new Bijection<Specialization, UUID>()
				{

					@Override
					public UUID forward(Specialization specialization)
					{
						return specialization.getUuid();
					}

					@Override
					public Specialization backward(UUID uuid)
					{
						throw new UnsupportedOperationException();
					}
				}, st.specializations(transaction)));
			}
		}

	}

	public void checkTermParameterIdentification(Transaction transaction)
	{
		checkTermParameterIdentificationUuids(transaction, Collections.singleton(getUuid()));
	}

	public static void checkTermParameterIdentifications(Transaction transaction, Collection<Statement> statements)
	{
		checkTermParameterIdentificationUuids(transaction, new BijectionCollection<>(new Bijection<Statement, UUID>()
		{

			@Override
			public UUID forward(Statement statement)
			{
				return statement.getUuid();
			}

			@Override
			public Statement backward(UUID uuid)
			{
				throw new UnsupportedOperationException();
			}
		}, statements));
	}

	protected Map<ParameterVariableTerm, DomainParameterIdentification> domainParameterIdentificationMap()
	{
		return getTerm().domainParameterIdentificationMap(getTermParameterIdentification());
	}

	protected Map<ParameterVariableTerm, Identifier> parameterIdentifierMap()
	{
		return getTerm().parameterIdentifierMap(getTermParameterIdentification());
	}

}
