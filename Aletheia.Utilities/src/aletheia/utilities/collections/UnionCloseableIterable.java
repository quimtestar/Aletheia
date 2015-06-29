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

/**
 * A {@link UnionIterable} of {@link CloseableIterable}s that is a
 * {@link CloseableIterable}.
 *
 * @author Quim Testar
 */
public class UnionCloseableIterable<E> extends UnionIterable<E>implements CloseableIterable<E>
{

	public UnionCloseableIterable(CloseableIterable<? extends CloseableIterable<E>> inner)
	{
		super(inner);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CloseableIterable<? extends CloseableIterable<E>> getInner()
	{
		return (CloseableIterable<? extends CloseableIterable<E>>) super.getInner();
	}

	protected class MyIterator extends UnionIterable<E>.MyIterator implements CloseableIterator<E>
	{

		@Override
		protected CloseableIterator<E> getIterator1()
		{
			return (CloseableIterator<E>) super.getIterator1();
		}

		@Override
		protected CloseableIterator<? extends Iterable<E>> getIterator0()
		{
			return (CloseableIterator<? extends Iterable<E>>) super.getIterator0();
		}

		@Override
		public void close()
		{
			getIterator0().close();
			if (getIterator1() != null)
				getIterator1().close();
		}

	}

	@Override
	public CloseableIterator<E> iterator()
	{
		return new MyIterator();
	}

}
