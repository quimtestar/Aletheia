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

import java.util.Set;
import java.util.UUID;

import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.UnfoldingContextsByDeclaration;
import aletheia.persistence.entities.statement.DeclarationEntity;

/**
 * <p>
 * A declaration is a statement that expresses that its variable will be treated
 * as a synonym of an expressible term with the already-existent variables of
 * it's context.
 * </p>
 * <p>
 * The mentioned term will be called the <i>value</i> of the declaration. The
 * term of this kind of statements will be computed as the type of the value
 * term (so both the value and the variable it comes to be equivalent to it have
 * the same type).
 * </p>
 * <p>
 * To relate a declaration to its value, the {@linkplain UnfoldingContext
 * unfolding context statement} is used.
 * </p>
 * 
 * 
 */
public class Declaration extends Statement
{
	/**
	 * Creates a new declaration statement from scratch.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param uuid
	 *            The UUID associated to this statement (i.e. the variable that
	 *            identifies this statement). Used as unique identifier of a
	 *            statement. If null, a new one will be generated.
	 * @param context
	 *            The context that enclosures this declaration statement.
	 * @param value
	 *            The value of this declaration.
	 * 
	 * @throws StatementException
	 */
	protected Declaration(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, Context context, Term value) throws StatementException
	{
		super(persistenceManager, transaction, DeclarationEntity.class, uuid, context, computeTerm(transaction, context, value));
		Set<VariableTerm> undefined = context.undefinedVariables(transaction, value);
		if (!undefined.isEmpty())
			throw new UndefinedVariableStatementException(context, transaction, undefined);
		getEntity().setValue(value);
		Set<UUID> uuidDependencies = getEntity().getUuidDependencies();
		try
		{
			for (IdentifiableVariableTerm v : value.freeIdentifiableVariables())
				uuidDependencies.add(v.getUuid());
		}
		catch (ClassCastException e)
		{
			throw new FreeVariableNotIdentifiableStatementException(e);
		}
	}

	public static class DeclarationException extends StatementException
	{
		private static final long serialVersionUID = 8350596875327082405L;

		public DeclarationException()
		{
			super();
		}

		public DeclarationException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public DeclarationException(String message)
		{
			super(message);
		}

		public DeclarationException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Computes the term of the statement, that is, the value's type. Any
	 * exception thrown while the type computation will be caught and embedded
	 * into a {@link DeclarationException}
	 * 
	 * @param transaction
	 *            The transaction used in the operation.
	 * @param context
	 *            The context of the declaration being constructed.
	 * @param value
	 *            The value.
	 * @return The computed term.
	 * @throws DeclarationException
	 */
	private static Term computeTerm(Transaction transaction, Context context, Term value) throws DeclarationException
	{
		if (value.getType() == null)
		{
			String svalue = value.toString(context.variableToIdentifier(transaction));
			throw new DeclarationException("'" + svalue + "' has no type");
		}
		return value.getType();
	}

	/**
	 * Calls to
	 * {@link #Declaration(PersistenceManager, Transaction, UUID, Context, Term)}
	 * with the uuid set to null, so a new UUID will be generated for the
	 * statement.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction to be used in the creation of this statement.
	 * @param context
	 *            The context that enclosures this declaration statement.
	 * @param value
	 *            The value of this declaration.
	 * @throws StatementException
	 */
	protected Declaration(PersistenceManager persistenceManager, Transaction transaction, Context context, Term value) throws StatementException
	{
		this(persistenceManager, transaction, null, context, value);
	}

	/**
	 * Creates a declaration statement that envelopes an already existing
	 * {@link DeclarationEntity}
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The persistence entity that will be enveloped in this
	 *            statement.
	 */
	public Declaration(PersistenceManager persistenceManager, DeclarationEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public DeclarationEntity getEntity()
	{
		return (DeclarationEntity) super.getEntity();
	}

	/**
	 * @return The value of this declaration.
	 */
	public Term getValue()
	{
		return getEntity().getValue();
	}

	/**
	 * For a declaration to be proven, the method
	 * {@link Statement#calcProved(Transaction)} must return true and the
	 * statements associated to all the free variables of the value must be
	 * proven.
	 */
	@Override
	protected boolean calcProved(Transaction transaction)
	{
		if (!super.calcProved(transaction))
			return false;
		for (VariableTerm var : getValue().freeVariables())
		{
			Statement st = getPersistenceManager().statements(transaction).get(var);
			if (!st.isProved())
				return false;
		}
		return true;

	}

	/**
	 * Constructs the set of unfolding contexts that unfold this declaration.
	 * 
	 * @param transaction
	 *            The transaction to be used in the operations on this set.
	 * @return The set.
	 * 
	 * @see UnfoldingContext
	 * @see UnfoldingContextsByDeclaration
	 */
	public UnfoldingContextsByDeclaration unfoldingContexts(Transaction transaction)
	{
		return getPersistenceManager().unfoldingContextsByDeclaration(transaction, this);
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " [Declaration: " + getValue().toString(parentVariableToIdentifier(transaction)) + "]";
	}

	@Override
	public Declaration refresh(Transaction transaction)
	{
		return (Declaration) super.refresh(transaction);
	}

}
