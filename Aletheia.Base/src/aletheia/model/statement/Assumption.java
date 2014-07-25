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
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.statement.AssumptionEntity;
import aletheia.persistence.entities.statement.StatementEntity;

/**
 * <p>
 * An assumption statement. The assumptions are the statements in a contexts
 * considered as hypothesis and are created in creation time of the context (in
 * some sense, they are part of the identity of their contexts itself). One can
 * say that they are inferred out of nothing. The only extra information that an
 * assumptions brings is an integer called 'order', which is immutable, and
 * corresponds to the position this assumption has on its context.
 * </p>
 * <p>
 * The {@link StatementEntity} associated to an assumption statement must be a
 * {@link AssumptionEntity}.
 * </p>
 */
public class Assumption extends Statement
{
	/**
	 * Creates a new assumption with the specified UUID.
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
	 *            The context
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @param order
	 *            The order this assumption has in its context.
	 * @throws StatementException
	 */
	protected Assumption(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, Context context, Term term, int order)
			throws StatementException
	{
		super(persistenceManager, transaction, AssumptionEntity.class, uuid, context, term);
		getEntity().setOrder(order);
	}

	/**
	 * Creates a new assumption assigning a new UUID.
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param transaction
	 *            The transaction associated to the persistence manager that
	 *            will be used in the creation of this statement.
	 * @param context
	 *            The context
	 * @param term
	 *            The term representing the mathematical sentence which this
	 *            statement represents, or the type of the variable associated
	 *            to this statement. Since the term associated to the statement
	 *            can't have projections pending, the actual term used is the
	 *            unprojection of this one.
	 * @param order
	 *            The order this assumption has in its context.
	 * @throws StatementException
	 */
	protected Assumption(PersistenceManager persistenceManager, Transaction transaction, Context context, Term term, int order) throws StatementException
	{
		this(persistenceManager, transaction, null, context, term, order);
	}

	/**
	 * Creates an assumption that envelopes an already existing
	 * {@link AssumptionEntity}
	 * 
	 * @param persistenceManager
	 *            The persistence manager that will manage the persistence state
	 *            of this statement.
	 * @param entity
	 *            The
	 */
	public Assumption(PersistenceManager persistenceManager, AssumptionEntity entity)
	{
		super(persistenceManager, entity);
	}

	@Override
	public AssumptionEntity getEntity()
	{
		return (AssumptionEntity) super.getEntity();
	}

	/**
	 * @return The order of this assumption
	 */
	public int getOrder()
	{
		return getEntity().getOrder();
	}

	@Override
	public String toString(Transaction transaction)
	{
		return super.toString(transaction) + " [Assumption: " + getOrder() + "]";
	}

	@Override
	public Assumption refresh(Transaction transaction)
	{
		return (Assumption) super.refresh(transaction);
	}

}