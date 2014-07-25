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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import aletheia.utilities.MiscUtilities;

/**
 * An implementation of a {@link Collection} that filters out the elements of a
 * given {@link Collection}.
 *
 * @see Filter
 *
 * @author Quim Testar
 */
public class FilteredCollection<E> extends AbstractCollection<E>
{
	private final Filter<E> filter;
	private final Collection<E> inner;

	public FilteredCollection(Filter<E> filter, Collection<E> inner)
	{
		this.filter = filter;
		this.inner = inner;
	}

	protected Filter<E> getFilter()
	{
		return filter;
	}

	protected Collection<E> getInner()
	{
		return inner;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o)
	{
		try
		{
			return filter.filter((E) o) && inner.contains(o);
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	protected class FilteredIterator implements Iterator<E>
	{
		private final Iterator<E> innerIterator;
		private boolean hasNext;
		private E next;

		public FilteredIterator(Iterator<E> innerIterator)
		{
			super();
			this.innerIterator = innerIterator;
			advance();
		}

		protected Iterator<E> getInnerIterator()
		{
			return innerIterator;
		}

		private void advance()
		{
			hasNext = true;
			do
			{
				if (getInnerIterator().hasNext())
					next = getInnerIterator().next();
				else
					hasNext = false;
			} while (hasNext && !filter.filter(next));
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
			E e = next;
			advance();
			return e;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Iterator<E> iterator()
	{
		return new FilteredIterator(inner.iterator());
	}

	@Override
	public int size()
	{
		int n = 0;
		for (Iterator<E> iterator = iterator(); iterator.hasNext(); iterator.next())
			n++;
		return n;
	}

	@Override
	public boolean isEmpty()
	{
		return !iterator().hasNext();
	}

	@Override
	public Object[] toArray()
	{
		return MiscUtilities.iterableToArray(this);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return MiscUtilities.iterableToArray(this, a);
	}

}
