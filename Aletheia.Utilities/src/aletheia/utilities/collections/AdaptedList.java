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
package aletheia.utilities.collections;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Gives a read-only view of a {@link List} as a {@link List} with a type
 * parameter that is a superclass of the original's.
 *
 * @param <E>
 *            The type of the resulting {@link List} view.
 *
 * @author Quim Testar
 */
public class AdaptedList<E> extends AdaptedCollection<E> implements List<E>
{

	public AdaptedList(List<? extends E> inner)
	{
		super(inner);
	}

	@Override
	protected List<? extends E> getInner()
	{
		return (List<? extends E>) super.getInner();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index)
	{
		return getInner().get(index);
	}

	@Override
	public E set(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o)
	{
		return getInner().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return getInner().lastIndexOf(o);
	}

	private class MyListIterator implements ListIterator<E>
	{
		private final ListIterator<? extends E> inner;

		private MyListIterator(ListIterator<? extends E> inner)
		{
			this.inner = inner;
		}

		@Override
		public boolean hasNext()
		{
			return inner.hasNext();
		}

		@Override
		public E next()
		{
			return inner.next();
		}

		@Override
		public boolean hasPrevious()
		{
			return inner.hasPrevious();
		}

		@Override
		public E previous()
		{
			return inner.previous();
		}

		@Override
		public int nextIndex()
		{
			return inner.nextIndex();
		}

		@Override
		public int previousIndex()
		{
			return inner.previousIndex();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
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
		return new MyListIterator(getInner().listIterator());
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return new MyListIterator(getInner().listIterator(index));
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		return new AdaptedList<>(getInner().subList(fromIndex, toIndex));
	}

}
