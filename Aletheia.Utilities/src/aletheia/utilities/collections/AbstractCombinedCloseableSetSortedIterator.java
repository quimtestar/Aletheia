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
abstract class AbstractCombinedCloseableSetSortedIterator<E> extends AbstractCombinedSetSortedIterator<E> implements CloseableSet<E>
{

	private static final long serialVersionUID = 6946119121966088525L;

	/**
	 * @param front
	 *            The front set.
	 */
	public AbstractCombinedCloseableSetSortedIterator(CloseableSet<E> front)
	{
		super(front);
	}

	@Override
	protected CloseableSet<E> getFront()
	{
		return (CloseableSet<E>) super.getFront();
	}

	@Override
	protected abstract CloseableSet<E> getBack();

	protected class CombinedCloseableSortedIterator extends CombinedSortedIterator<E> implements CloseableIterator<E>
	{
		protected CombinedCloseableSortedIterator(CloseableIterator<E> frontIterator, CloseableIterator<E> backIterator)
		{
			super(frontIterator, backIterator, resolvedComparator());
		}

		@Override
		protected CloseableIterator<? extends E> getFrontIterator()
		{
			return (CloseableIterator<? extends E>) super.getFrontIterator();
		}

		@Override
		protected CloseableIterator<? extends E> getBackIterator()
		{
			return (CloseableIterator<? extends E>) super.getBackIterator();
		}

		@Override
		public void close()
		{
			getFrontIterator().close();
			getBackIterator().close();
		}

	}

	@Override
	public CloseableIterator<E> iterator()
	{
		return new CombinedCloseableSortedIterator(getFront().iterator(), getBack().iterator());
	}

}
