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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterable} on the top of another {@link Iterable} skipping the
 * consecutive duplicates
 *
 * @author Quim Testar
 */
public class UniqueIterable<E> implements Iterable<E>
{
	private final Iterable<E> inner;

	public UniqueIterable(Iterable<E> inner)
	{
		this.inner = inner;
	}

	protected Iterable<E> getInner()
	{
		return inner;
	}

	protected static class UniqueIterator<E> implements Iterator<E>
	{
		private final Iterator<E> inner;
		private E next = null;

		protected UniqueIterator(Iterator<E> inner)
		{
			this.inner = inner;
			if (inner.hasNext())
				next = inner.next();
		}

		protected Iterator<E> getInner()
		{
			return inner;
		}

		private E advance()
		{
			if (next == null)
				throw new NoSuchElementException();
			E last = next;
			while (true)
			{
				if (!inner.hasNext())
				{
					next = null;
					break;
				}
				next = inner.next();
				if (!last.equals(next))
					break;
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
		return new UniqueIterator<E>(inner.iterator());
	}

}
