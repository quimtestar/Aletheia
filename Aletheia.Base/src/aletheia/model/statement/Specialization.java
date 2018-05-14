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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.TypeException;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.statement.SpecializationEntity;

/**
 * <p>
 * A specialization is a statement that is a restricted version of another
 * statement. It corresponds to a generalization of the modus ponens rule of
 * inference.
 * </p>
 * <p>
 * An specialization is constructed using the following parameters:
 * <ul>
 * <li><b>The general:</b>Another statement belonging to the same context.</li>
 * <li><b>The instance:</b>A term definable on the context and composable (in
 * terms of type-consistency) to the general's term</li>
 * </ul>
 * </p>
 * <p>
 * The term of an specialization is computed as the composition of the general's
 * term with the instance.
 * </p>
 *
 */
public class Specialization extends Statement
{
	/**
	 * Creates a new specialization statement from scratch.
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction associated to the persistence manager that
	 *            will be used in the creation of this statement.
	 * @param uuid
	 *            The UUID associated to this statement (i.e. the variable that
	 *            identifies this statement). Used as unique identifier of a
	 *            statement. If null, a new one will be generated.
	 * @param context
	 *            The context.
	 * @param general
	 *            The general statement used that is specialized.
	 * @param instance
	 *            The instance term used in the specialization.
	 * @param instanceProof
	 * @throws StatementException
	 */
	protected Specialization(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, Context context, Statement general, Term instance,
			Statement instanceProof) throws StatementException
	{
		super(persistenceManager, transaction, SpecializationEntity.class, uuid, context, computeTerm(transaction, context, general, instance, instanceProof));
		getEntity().setGeneralUuid(general.getUuid());
		getEntity().setInstance(instance);
		getEntity().setInstanceProofUuid(instanceProof.getUuid());
		Set<UUID> uuidDependencies = getEntity().getUuidDependencies();
		uuidDependencies.add(general.getUuid());
		try
		{
			for (IdentifiableVariableTerm v : instance.freeIdentifiableVariables())
				uuidDependencies.add(v.getUuid());
		}
		catch (ClassCastException e)
		{
			throw new FreeVariableNotIdentifiableStatementException(e);
		}
		uuidDependencies.add(instanceProof.getUuid());
	}

	/**
	 * Creates a statement that envelopes an already existing
	 * {@link SpecializationEntity}
	 *
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The persistence entity that will be enveloped in this
	 *            statement.
	 */
	public Specialization(PersistenceManager persistenceManager, SpecializationEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public SpecializationEntity getEntity()
	{
		return (SpecializationEntity) super.getEntity();
	}

	public static abstract class SpecializationException extends StatementException
	{
		private static final long serialVersionUID = -5181126631681562425L;

		private SpecializationException()
		{
			super();
		}

		private SpecializationException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private SpecializationException(String message)
		{
			super(message);
		}

		private SpecializationException(Throwable cause)
		{
			super(cause);
		}
	}

	public static abstract class InvalidStatementException extends SpecializationException
	{
		private static final long serialVersionUID = 8377894347587007924L;
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

	public static abstract class InvalidGeneralStatementException extends InvalidStatementException
	{
		private static final long serialVersionUID = 8127879566374729178L;

		private InvalidGeneralStatementException(String messagePrefix, Statement general)
		{
			super(messagePrefix, general);
		}

		public Statement getGeneral()
		{
			return getStatement();
		}

	}

	public static class GeneralStatementNotInContextException extends InvalidGeneralStatementException
	{
		private static final long serialVersionUID = 6462483287028995345L;

		private GeneralStatementNotInContextException(Statement general)
		{
			super("General statement not in context: ", general);
		}

	}

	public static class GeneralStatementNotComposableException extends InvalidGeneralStatementException
	{
		private static final long serialVersionUID = -6803016941538989470L;

		private GeneralStatementNotComposableException(Statement general)
		{
			super("General statement not composable: ", general);
		}
	}

	public static abstract class InvalidInstanceException extends SpecializationException
	{
		private static final long serialVersionUID = -7225829170291763310L;

		private InvalidInstanceException()
		{
			super();
		}

		private InvalidInstanceException(String message, Throwable cause)
		{
			super(message, cause);
		}

		private InvalidInstanceException(String message)
		{
			super(message);
		}

		private InvalidInstanceException(Throwable cause)
		{
			super(cause);
		}
	}

	public static class UndefinedVariablesInInstanceException extends InvalidInstanceException
	{
		private static final long serialVersionUID = -7006646986046587390L;

		private final Set<String> undefined;

		private UndefinedVariablesInInstanceException(Set<String> undefined)
		{
			this.undefined = undefined;
		}

		@Override
		public String getMessage()
		{
			StringBuffer sb = new StringBuffer("Undefined variables in instance: ");
			boolean first = true;
			for (String var : undefined)
			{
				if (!first)
					sb.append(", ");
				sb.append(var.toString());
				first = false;
			}
			return sb.toString();
		}

	}

	public static class BadInstanceException extends InvalidInstanceException
	{
		private static final long serialVersionUID = 1956058202547292190L;

		private final TypeException exception;

		private BadInstanceException(TypeException exception)
		{
			super();
			this.exception = exception;
		}

		public TypeException getException()
		{
			return exception;
		}

		@Override
		public String getMessage()
		{
			return exception.getMessage();
		}
	}

	public static abstract class InstanceProofStatementException extends InvalidStatementException
	{

		private static final long serialVersionUID = 3442764995403083380L;

		private InstanceProofStatementException(String messagePrefix, Statement instanceProof)
		{
			super(messagePrefix, instanceProof);
		}

		public Statement getInstanceProof()
		{
			return getStatement();
		}

	}

	public static class InstanceProofStatementNotInContextException extends InstanceProofStatementException
	{
		private static final long serialVersionUID = -3766232259419778304L;

		private InstanceProofStatementNotInContextException(Statement instanceProof)
		{
			super("Instance proof statement not in context: ", instanceProof);
		}
	}

	public static class InstanceProofStatementDoesntMatchException extends InstanceProofStatementException
	{
		private static final long serialVersionUID = -4818169169330766472L;
		private final Term instance;

		private InstanceProofStatementDoesntMatchException(Statement instanceProof, Term instance)
		{
			super("Instance type does not match with proof statement's term: ", instanceProof);
			this.instance = instance;
		}

		public Term getInstance()
		{
			return instance;
		}
	}

	/**
	 * Computes the term that will be associated with a newly created
	 * specialization. Makes a composition of the general's term with the
	 * instance, and any exception thrown in the process is caught and enveloped
	 * into {@link SpecializationException}.
	 *
	 * @param transaction
	 *            The transaction used in the operation.
	 * @param context
	 *            The context of the specialization being constructed.
	 * @param general
	 *            The general statement of the specialization being constructed.
	 * @param instance
	 *            The instance of the statement being constructed.
	 * @param instanceProof
	 * @return The computed term.
	 * @throws SpecializationException
	 */
	private static Term computeTerm(Transaction transaction, Context context, Statement general, Term instance, Statement instanceProof)
			throws SpecializationException
	{
		if (!context.statements(transaction).containsKey(general.getVariable()))
			throw new GeneralStatementNotInContextException(general);
		if (!(context.statements(transaction).keySet().containsAll(instance.freeVariables())))
		{
			Set<String> undefined = new HashSet<>();
			for (VariableTerm var : instance.freeVariables())
			{
				if (!context.statements(transaction).containsKey(var))
				{
					if (var instanceof IdentifiableVariableTerm)
					{
						IdentifiableVariableTerm idVar = (IdentifiableVariableTerm) var;
						Identifier id = context.variableToIdentifier(transaction).get(idVar);
						if (id != null)
							undefined.add(id.toString());
						else
							undefined.add(idVar.getUuid().toString());
					}
					else
						undefined.add(var.toString());
				}
			}
			throw new UndefinedVariablesInInstanceException(undefined);
		}
		if (!(context.statements(transaction).containsKey(instanceProof.getVariable())))
			throw new InstanceProofStatementNotInContextException(instanceProof);
		if (!instance.getType().equals(instanceProof.getTerm()))
			throw new InstanceProofStatementDoesntMatchException(instanceProof, instance);
		Term term = general.getTerm();
		try
		{
			return term.compose(instance);
		}
		catch (TypeException e)
		{
			throw new BadInstanceException(e);
		}
	}

	/**
	 * The UUID of the general statement of this specialization.
	 *
	 * @return The UUID.
	 */
	public UUID getGeneralUuid()
	{
		return getEntity().getGeneralUuid();
	}

	/**
	 * The general statement of this specialization
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The general statement.
	 */
	public Statement getGeneral(Transaction transaction)
	{
		return getPersistenceManager().getStatement(transaction, getGeneralUuid());
	}

	/**
	 * The instance term of this specialization.
	 *
	 * @return The instance.
	 */
	public Term getInstance()
	{
		return getEntity().getInstance();
	}

	/**
	 * The UUID of the instance proof statement of this specialization.
	 *
	 * @return The UUID.
	 */
	public UUID getInstanceProofUuid()
	{
		return getEntity().getInstanceProofUuid();
	}

	/**
	 * The instance proof statement of this specialization
	 *
	 * @param transaction
	 *            The transaction to be used in the operation.
	 * @return The instance proof statement.
	 */
	public Statement getInstanceProof(Transaction transaction)
	{
		return getPersistenceManager().getStatement(transaction, getInstanceProofUuid());
	}

	/**
	 * For a specialization statement to be proven, the method
	 * {@link Statement#calcProved(Transaction)} must return true, the general
	 * statement must be proven, and all the free variables of the instance must
	 * correspond to a proven statement.
	 */
	@Override
	protected boolean calcProved(Transaction transaction)
	{
		if (!super.calcProved(transaction))
			return false;
		if (!getGeneral(transaction).isProved())
			return false;
		for (VariableTerm var : getInstance().freeVariables())
		{
			if (!getPersistenceManager().statements(transaction).get(var).isProved())
				return false;
		}
		if (!getInstanceProof(transaction).isProved())
			return false;
		return true;
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " [Specialization: " + getGeneral(transaction).label() + " <- " + " "
				+ getInstance().toString(parentVariableToIdentifier(transaction))
				+ (getInstanceProof(transaction).getVariable().equals(getInstance()) ? "" : " ~~ " + getInstanceProof(transaction).label()) + "]";
	}

	@Override
	public Specialization refresh(Transaction transaction)
	{
		return (Specialization) super.refresh(transaction);
	}

	@Override
	protected Specialization undeleteStatement(Transaction transaction, Context context) throws UndeleteStatementException
	{
		try
		{
			return context.specialize(transaction, getUuid(), getGeneral(transaction), getInstance(), getInstanceProof(transaction));
		}
		catch (StatementException e)
		{
			throw new UndeleteStatementException(e);
		}
	}

}
