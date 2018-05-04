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

/**
 * A combined closeable set of any two other closeable sets. The elements in the
 * front set will shadow the elements on the back set. The iterator is sorted
 * according an specified comparator or the natural order of the elements
 * (assuming the components' respective iterators are sorted using the very same
 * comparator).
 *
 * @param <E>
 *            The elements' type.
 *
 * @author Quim Testar
 */
class CombinedCloseableSetSortedIterator<E> extends AbstractCombinedCloseableSetSortedIterator<E>
{
	private static final long serialVersionUID = 6848104748723213218L;

	private final CloseableSet<E> back;
	private final Comparator<? super E> comparator;

	/**
	 * If the specified comparator is null the natural order of the elements
	 * will be used.
	 *
	 * @param front
	 *            The front set.
	 * @param back
	 *            The back set.
	 * @param comparator
	 *            The comparator.
	 */
	public CombinedCloseableSetSortedIterator(CloseableSet<E> front, CloseableSet<E> back, Comparator<? super E> comparator)
	{
		super(front);
		this.comparator = comparator;
		this.back = back;
	}

	@Override
	protected CloseableSet<E> getBack()
	{
		return back;
	}

	/**
	 * The comparator specified ad construction time. Might be null.
	 *
	 * @return The comparator specified at construction time.
	 */
	@Override
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

}
