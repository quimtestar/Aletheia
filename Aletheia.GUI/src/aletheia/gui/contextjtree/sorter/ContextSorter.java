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

public class ContextSorter extends StatementSorter
{

	protected ContextSorter(GroupSorter<? extends Statement> group, Context context)
	{
		super(group, context);
	}

	@Override
	public Context getStatement()
	{
		return (Context) super.getStatement();
	}

	@Override
	public Context getStatement(Transaction transaction)
	{
		return (Context) super.getStatement(transaction);
	}

	public ContextGroupSorter makeContextGroupSorter()
	{
		return new ContextGroupSorter(this);
	}

}
