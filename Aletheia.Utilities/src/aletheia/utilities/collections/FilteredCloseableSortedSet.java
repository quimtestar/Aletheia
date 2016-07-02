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

/**
 * A {@link FilteredSortedSet} that is also a {@link CloseableSet}.
 *
 * @author Quim Testar
 */
public class FilteredCloseableSortedSet<E> extends FilteredCloseableSet<E> implements CloseableSortedSet<E>
{

	public FilteredCloseableSortedSet(Filter<E> filter, CloseableSortedSet<E> inner)
	{
		super(filter, inner);
	}

	@Override
	protected CloseableSortedSet<E> getInner()
	{
		return (CloseableSortedSet<E>) super.getInner();
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return getInner().comparator();
	}

	@Override
	public FilteredCloseableSortedSet<E> subSet(E fromElement, E toElement)
	{
		return new FilteredCloseableSortedSet<>(getFilter(), getInner().subSet(fromElement, toElement));
	}

	@Override
	public FilteredCloseableSortedSet<E> headSet(E toElement)
	{
		return new FilteredCloseableSortedSet<>(getFilter(), getInner().headSet(toElement));
	}

	@Override
	public FilteredCloseableSortedSet<E> tailSet(E fromElement)
	{
		return new FilteredCloseableSortedSet<>(getFilter(), getInner().tailSet(fromElement));
	}

	@Override
	public E first()
	{
		CloseableIterator<E> iterator = iterator();
		try
		{
			return iterator.next();
		}
		finally
		{
			iterator.close();
		}
	}

	@Override
	public E last()
	{
		CloseableSortedSet<E> set = getInner();
		while (true)
		{
			E e = set.last();
			if (getFilter().filter(e))
				return e;
			set = headSet(e);
		}
	}

}
