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

import aletheia.model.authority.StatementAuthority;
import aletheia.model.parameteridentification.ParameterIdentification;
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
	 * @param valueProof
	 *
	 * @throws StatementException
	 */
	protected Declaration(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, Context context, Term value, Statement valueProof)
			throws StatementException
	{
		super(persistenceManager, transaction, DeclarationEntity.class, uuid, context, computeTerm(transaction, context, value, valueProof));
		if (!value.castFree())
			throw new NonCastFreeStatementException();
		Set<VariableTerm> undefined = context.undefinedVariables(transaction, value);
		if (!undefined.isEmpty())
			throw new UndefinedVariableStatementException(context, transaction, undefined);
		getEntity().setValue(value);
		getEntity().setValueProofUuid(valueProof.getUuid());
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
		uuidDependencies.add(valueProof.getUuid());
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

	public static abstract class InvalidStatementException extends DeclarationException
	{
		private static final long serialVersionUID = -6677050304893682802L;
		private final Statement statement;

		private InvalidStatementException(String messagePrefix, Statement statement)
		{
			super(messagePrefix + (statement.getIdentifier() == null ? statement.getVariable().toString() : statement.getIdentifier().toString()));
			this.statement = statement;
		}

		public Statement getStatement()
		{
			return statement;
		}

	}

	public static abstract class ValueProofStatementException extends InvalidStatementException
	{

		private static final long serialVersionUID = -6685092308358744436L;

		private ValueProofStatementException(String messagePrefix, Statement valueProof)
		{
			super(messagePrefix, valueProof);
		}

		public Statement getValueProof()
		{
			return getStatement();
		}

	}

	public static class ValueProofStatementNotInContextException extends ValueProofStatementException
	{
		private static final long serialVersionUID = -3171924267152732609L;

		private ValueProofStatementNotInContextException(Statement valueProof)
		{
			super("Value proof statement not in context: ", valueProof);
		}
	}

	public static class ValueProofStatementDoesntMatchException extends ValueProofStatementException
	{
		private static final long serialVersionUID = -4248408870645572029L;
		private final Term value;

		private ValueProofStatementDoesntMatchException(Statement valueProof, Term value)
		{
			super("Instance type does not match with proof statement's term: ", valueProof);
			this.value = value;
		}

		public Term getValue()
		{
			return value;
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
	 * @param valueProof
	 * @return The computed term.
	 * @throws DeclarationException
	 */
	private static Term computeTerm(Transaction transaction, Context context, Term value, Statement valueProof) throws DeclarationException
	{
		if (value.getType() == null)
		{
			String svalue = value.toString(context.variableToIdentifier(transaction));
			throw new DeclarationException("'" + svalue + "' has no type");
		}
		if (!(context.statements(transaction).containsKey(valueProof.getVariable())))
			throw new ValueProofStatementNotInContextException(valueProof);
		if (!value.getType().equals(valueProof.getTerm()))
			throw new ValueProofStatementDoesntMatchException(valueProof, value);
		return value.getType();
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

	public ParameterIdentification getValueParameterIdentification()
	{
		return getEntity().getValueParameterIdentification();
	}

	private void setValueParameterIdentification(ParameterIdentification valueParameterIdentification)
	{
		getEntity().setValueParameterIdentification(valueParameterIdentification);
	}

	private void setValueParameterIdentification(Transaction transaction, ParameterIdentification valueParameterIdentification, boolean force)
			throws SignatureIsValidException
	{
		lockAuthority(transaction);
		StatementAuthority statementAuthority = getAuthority(transaction);
		if (statementAuthority != null)
		{
			if (force)
				statementAuthority.clearSignatures(transaction);
			else if (statementAuthority.isValidSignature())
				throw new SignatureIsValidException("Can't update parameter identifications with valid signatures");
		}
		setValueParameterIdentification(valueParameterIdentification);
		persistenceUpdate(transaction);
		Iterable<StateListener> listeners = stateListeners();
		synchronized (listeners)
		{
			for (StateListener listener : listeners)
				listener.valueParameterIdentificationUpdated(transaction, this, valueParameterIdentification);
		}
	}

	public void updateValueParameterIdentification(Transaction transaction, ParameterIdentification valueParameterIdentification, boolean force)
			throws SignatureIsValidException
	{
		Declaration declaration = refresh(transaction);
		if (declaration != null)
			declaration.setValueParameterIdentification(transaction, valueParameterIdentification, force);

	}

	public void updateValueParameterIdentification(Transaction transaction, ParameterIdentification valueParameterIdentification)
			throws SignatureIsValidException
	{
		updateValueParameterIdentification(transaction, valueParameterIdentification, false);
	}

	/**
	 * The UUID of the value proof statement of this declaration.
	 *
	 * @return The UUID.
	 */
	public UUID getValueProofUuid()
	{
		return getEntity().getValueProofUuid();
	}

	/**
	 * The value proof statement of this declaration
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The value proof statement.
	 */
	public Statement getValueProof(Transaction transaction)
	{
		return getPersistenceManager().getStatement(transaction, getValueProofUuid());
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
		if (!getValueProof(transaction).isProved())
			return false;
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
		return super.toString(transaction) + " [Declaration: " + getValue().toString(parentVariableToIdentifier(transaction))
				+ (getValueProof(transaction).getVariable().equals(getValue()) ? "" : " ~~ " + getValueProof(transaction).label()) + "]";
	}

	@Override
	public Declaration refresh(Transaction transaction)
	{
		return (Declaration) super.refresh(transaction);
	}

	@Override
	protected Declaration undeleteStatement(Transaction transaction, Context context) throws UndeleteStatementException
	{
		try
		{
			return context.declare(transaction, getUuid(), getValue(), getValueProof(transaction));
		}
		catch (StatementException e)
		{
			throw new UndeleteStatementException(e);
		}
	}

}
