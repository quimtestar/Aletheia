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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A combined set of any two other sets. The elements in the front set will
 * shadow the elements on the back set. The iterator is sorted according an
 * specified comparator or the natural order of the elements (assuming the
 * components' respective iterators are sorted using the very same comparator).
 * 
 * @param <E>
 *            The elements' type.
 */
class CombinedSetSortedIterator<E> extends CombinedSet<E>
{
	private static final long serialVersionUID = -1286644014041708380L;

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
	public CombinedSetSortedIterator(Set<E> front, Set<E> back, Comparator<? super E> comparator)
	{
		super(front, back);
		this.comparator = comparator;
	}

	/**
	 * The comparator specified ad contstruction time. Might be null.
	 * 
	 * @return The comparator specified at construction time.
	 */
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

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
		return new Comparator<E>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public int compare(E e1, E e2) throws ClassCastException
			{
				return ((Comparable<E>) e1).compareTo(e2);
			};
		};
	}

	/**
	 * We keep track of both iterators simultaneously and take one or another
	 * next elements as the next element of this iterator according to the
	 * comparison between them. Note: The front and back iterators <b>must</b>
	 * have the same order; if that is not the case, the result is
	 * indeterminate.
	 */
	@Override
	public Iterator<E> iterator()
	{
		final Iterator<E> frontIterator = getFront().iterator();
		final Iterator<E> backIterator = getBack().iterator();

		return new AbstractReadOnlyIterator<E>()
		{
			E nextFront;
			E nextBack;
			boolean hasNextFront;
			boolean hasNextBack;

			{
				try
				{
					nextFront = frontIterator.next();
					hasNextFront = true;
				}
				catch (NoSuchElementException e)
				{
					hasNextFront = false;
				}
				try
				{
					nextBack = backIterator.next();
					hasNextBack = true;
				}
				catch (NoSuchElementException e)
				{
					hasNextBack = false;
				}
			}

			@Override
			public boolean hasNext()
			{
				return hasNextFront || hasNextBack;
			}

			@Override
			public E next()
			{
				if (!hasNextFront)
				{
					if (!hasNextBack)
						throw new NoSuchElementException();
					E ret = nextBack;
					try
					{
						nextBack = backIterator.next();
					}
					catch (NoSuchElementException e)
					{
						hasNextBack = false;
					}
					return ret;
				}
				else if (!hasNextBack)
				{
					E ret = nextFront;
					try
					{
						nextFront = frontIterator.next();
					}
					catch (NoSuchElementException e)
					{
						hasNextFront = false;
					}
					return ret;
				}
				else
				{
					int c = resolvedComparator().compare(nextFront, nextBack);
					if (c < 0)
					{
						E ret = nextFront;
						try
						{
							nextFront = frontIterator.next();
						}
						catch (NoSuchElementException e)
						{
							hasNextFront = false;
						}
						return ret;
					}
					else if (c > 0)
					{
						E ret = nextBack;
						try
						{
							nextBack = backIterator.next();
						}
						catch (NoSuchElementException e)
						{
							hasNextBack = false;
						}
						return ret;
					}
					else
					{
						E ret = nextFront;
						try
						{
							nextFront = frontIterator.next();
						}
						catch (NoSuchElementException e)
						{
							hasNextFront = false;
						}
						try
						{
							nextBack = backIterator.next();
						}
						catch (NoSuchElementException e)
						{
							hasNextBack = false;
						}
						return ret;
					}
				}

			}

		};
	}

}
