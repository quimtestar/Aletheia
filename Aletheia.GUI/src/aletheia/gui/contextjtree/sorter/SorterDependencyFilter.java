package aletheia.gui.contextjtree.sorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import aletheia.utilities.collections.UnionIterable;

public class SorterDependencyFilter<S extends Sorter> implements Iterable<S>
{
	private final static Comparator<Sorter> sorterComparator = new Comparator<Sorter>()
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
		final List<S> buffered = new BufferedList<S>(inner);
		final Map<Statement, S> sorterMap = new HashMap<Statement, S>();
		for (S sorter : buffered)
		{
			Statement st = sorter2Statement(sorter);
			if (st != null)
				sorterMap.put(st, sorter);
		}

		Iterable<Iterable<S>> iterable2 = new Iterable<Iterable<S>>()
		{

			@Override
			public Iterator<Iterable<S>> iterator()
			{
				return new Iterator<Iterable<S>>()
				{

					final Iterator<S> iterator = buffered.iterator();
					final Set<S> visited = new HashSet<S>();

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public Iterable<S> next()
					{
						Stack<S> stack = new Stack<S>();
						stack.push(iterator.next());
						Stack<S> stack2 = new Stack<S>();
						while (!stack.isEmpty())
						{
							S sorter = stack.pop();
							stack2.push(sorter);
							Statement st = sorter2Statement(sorter);
							if (st != null)
							{
								ArrayList<S> depSList = new ArrayList<S>();
								for (Statement dep : st.localDependencies(transaction))
								{
									S depS = sorterMap.get(dep);
									if (depS != null && !visited.contains(depS))
										depSList.add(depS);
								}
								Collections.sort(depSList, sorterComparator);
								stack.addAll(depSList);
							}
						}
						List<S> list = new ArrayList<S>();
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

				};
			}
		};
		return new UnionIterable<S>(iterable2).iterator();

	}

}
