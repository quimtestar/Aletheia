/*******************************************************************************
 * Copyright (c) 2014, 2017 Quim Testar.
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
package aletheia.persistence.collections.statement;

import aletheia.model.statement.Context;
import aletheia.model.term.SimpleTerm;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.PersistenceManagerDataStructure;
import aletheia.utilities.collections.CloseableSet;
import aletheia.utilities.collections.EmptyCloseableSet;

/**
 * The set of {@link Context}s that descend from a given one and whose
 * consequent matches a given one.
 *
 * @see PersistenceManager#descendantContextsByConsequent(aletheia.persistence.Transaction,
 *      Context, aletheia.model.term.SimpleTerm)
 */
public interface DescendantContextsByConsequent extends PersistenceManagerDataStructure, CloseableSet<Context>
{
	/**
	 * The context.
	 *
	 * @return The context.
	 */
	public Context getContext();

	/**
	 * The consequent.
	 *
	 * @return The consequent.
	 */
	public SimpleTerm getConsequent();

	boolean smaller(int size);

	static class Empty extends EmptyCloseableSet<Context> implements DescendantContextsByConsequent
	{
		private final Transaction transaction;
		private final Context context;

		public Empty(Transaction transaction, Context context)
		{
			super();
			this.transaction = transaction;
			this.context = context;
		}

		@Override
		public PersistenceManager getPersistenceManager()
		{
			return getPersistenceManager();
		}

		@Override
		public Transaction getTransaction()
		{
			return transaction;
		}

		@Override
		public Context getContext()
		{
			return context;
		}

		@Override
		public SimpleTerm getConsequent()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean smaller(int size)
		{
			return size > 0;
		}
	}

}
