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

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * A combined sorted set of any two other sorted sets. The elements in the front
 * set will shadow the elements on the back set.
 *
 * @param <E>
 *            The elements' type.
 *
 * @author Quim Testar
 */
public class CombinedSortedSet<E> extends CombinedSetSortedIterator<E> implements SortedSet<E>
{
	private static final long serialVersionUID = 5196602047719292556L;

	/**
	 * The comparators of front and back set must be the same, and will be used
	 * as a the comparator of this set.
	 *
	 * @param front
	 *            The front sorted set.
	 * @param back
	 *            The back sorted set.
	 */
	public CombinedSortedSet(SortedSet<E> front, SortedSet<E> back)
	{
		super(front, back, front.comparator());
		try
		{
			if ((front.comparator() != back.comparator()) && !front.comparator().equals(back.comparator()))
				throw new Error("Comparators differ");
		}
		catch (NullPointerException e)
		{
			throw new Error("Comparators differ");
		}
	}

	@Override
	protected SortedSet<E> getFront()
	{
		return (SortedSet<E>) super.getFront();
	}

	@Override
	protected SortedSet<E> getBack()
	{
		return (SortedSet<E>) super.getBack();
	}

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
	public SortedSet<E> headSet(E toElement)
	{
		return new CombinedSortedSet<E>(getFront().headSet(toElement), getBack().headSet(toElement));
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
	public SortedSet<E> subSet(E fromElement, E toElement)
	{
		return new CombinedSortedSet<E>(getFront().subSet(fromElement, toElement), getBack().subSet(fromElement, toElement));
	}

	@Override
	public SortedSet<E> tailSet(E toElement)
	{
		return new CombinedSortedSet<E>(getFront().tailSet(toElement), getBack().tailSet(toElement));
	}

}
