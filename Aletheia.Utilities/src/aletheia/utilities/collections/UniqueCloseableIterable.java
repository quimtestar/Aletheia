/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
 * A {@link UniqueIterable} that is also {@link CloseableIterable}.
 *
 * @author Quim Testar
 */
public class UniqueCloseableIterable<E> extends UniqueIterable<E> implements CloseableIterable<E>
{

	public UniqueCloseableIterable(CloseableIterable<E> inner)
	{
		super(inner);
	}

	@Override
	protected CloseableIterable<E> getInner()
	{
		return (CloseableIterable<E>) super.getInner();
	}

	protected static class UniqueCloseableIterator<E> extends UniqueIterator<E> implements CloseableIterator<E>
	{

		protected UniqueCloseableIterator(CloseableIterator<E> inner)
		{
			super(inner);
		}

		@Override
		protected CloseableIterator<E> getInner()
		{
			return (CloseableIterator<E>) super.getInner();
		}

		@Override
		public void close()
		{
			getInner().close();
		}

	}

	@Override
	public CloseableIterator<E> iterator()
	{
		return new UniqueCloseableIterator<>(getInner().iterator());
	}

}
