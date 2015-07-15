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
package aletheia.model.nomenclator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NamespaceExtreme;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.statement.Statement.SignatureIsValidException;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.persistence.PersistenceListener;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.LocalIdentifierToStatement;
import aletheia.persistence.collections.statement.LocalStatementToIdentifier;
import aletheia.protocol.Exportable;
import aletheia.utilities.collections.AbstractReadOnlyMap;
import aletheia.utilities.collections.AbstractReadOnlySortedMap;
import aletheia.utilities.collections.CloseableMap;

/**
 * <p>
 * Class representation of a nomenclator.
 * </p>
 * <p>
 * A nomenclator is associated to a persistence transaction that will be used on
 * every operation with this data structure. Note that the nomenclator will only
 * be usable while the transaction is alive.
 * </p>
 * <p>
 * This class is abstract, a subclass of this will be used depending on if the
 * context is the {@link RootContext} or not.
 * </p>
 *
 * @see aletheia.model.nomenclator
 */
public abstract class Nomenclator implements Serializable, Exportable
{
	private static final long serialVersionUID = 3932768133280648018L;

	public abstract class NomenclatorException extends Exception
	{
		private static final long serialVersionUID = -7221581138887786558L;

		protected NomenclatorException()
		{
			super();
		}

		protected NomenclatorException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected NomenclatorException(String message)
		{
			super(message);
		}

		protected NomenclatorException(Throwable cause)
		{
			super(cause);
		}

	}

	private final PersistenceManager persistenceManager;
	private final Transaction transaction;
	private final LocalIdentifierToStatement localIdentifierToStatement;
	private final LocalStatementToIdentifier localStatementToIdentifier;

	public interface Listener extends PersistenceListener
	{
		public void statementIdentified(Transaction transaction, Statement statement, Identifier identifier);

		public void statementUnidentified(Transaction transaction, Statement statement, Identifier identifier);
	}

	/**
	 * Creates a nomenclator for a given context with a given transaction.
	 *
	 * @param transaction
	 *            The transaction.
	 * @param context
	 *            The context.
	 */
	public Nomenclator(PersistenceManager persistenceManager, Transaction transaction, LocalIdentifierToStatement localIdentifierToStatement,
			LocalStatementToIdentifier localStatementToIdentifier)
	{
		super();
		this.persistenceManager = persistenceManager;
		this.transaction = transaction;
		this.localIdentifierToStatement = localIdentifierToStatement;
		this.localStatementToIdentifier = localStatementToIdentifier;
	}

	protected PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	/**
	 * @return The persistence transaction associated to this nomenclator.
	 */
	protected Transaction getTransaction()
	{
		return transaction;
	}

	protected LocalIdentifierToStatement getLocalIdentifierToStatement()
	{
		return localIdentifierToStatement;
	}

	protected LocalStatementToIdentifier getLocalStatementToIdentifier()
	{
		return localStatementToIdentifier;
	}

	/**
	 * A mapping from identifiers to accessible statements.
	 *
	 * @return The map.
	 */
	public abstract SortedMap<Identifier, Statement> identifierToStatement();

	/**
	 * A mapping from accessible statements to identifiers.
	 *
	 * @return The map.
	 */
	public abstract Map<Statement, Identifier> statementToIdentifier();

	public class AlreadyUsedIdentifierException extends NomenclatorException
	{
		private static final long serialVersionUID = 3024416208547624027L;

		private final Identifier identifier;

		private AlreadyUsedIdentifierException(Identifier identifier)
		{
			super();
			this.identifier = identifier;
		}

		@Override
		public String getMessage()
		{
			return "Already Used identifier: " + identifier;
		}

	}

	public class AlreadyIdentifiedStatementException extends NomenclatorException
	{
		private static final long serialVersionUID = 5777253096931848662L;

		private AlreadyIdentifiedStatementException(Transaction transaction, Statement statement)
		{
			super("Already identified statement: " + statement.identifier(transaction));
		}

	}

	public class InvalidIdentifierException extends NomenclatorException
	{
		private static final long serialVersionUID = -1154036450164596725L;

		private InvalidIdentifierException(Identifier identifier)
		{
			super("Invalid identifier: " + identifier);
		}

	}

	public class BadStatementException extends NomenclatorException
	{
		private static final long serialVersionUID = -7086854570954862190L;

		private BadStatementException()
		{
			super();
		}

		private BadStatementException(Throwable cause)
		{
			super(cause);
		}
	}

	public class StatementNotInContextException extends BadStatementException
	{
		private static final long serialVersionUID = 2169494984552597475L;

	}

	public class SignatureIsValidNomenclatorException extends NomenclatorException
	{
		private static final long serialVersionUID = 6746233943674285453L;

		private SignatureIsValidNomenclatorException(SignatureIsValidException cause)
		{
			super(cause.getMessage(), cause);
		}

	}

	protected abstract CloseableMap<IdentifiableVariableTerm, Statement> localStatements();

	protected abstract Map<IdentifiableVariableTerm, Statement> statements();

	public void identifyStatement(Identifier identifier, Statement statement) throws NomenclatorException
	{
		identifyStatement(identifier, statement, false);
	}

	/**
	 * Gives a local unidentified statement a new identifier.
	 *
	 * @param identifier
	 *            The identifier
	 * @param statement
	 *            The statement
	 * @throws NomenclatorException
	 */
	public void identifyStatement(Identifier identifier, Statement statement, boolean force) throws NomenclatorException
	{
		if (identifier instanceof NamespaceExtreme)
			throw new InvalidIdentifierException(identifier);
		if (!localStatements().containsKey(statement.getVariable()))
			throw new StatementNotInContextException();
		if (getLocalIdentifierToStatement().containsKey(identifier))
			throw new AlreadyUsedIdentifierException(identifier);
		if (getLocalStatementToIdentifier().containsKey(statement))
			throw new AlreadyIdentifiedStatementException(getTransaction(), statement);
		Statement statement_ = statement.refresh(getTransaction());
		try
		{
			statement_.setIdentifier(getTransaction(), identifier, force);
		}
		catch (SignatureIsValidException e)
		{
			throw new SignatureIsValidNomenclatorException(e);
		}
		Iterable<Listener> listeners = listeners();
		synchronized (listeners)
		{
			for (Listener listener : listeners)
				listener.statementIdentified(transaction, statement_, identifier);
		}
	}

	public class UnknownIdentifierException extends NomenclatorException
	{
		private static final long serialVersionUID = 1707776888772948093L;

	}

	public Statement unidentifyStatement(Identifier identifier) throws UnknownIdentifierException, SignatureIsValidNomenclatorException
	{
		return unidentifyStatement(identifier, false);
	}

	/**
	 * Unassigns an identifier from a local statement.
	 *
	 * @param identifier
	 *            The identifier to be unassigned.
	 * @return The statement that had that identifier.
	 * @throws UnknownIdentifierException
	 * @throws SignatureIsValidNomenclatorException
	 */
	public Statement unidentifyStatement(Identifier identifier, boolean force) throws UnknownIdentifierException, SignatureIsValidNomenclatorException
	{
		Statement statement = getLocalIdentifierToStatement().get(identifier);
		if (statement == null)
			throw new UnknownIdentifierException();
		try
		{
			statement.setIdentifier(getTransaction(), null, force);
		}
		catch (SignatureIsValidException e)
		{
			throw new SignatureIsValidNomenclatorException(e);
		}
		Iterable<Listener> listeners = listeners();
		synchronized (listeners)
		{
			for (Listener listener : listeners)
				listener.statementUnidentified(transaction, statement, identifier);
		}
		return statement;
	}

	/**
	 * A mapping from identifiers to the variables of the statements that they
	 * identify.
	 *
	 * @return The map.
	 */
	public SortedMap<Identifier, IdentifiableVariableTerm> identifierToVariable()
	{
		final SortedMap<Identifier, Statement> i2s = identifierToStatement();
		final Map<Statement, Identifier> s2i = statementToIdentifier();
		return new AbstractReadOnlySortedMap<Identifier, IdentifiableVariableTerm>()
		{
			@Override
			public boolean containsKey(Object key)
			{
				return i2s.containsKey(key);
			}

			@Override
			public boolean containsValue(Object value)
			{
				return s2i.containsKey(statements().get(value));
			}

			@Override
			public IdentifiableVariableTerm get(Object key)
			{
				try
				{
					return i2s.get(key).getVariable();
				}
				catch (NullPointerException e)
				{
					return null;
				}
			}

			@Override
			public boolean isEmpty()
			{
				return i2s.isEmpty();
			}

			@Override
			public int size()
			{
				return i2s.size();
			}

			@Override
			public Comparator<? super Identifier> comparator()
			{
				return null;
			}

			@Override
			public Set<Entry<Identifier, IdentifiableVariableTerm>> entrySet()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Identifier firstKey()
			{
				return i2s.firstKey();
			}

			@Override
			public SortedMap<Identifier, IdentifiableVariableTerm> headMap(Identifier toKey)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<Identifier> keySet()
			{
				return i2s.keySet();
			}

			@Override
			public Identifier lastKey()
			{
				return i2s.lastKey();
			}

			@Override
			public SortedMap<Identifier, IdentifiableVariableTerm> subMap(Identifier fromKey, Identifier toKey)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public SortedMap<Identifier, IdentifiableVariableTerm> tailMap(Identifier fromKey)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Collection<IdentifiableVariableTerm> values()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * A mapping from the variables to the identifiers the statements associated
	 * to them hav in this nomenclator.
	 *
	 * @return The map.
	 */
	public Map<IdentifiableVariableTerm, Identifier> variableToIdentifier()
	{
		final SortedMap<Identifier, Statement> i2s = identifierToStatement();
		final Map<Statement, Identifier> s2i = statementToIdentifier();
		return new AbstractReadOnlyMap<IdentifiableVariableTerm, Identifier>()
		{
			@Override
			public boolean containsKey(Object key)
			{
				return s2i.containsKey(statements().get(key));
			}

			@Override
			public boolean containsValue(Object value)
			{
				return i2s.containsKey(value);
			}

			@Override
			public Identifier get(Object key)
			{
				Statement st = statements().get(key);
				if (st != null)
					return s2i.get(st);
				else
					return null;
			}

			@Override
			public boolean isEmpty()
			{
				return s2i.isEmpty();
			}

			@Override
			public int size()
			{
				return s2i.size();
			}

			@Override
			public Set<Entry<IdentifiableVariableTerm, Identifier>> entrySet()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<IdentifiableVariableTerm> keySet()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<Identifier> values()
			{
				return i2s.keySet();
			}

		};
	}

	/**
	 * True if and only if a given identifier is assigned to a local statement
	 * to the context.
	 *
	 * @param identifier
	 *            The identifier.
	 * @return Is it local?
	 */
	public boolean isLocalIdentifier(Identifier identifier)
	{
		return localIdentifierToStatement.containsKey(identifier);
	}

	/**
	 * A mapping from identifiers to local statements.
	 *
	 * @return The map.
	 */
	public SortedMap<Identifier, Statement> localIdentifierToStatement()
	{
		return Collections.unmodifiableSortedMap(localIdentifierToStatement);
	}

	/**
	 * A mapping from local statements to identifiers.
	 *
	 * @return The map.
	 */
	public Map<Statement, Identifier> localStatementToIdentifier()
	{
		return Collections.unmodifiableMap(localStatementToIdentifier);
	}

	public abstract void addListener(Listener listener);

	public abstract void removeListener(Listener listener);

	protected abstract Iterable<Listener> listeners();

}
