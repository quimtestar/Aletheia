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
package aletheia.utilities.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * A view of a {@link SortedSet} that inverts its order.
 *
 * @author Quim Testar
 */
public class ReverseSortedSet<E> implements SortedSet<E>
{
	private final SortedSet<E> inner;
	private final Comparator<? super E> comparator;

	public ReverseSortedSet(SortedSet<E> inner)
	{
		this.inner = inner;
		Comparator<? super E> innerComparator = inner.comparator();
		if (innerComparator == null)
			this.comparator = Collections.reverseOrder();
		else
			this.comparator = Collections.reverseOrder(innerComparator);
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return inner.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<>()
		{
			private SortedSet<E> remain = inner;
			private E last = null;

			@Override
			public boolean hasNext()
			{
				return !remain.isEmpty();
			}

			@Override
			public E next()
			{
				last = remain.last();
				remain = remain.headSet(last);
				return last;
			}

			@Override
			public void remove()
			{
				if (last == null)
					throw new IllegalStateException();
				inner.remove(last);
				last = null;
			}

		};
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

	@Override
	public Object[] toArray()
	{
		Object[] a = inner.toArray();
		for (int i = 0; i < a.length / 2; i++)
		{
			Object o = a[i];
			a[i] = a[a.length - 1 - i];
			a[a.length - 1 - i] = o;
		}
		return a;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement)
	{
		return headSet(toElement).tailSet(fromElement);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		T[] b = inner.toArray(a);
		for (int i = 0; i < b.length / 2; i++)
		{
			T o = b[i];
			b[i] = b[b.length - 1 - i];
			b[b.length - 1 - i] = o;
		}
		return a;
	}

	@Override
	public SortedSet<E> headSet(E toElement)
	{
		SortedSet<E> tailSet = inner.tailSet(toElement);
		Iterator<E> iterator = tailSet.iterator();
		if (iterator.hasNext())
		{
			E first = iterator.next();
			if (comparator.compare(first, toElement) < 0)
				return new ReverseSortedSet<>(tailSet);
			else
			{
				if (iterator.hasNext())
				{
					E second = iterator.next();
					return new ReverseSortedSet<>(inner.tailSet(second));
				}
				else
					return new EmptySortedSet<>(comparator);
			}
		}
		else
			return new EmptySortedSet<>(comparator);
	}

	@Override
	public boolean add(E e)
	{
		return inner.add(e);
	}

	@Override
	public SortedSet<E> tailSet(E fromElement)
	{
		SortedSet<E> tail = new ReverseSortedSet<>(inner.headSet(fromElement));
		if (inner.contains(fromElement))
			return new CombinedSortedSet<>(new SingletonSortedSet<>(fromElement, tail.comparator()), tail);
		else
			return tail;

	}

	@Override
	public boolean remove(Object o)
	{
		return inner.remove(o);
	}

	@Override
	public E first()
	{
		return inner.last();
	}

	@Override
	public E last()
	{
		return inner.first();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return inner.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return inner.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return inner.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return inner.removeAll(c);
	}

	@Override
	public void clear()
	{
		inner.clear();
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for (E e : this)
		{
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(e.toString());
		}
		sb.append("]");
		return sb.toString();
	}

}
