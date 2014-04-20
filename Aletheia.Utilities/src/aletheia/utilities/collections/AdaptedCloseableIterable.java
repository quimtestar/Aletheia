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

public class AdaptedCloseableIterable<E> extends AdaptedIterable<E> implements CloseableIterable<E>
{

	public AdaptedCloseableIterable(CloseableIterable<? extends E> inner)
	{
		super(inner);
	}

	@Override
	protected CloseableIterable<? extends E> getInner()
	{
		return (CloseableIterable<? extends E>) super.getInner();
	}

	@Override
	public CloseableIterator<E> iterator()
	{
		final CloseableIterator<? extends E> iterator = getInner().iterator();
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
				throw new UnsupportedOperationException();
			}

			@Override
			public void close()
			{
				iterator.close();
			}

		};
	}

}
