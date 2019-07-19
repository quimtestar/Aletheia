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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * A combined set of any two other sets. The elements in the front set will
 * shadow the elements on the back set. The iterator is sorted according an
 * specified comparator or the natural order of the elements (assuming the
 * components' respective iterators are sorted using the very same comparator).
 *
 * @param <E>
 *            The elements' type.
 *
 * @author Quim Testar
 */
abstract class AbstractCombinedSetSortedIterator<E> extends AbstractCombinedSet<E>
{
	private static final long serialVersionUID = -2242012560285707716L;

	/**
	 * @param front
	 *            The front set.
	 */
	public AbstractCombinedSetSortedIterator(Set<E> front)
	{
		super(front);
	}

	/**
	 * The comparator specified ad construction time. Might be null.
	 *
	 * @return The comparator specified at construction time.
	 */
	public abstract Comparator<? super E> comparator();

	/**
	 * The comparator that will be used in the iterator. If no comparator was
	 * specified at construction time, a natural comparator is created using the
	 * {@link Comparable#compareTo(Object)} method of the elements. In that
	 * case, a {@link ClassCastException} might be thrown (not now but when
	 * using the comparator) if an element that does no implement
	 * {@link Comparable} is found.
	 *
	 * @return The comparator.
	 */
	protected Comparator<? super E> resolvedComparator()
	{
		Comparator<? super E> comp = comparator();
		if (comp != null)
			return comp;
		return new Comparator<>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public int compare(E e1, E e2) throws ClassCastException
			{
				return ((Comparable<E>) e1).compareTo(e2);
			};
		};
	}

	@Override
	public Iterator<E> iterator()
	{
		return new CombinedSortedIterator<>(getFront().iterator(), getBack().iterator(), resolvedComparator());
	}

}
