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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person;
import aletheia.model.authority.RootContextAuthority;
import aletheia.model.authority.StatementAuthority.AuthorityCreationException;
import aletheia.model.authority.StatementAuthority.AuthorityWithNoParentException;
import aletheia.model.authority.StatementAuthority.DependentUnpackedSignatureRequests;
import aletheia.model.identifier.Identifier;
import aletheia.model.local.RootContextLocal;
import aletheia.model.nomenclator.Nomenclator;
import aletheia.model.nomenclator.RootNomenclator;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaTermParser;
import aletheia.parser.TermParserException;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceListenerManager.Listeners;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.statement.RootContextEntity;
import aletheia.utilities.collections.CloseableIterable;
import aletheia.utilities.collections.CloseableIterator;
import aletheia.utilities.collections.CloseableMap;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.UnmodifiableCloseableMap;

/**
 * <p>
 * The root context is a special context statement that serves as an ancestor of
 * all the statements in the system. It is the only kind of statement that is
 * not in any context.
 * </p>
 * <p>
 * Since it is not in any context, the term of this context can't have any
 * independent variable.
 * </p>
 */
public class RootContext extends Context
{
	private static final Logger logger = LoggerManager.instance.logger();

	public static interface TopStateListener extends PersistenceListener
	{
		public void rootContextAdded(Transaction transaction, RootContext rootContext);

		public void rootContextDeleted(Transaction transaction, RootContext rootContext, Identifier identifier);

	}

	/**
	 * Creates a root context from scratch.
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 *
	 * @throws StatementException
	 */
	private RootContext(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, List<UUID> uuidAssumptions, Term term)
			throws StatementException
	{
		super(persistenceManager, transaction, RootContextEntity.class, uuid, uuidAssumptions, null, term);
	}

	/**
	 * Creates a context that envelopes an already existing
	 * {@link RootContextEntity}
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The persistence entity that will be enveloped in this root
	 *            context statement.
	 */
	public RootContext(PersistenceManager persistenceManager, RootContextEntity entity)
	{
		super(persistenceManager, entity);
	}

	/**
	 * Since the root context statement is the only statement that isn't in any
	 * context, this method always throws and
	 * {@link UnsupportedOperationException}
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Context getContext(Transaction transaction) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableMap<IdentifiableVariableTerm, Statement> statements(Transaction transaction)
	{
		return new UnmodifiableCloseableMap<>(getLocalStatements(transaction));
	}

	@Override
	public CloseableMap<Term, CloseableSet<Statement>> statementsByTerm(Transaction transaction)
	{
		return getLocalStatementsByTerm(transaction);
	}

	/**
	 * Creates the root context statement in an empty persistence environment.
	 *
	 * @param persistenceManager
	 *            The persistence manager associated to this statement.
	 * @param transaction
	 *            The transaction that will be used in this operation.
	 * @return The root context statement.
	 * @throws StatementException
	 */
	public static RootContext create(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, List<UUID> uuidAssumptions, Term term)
			throws StatementException
	{
		RootContext rootContext = new RootContext(persistenceManager, transaction, uuid, uuidAssumptions, term);
		rootContext.persistenceUpdate(transaction);
		checkProved(transaction, Arrays.<Statement> asList(rootContext));
		rootContext = rootContext.refresh(transaction);
		Listeners<TopStateListener> listeners = persistenceManager.getListenerManager().getRootContextTopStateListeners();
		synchronized (listeners)
		{
			for (TopStateListener l : listeners)
				l.rootContextAdded(transaction, rootContext);
		}
		return rootContext;
	}

	public static RootContext create(PersistenceManager persistenceManager, Transaction transaction, Term term) throws StatementException
	{
		return create(persistenceManager, transaction, null, null, term);
	}

	public static RootContext create(PersistenceManager persistenceManager, Transaction transaction, String termString)
			throws TermParserException, StatementException
	{
		return create(persistenceManager, transaction, AletheiaTermParser.parseTerm(termString));
	}

	@Override
	public RootContextLocal getLocal(Transaction transaction)
	{
		return (RootContextLocal) super.getLocal(transaction);
	}

	@Override
	public RootContextLocal getOrCreateLocal(Transaction transaction)
	{
		return (RootContextLocal) super.getOrCreateLocal(transaction);
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " [Root]";
	}

	@Override
	public void delete(Transaction transaction) throws SignatureIsValidException
	{
		try
		{
			deleteUnpackedSignatureRequestSetByPath(transaction);
			Stack<Statement> stack = new Stack<>();
			stack.addAll(localStatements(transaction).values());
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
						if (!locals.isEmpty())
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
							for (StateListener listener : listeners)
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
			deleteAuthority(transaction, true);
			deleteLocal(transaction);
			Identifier id = identifier(transaction);
			getPersistenceManager().deleteStatement(transaction, this);
			Listeners<TopStateListener> listeners = getPersistenceManager().getListenerManager().getRootContextTopStateListeners();
			synchronized (listeners)
			{
				for (TopStateListener l : listeners)
					l.rootContextDeleted(transaction, this, id);
			}
		}
		catch (DependentUnpackedSignatureRequests e)
		{
			throw new Error(e);
		}
	}

	@Override
	public void deleteCascade(Transaction transaction) throws SignatureIsValidException
	{
		delete(transaction);
	}

	public static void delete(Transaction transaction, CloseableIterable<? extends RootContext> rootContexts) throws SignatureIsValidException
	{
		CloseableIterator<? extends RootContext> iterator = rootContexts.iterator();
		try
		{
			while (iterator.hasNext())
				iterator.next().delete(transaction);
		}
		finally
		{
			iterator.close();
		}
	}

	public static void deleteCascade(Transaction transaction, CloseableIterable<? extends RootContext> rootContexts) throws SignatureIsValidException
	{
		delete(transaction, rootContexts);
	}

	private RootNomenclator rootNomenclator(Transaction transaction)
	{
		return new RootNomenclator(getPersistenceManager(), transaction, this);
	}

	@Override
	public RootNomenclator getParentNomenclator(Transaction transaction)
	{
		return rootNomenclator(transaction);
	}

	@Override
	public RootContext refresh(Transaction transaction)
	{
		return (RootContext) super.refresh(transaction);
	}

	public void addRootNomenclatorListener(Nomenclator.Listener listener)
	{
		getPersistenceManager().getListenerManager().getRootNomenclatorListeners().add(getUuid(), listener);
	}

	public void removeRootNomenclatorListener(Nomenclator.Listener listener)
	{
		getPersistenceManager().getListenerManager().getRootNomenclatorListeners().remove(getUuid(), listener);
	}

	@Override
	public Set<Statement> localDependencies(Transaction transaction)
	{
		return Collections.emptySet();
	}

	public UUID getSignatureUuid(Transaction transaction)
	{
		RootContextAuthority rootContextAuthority = getAuthority(transaction);
		if (rootContextAuthority == null)
			return null;
		return rootContextAuthority.getSignatureUuid();
	}

	@Override
	public RootContextAuthority getAuthority(Transaction transaction)
	{
		return (RootContextAuthority) super.getAuthority(transaction);
	}

	@Override
	public RootContextAuthority createAuthorityOverwrite(Transaction transaction, Person author, Date creationDate) throws AuthorityWithNoParentException
	{
		return (RootContextAuthority) super.createAuthorityOverwrite(transaction, author, creationDate);
	}

	@Override
	public RootContextAuthority createAuthority(Transaction transaction, Person author) throws AuthorityCreationException
	{
		return (RootContextAuthority) super.createAuthority(transaction, author);
	}

	@Override
	public RootContextAuthority getOrCreateAuthority(Transaction transaction, Person author) throws AuthorityWithNoParentException
	{
		return (RootContextAuthority) super.getOrCreateAuthority(transaction, author);
	}

}
