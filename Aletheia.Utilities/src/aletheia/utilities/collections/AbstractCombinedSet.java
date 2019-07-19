/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.util.Iterator;
import java.util.Set;

/**
 * Abstract implementation of a combined {@link Set}. The elements in the front
 * {@link Set} will shadow the elements in the back {@link Set}. The management
 * of the back {@link Set} is kept abstract.
 *
 * @param <E>
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedSet<E> extends AbstractCombinedCollection<E> implements Set<E>
{
	private static final long serialVersionUID = 2341397957245092788L;

	public AbstractCombinedSet(Set<E> front)
	{
		super(front);
	}

	@Override
	protected Set<E> getFront()
	{
		return (Set<E>) super.getFront();
	}

	@Override
	protected abstract Set<E> getBack();

	/**
	 * Moves a pair of iterators one position and returns the next element
	 * found. If the front iterator still can be moved forward, we move it and
	 * return the element found; if not we use the back iterator, skipping the
	 * elements that are also in the front set. we advance it one position
	 *
	 * @param frontIterator
	 * @param backIterator
	 * @return The next element found in either iterator.
	 */
	@Override
	protected E moveIteratorForward(Iterator<E> frontIterator, Iterator<E> backIterator)
	{
		if (frontIterator.hasNext())
			return frontIterator.next();
		E next = backIterator.next();
		while (getFront().contains(next))
			next = backIterator.next();
		return next;
	}

	/**
	 * The size is equals to the back set size plus the number of elements of
	 * the front set that are not in the back set.
	 */
	@Override
	public int size()
	{
		int size = getBack().size();
		for (E e : getFront())
			if (!getBack().contains(e))
				size++;
		return size;
	}

}
