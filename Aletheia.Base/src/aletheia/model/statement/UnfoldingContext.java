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

import java.util.UUID;

import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.model.term.Term.TypeException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.statement.UnfoldingContextEntity;

/**
 * <p>
 * An unfolding context statement is a special context statement where the term
 * used to establish its antecedents (and so, construct its assumptions) and
 * consequent (the inner term) is computed replacing a variable of the
 * statements term that is associated with a {@linkplain Declaration declaration
 * statement} with its value on the statement's term (the outer term). In a
 * regular context statement the inner and outer terms are the same.
 * </p>
 * <p>
 * These kind of statements are used to establish the properties of declared
 * variables.
 * </p>
 */
public class UnfoldingContext extends Context
{
	/**
	 * Creates a new unfolding context statement from scratch.
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
	 *            The context that enclosures this context statement.
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @param declaration
	 *            The declaration to be unfolded.
	 */
	protected UnfoldingContext(Transaction transaction, PersistenceManager persistenceManager, UUID uuid, Context context, Term term, Declaration declaration)
			throws StatementException
	{
		super(persistenceManager, transaction, UnfoldingContextEntity.class, uuid, context, term, computeTerm(transaction, context, term, declaration));
		getEntity().setDeclarationUuid(declaration.getUuid());
		getEntity().getUuidDependencies().add(declaration.getUuid());
	}

	/**
	 * Creates a unfolding context that envelopes an already existing
	 * {@link UnfoldingContextEntity}
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The persistence entity that will be enveloped in this
	 *            statement.
	 */
	public UnfoldingContext(PersistenceManager persistenceManager, UnfoldingContextEntity entity)
	{
		super(persistenceManager, entity);
	}

	public static abstract class UnfoldingContextException extends ContextException
	{
		private static final long serialVersionUID = 4279182905943076126L;

		public UnfoldingContextException()
		{
			super();
		}

		public UnfoldingContextException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UnfoldingContextException(String message)
		{
			super(message);
		}

		public UnfoldingContextException(Throwable cause)
		{
			super(cause);
		}
	}

	public static class BadDeclarationUnfoldingContextException extends UnfoldingContextException
	{
		private static final long serialVersionUID = -3002748284467245523L;

		private final Declaration declaration;

		public BadDeclarationUnfoldingContextException(Declaration declaration)
		{
			super();
			this.declaration = declaration;
		}

		protected Declaration getDeclaration()
		{
			return declaration;
		}

	}

	public static class UnfoldingContextReplaceException extends UnfoldingContextException
	{
		private static final long serialVersionUID = -8033335562227425924L;

		private final TypeException exception;

		public UnfoldingContextReplaceException(TypeException exception)
		{
			super();
			this.exception = exception;
		}

		public TypeException getException()
		{
			return exception;
		}
	}

	/**
	 * Computes the term that will be associated with a newly created unfolding
	 * context. Takes the term and replaces the declaration's variable with the
	 * declaration's value in it. Any exception thrown in the process is caught
	 * and embedded into an {@link UnfoldingContextException}
	 *
	 * @param transaction
	 *            The transaction used in the operation.
	 * @param context
	 *            The context of the specialization being constructed.
	 * @param term
	 *            The term of the unfolding statement being constructed.
	 * @param declaration
	 *            The declaration of the unfolding statement being constructed.
	 * @return The computed term.
	 * @throws UnfoldingContextException
	 */
	private static Term computeTerm(Transaction transaction, Context context, Term term, Declaration declaration) throws UnfoldingContextException
	{
		if (!context.statements(transaction).containsKey(declaration.getVariable()))
			throw new BadDeclarationUnfoldingContextException(declaration);
		try
		{
			return term.replace(declaration.getVariable(), declaration.getValue());
		}
		catch (ReplaceTypeException e)
		{
			throw new UnfoldingContextReplaceException(e);
		}
	}

	@Override
	public UnfoldingContextEntity getEntity()
	{
		return (UnfoldingContextEntity) super.getEntity();
	}

	/**
	 * The UUID of the declaration of this unfolding context.
	 *
	 * @return The UUID.
	 */
	public UUID getDeclarationUuid()
	{
		return getEntity().getDeclarationUuid();
	}

	/**
	 * The declaration statement associated to this unfolding context.
	 *
	 * @param transaction
	 *            The transaction to use in this operation.
	 * @return The declaration.
	 */
	public Declaration getDeclaration(Transaction transaction)
	{
		return (Declaration) getPersistenceManager().getStatement(transaction, getDeclarationUuid());
	}

	/**
	 * For a unfolding context statement to be proven, the method
	 * {@link Statement#calcProved(Transaction)} must return true and the
	 * declaration statement must be proven.
	 */
	@Override
	protected boolean calcProved(Transaction transaction)
	{
		if (!super.calcProved(transaction))
			return false;
		if (!getDeclaration(transaction).isProved())
			return false;
		return true;
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " [Unfolding: " + getDeclaration(transaction).getVariable().toString(parentVariableToIdentifier(transaction))
				+ "]";
	}

	@Override
	public UnfoldingContext refresh(Transaction transaction)
	{
		return (UnfoldingContext) super.refresh(transaction);
	}

	@Override
	protected UnfoldingContext undeleteStatement(Transaction transaction, Context context) throws UndeleteStatementException
	{
		try
		{
			return context.openUnfoldingSubContext(transaction, getUuid(), getTerm(), getDeclaration(transaction));
		}
		catch (StatementException e)
		{
			throw new UndeleteStatementException(e);
		}
	}

}
