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

public class UnionIterable<E> implements Iterable<E>
{
	private final Iterable<? extends Iterable<E>> inner;

	public UnionIterable(Iterable<? extends Iterable<E>> inner)
	{
		this.inner = inner;
	}

	protected Iterable<? extends Iterable<E>> getInner()
	{
		return inner;
	}

	protected class MyIterator implements Iterator<E>
	{
		private final Iterator<? extends Iterable<E>> iterator0;
		private Iterator<E> iterator1;

		protected MyIterator()
		{
			this.iterator0 = inner.iterator();
			try
			{
				do
				{
					this.iterator1 = this.iterator0.next().iterator();
				} while (!iterator1.hasNext());
			}
			catch (NoSuchElementException e)
			{
				this.iterator1 = null;
			}
		}

		protected Iterator<E> getIterator1()
		{
			return iterator1;
		}

		protected void setIterator1(Iterator<E> iterator1)
		{
			this.iterator1 = iterator1;
		}

		protected Iterator<? extends Iterable<E>> getIterator0()
		{
			return iterator0;
		}

		@Override
		public boolean hasNext()
		{
			return iterator1 != null;
		}

		@Override
		public E next()
		{
			if (iterator1 == null)
				throw new NoSuchElementException();
			E e = iterator1.next();
			try
			{
				while (!iterator1.hasNext())
					iterator1 = iterator0.next().iterator();
			}
			catch (NoSuchElementException e1)
			{
				iterator1 = null;
			}
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
		return new MyIterator();
	}

}
