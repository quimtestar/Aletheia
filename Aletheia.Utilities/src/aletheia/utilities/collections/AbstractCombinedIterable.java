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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract implementation of a combined {@link Iterable}. A combined
 * {@link Iterable} consists of two {@link Iterable}s: the front
 * {@link Iterable} and the back {@link Iterable}. The resulting
 * {@link Iterable} has both the front's elements and the back's elements. When
 * iterating across the elements of this {@link Iterable} we first iterate
 * across the front {@link Iterable} and then the across the back
 * {@link Iterable}. The management of the back {@link Iterable} is kept
 * abstract.
 * 
 * @param <E>
 *            The elements' type.
 * 
 * @see Collection
 * 
 * @author Quim Testar
 */
public abstract class AbstractCombinedIterable<E> implements Serializable, Iterable<E>
{
	private static final long serialVersionUID = -3737647852678501284L;

	private final Iterable<E> front;

	/**
	 * @param front
	 *            The front iterable.
	 */
	public AbstractCombinedIterable(Iterable<E> front)
	{
		this.front = front;
	}

	/**
	 * @return The front iterable.
	 */
	protected Iterable<E> getFront()
	{
		return front;
	}

	/**
	 * The managing of the back collection is left for the subclasses, so this
	 * method must be overriden.
	 * 
	 * @return The back collection.
	 */
	protected abstract Iterable<E> getBack();

	/**
	 * Moves a pair of iterators one position and returns the next element
	 * found. If the front iterator still can be moved forward, we move it and
	 * return the element found; if not we use the back iterator. we advance it
	 * one position
	 * 
	 * @param frontIterator
	 * @param backIterator
	 * @return The next element found in either iterator.
	 */
	protected E moveIteratorForward(Iterator<E> frontIterator, Iterator<E> backIterator)
	{
		if (frontIterator.hasNext())
			return frontIterator.next();
		E next = backIterator.next();
		return next;
	}

	protected class CombinedIterator extends AbstractReadOnlyIterator<E>
	{
		private final Iterator<E> frontIterator;
		private final Iterator<E> backIterator;

		private E next;
		private boolean hasNext;

		protected CombinedIterator(Iterator<E> frontIterator, Iterator<E> backIterator)
		{
			this.frontIterator = frontIterator;
			this.backIterator = backIterator;
			try
			{
				next = moveIteratorForward(frontIterator, backIterator);
				hasNext = true;
			}
			catch (NoSuchElementException e)
			{
				hasNext = false;
			}
		}

		protected Iterator<E> getFrontIterator()
		{
			return frontIterator;
		}

		protected Iterator<E> getBackIterator()
		{
			return backIterator;
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public E next()
		{
			if (!hasNext)
				throw new NoSuchElementException();
			E pre = next;
			try
			{
				next = moveIteratorForward(frontIterator, backIterator);
			}
			catch (NoSuchElementException e)
			{
				hasNext = false;
			}
			return pre;
		}

	}

	/**
	 * We first iterate across the front collection and when finished, we
	 * iterate across the back collection.
	 */
	@Override
	public Iterator<E> iterator()
	{
		return new CombinedIterator(front.iterator(), getBack().iterator());
	}

}
