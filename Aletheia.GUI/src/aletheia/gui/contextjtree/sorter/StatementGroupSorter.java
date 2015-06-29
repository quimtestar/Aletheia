/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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
package aletheia.gui.contextjtree.sorter;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.statement.LocalSortedStatements;

public class StatementGroupSorter extends GroupSorter<Statement>
{
	private final Context context;

	protected StatementGroupSorter(StatementGroupSorter group, Identifier prefix, Context context)
	{
		super(group, prefix);
		this.context = context;
	}

	@Override
	public StatementGroupSorter getGroup()
	{
		return (StatementGroupSorter) super.getGroup();
	}

	public Context getContext()
	{
		return context;
	}

	@Override
	public Statement getStatement(Transaction transaction)
	{
		if (getPrefix() == null)
			return null;
		Statement statement = context.localIdentifierToStatement(transaction).get(getPrefix());
		if (statement instanceof Assumption)
			return null;
		return statement;
	}

	@Override
	public LocalSortedStatements sortedStatements(Transaction transaction)
	{
		LocalSortedStatements sortedStatements = context.localSortedStatements(transaction);
		if (getPrefix() != null)
			return sortedStatements.subSet(getPrefix(), getPrefix().terminator());
		else
			return sortedStatements;

	}

	@Override
	protected StatementGroupSorter subGroupSorter(Identifier prefix)
	{
		return new StatementGroupSorter(this, prefix, context);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatementGroupSorter other = (StatementGroupSorter) obj;
		if (context == null)
		{
			if (other.context != null)
				return false;
		}
		else if (!context.equals(other.context))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[context:" + context.label() + "]";
	}

}
