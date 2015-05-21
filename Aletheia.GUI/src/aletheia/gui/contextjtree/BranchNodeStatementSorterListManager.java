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
package aletheia.gui.contextjtree;

import java.util.Collection;

import aletheia.gui.contextjtree.statementsorter.GroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.StatementSorter;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.BufferedList;

public class BranchNodeStatementSorterListManager<S extends Statement>
{
	private final GroupStatementSorter<S> groupStatementSorter;
	private final BufferedList<StatementSorter<S>> statementSorterList;

	public BranchNodeStatementSorterListManager(GroupStatementSorter<S> groupStatementSorter)
	{
		this.groupStatementSorter=groupStatementSorter;
		this.statementSorterList = new BufferedList<StatementSorter<S>>(groupStatementSorter);
	}

	
	GroupStatementSorter<S> getGroupStatementSorter()
	{
		return groupStatementSorter;
	}

	public BufferedList<StatementSorter<S>> getStatementSorterList()
	{
		return statementSorterList;
	}

	public boolean checkStatementInsert(Statement statement)
	{
		//TODO
		return false;
	}

	public boolean checkStatementRemove(Statement statement)
	{
		//TODO
		return false;
	}

	

}
