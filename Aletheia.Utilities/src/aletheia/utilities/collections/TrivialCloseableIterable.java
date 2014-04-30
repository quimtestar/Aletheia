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

/**
 * A {@link CloseableIterable} on the top of a regular {@link Iterable} and
 * whose {@link CloseableIterator#close()} method does nothing.
 * 
 * @author Quim Testar
 */
public class TrivialCloseableIterable<E> implements CloseableIterable<E>
{
	private final Iterable<E> inner;

	public TrivialCloseableIterable(Iterable<E> inner)
	{
		super();
		this.inner = inner;
	}

	@Override
	public CloseableIterator<E> iterator()
	{
		final Iterator<E> iterator = this.inner.iterator();
		return new CloseableIterator<E>()
		{

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public E next()
			{
				return iterator.next();
			}

			@Override
			public void remove()
			{
				iterator.remove();
			}

			@Override
			public void close()
			{
			}

		};
	}

}
