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

public class FilteredCloseableCollection<E> extends FilteredCollection<E> implements CloseableCollection<E>
{
	public FilteredCloseableCollection(Filter<E> filter, CloseableCollection<E> inner)
	{
		super(filter, inner);
	}

	@Override
	protected CloseableCollection<E> getInner()
	{
		return (CloseableCollection<E>) super.getInner();
	}

	protected class FilteredCloseableIterator extends FilteredIterator implements CloseableIterator<E>
	{

		public FilteredCloseableIterator(CloseableIterator<E> innerIterator)
		{
			super(innerIterator);
		}

		@Override
		protected CloseableIterator<E> getInnerIterator()
		{
			return (CloseableIterator<E>) super.getInnerIterator();
		}

		@Override
		public void close()
		{
			getInnerIterator().close();
		}
	}

	@Override
	public CloseableIterator<E> iterator()
	{
		return new FilteredCloseableIterator(getInner().iterator());
	}

	@Override
	public boolean isEmpty()
	{
		CloseableIterator<E> iterator = iterator();
		try
		{
			return !iterator.hasNext();
		}
		finally
		{
			iterator.close();
		}
	}

	@Override
	protected ArrayList<E> toArrayList()
	{
		ArrayList<E> list = new ArrayList<E>();
		CloseableIterator<E> iterator = iterator();
		try
		{
			while (iterator.hasNext())
				list.add(iterator.next());
		}
		finally
		{
			iterator.close();
		}
		return list;
	}

}
