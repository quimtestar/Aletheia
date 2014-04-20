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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A view as a {@link Collection} of the difference (in the terms of set theory)
 * between two collections. If the subtrahend collections
 * {@link Collection#contains(Object)} is not efficient (as in a {@link Set})
 * the operations will be quite slow.
 * 
 * @param <E>
 *            The element's type.
 */
public class DifferenceCollection<E> extends AbstractReadOnlyCollection<E> implements Collection<E>
{
	private final Collection<E> minuend;
	private final Collection<E> subtrahend;

	/**
	 * Creates a new difference collection.
	 * 
	 * @param minuend
	 *            The minuend of the difference. This collection is contained
	 *            into the minuend.
	 * @param subtrahend
	 *            The subrahend of the difference. This collection hasn't any
	 *            element that is in the subtrahend.
	 */
	public DifferenceCollection(Collection<E> minuend, Collection<E> subtrahend)
	{
		super();
		this.minuend = minuend;
		this.subtrahend = subtrahend;
	}

	/**
	 * The minuend collection.
	 * 
	 * @return The minuend collection.
	 */
	public Collection<E> getMinuend()
	{
		return minuend;
	}

	/**
	 * The subtrahend collection.
	 * 
	 * @return The subtrahend collection.
	 */
	public Collection<E> getSubtrahend()
	{
		return subtrahend;
	}

	@Override
	public int size()
	{
		int n = 0;
		for (Iterator<E> i = iterator(); i.hasNext(); i.next())
			n++;
		return n;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public boolean contains(Object o)
	{
		return minuend.contains(o) && !subtrahend.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		final Iterator<E> iterator = minuend.iterator();

		return new AbstractReadOnlyIterator<E>()
		{

			E next = advance();

			private E advance()
			{
				next = null;
				while (iterator.hasNext())
				{
					next = iterator.next();
					if (!subtrahend.contains(next))
						break;
					next = null;
				}
				return next;
			}

			@Override
			public boolean hasNext()
			{
				return next != null;
			}

			@Override
			public E next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				E e = next;
				next = advance();
				return e;
			}

		};
	}

	@Override
	public Object[] toArray()
	{
		return toArray(new Object[0]);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return new ArrayList<E>(this).toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object o : c)
		{
			if (!contains(o))
				return false;
		}
		return true;
	}

}
