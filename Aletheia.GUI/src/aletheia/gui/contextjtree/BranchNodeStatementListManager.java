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
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import aletheia.model.statement.Statement;
import aletheia.utilities.collections.BufferedList;

class BranchNodeStatementListManager<S extends Statement>
{
	private final BufferedList<S> statementList;
	private final Map<UUID, Integer> uuidIndexes;

	BranchNodeStatementListManager(Collection<? extends S> statements)
	{
		statementList = new BufferedList<S>(statements);
		uuidIndexes = new HashMap<UUID, Integer>();
		for (ListIterator<S> iterator = statementList.listIterator(); iterator.hasNext();)
		{
			int i = iterator.nextIndex();
			Integer old = uuidIndexes.put(iterator.next().getUuid(), i);
			if (old != null)
				throw new IllegalArgumentException();
		}
	}

	boolean checkStatementInsert(Statement statement)
	{
		Integer index = uuidIndexes.get(statement.getUuid());
		if (index == null)
			return false;
		Statement statement_ = statementList.get(index);
		if ((statement_.getIdentifier() == null) != (statement.getIdentifier() == null))
			return false;
		if ((statement_.getIdentifier() != null && !statement_.getIdentifier().equals(statement.getIdentifier())))
			return false;
		return true;
	}

	boolean checkStatementRemove(Statement statement)
	{
		Integer index = uuidIndexes.get(statement.getUuid());
		if (index == null)
			return true;
		return false;
	}

	BufferedList<S> getStatementList()
	{
		return statementList;
	}

}
