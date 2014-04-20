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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public class EmptySortedSet<E> extends AbstractReadOnlySortedSet<E> implements SortedSet<E>
{

	private final Comparator<? super E> comparator;

	public EmptySortedSet(Comparator<? super E> comparator)
	{
		this.comparator = comparator;
	}

	public EmptySortedSet()
	{
		this(null);
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public boolean contains(Object o)
	{
		return false;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{

			@Override
			public boolean hasNext()
			{
				return false;
			}

			@Override
			public E next()
			{
				throw new NoSuchElementException();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public Object[] toArray()
	{
		return new Object[0];
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		Arrays.fill(a, null);
		return a;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return c.isEmpty();
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement)
	{
		return this;
	}

	@Override
	public SortedSet<E> headSet(E toElement)
	{
		return this;
	}

	@Override
	public SortedSet<E> tailSet(E fromElement)
	{
		return this;
	}

	@Override
	public E first()
	{
		throw new NoSuchElementException();
	}

	@Override
	public E last()
	{
		throw new NoSuchElementException();
	}

}
