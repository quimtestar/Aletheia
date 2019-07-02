/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public abstract class AbstractCombinedSortedSet<E> extends AbstractCombinedSetSortedIterator<E> implements SortedSet<E>
{
	private static final long serialVersionUID = -100517828157391333L;

	public AbstractCombinedSortedSet(SortedSet<E> front)
	{
		super(front);
	}

	@Override
	protected SortedSet<E> getFront()
	{
		return (SortedSet<E>) super.getFront();
	}

	@Override
	protected abstract SortedSet<E> getBack();

	@Override
	public Comparator<? super E> comparator()
	{
		return getFront().comparator();
	}

	@Override
	public E first()
	{
		E e1;
		try
		{
			e1 = getFront().first();
		}
		catch (NoSuchElementException e)
		{
			return getBack().first();
		}
		E e2;
		try
		{
			e2 = getBack().first();
		}
		catch (NoSuchElementException e)
		{
			return getFront().first();
		}
		return resolvedComparator().compare(e1, e2) <= 0 ? e1 : e2;

	}

	@Override
	public E last()
	{
		E e1;
		try
		{
			e1 = getFront().last();
		}
		catch (NoSuchElementException e)
		{
			return getBack().last();
		}
		E e2;
		try
		{
			e2 = getBack().last();
		}
		catch (NoSuchElementException e)
		{
			return getFront().last();
		}
		return resolvedComparator().compare(e1, e2) >= 0 ? e1 : e2;
	}

	@Override
	public SortedSet<E> headSet(final E toElement)
	{
		return new AbstractCombinedSortedSet<>(getFront().headSet(toElement))
		{

			private static final long serialVersionUID = -6388856838720080057L;

			@Override
			protected SortedSet<E> getBack()
			{
				return getBack().headSet(toElement);
			}
		};
	}

	@Override
	public SortedSet<E> subSet(final E fromElement, final E toElement)
	{
		return new AbstractCombinedSortedSet<>(getFront().subSet(fromElement, toElement))
		{

			private static final long serialVersionUID = 7028830817059557949L;

			@Override
			protected SortedSet<E> getBack()
			{
				return getBack().subSet(fromElement, toElement);
			}
		};
	}

	@Override
	public SortedSet<E> tailSet(final E fromElement)
	{
		return new AbstractCombinedSortedSet<>(getFront().tailSet(fromElement))
		{

			private static final long serialVersionUID = -8634550355258915121L;

			@Override
			protected SortedSet<E> getBack()
			{
				return getBack().tailSet(fromElement);
			}
		};
	}

}
