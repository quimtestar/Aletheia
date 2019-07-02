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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.model.statement.Assumption;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.ReverseList;
import aletheia.utilities.collections.UnionIterable;

public class SorterDependencyFilter<S extends Sorter> implements Iterable<S>
{
	private final static Comparator<Sorter> sorterComparator = new Comparator<>()
	{

		private int assumptionOrder(Sorter s)
		{
			if (s instanceof StatementSorter)
			{
				Statement st = ((StatementSorter) s).getStatement();
				if (st instanceof Assumption)
					return ((Assumption) st).getOrder();
			}
			return Integer.MAX_VALUE;
		}

		@Override
		public int compare(Sorter s1, Sorter s2)
		{
			int c = 0;
			c = Integer.compare(assumptionOrder(s1), assumptionOrder(s2));
			if (c != 0)
				return c;
			c = Boolean.compare(s1.getPrefix() == null, s2.getPrefix() == null);
			if (c != 0)
				return c;
			if (s1.getPrefix() != null)
				return s1.getPrefix().compareTo(s2.getPrefix());
			return c;
		}

	};

	private final Iterable<? extends S> inner;
	private final Transaction transaction;

	public SorterDependencyFilter(Iterable<? extends S> inner, Transaction transaction)
	{
		this.inner = inner;
		this.transaction = transaction;
	}

	private final Statement sorter2Statement(S sorter)
	{
		if (sorter instanceof StatementSorter)
			return ((StatementSorter) sorter).getStatement();
		else if (sorter instanceof StatementGroupSorter)
		{
			Statement st = sorter.getStatement(transaction);
			if (st instanceof Assumption)
				return null;
			return st;
		}
		else
			return null;
	}

	@Override
	public Iterator<S> iterator()
	{
		final List<S> buffered = new BufferedList<>(inner);
		final Map<Statement, S> sorterMap = new HashMap<>();
		for (S sorter : buffered)
		{
			Statement st = sorter2Statement(sorter);
			if (st != null)
				sorterMap.put(st, sorter);
		}

		Iterable<Iterable<S>> iterable2 = new Iterable<>()
		{

			@Override
			public Iterator<Iterable<S>> iterator()
			{
				return new Iterator<>()
				{

					final Iterator<S> iterator = buffered.iterator();
					final Set<S> visited = new HashSet<>();

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public Iterable<S> next()
					{
						Deque<S> stack = new ArrayDeque<>();
						stack.offer(iterator.next());
						Stack<S> stack2 = new Stack<>();
						while (!stack.isEmpty())
						{
							S sorter = stack.poll();
							stack2.push(sorter);
							Statement st = sorter2Statement(sorter);
							if (st != null)
							{
								ArrayList<S> depSList = new ArrayList<>();
								for (Statement dep : st.localDependencies(transaction))
								{
									S depS = sorterMap.get(dep);
									if (depS != null && !visited.contains(depS))
										depSList.add(depS);
								}
								Collections.sort(depSList, sorterComparator);
								stack.addAll(new ReverseList<>(depSList));
							}
						}
						List<S> list = new ArrayList<>();
						while (!stack2.isEmpty())
						{
							S sorter = stack2.pop();
							if (!visited.contains(sorter))
							{
								visited.add(sorter);
								list.add(sorter);
							}
						}
						return list;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

				};
			}
		};
		return new UnionIterable<>(iterable2).iterator();

	}

}
