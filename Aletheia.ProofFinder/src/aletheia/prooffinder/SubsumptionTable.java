/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SubsumptionTable
{
	private final Map<QueueSubEntry, Set<QueueEntry>> table;

	public SubsumptionTable()
	{
		table = new HashMap<>();
	}

	public void add(QueueEntry qe)
	{
		for (QueueSubEntry qse : qe.subEntries())
		{
			Set<QueueEntry> set = table.get(qse);
			if (set == null)
			{
				set = new HashSet<>();
				table.put(qse, set);
			}
			set.add(qe);
		}
	}

	public boolean isSubsumed(QueueEntry qe)
	{
		for (QueueSubEntry qse : qe.subEntries())
		{
			Set<QueueEntry> set = table.get(qse);
			if (set != null)
			{
				for (QueueEntry qe_ : set)
				{
					if (qe_.subsumes(qe))
						return true;
				}
			}
		}
		return false;
	}

}
