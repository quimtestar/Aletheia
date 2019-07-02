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
import java.util.List;
import java.util.ListIterator;

/**
 * Abstract implementation of a combined {@link List}. Essentially the same
 * implementation as {@link AbstractCombinedCollection} adding the specific
 * method for lists. The management of the back list is kept abstract.
 *
 * @param <E>
 *
 * @see AbstractCombinedCollection
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedList<E> extends AbstractCombinedCollection<E> implements List<E>
{
	private static final long serialVersionUID = -699888443928184976L;

	public AbstractCombinedList(List<E> front)
	{
		super(front);
	}

	@Override
	protected List<E> getFront()
	{
		return (List<E>) super.getFront();
	}

	@Override
	protected abstract List<E> getBack();

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Elements on the back list are treated as being arranged just after the
	 * elements on the front list. Consistent with {@link #iterator()}.
	 */
	@Override
	public E get(int index)
	{
		if (index < getFront().size())
			return getFront().get(index);
		else
			return getBack().get(index - getFront().size());
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

	/**
	 * Consistent with {@link #get(int)}.
	 */
	@Override
	public int indexOf(Object o)
	{
		int i = getFront().indexOf(o);
		if (i >= 0)
			return i;
		else
		{
			i = getBack().indexOf(o);
			if (i >= 0)
				return getFront().size() + i;
			else
				return -1;
		}

	}

	/**
	 * Consistent with {@link #get(int)}.
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		int i = getBack().lastIndexOf(o);
		if (i >= 0)
			return getFront().size() + i;
		else
		{
			i = getFront().indexOf(o);
			if (i >= 0)
				return i;
			else
				return -1;
		}
	}

	/**
	 * Calls to {@link #listIterator(int)} with the parameter 0.
	 */
	@Override
	public ListIterator<E> listIterator()
	{
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		final ListIterator<E> liFront, liBack;
		if (index < getFront().size())
		{
			liFront = getFront().listIterator(index);
			liBack = getBack().listIterator(0);
		}
		else
		{
			liFront = getFront().listIterator(getFront().size());
			liBack = getBack().listIterator(index - getFront().size());
		}

		return new ListIterator<>()
		{

			@Override
			public boolean hasNext()
			{
				return liFront.hasNext() || liBack.hasNext();
			}

			@Override
			public E next()
			{
				if (liFront.hasNext())
					return liFront.next();
				else
					return liBack.next();
			}

			@Override
			public boolean hasPrevious()
			{
				return liBack.hasPrevious() || liFront.hasPrevious();
			}

			@Override
			public E previous()
			{
				if (liBack.hasPrevious())
					return liBack.previous();
				else
					return liFront.previous();
			}

			@Override
			public int nextIndex()
			{
				if (liFront.hasNext())
					return liFront.nextIndex();
				else
					return getFront().size() + liBack.nextIndex();
			}

			@Override
			public int previousIndex()
			{
				if (liBack.hasPrevious())
					return getFront().size() + liBack.previousIndex();
				else
					return liFront.previousIndex();
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

		};
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		if (fromIndex < getFront().size())
		{
			if (toIndex <= getFront().size())
				return getFront().subList(fromIndex, toIndex);
			else
				return new CombinedList<>(getFront().subList(fromIndex, getFront().size()), getBack().subList(0, toIndex - getFront().size()));
		}
		else
			return getBack().subList(fromIndex - getFront().size(), toIndex - getFront().size());

	}

}
