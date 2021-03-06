/*******************************************************************************
 * Copyright (c) 2016, 2019 Quim Testar.
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * A {@link CloseableSortedSet} on the top of a regular {@link SortedSet} and
 * whose {@link CloseableSortedIterator#close()} method does nothing.
 *
 * @author Quim Testar
 */
public class TrivialCloseableSortedSet<E> extends AbstractCloseableSortedSet<E>
{
	private final SortedSet<E> inner;

	public TrivialCloseableSortedSet(SortedSet<E> inner)
	{
		super();
		this.inner = inner;
	}

	@Override
	public CloseableIterator<E> iterator()
	{
		final Iterator<E> iterator = this.inner.iterator();
		return new CloseableIterator<>()
		{

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public E next()
			{
				return iterator.next();
			}

			@Override
			public void remove()
			{
				iterator.remove();
			}

			@Override
			public void close()
			{
			}

		};
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
	public Object[] toArray()
	{
		return inner.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return inner.toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		return inner.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		return inner.remove(o);
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
	public CloseableSortedSet<E> subSet(E fromElement, E toElement)
	{
		return new TrivialCloseableSortedSet<>(inner.subSet(fromElement, toElement));
	}

	@Override
	public CloseableSortedSet<E> headSet(E toElement)
	{
		return new TrivialCloseableSortedSet<>(inner.headSet(toElement));
	}

	@Override
	public CloseableSortedSet<E> tailSet(E fromElement)
	{
		return new TrivialCloseableSortedSet<>(inner.tailSet(fromElement));
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return inner.comparator();
	}

	@Override
	public E first()
	{
		return inner.first();
	}

	@Override
	public E last()
	{
		return inner.last();
	}

}
