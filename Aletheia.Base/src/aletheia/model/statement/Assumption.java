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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.model.term.Term;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.statement.AssumptionEntity;
import aletheia.persistence.entities.statement.StatementEntity;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.primitive.ByteProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

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

	private static UUID generateUuid(UUID contextUuid, int order)
	{
		class UUIDGenerationData
		{
			final UUID contextUuid;
			final int order;

			public UUIDGenerationData(UUID contextUuid, int order)
			{
				super();
				this.contextUuid = contextUuid;
				this.order = order;
			}

		}

		class UUIDGenerationProtocol extends Protocol<UUIDGenerationData>
		{
			final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			final ByteProtocol byteProtocol = new ByteProtocol(0);

			public UUIDGenerationProtocol()
			{
				super(0);
			}

			@Override
			public void send(DataOutput out, UUIDGenerationData data) throws IOException
			{
				uuidProtocol.send(out, data.contextUuid);
				for (int x = data.order; x != 0; x >>>= 8)
					byteProtocol.send(out, (byte) (x & 0xff));
			}

			@Override
			public UUIDGenerationData recv(DataInput in) throws IOException, ProtocolException
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				throw new UnsupportedOperationException();
			}

		}

		return UUID.nameUUIDFromBytes(new UUIDGenerationProtocol().toByteArray(new UUIDGenerationData(contextUuid, order)));
	}

	/**
	 * Creates a new assumption with the specified UUID.
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
		super(persistenceManager, transaction, AssumptionEntity.class, generateUuid(context.getUuid(), order), context, term);
		getEntity().setOrder(order);
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

	@Override
	protected Assumption undeleteStatement(Transaction transaction, Context context) throws UndeleteStatementException
	{
		return context.assumptions(transaction).get(getOrder());
	}

}
