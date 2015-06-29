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

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextGroupSorter extends StatementGroupSorter implements TopGroupSorter<Statement>
{
	private final ContextSorter contextSorter;

	protected ContextGroupSorter(ContextSorter contextSorter)
	{
		super(null, null, contextSorter.getStatement());
		this.contextSorter = contextSorter;
	}

	public ContextSorter getContextSorter()
	{
		return contextSorter;
	}

	@Override
	public StatementGroupSorter getGroup()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Context getStatement(Transaction transaction)
	{
		return getContext();
	}

	@Override
	public boolean degenerate(Transaction transaction)
	{
		return false;
	}

}
