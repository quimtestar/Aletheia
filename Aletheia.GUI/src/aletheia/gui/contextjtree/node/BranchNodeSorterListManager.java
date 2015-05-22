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
package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.BufferedList;

public class BranchNodeSorterListManager<S extends Statement>
{
	private final GroupSorter<S> groupSorter;
	private final BufferedList<Sorter> sorterList;

	public BranchNodeSorterListManager(GroupSorter<S> groupStatementSorter)
	{
		this.groupSorter = groupStatementSorter;
		this.sorterList = new BufferedList<Sorter>(groupStatementSorter);
	}

	GroupSorter<S> getGroupSorter()
	{
		return groupSorter;
	}

	public BufferedList<Sorter> getSorterList()
	{
		return sorterList;
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
