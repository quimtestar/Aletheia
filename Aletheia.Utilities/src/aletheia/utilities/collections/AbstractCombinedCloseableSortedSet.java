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

/**
 * An {@link AbstractCombinedSortedSet} for {@link CloseableSortedSet}s.
 */
public abstract class AbstractCombinedCloseableSortedSet<E> extends AbstractCombinedCloseableSetSortedIterator<E> implements CloseableSortedSet<E>
{

	private static final long serialVersionUID = 8450561525825316243L;

	public AbstractCombinedCloseableSortedSet(CloseableSortedSet<E> front)
	{
		super(front);
	}

	@Override
	protected CloseableSortedSet<E> getFront()
	{
		return (CloseableSortedSet<E>) super.getFront();
	}

	@Override
	protected abstract CloseableSortedSet<E> getBack();

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
	public CloseableSortedSet<E> headSet(final E toElement)
	{
		return new AbstractCombinedCloseableSortedSet<>(getFront().headSet(toElement))
		{

			private static final long serialVersionUID = -6388856838720080057L;

			@Override
			protected CloseableSortedSet<E> getBack()
			{
				return getBack().headSet(toElement);
			}
		};
	}

	@Override
	public CloseableSortedSet<E> subSet(final E fromElement, final E toElement)
	{
		return new AbstractCombinedCloseableSortedSet<>(getFront().subSet(fromElement, toElement))
		{

			private static final long serialVersionUID = 7028830817059557949L;

			@Override
			protected CloseableSortedSet<E> getBack()
			{
				return getBack().subSet(fromElement, toElement);
			}
		};
	}

	@Override
	public CloseableSortedSet<E> tailSet(final E fromElement)
	{
		return new AbstractCombinedCloseableSortedSet<>(getFront().tailSet(fromElement))
		{

			private static final long serialVersionUID = -8634550355258915121L;

			@Override
			protected CloseableSortedSet<E> getBack()
			{
				return getBack().tailSet(fromElement);
			}
		};
	}

}
