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
package aletheia.prooffinder;

import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;

public class VirtualStatement
{
	private final PureQueueSubEntry pureQueueSubEntry;
	private final VariableTerm variable;
	private final int order;

	public VirtualStatement(PureQueueSubEntry pureQueueSubEntry, VariableTerm variable, int order)
	{
		super();
		this.pureQueueSubEntry = pureQueueSubEntry;
		this.variable = variable;
		this.order = order;
	}

	public PureQueueSubEntry getPureQueueSubEntry()
	{
		return pureQueueSubEntry;
	}

	public VariableTerm getVariable()
	{
		return variable;
	}

	public int getOrder()
	{
		return order;
	}

	public Term getTerm()
	{
		return variable.getType();
	}

	public Context getContext()
	{
		return pureQueueSubEntry.getContext();
	}

	public String toString(Transaction transaction)
	{
		return variable.toString() + ": " + variable.getType().toString(getContext().variableToIdentifier(transaction));
	}

	@Override
	public String toString()
	{
		Transaction transaction = pureQueueSubEntry.getCandidateFinder().getPersistenceManager().beginDirtyTransaction();
		try
		{
			return toString(transaction);
		}
		finally
		{
			transaction.abort();
		}
	}

}
