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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @deprecated Use {@link Collections#nCopies(int, Object)} instead.
 */
@Deprecated
public class RepetitionList<E> extends AbstractReadOnlyList<E>
{
	private final int size;
	private final E element;

	public RepetitionList(int size, E element)
	{
		if (size < 0)
			throw new IllegalArgumentException();
		this.size = size;
		this.element = element;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		return size <= 0;
	}

	@Override
	public boolean contains(Object o)
	{
		if (element == null)
			return o == null;
		else
			return element.equals(o);
	}

	private class MyIterator implements Iterator<E>
	{
		int i = 0;

		@Override
		public boolean hasNext()
		{
			return i < size;
		}

		@Override
		public E next()
		{
			if (i >= size)
				throw new NoSuchElementException();
			i++;
			return element;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Iterator<E> iterator()
	{
		return new MyIterator();
	}

	@Override
	public Object[] toArray()
	{
		Object[] a = new Object[size];
		Arrays.fill(a, element);
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a)
	{
		if (size > a.length)
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		for (int i = 0; i < size; i++)
			a[i] = (T) element;
		for (int i = size; i < a.length; i++)
			a[i] = null;
		return a;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object o : c)
		{
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public E get(int index)
	{
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return element;
	}

	@Override
	public int indexOf(Object o)
	{
		if (isEmpty())
			return -1;
		else if (contains(o))
			return 0;
		else
			return -1;
	}

	@Override
	public int lastIndexOf(Object o)
	{
		if (isEmpty())
			return -1;
		else if (contains(o))
			return size - 1;
		else
			return -1;
	}

	private class MyListIterator extends MyIterator implements ListIterator<E>
	{
		public MyListIterator(int index)
		{
			i = index;
		}

		public MyListIterator()
		{
			this(0);
		}

		@Override
		public boolean hasPrevious()
		{
			return i > 0;
		}

		@Override
		public E previous()
		{
			if (i <= 0)
				throw new NoSuchElementException();
			i--;
			return element;
		}

		@Override
		public int nextIndex()
		{
			return i;
		}

		@Override
		public int previousIndex()
		{
			return i - 1;
		}

		@Override
		public void set(E e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e)
		{
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new MyListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return new MyListIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		if (toIndex < fromIndex)
			throw new IndexOutOfBoundsException();
		return new RepetitionList<E>(toIndex - fromIndex, element);
	}

}
