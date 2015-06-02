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
package aletheia.gui.contextjtree.node.old;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.Namespace;
import aletheia.model.statement.Statement;
import aletheia.utilities.collections.BufferedList;

public class BranchNodeSorterListManager<S extends Statement>
{
	private final BufferedList<Sorter> sorterList;
	private final Map<UUID, Integer> uuidIndexes;
	private final SortedMap<Namespace, Integer> prefixIndexes;

	public BranchNodeSorterListManager(GroupSorter<S> groupStatementSorter)
	{
		sorterList = new BufferedList<Sorter>(groupStatementSorter);
		uuidIndexes = new HashMap<UUID, Integer>();
		prefixIndexes = new TreeMap<Namespace, Integer>();
		for (ListIterator<Sorter> iterator = sorterList.listIterator(); iterator.hasNext();)
		{
			int i = iterator.nextIndex();
			Sorter sorter = iterator.next();
			if (sorter instanceof StatementSorter)
				uuidIndexes.put(((StatementSorter) sorter).getStatement().getUuid(), i);
			if (sorter.getPrefix() != null)
				prefixIndexes.put(sorter.getPrefix(), i);
		}
	}

	public BufferedList<Sorter> getSorterList()
	{
		return sorterList;
	}

	//TODO remove
	public boolean checkStatementInsert(Statement statement)
	{
		//TODO
		return false;
	}

	//TODO remove
	public boolean checkStatementRemove(Statement statement)
	{
		//TODO
		return false;
	}

	public StatementSorter findSingletonSorter(Statement statement)
	{
		Integer index = uuidIndexes.get(statement.getUuid());
		if (index == null)
			return null;
		StatementSorter singletonSorter = (StatementSorter) sorterList.get(index);
		Statement statement_ = singletonSorter.getStatement();
		if ((statement_.getIdentifier() == null) != (statement.getIdentifier() == null))
			return null;
		if ((statement_.getIdentifier() != null && !statement_.getIdentifier().equals(statement.getIdentifier())))
			return null;
		return singletonSorter;
	}

	public Sorter findSorter(Identifier identifier)
	{
		SortedMap<Namespace, Integer> head = prefixIndexes.headMap(identifier);
		if (head.isEmpty())
			return null;
		return sorterList.get(head.get(head.lastKey()));
	}

	public GroupSorter<?> findGroupSorter(Statement statement)
	{
		Identifier identifier = statement.getIdentifier();
		if (identifier == null)
			return null;
		Sorter sorter = findSorter(identifier);
		if (sorter == null)
			return null;
		if (!(sorter instanceof GroupSorter))
			return null;
		return (GroupSorter<?>) sorter;
	}

	public Sorter findSorter(Statement statement)
	{
		StatementSorter singletonSorter = findSingletonSorter(statement);
		if (singletonSorter != null)
			return singletonSorter;

		GroupSorter<?> groupSorter = findGroupSorter(statement);
		if (groupSorter != null)
			return groupSorter;

		return null;
	}

}
