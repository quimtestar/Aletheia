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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.ContextAuthority;
import aletheia.model.authority.Person;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthority.AuthorityCreationException;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.authority.StatementAuthority.DependentUnpackedSignatureRequests;
import aletheia.model.authority.UnpackedSignatureRequest;
import aletheia.model.catalog.Catalog;
import aletheia.model.catalog.RootCatalog;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.nomenclator.Nomenclator.AlreadyIdentifiedStatementException;
import aletheia.model.nomenclator.Nomenclator.AlreadyUsedIdentifierException;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.nomenclator.Nomenclator.UnknownIdentifierException;
import aletheia.model.nomenclator.SubNomenclator;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.SimpleTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.Term.UnprojectException;
import aletheia.model.term.VariableTerm;
import aletheia.parser.AletheiaTermParser;
import aletheia.parser.TermParserException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.authority.SignatureRequestContextCreationDateCollection;
import aletheia.persistence.collections.authority.UnpackedSignatureRequestSetByContextPath;
import aletheia.persistence.collections.statement.AssumptionList;
import aletheia.persistence.collections.statement.DescendantContextsByConsequent;
import aletheia.persistence.collections.statement.LocalSortedStatements;
import aletheia.persistence.collections.statement.LocalStatementsByTerm;
import aletheia.persistence.collections.statement.LocalStatementsMap;
import aletheia.persistence.collections.statement.SubContextsSet;
import aletheia.persistence.entities.statement.ContextEntity;
import aletheia.utilities.collections.AbstractCloseableCollection;
import aletheia.utilities.collections.AdaptedList;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableSet;
import aletheia.utilities.collections.BijectionList;
import aletheia.utilities.collections.CastBijection;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.CombinedCloseableMultimap;
import aletheia.utilities.collections.CombinedMap;
import aletheia.utilities.collections.CombinedSet;
import aletheia.utilities.collections.EmptyCloseableSet;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCloseableSet;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.collections.TrivialCloseableCollection;
import aletheia.utilities.collections.UnionCloseableCollection;
import aletheia.utilities.collections.UnmodifiableCloseableMap;

/**
 * <p>
 * A context statement is related hierarchically to a set of statements and a
 * {@link SimpleTerm} called the <b>consequent</b> and corresponds to a
 * mathematical conditional proof of the term associated to it. The antecedents
 * of the proof correspond to assumption statements of the context.
 * </p>
 * <p>
 * A context will be proved if there is one of its proven statements or a proven
 * statement of any of its ancestor contexts (note: *NOT* the descendant
 * contexts) whose term matches the consequent term of this context .
 * </p>
 * 
 */
public class Context extends Statement
{
	private static final Logger logger = LoggerManager.logger();

	public abstract static class ContextException extends StatementException
	{
		private static final long serialVersionUID = 339296390327090669L;

		public ContextException()
		{
			super();
		}

		public ContextException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ContextException(String message)
		{
			super(message);
		}

		public ContextException(Throwable cause)
		{
			super(cause);
		}

	}

	public class InvalidTermContextException extends ContextException
	{
		private static final long serialVersionUID = -161382834988049591L;

		public InvalidTermContextException()
		{
			super("Invalid term");
		}

		protected InvalidTermContextException(String message)
		{
			super(message);
		}

		public InvalidTermContextException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public InvalidTermContextException(Throwable cause)
		{
			super(cause);
		}

	}

	public class InconsistentTypeContextException extends InvalidTermContextException
	{
		private static final long serialVersionUID = 6231157957681195975L;

		public InconsistentTypeContextException()
		{
			super("Inconsistent type");
		}
	}

	/**
	 * Calls to
	 * {@link Context#Context(PersistenceManager, Transaction, Class, UUID, List, Context, Term, Term)}
	 * with innerTerm = term.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param entityClass
	 *            The type object of the persistent entity (the generic
	 *            interface, not the actual implementation of persistence) that
	 *            will be created for storing the persistent state of this
	 *            statement. Will depend on the actual subclass of
	 *            {@link Context} that is actually being created.
	 * @param uuid
	 *            The UUID associated to this statement (i.e. the variable that
	 *            identifies this statement). Used as unique identifier of a
	 *            statement. If null, a new one will be generated.
	 * @param uuidAssumptions
	 *            The lists of UUIDs that will be assigned to the assumptions to
	 *            be created in this context.
	 * @param context
	 *            The context that enclosures this context statement.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @throws StatementException
	 */
	protected Context(PersistenceManager persistenceManager, Transaction transaction, Class<? extends ContextEntity> entityClass, UUID uuid,
			List<UUID> uuidAssumptions, Context context, Term term) throws StatementException
	{
		this(persistenceManager, transaction, entityClass, uuid, uuidAssumptions, context, term, term);
	}

	/**
	 * Calls to
	 * {@link Context#Context(PersistenceManager, Transaction, Class, UUID, List, Context, Term, Term)}
	 * with uuid and uuidAssumptions set to null and innerTerm = term.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param entityClass
	 *            The type object of the persistent entity (the generic
	 *            interface, not the actual implementation of persistence) that
	 *            will be created for storing the persistent state of this
	 *            statement. Will depend on the actual subclass of
	 *            {@link Context} that is actually being created.
	 * @param context
	 *            The context that enclosures this context statement.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @throws StatementException
	 */
	protected Context(PersistenceManager persistenceManager, Transaction transaction, Class<? extends ContextEntity> entityClass, Context context, Term term)
			throws StatementException
	{
		this(persistenceManager, transaction, entityClass, null, null, context, term, term);
	}

	/**
	 * Creates a new context from scratch. The assumptions of this context are
	 * also computed and generated.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param entityClass
	 *            The type object of the persistent entity (the generic
	 *            interface, not the actual implementation of persistence) that
	 *            will be created for storing the persistent state of this
	 *            statement. Will depend on the actual subclass of
	 *            {@link Context} that is actually being created.
	 * @param uuid
	 *            The UUID associated to this statement (i.e. the variable that
	 *            identifies this statement). Used as unique identifier of a
	 *            statement. If null, a new one will be generated.
	 * @param uuidAssumptions
	 *            The lists of UUIDs that will be assigned to the assumptions to
	 *            be created in this context.
	 * @param context
	 *            The context that enclosures this context statement.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @param innerTerm
	 *            The term that is used to compute the antecedents and
	 *            consequent of this context. It will be identical to the former
	 *            parameter 'term', except for the instances of the subclass
	 *            {@link UnfoldingContext}.
	 * @throws StatementException
	 */
	protected Context(PersistenceManager persistenceManager, Transaction transaction, Class<? extends ContextEntity> entityClass, UUID uuid,
			List<UUID> uuidAssumptions, Context context, Term term, Term innerTerm) throws StatementException
	{
		super(persistenceManager, transaction, entityClass, uuid, context, term);
		persistenceUpdate(transaction);
		int i = 0;
		Term innerTerm_;
		try
		{
			innerTerm_ = innerTerm.unproject();
		}
		catch (UnprojectException e1)
		{
			throw new InvalidTermContextException(e1);
		}
		while (innerTerm_ instanceof FunctionTerm)
		{
			FunctionTerm functionTerm = (FunctionTerm) innerTerm_;
			Assumption assumption = (uuidAssumptions == null) ? new Assumption(persistenceManager, transaction, this, functionTerm.getParameter().getType(), i)
					: new Assumption(persistenceManager, transaction, uuidAssumptions.get(i), this, functionTerm.getParameter().getType(), i);
			addStatement(transaction, assumption, false);
			try
			{
				innerTerm_ = functionTerm.getBody().replace(functionTerm.getParameter(), assumption.getVariable());
			}
			catch (ReplaceTypeException e)
			{
				throw new Error(e);
			}
			i++;
		}
		if (!(innerTerm_ instanceof SimpleTerm))
			throw new InvalidTermContextException();
		getEntity().setConsequent((SimpleTerm) innerTerm_);
	}

	/**
	 * Calls to
	 * {@link Context#Context(PersistenceManager, Transaction, Class, UUID, List, Context, Term, Term)}
	 * with uuid and uuidAssumptions set to null.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param entityClass
	 *            The type object of the persistent entity (the generic
	 *            interface, not the actual implementation of persistence) that
	 *            will be created for storing the persistent state of this
	 *            statement. Will depend on the actual subclass of
	 *            {@link Context} that is actually being created.
	 * @param context
	 *            The context that enclosures this context statement.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @param innerTerm
	 *            The term that is used to compute the antecedents and
	 *            consequent of this context. It will be identical to the former
	 *            parameter 'term', except for the instances of the subclass
	 *            {@link UnfoldingContext}.
	 * @throws StatementException
	 */
	protected Context(PersistenceManager persistenceManager, Transaction transaction, Class<? extends ContextEntity> entityClass, Context context, Term term,
			Term innerTerm) throws StatementException
	{
		this(persistenceManager, transaction, entityClass, null, null, context, term, innerTerm);
	}

	/**
	 * Creates a context that envelopes an already existing
	 * {@link ContextEntity}
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The persistence entity that will be enveloped in this
	 *            statement.
	 */
	public Context(PersistenceManager persistenceManager, ContextEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public ContextEntity getEntity()
	{
		return (ContextEntity) super.getEntity();
	}

	/**
	 * Creates a mapping between variables and statements for the local
	 * statements of this context.
	 * 
	 * @param transaction
	 *            The persistence transaction across which the operations on
	 *            this map will be performed.
	 * @return The map.
	 * 
	 * @see LocalStatementsMap
	 */
	protected LocalStatementsMap getLocalStatements(Transaction transaction)
	{
		return getPersistenceManager().localStatements(transaction, this);
	}

	/**
	 * Creates a mapping between terms and statements for the local statements
	 * of this context. Useful for finding the set of statements that match a
	 * particular term.
	 * 
	 * @param transaction
	 *            The persistence transaction across which the operations on
	 *            this map will be performed.
	 * @return The map
	 * 
	 * @see LocalStatementsByTerm
	 */
	protected LocalStatementsByTerm getLocalStatementsByTerm(Transaction transaction)
	{
		return getPersistenceManager().localStatementsByTerm(transaction, this);
	}

	/**
	 * Creates a list of the assumptions of this context.
	 * 
	 * @param transaction
	 *            The persistence transaction across which the operations on
	 *            this list will be performed.
	 * @return The list.
	 * 
	 * @see AssumptionList
	 */
	protected AssumptionList getAssumptionList(Transaction transaction)
	{
		return getPersistenceManager().assumptionList(transaction, this);
	}

	/**
	 * The set of direct subcontexts of this context.
	 * 
	 * @param transaction
	 *            The persistence transaction across which the operations on
	 *            this set will be performed.
	 * @return The set.
	 * 
	 * @see SubContextsSet
	 */
	public SubContextsSet subContexts(Transaction transaction)
	{
		return getPersistenceManager().subContexts(transaction, this);
	}

	/**
	 * Creates a nomenclator for this context. A nomenclator is a bidirectional
	 * directory of the existing correspondences between identifiers and
	 * statements for this context.
	 * 
	 * @param transaction
	 *            The persistence transaction across which the operations on
	 *            this set will be performed.
	 * @return The nomenclator.
	 * 
	 * @see SubNomenclator
	 */
	public SubNomenclator getNomenclator(Transaction transaction)
	{
		return new SubNomenclator(getPersistenceManager(), transaction, this);
	}

	/**
	 * Adds a statement to this context at the persistence level, rechecks the
	 * proven status of the affected statements (the added statement and its
	 * dependents) and triggers the notifications to this context's listeners.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statement
	 *            The statement to be added.
	 * @param checkContext
	 *            If true, checks the context. To be set to false at context
	 *            construction time, when it is not convenient to check the
	 *            context that isn't still fully created.
	 */
	private void addStatement(Transaction transaction, Statement statement, boolean checkContext)
	{
		statement.persistenceUpdate(transaction);
		statement.checkProved(transaction, checkContext);
		Iterable<StateListener> listeners = stateListeners();
		synchronized (listeners)
		{
			for (StateListener listener : listeners)
				listener.statementAddedToContext(transaction, this, statement);
		}
	}

	/**
	 * Calls to {@link #addStatement(Transaction, Statement, boolean)} with
	 * checkContext set to true.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statement
	 *            The statement to add.
	 */
	private void addStatement(Transaction transaction, Statement statement)
	{
		addStatement(transaction, statement, true);
	}

	/**
	 * @return The consequent of this context.
	 */
	public SimpleTerm getConsequent()
	{
		return getEntity().getConsequent();
	}

	/**
	 * The set of potential solvers for this context. All the statements in this
	 * context or any ancestor context whose term matches with the consequent of
	 * this context. If all the dependencies of this context and moreover any of
	 * these statements is proven, this context will also be proven.
	 * 
	 * @param transaction
	 *            The persistence transaction to be used in the operation.
	 * @return The set.
	 */
	public CloseableSet<Statement> solvers(Transaction transaction)
	{
		CloseableSet<Statement> set = statementsByTerm(transaction).get(getConsequent());
		if (set == null)
			return new EmptyCloseableSet<>();
		return set;
	}

	/**
	 * For a context to be proven, the method
	 * {@link Statement#calcProved(Transaction)} must return true and at least
	 * one of its solvers must be proven.
	 */
	@Override
	protected boolean calcProved(Transaction transaction)
	{
		if (!super.calcProved(transaction))
			return false;
		CloseableIterator<Statement> it = solvers(transaction).iterator();
		try
		{
			while (it.hasNext())
			{
				Statement st = it.next();
				if (st.isProved())
					return true;
			}
		}
		finally
		{
			it.close();
		}
		return false;
	}

	/**
	 * Creates a new context in this context.
	 * 
	 * @param transaction
	 *            The persistence transaction used in this operation.
	 * @param uuid
	 *            The UUID that will be assigned to this statement. If null, a
	 *            new UUID will be generated.
	 * @param uuidAssumptions
	 *            The list of UUIDs that will be assigned to the assumptions
	 *            created into this statement. If null, a new set of UUIDs will
	 *            be generated.
	 * @param term
	 *            The term of this context.
	 * @return The new context.
	 * @throws StatementException
	 */
	public Context openSubContext(Transaction transaction, UUID uuid, List<UUID> uuidAssumptions, Term term) throws StatementException
	{
		Context ctx = new Context(getPersistenceManager(), transaction, ContextEntity.class, uuid, uuidAssumptions, this, term);
		addStatement(transaction, ctx);
		return ctx;

	}

	/**
	 * Calls to {@link #openSubContext(Transaction, UUID, List, Term)} with uuid
	 * and uuidAssumptions set to null, so the UUIDs will be fresh-generated.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param term
	 *            The term of this context.
	 * @return The new context.
	 * @throws StatementException
	 */
	public Context openSubContext(Transaction transaction, Term term) throws StatementException
	{
		return openSubContext(transaction, null, null, term);
	}

	/**
	 * Creates a new unfolding context in this context.
	 * 
	 * @param transaction
	 *            The persistence transaction used in this operation.
	 * @param uuid
	 *            The UUID that will be assigned to this statement. If null, a
	 *            new UUID will be generated.
	 * @param uuidAssumptions
	 *            The list of UUIDs that will be assigned to the assumptions
	 *            created into this statement. If null, a new set of UUIDs will
	 *            be generated.
	 * @param term
	 *            The (outer) term of this context.
	 * @param declaration
	 *            The declaration statement that will be unfolded in this
	 *            context.
	 * @return The new unfolding context
	 * @throws StatementException
	 */
	public UnfoldingContext openUnfoldingSubContext(Transaction transaction, UUID uuid, List<UUID> uuidAssumptions, Term term, Declaration declaration)
			throws StatementException
	{
		UnfoldingContext ctx = new UnfoldingContext(transaction, getPersistenceManager(), uuid, uuidAssumptions, this, term, declaration);
		addStatement(transaction, ctx);
		return ctx;
	}

	/**
	 * Calls to {@link #openUnfoldingSubContext(Transaction, Term, Declaration)}
	 * with uuid and uuidAssumptions set to null, so the UUIDs will be
	 * fresh-generated.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param term
	 *            The (outer) term of this context.
	 * @param declaration
	 *            The declaration to unfold.
	 * @return The new unfolding context.
	 * @throws StatementException
	 */
	public UnfoldingContext openUnfoldingSubContext(Transaction transaction, Term term, Declaration declaration) throws StatementException
	{
		return openUnfoldingSubContext(transaction, null, null, term, declaration);
	}

	/**
	 * The full mapping of pairs variable-statement that are accessible from
	 * this context. That is, the union of all the
	 * {@link #localStatements(Transaction)} of the ancestors of this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 */
	public Map<IdentifiableVariableTerm, Statement> statements(Transaction transaction)
	{
		return new CombinedMap<IdentifiableVariableTerm, Statement>(getLocalStatements(transaction), getContext(transaction).statements(transaction));
	}

	/**
	 * The full mapping of statements by term that are accessible from this
	 * context. That is the union of all the
	 * {@link #localStatementsByTerm(Transaction)} of the ancestors of this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 */
	public CloseableMap<Term, CloseableSet<Statement>> statementsByTerm(Transaction transaction)
	{
		return new CombinedCloseableMultimap<Term, Statement>(getLocalStatementsByTerm(transaction), getContext(transaction).statementsByTerm(transaction));
	}

	/**
	 * The mapping of pairs variable-statement that are local of this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 */
	public CloseableMap<Term, CloseableSet<Statement>> localStatementsByTerm(Transaction transaction)
	{
		return getLocalStatementsByTerm(transaction);
	}

	/**
	 * The list of assumptions of this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the list.
	 * @return The list.
	 */
	public List<Assumption> assumptions(Transaction transaction)
	{
		return Collections.unmodifiableList(getAssumptionList(transaction));
	}

	/**
	 * Given a term, computes the set of its free variables that are not defined
	 * under this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param term
	 *            The term.
	 * @return The set.
	 */
	protected Set<VariableTerm> undefinedVariables(Transaction transaction, Term term)
	{
		Set<VariableTerm> undefined = term.freeVariables();
		for (Iterator<VariableTerm> i = undefined.iterator(); i.hasNext();)
		{
			VariableTerm var = i.next();
			if (statements(transaction).containsKey(var))
				i.remove();
		}
		return undefined;
	}

	/**
	 * Creates a new declaration in this context.
	 * 
	 * @param transaction
	 *            The persistence transaction used in this operation.
	 * @param uuid
	 *            The UUID that will be assigned to this statement. If null, a
	 *            new UUID will be generated.
	 * @param value
	 *            The value assigned to this declaration.
	 * @return The new declaration.
	 * @throws StatementException
	 */
	public Declaration declare(Transaction transaction, UUID uuid, Term value) throws StatementException
	{
		Declaration dec = new Declaration(getPersistenceManager(), transaction, uuid, this, value);
		addStatement(transaction, dec);
		return dec;
	}

	/**
	 * Calls to {@link #declare(Transaction, UUID, Term)} with the UUID set to
	 * null.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param value
	 *            The value assigned to this declaration.
	 * @return The new declaration.
	 * @throws StatementException
	 */
	public Declaration declare(Transaction transaction, Term value) throws StatementException
	{
		return declare(transaction, null, value);
	}

	/**
	 * Creates a new specialization statement in this context.
	 * 
	 * @param transaction
	 *            The persistence transaction used in this operation.
	 * @param uuid
	 *            The UUID that will be assigned to this statement. If null, a
	 *            new UUID will be generated.
	 * @param general
	 *            The statement specialized.
	 * @param instance
	 *            The instance used to specialize.
	 * @return The new specialization statement.
	 * @throws StatementException
	 */
	public Specialization specialize(Transaction transaction, UUID uuid, Statement general, Term instance) throws StatementException
	{
		Specialization spec = new Specialization(getPersistenceManager(), transaction, uuid, this, general, instance);
		addStatement(transaction, spec);
		return spec;
	}

	/**
	 * Calls to {@link #specialize(Transaction, UUID, Statement, Term)} with the
	 * UUID set to null.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param general
	 *            The statement specialized.
	 * @param instance
	 *            The instance used to specialize.
	 * @return The new specialization statement.
	 * @throws StatementException
	 */
	public Specialization specialize(Transaction transaction, Statement general, Term instance) throws StatementException
	{
		return specialize(transaction, null, general, instance);
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " [Context]";
	}

	public LocalSortedStatements localSortedStatements(Transaction transaction)
	{
		return getPersistenceManager().localSortedStatements(transaction, this);
	}

	public CloseableCollection<Statement> localDependencySortedStatements(final Transaction transaction)
	{
		final LocalSortedStatements localSortedStatements = localSortedStatements(transaction);
		return new UnionCloseableCollection<Statement>(new AbstractCloseableCollection<CloseableCollection<Statement>>()
		{

			@Override
			public CloseableIterator<CloseableCollection<Statement>> iterator()
			{
				return new CloseableIterator<CloseableCollection<Statement>>()
				{
					final CloseableIterator<Statement> iterator = localSortedStatements.iterator();
					final Set<Statement> visited = new HashSet<Statement>();

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public CloseableCollection<Statement> next()
					{
						Stack<Statement> stack = new Stack<Statement>();
						stack.push(iterator.next());
						Stack<Statement> stack2 = new Stack<Statement>();
						Map<Statement, List<Statement>> dependencyMap = new HashMap<Statement, List<Statement>>();
						while (!stack.isEmpty())
						{
							Statement st = stack.pop();
							stack2.push(st);
							List<Statement> list = dependencyMap.get(st);
							if (list == null)
							{
								list = new ArrayList<Statement>(new FilteredCollection<Statement>(new Filter<Statement>()
								{
									@Override
									public boolean filter(Statement e)
									{
										return !visited.contains(e);
									}
								}, st.localDependencies(transaction)));
								Collections.sort(list, localSortedStatements.comparator());
								dependencyMap.put(st, list);
							}
							stack.addAll(list);
						}
						List<Statement> list = new ArrayList<Statement>();
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
				return localSortedStatements.size();
			}
		});
	}

	/**
	 * Assigns a local statement to a identifier.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param identifier
	 *            The identifier.
	 * @param statement
	 *            The statement.
	 * @throws NomenclatorException
	 * @throws StatementNotInContextException
	 * @throws AlreadyUsedIdentifierException
	 * @throws AlreadyIdentifiedStatementException
	 */
	@Deprecated
	public void identifyStatement(Transaction transaction, Identifier identifier, Statement statement) throws NomenclatorException
	{
		getNomenclator(transaction).identifyStatement(identifier, statement);
	}

	/**
	 * Unassigns the identifier assigned to a local statement.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param identifier
	 *            The identifier to unassign.
	 * @throws UnknownIdentifierException
	 */
	@Deprecated
	public void unidentifyStatement(Transaction transaction, Identifier identifier) throws UnknownIdentifierException
	{
		getNomenclator(transaction).unidentifyStatement(identifier);
	}

	/**
	 * The composed map from identifiers to statements of the ancestors of this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 * 
	 * @see SubNomenclator#identifierToStatement()
	 */
	public SortedMap<Identifier, Statement> identifierToStatement(Transaction transaction)
	{
		return getNomenclator(transaction).identifierToStatement();
	}

	/**
	 * The local map from identifiers to statements of this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 * 
	 * @see SubNomenclator#localIdentifierToStatement()
	 */
	public SortedMap<Identifier, Statement> localIdentifierToStatement(Transaction transaction)
	{
		return getNomenclator(transaction).localIdentifierToStatement();
	}

	/**
	 * The composed map from statements to identifiers of the ancestors of this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 * 
	 * @see SubNomenclator#statementToIdentifier()
	 */
	public Map<Statement, Identifier> statementToIdentifier(Transaction transaction)
	{
		return getNomenclator(transaction).statementToIdentifier();
	}

	/**
	 * The local map from statements to identifiers.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 * 
	 * @see SubNomenclator#localStatementToIdentifier()
	 */
	public Map<Statement, Identifier> localStatementToIdentifier(Transaction transaction)
	{
		return getNomenclator(transaction).localStatementToIdentifier();
	}

	/**
	 * The composed map from identifiers to variables of the ancestors of this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 * 
	 * @see SubNomenclator#identifierToVariable()
	 */
	public SortedMap<Identifier, IdentifiableVariableTerm> identifierToVariable(Transaction transaction)
	{
		return getNomenclator(transaction).identifierToVariable();
	}

	/**
	 * The local map from variable to identifier.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map.
	 * 
	 * @see SubNomenclator#variableToIdentifier()
	 */
	public Map<IdentifiableVariableTerm, Identifier> variableToIdentifier(Transaction transaction)
	{
		return getNomenclator(transaction).variableToIdentifier();
	}

	/**
	 * Parse a string into a term using the identifiers defined in this context
	 * (and ancestors).
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param s
	 *            The string to be parsed.
	 * @return The resulting term.
	 * @throws TermParserException
	 */
	public Term parseTerm(Transaction transaction, String s) throws TermParserException
	{
		return AletheiaTermParser.parseTerm(this, transaction, s);
	}

	public class StatementNotInContextException extends ContextException
	{
		private static final long serialVersionUID = 8332873787690492305L;

		public StatementNotInContextException()
		{
			super("Statement not in context");
		}

		public StatementNotInContextException(Throwable e)
		{
			super("Statement not in context", e);
		}

	}

	public class StatementHasDependentsException extends ContextException
	{
		private static final long serialVersionUID = 2735514901951799739L;

		public StatementHasDependentsException()
		{
			super("Statement has dependents");
		}

	}

	public class CantDeleteAssumptionException extends ContextException
	{
		private static final long serialVersionUID = 5530456734976983209L;

		public CantDeleteAssumptionException()
		{
			super("Can't delete assumption");
		}

	}

	/**
	 * Delete a statement from this context, checking first if there are no
	 * other statements depending on them. With the deletion, the proven status
	 * of the pertinent statements is updated, so it's possible that the
	 * execution of this methods spans through a long period of time.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statement
	 *            The statement to delete.
	 * @throws StatementNotInContextException
	 * @throws StatementHasDependentsException
	 * @throws CantDeleteAssumptionException
	 * @throws DependentUnpackedSignatureRequests
	 */
	public void deleteStatement(Transaction transaction, Statement statement) throws StatementNotInContextException, StatementHasDependentsException,
			CantDeleteAssumptionException, DependentUnpackedSignatureRequests
	{
		if (statement instanceof Assumption)
			throw new CantDeleteAssumptionException();
		if (!getLocalStatements(transaction).containsKey(statement.getVariable()))
			throw new StatementNotInContextException();
		if (!statement.getDependents(transaction).isEmpty())
			throw new StatementHasDependentsException();
		statement.deleteAuthority(transaction);
		statement.deleteLocal(transaction);
		if (statement instanceof Context)
		{
			Context ctx = (Context) statement;
			ctx.deleteUnpackedSignatureRequestSetByPath(transaction);
			Stack<Statement> stack = new Stack<Statement>();
			stack.addAll(ctx.localStatements(transaction).values());
			while (!stack.isEmpty())
			{
				logger.trace("---> deleting context: " + stack.size());
				Statement st = stack.peek();
				if (getPersistenceManager().statements(transaction).containsKey(st.getVariable()))
				{
					boolean pushed = false;
					if (st instanceof Context)
					{
						Collection<Statement> locals = ((Context) st).localStatements(transaction).values();
						if (locals.size() > 0)
						{
							stack.addAll(locals);
							pushed = true;
						}
					}
					Set<Statement> dependents = st.dependents(transaction);
					if (!dependents.isEmpty())
					{
						stack.addAll(dependents);
						pushed = true;
					}
					if (!pushed)
					{
						stack.pop();
						Context ctx_ = st.getContext(transaction);
						st.deleteAuthorityNoCheckSignedProof(transaction);
						st.deleteLocal(transaction);
						getPersistenceManager().deleteStatement(transaction, st);
						Iterable<StateListener> listeners = ctx_.stateListeners();
						synchronized (listeners)
						{
							Identifier identifier = null;
							boolean first = true;
							for (StateListener listener : ctx_.stateListeners())
							{
								if (first)
								{
									identifier = st.identifier(transaction);
									first = false;
								}
								listener.statementDeletedFromContext(transaction, this, st, identifier);
							}
						}
					}
				}
				else
					stack.pop();
			}

		}

		boolean proved = statement.isProved();
		getPersistenceManager().deleteStatement(transaction, statement);
		if (proved)
		{
			Set<Statement> reseted = new HashSet<Statement>();
			for (Context ctx : descendantContextsByConsequent(transaction, statement.getTerm()))
				reseted.addAll(ctx.resetProvedDependents(transaction));
			checkProved(transaction, reseted);
		}
		Iterable<StateListener> listeners = stateListeners();
		synchronized (listeners)
		{
			Identifier identifier = null;
			boolean first = true;
			for (StateListener listener : stateListeners())
			{
				if (first)
				{
					identifier = statement.identifier(transaction);
					first = false;
				}
				listener.statementDeletedFromContext(transaction, this, statement, identifier);
			}
		}
	}

	/**
	 * Recursively clears the proven flags of this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The set of affected statements.
	 */
	private Set<Statement> resetProvedDependents(Transaction transaction)
	{
		Set<Statement> set = new HashSet<Statement>();
		Stack<Statement> stack = new Stack<Statement>();
		stack.push(this);
		while (!stack.isEmpty())
		{
			logger.debug("---> resetProvedDependents:" + stack.size() + " " + set.size());
			Statement st = stack.pop();
			set.add(st);
			if (st.isProved())
			{
				st.setProved(transaction, false);
				Iterable<StateListener> listeners = st.stateListeners();
				synchronized (listeners)
				{
					for (StateListener listener : listeners)
						listener.provedStateChanged(transaction, st, false);
				}

				stack.addAll(st.dependents(transaction));
				if (!(st instanceof RootContext))
					stack.addAll(st.getContext(transaction).descendantContextsByConsequent(transaction, st.getTerm()));
				else
					stack.addAll(((Context) st).descendantContextsByConsequent(transaction, st.getTerm()));
			}
		}
		return set;
	}

	/**
	 * Returns an {@link Iterable} of statements that descend of this context,
	 * in preorder
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The iterable.
	 */
	public CloseableIterable<Statement> descendentStatements(final Transaction transaction)
	{
		return new CloseableIterable<Statement>()
		{
			@Override
			public CloseableIterator<Statement> iterator()
			{
				return new CloseableIterator<Statement>()
				{
					final Stack<CloseableIterator<Statement>> stack;
					{
						stack = new Stack<CloseableIterator<Statement>>();
						stack.push(localStatements(transaction).values().iterator());
					}
					Statement next = advance();

					private Statement advance()
					{
						while (!stack.isEmpty())
						{
							CloseableIterator<Statement> iterator = stack.peek();
							if (iterator.hasNext())
							{
								Statement st = iterator.next();
								if (st instanceof Context)
								{
									Context ctx = (Context) st;
									stack.push(ctx.localStatements(transaction).values().iterator());
								}
								return st;
							}
							iterator.close();
							stack.pop();
						}
						return null;
					}

					@Override
					public Statement next()
					{
						if (next == null)
							throw new NoSuchElementException();
						Statement st = next;
						next = advance();
						return st;
					}

					@Override
					public boolean hasNext()
					{
						return next != null;
					}

					@Override
					public void close()
					{
						while (!stack.isEmpty())
							stack.pop().close();
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

					@Override
					protected void finalize() throws Throwable
					{
						close();
						super.finalize();
					}
				};
			}
		};
	}

	/**
	 * The set of descendant contexts whose consequent match a term.
	 * 
	 * @param transaction
	 *            Persistence transaction to use in the operations on the set.
	 * @param consequent
	 *            The term that will be matched.
	 * @return The set.
	 */
	public CloseableSet<Context> descendantContextsByConsequent(Transaction transaction, Term consequent)
	{
		if (consequent instanceof SimpleTerm)
			return descendantContextsByConsequent(transaction, (SimpleTerm) consequent);
		else
			return new EmptyCloseableSet<Context>();
	}

	/**
	 * The set of descendant contexts whose consequent match a simple term.
	 * 
	 * @param transaction
	 *            Persistence transaction to use in the operations on the set.
	 * @param consequent
	 *            The simple term that will be matched.
	 * @return The set.
	 */
	private DescendantContextsByConsequent descendantContextsByConsequent(Transaction transaction, SimpleTerm consequent)
	{
		return getPersistenceManager().descendantContextsByConsequent(transaction, this, consequent);
	}

	/**
	 * Deletes a statement cascading on its dependents.
	 * 
	 * @param transaction
	 *            Persistence transaction to use in this operation.
	 * @param statement
	 *            The statement to delete.
	 * @throws StatementNotInContextException
	 * 
	 * @see #deleteStatement(Transaction, Statement)
	 */
	public void deleteStatementCascade(Transaction transaction, Statement statement) throws StatementNotInContextException
	{
		Stack<Statement> stack = new Stack<Statement>();
		if (statement instanceof Assumption)
			stack.push(statement.getContext(transaction));
		else
			stack.push(statement);
		Set<Statement> visited = new HashSet<Statement>();
		while (!stack.isEmpty())
		{
			logger.trace("--> Cascade delete: " + stack.size());
			Statement st = stack.peek();
			if (getPersistenceManager().statements(transaction).containsKey(st.getVariable()))
			{
				Statement st__ = st;
				while (st__ != null)
				{
					if (visited.contains(st__))
						break;
					if (st__ instanceof RootContext)
						st__ = null;
					else
						st__ = st__.getContext(transaction);
				}
				if (st__ != null)
					stack.pop();
				else
				{
					boolean pushed = false;
					for (Statement st_ : st.dependents(transaction))
					{
						if (!(st_ instanceof Assumption))
						{
							stack.push(st_);
							pushed = true;
						}
						else
						{
							stack.push(st_.getContext(transaction));
							pushed = true;
						}
					}
					if (!pushed)
					{
						try
						{
							st.deleteWithRemovalFromDependentUnpackedSignatureRequests(transaction);
						}
						catch (StatementHasDependentsException | CantDeleteAssumptionException e)
						{
							throw new Error(e);
						}
						visited.add(st);
						stack.pop();
					}
				}
			}
			else
				stack.pop();
		}
	}

	/**
	 * Translates a term from one context to another using an specified
	 * statement map. In other words, it replaces in the term all the variables
	 * that make sense on one context by corresponding variables on the other
	 * context.
	 * 
	 * @param transaction
	 *            The transaction used in the operation.
	 * @param map
	 *            The statement map used for the translation.
	 * @param ctxOrig
	 *            The original context of the term.
	 * @param term
	 *            The original term.
	 * @return The translated term.
	 * @throws ReplaceTypeException
	 */
	private Term translateTerm(Transaction transaction, Map<Statement, Statement> map, Context ctxOrig, Term term) throws ReplaceTypeException
	{
		List<Term.Replace> replaces = new ArrayList<Term.Replace>();
		for (VariableTerm var : term.freeVariables())
		{
			Statement stOrig = ctxOrig.statements(transaction).get(var);
			Statement stDest = map.get(stOrig);
			if (stDest != null)
				replaces.add(new Term.Replace(var, stDest.getVariable()));
		}
		return term.replace(replaces);
	}

	public class CopyStatementException extends StatementException
	{
		private static final long serialVersionUID = -3007370131448437354L;

		public CopyStatementException()
		{
			super();
		}

		public CopyStatementException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public CopyStatementException(String message)
		{
			super(message);
		}

		public CopyStatementException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Copies a statement (belonging or not to this context) into this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statement
	 *            The statement to be copied.
	 * @param initMap
	 *            The correspondence map to be used initially in the copy
	 *            operation. To be used when copying a statement from another
	 *            context.
	 * @return The new statement.
	 * @throws StatementException
	 */
	private Statement privateCopy(Transaction transaction, Statement statement, Map<Statement, Statement> initMap) throws StatementException
	{
		Map<Statement, Statement> map = privateCopy(transaction, Arrays.asList(statement), initMap, Collections.singleton(statement));
		return map.get(statement);
	}

	/**
	 * Copies a list of statements (belonging or not to this context) into this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The list of statements to be copied (in that order).
	 * @param initMap
	 *            The correspondence map to be used initially in the copy
	 *            operation. To be used when copying a statement from another
	 *            context.
	 * @return A map of correspondences between the original statements and the
	 *         copied statements.
	 * @throws StatementException
	 */
	@SuppressWarnings("unused")
	private Map<Statement, Statement> privateCopy(Transaction transaction, List<Statement> statements, Map<Statement, Statement> initMap)
			throws StatementException
	{
		return privateCopy(transaction, statements, initMap, Collections.<Statement> emptySet());
	}

	public class CyclicCopyContextException extends CopyStatementException
	{
		private static final long serialVersionUID = 8205703817184946653L;

		public CyclicCopyContextException()
		{
		}

		@Override
		public String getMessage()
		{
			return "Copy has a cycle";
		}

	}

	/**
	 * Copies a list of statements (belonging or not to this context) into this
	 * context. Some of that statements can be specified to not be identified
	 * (usually the original identifiers are copied to the resulting
	 * statements).
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The list of statements to be copied (in that order).
	 * @param initMap
	 *            The correspondence map to be used initially in the copy
	 *            operation. To be used when copying a statement from another
	 *            context.
	 * @param excludeFromIdentify
	 *            The set of statements that will be excluded from identifying.
	 * @return A map of correspondences between the original statements and the
	 *         copied statements.
	 * @throws CopyStatementException
	 */
	private Map<Statement, Statement> privateCopy(Transaction transaction, List<Statement> statements, Map<Statement, Statement> initMap,
			Set<Statement> excludeFromIdentify) throws CopyStatementException
	{
		Map<Statement, Statement> map = new HashMap<Statement, Statement>(initMap);
		Set<Statement> copied = new HashSet<Statement>(initMap.values());
		Queue<Statement> queue = new ArrayDeque<Statement>();
		queue.addAll(statements);
		while (!queue.isEmpty())
		{
			Statement stOrig = queue.poll();
			if (copied.contains(stOrig))
				throw new CyclicCopyContextException();
			Context ctxStOrig = stOrig.getContext(transaction);
			Context ctxParentDest = (Context) map.get(ctxStOrig);
			if (ctxParentDest == null)
				ctxParentDest = this;
			Statement stDest;
			if (stOrig instanceof Assumption)
			{
				Assumption asOrig = (Assumption) stOrig;
				stDest = ctxParentDest.assumptions(transaction).get(asOrig.getOrder());
			}
			else if (stOrig instanceof Declaration)
			{
				Declaration decOrig = (Declaration) stOrig;
				Declaration decDest;
				try
				{
					decDest = ctxParentDest.declare(transaction, translateTerm(transaction, map, ctxStOrig, decOrig.getValue()));
				}
				catch (ReplaceTypeException e)
				{
					throw new CopyStatementException(e);
				}
				catch (StatementException e)
				{
					throw new CopyStatementException(e);
				}
				stDest = decDest;
			}
			else if (stOrig instanceof Specialization)
			{
				Specialization specOrig = (Specialization) stOrig;
				Statement genDest = map.get(specOrig.getGeneral(transaction));
				if (genDest == null)
					genDest = specOrig.getGeneral(transaction);
				Specialization specDest;
				try
				{
					specDest = ctxParentDest.specialize(transaction, genDest, translateTerm(transaction, map, ctxStOrig, specOrig.getInstance()));
				}
				catch (ReplaceTypeException e)
				{
					throw new CopyStatementException(e);
				}
				catch (StatementException e)
				{
					throw new CopyStatementException(e);
				}
				stDest = specDest;
			}
			else if (stOrig instanceof Context)
			{
				Context ctxOrig = (Context) stOrig;
				if (ctxOrig instanceof UnfoldingContext)
				{
					UnfoldingContext unfOrig = (UnfoldingContext) ctxOrig;
					Declaration decDest = (Declaration) map.get(unfOrig.getDeclaration(transaction));
					if (decDest == null)
						decDest = unfOrig.getDeclaration(transaction);
					UnfoldingContext unfDest;
					try
					{
						unfDest = ctxParentDest.openUnfoldingSubContext(transaction, translateTerm(transaction, map, ctxStOrig, unfOrig.getTerm()), decDest);
					}
					catch (ReplaceTypeException e)
					{
						throw new CopyStatementException(e);
					}
					catch (StatementException e)
					{
						throw new CopyStatementException(e);
					}
					stDest = unfDest;
				}
				else if (ctxOrig instanceof RootContext)
					throw new Error();
				else
				{
					Context ctxDest;
					try
					{
						ctxDest = ctxParentDest.openSubContext(transaction, translateTerm(transaction, map, ctxStOrig, ctxOrig.getTerm()));
					}
					catch (ReplaceTypeException e)
					{
						throw new CopyStatementException(e);
					}
					catch (StatementException e)
					{
						throw new CopyStatementException(e);
					}
					stDest = ctxDest;
				}
				queue.addAll(ctxOrig.localDependencySortedStatements(transaction));
			}
			else
				throw new Error();
			map.put(stOrig, stDest);
			copied.add(stDest);
			if (!excludeFromIdentify.contains(stOrig))
			{
				Identifier id = ctxStOrig.statementToIdentifier(transaction).get(stOrig);
				if (id != null)
				{
					if (stDest.identifier(transaction) == null)
						try
						{
							stDest.getContext(transaction).identifyStatement(transaction, id, stDest);
						}
						catch (NomenclatorException e)
						{
							throw new CopyStatementException(e);
						}
				}
			}
		}
		return map;
	}

	/**
	 * Copies a statement (belonging or not to this context) into this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statement
	 *            The statement to be copied.
	 * @param initMap
	 *            The correspondence map to be used initially in the copy
	 *            operation. To be used when copying a statement from another
	 *            context.
	 * @return The new statement.
	 * @throws StatementException
	 * 
	 * @see #privateCopy(Transaction, Statement, Map)
	 */
	public Statement copy(Transaction transaction, Statement statement, Map<Statement, Statement> initMap) throws StatementException
	{
		Statement stDest = privateCopy(transaction, statement, initMap);
		return stDest;
	}

	/**
	 * Copies a list of statements (belonging or not to this context) into this
	 * context. Some of that statements can be specified to not be identified
	 * (usually the original identifiers are copied to the resulting
	 * statements).
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The list of statements to be copied (in that order).
	 * @param initMap
	 *            The correspondence map to be used initially in the copy
	 *            operation. To be used when copying a statement from another
	 *            context.
	 * @param excludeFromIdentify
	 *            The set of statements that will be excluded from identifying.
	 * @return The list of copied statements.
	 * @throws CopyStatementException
	 * 
	 * @see #privateCopy(Transaction, List, Map, Set)
	 */
	public List<Statement> copy(Transaction transaction, List<Statement> statements, Map<Statement, Statement> initMap, Set<Statement> excludeFromIdentify)
			throws StatementException
	{
		Map<Statement, Statement> map = privateCopy(transaction, statements, initMap, excludeFromIdentify);
		List<Statement> list = new ArrayList<Statement>();
		for (Statement st : statements)
			list.add(map.get(st));
		return list;
	}

	/**
	 * Copies a list of statements (belonging or not to this context) into this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The list of statements to be copied (in that order).
	 * @param initMap
	 *            The correspondence map to be used initially in the copy
	 *            operation. To be used when copying a statement from another
	 *            context.
	 * @return The list of copied statements.
	 * @throws CopyStatementException
	 * 
	 * @see #copy(Transaction, List, Map, Set)
	 */
	public List<Statement> copy(Transaction transaction, List<Statement> statements, Map<Statement, Statement> initMap) throws StatementException
	{
		return copy(transaction, statements, initMap, Collections.<Statement> emptySet());
	}

	/**
	 * Copies a statement (belonging or not to this context) into this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statement
	 *            The statement to be copied.
	 * @return The new statement.
	 * @throws StatementException
	 * 
	 * @see #copy(Transaction, Statement, Map)
	 */
	public Statement copy(Transaction transaction, Statement statement) throws StatementException
	{
		return copy(transaction, statement, Collections.<Statement, Statement> emptyMap());
	}

	/**
	 * Copies a list of statements (belonging or not to this context) into this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The list of statements to be copied (in that order).
	 * @return The list of copied statements.
	 * @throws CopyStatementException
	 * 
	 * @see #copy(Transaction, List, Map)
	 */
	public List<Statement> copy(Transaction transaction, List<Statement> statements) throws StatementException
	{
		return copy(transaction, statements, Collections.<Statement, Statement> emptyMap());
	}

	/**
	 * Copies a list of statements (belonging or not to this context) into this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param statements
	 *            The list of statements to be copied (in that order).
	 * @param excludeFromIdentify
	 *            The set of statements that will be excluded from identifying.
	 * @return The list of copied statements.
	 * @throws CopyStatementException
	 * 
	 * @see #copy(Transaction, List, Map, Set)
	 */
	public List<Statement> copy(Transaction transaction, List<Statement> statements, Set<Statement> excludeFromIdentify) throws StatementException
	{
		return copy(transaction, statements, Collections.<Statement, Statement> emptyMap(), excludeFromIdentify);
	}

	@Override
	protected void trace(Transaction transaction, PrintStream out, String indent)
	{
		super.trace(transaction, out, indent);
		indent += " ";
		for (Statement st : localDependencySortedStatements(transaction))
			st.trace(transaction, out, indent);
		out.println(indent + "|- " + getConsequent().toString(variableToIdentifier(transaction)));

	}

	/**
	 * Decides if a statement is a independent of this context. That is, it
	 * doesn't depends on any of the substatements of this context and so, it
	 * can be moved to the upper context.
	 * 
	 * @param transaction
	 *            The transaction used in this operation.
	 * @param statement
	 *            The statement to decide its independence.
	 * @return The decision.
	 */
	public boolean independent(Transaction transaction, Statement statement)
	{
		for (Statement dep : statement.dependencies(transaction))
		{
			if (getLocalStatements(transaction).containsKey(dep.getVariable()))
				return false;
		}
		if (statement instanceof Context)
		{
			Context ctx = (Context) statement;
			CloseableIterator<Map.Entry<IdentifiableVariableTerm, Statement>> iterator = ctx.getLocalStatements(transaction).entrySet().iterator();
			try
			{
				while (iterator.hasNext())
				{
					Statement st = iterator.next().getValue();
					if (!independent(transaction, st))
						return false;
				}
			}
			finally
			{
				iterator.close();
			}
		}
		return true;
	}

	public class DependentStatementException extends ContextException
	{
		private static final long serialVersionUID = 3735208237515507126L;
	}

	public class CantMoveAssumptionException extends ContextException
	{
		private static final long serialVersionUID = -126544398390279379L;
	}

	/**
	 * Does that identifier identify a local statement?
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @param id
	 *            The identifier
	 * @return Does it?
	 */
	public boolean isLocalIdentifier(Transaction transaction, Identifier id)
	{
		return getNomenclator(transaction).isLocalIdentifier(id);
	}

	/**
	 * Return the number of local statements in this contexts (including the
	 * assumptions).
	 * 
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The number of local statements.
	 */
	public int numLocalStatements(Transaction transaction)
	{
		return getLocalStatements(transaction).size();
	}

	/**
	 * The map from variables to statements of the local statements in this
	 * context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The map
	 * 
	 * @see #getLocalStatements(Transaction)
	 */
	public CloseableMap<IdentifiableVariableTerm, Statement> localStatements(Transaction transaction)
	{
		return new UnmodifiableCloseableMap<>(getLocalStatements(transaction));
	}

	@Override
	public Set<Statement> dependenciesThisAndDescendents(Transaction transaction)
	{
		return new CombinedSet<>(super.dependenciesThisAndDescendents(transaction), dependenciesDescendents(transaction));
	}

	/**
	 * The set of all the dependencies of all the statements that descend from
	 * this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the map.
	 * @return The set.
	 */
	public Set<Statement> dependenciesDescendents(Transaction transaction)
	{
		Set<Statement> dependencies = new HashSet<Statement>();
		for (Statement st : descendentStatements(transaction))
		{
			for (Statement dep : st.dependencies(transaction))
			{
				if (!isDescendent(transaction, dep))
					dependencies.add(dep);
			}
		}
		return dependencies;
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
		while (true)
		{
			if (equals(st))
				return true;
			if (st instanceof RootContext)
				return false;
			st = st.getContext(transaction);
		}

	}

	/**
	 * Creates a {@link Catalog} for this context.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on the catalog.
	 * @return The catalog.
	 * 
	 * @see Catalog
	 */
	public RootCatalog catalog()
	{
		return new RootCatalog(getPersistenceManager(), this);
	}

	public Catalog catalog(Namespace namespace)
	{
		try
		{
			Catalog catalog = catalog();
			for (String name : namespace.nameList())
				catalog = catalog.subCatalog(name);
			return catalog;
		}
		catch (InvalidNameException e)
		{
			throw new Error(e);
		}
	}

	@Override
	protected ContextLocal createLocal(Transaction transaction)
	{
		return (ContextLocal) super.createLocal(transaction);
	}

	@Override
	public ContextLocal getOrCreateLocal(Transaction transaction)
	{
		return (ContextLocal) super.getOrCreateLocal(transaction);
	}

	@Override
	public ContextLocal getLocal(Transaction transaction)
	{
		return (ContextLocal) super.getLocal(transaction);
	}

	@Override
	public void deleteLocal(Transaction transaction)
	{
		ContextLocal contextLocal = getLocal(transaction);
		if (contextLocal != null)
		{
			for (StatementLocal sl : getLocal(transaction).statementLocalSet(transaction))
				sl.getStatement(transaction).deleteLocal(transaction);
			super.deleteLocal(transaction);
		}
	}

	public CloseableSet<StatementAuthority> descendantContextAuthoritiesByConsequent(final Transaction transaction, Term consequent)
	{
		return new FilteredCloseableSet<>(new NotNullFilter<StatementAuthority>(), new BijectionCloseableSet<Context, StatementAuthority>(
				new Bijection<Context, StatementAuthority>()
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
				}, descendantContextsByConsequent(transaction, consequent)));
	}

	@Override
	public Context refresh(Transaction transaction)
	{
		return (Context) super.refresh(transaction);
	}

	public void addNomenclatorListener(Nomenclator.Listener listener)
	{
		getPersistenceManager().getListenerManager().getSubNomenclatorListeners().add(getUuid(), listener);
	}

	public void removeNomenclatorListener(Nomenclator.Listener listener)
	{
		getPersistenceManager().getListenerManager().getSubNomenclatorListeners().remove(getUuid(), listener);
	}

	public void addLocalStateListener(ContextLocal.StateListener listener)
	{
		getPersistenceManager().getListenerManager().getContextLocalStateListeners().add(getUuid(), listener);
	}

	public void removeLocalStateListener(ContextLocal.StateListener listener)
	{
		getPersistenceManager().getListenerManager().getContextLocalStateListeners().remove(getUuid(), listener);
	}

	public void createAuthorityRecursive(Transaction transaction, Person author) throws AuthorityWithNoParentException
	{
		getOrCreateAuthority(transaction, author);
		for (Statement st : descendentStatements(transaction))
			st.getOrCreateAuthority(transaction, author);
	}

	@Override
	public List<? extends Context> statementPath(Transaction transaction)
	{
		return statementPath(transaction, null);
	}

	@Override
	public List<? extends Context> statementPath(Transaction transaction, Context from)
	{
		return new BijectionList<Statement, Context>(new CastBijection<Statement, Context>(), new AdaptedList<>(super.statementPath(transaction, from)));
	}

	public UnpackedSignatureRequestSetByContextPath unpackedSignatureRequestSetByPath(Transaction transaction)
	{
		return getPersistenceManager().unpackedSignatureRequestSetByContextPath(transaction, this);
	}

	protected void deleteUnpackedSignatureRequestSetByPath(Transaction transaction)
	{
		for (UnpackedSignatureRequest unpackedSignatureRequest : unpackedSignatureRequestSetByPath(transaction))
			unpackedSignatureRequest.delete(transaction);
	}

	public SignatureRequestContextCreationDateCollection signatureRequestCreationDateCollection(Transaction transaction)
	{
		return getPersistenceManager().signatureRequestContextCreationDateCollection(transaction, getUuid());
	}

	@Override
	public ContextAuthority getAuthority(Transaction transaction)
	{
		return (ContextAuthority) super.getAuthority(transaction);
	}

	@Override
	public ContextAuthority createAuthorityOverwrite(Transaction transaction, Person author, Date creationDate) throws AuthorityWithNoParentException
	{
		return (ContextAuthority) super.createAuthorityOverwrite(transaction, author, creationDate);
	}

	@Override
	public ContextAuthority createAuthority(Transaction transaction, Person author) throws AuthorityCreationException
	{
		return (ContextAuthority) super.createAuthority(transaction, author);
	}

	@Override
	public ContextAuthority getOrCreateAuthority(Transaction transaction, Person author) throws AuthorityWithNoParentException
	{
		return (ContextAuthority) super.getOrCreateAuthority(transaction, author);
	}

	public Statement getStatementByHexRef(Transaction transaction, String hexRef)
	{
		Context ctx = this;
		while (true)
		{
			CloseableIterator<Statement> iterator = ctx.localStatements(transaction).values().iterator();
			try
			{
				while (iterator.hasNext())
				{
					Statement st = iterator.next();
					if (hexRef.equals(st.hexRef()))
						return st;
				}
			}
			finally
			{
				iterator.close();
			}
			if (ctx instanceof RootContext)
				break;
			ctx = ctx.getContext(transaction);
		}
		return null;
	}

}