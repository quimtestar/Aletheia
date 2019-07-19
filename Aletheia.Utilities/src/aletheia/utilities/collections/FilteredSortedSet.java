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

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * An implementation of a {@link SortedSet} that filters out the elements of a
 * given {@link SortedSet}.
 *
 * @see Filter
 *
 * @author Quim Testar
 */
public class FilteredSortedSet<E> extends FilteredSet<E> implements SortedSet<E>
{

	public FilteredSortedSet(Filter<E> filter, SortedSet<E> inner)
	{
		super(filter, inner);
	}

	@Override
	protected SortedSet<E> getInner()
	{
		return (SortedSet<E>) super.getInner();
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return getInner().comparator();
	}

	@Override
	public FilteredSortedSet<E> subSet(E fromElement, E toElement)
	{
		return new FilteredSortedSet<>(getFilter(), getInner().subSet(fromElement, toElement));
	}

	@Override
	public FilteredSortedSet<E> headSet(E toElement)
	{
		return new FilteredSortedSet<>(getFilter(), getInner().headSet(toElement));
	}

	@Override
	public FilteredSortedSet<E> tailSet(E fromElement)
	{
		return new FilteredSortedSet<>(getFilter(), getInner().tailSet(fromElement));
	}

	@Override
	public E first()
	{
		Iterator<E> iterator = iterator();
		return iterator.next();
	}

	@Override
	public E last()
	{
		FilteredSortedSet<E> set = this;
		while (true)
		{
			E e = set.last();
			if (getFilter().filter(e))
				return e;
			set = headSet(e);
		}
	}

}
