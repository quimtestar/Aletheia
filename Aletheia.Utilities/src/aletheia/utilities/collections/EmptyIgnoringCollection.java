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
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIgnoringCollection<E> extends AbstractCollection<E>
{

	@Override
	public boolean add(E e)
	{
		return false;
	}

	@Override
	public boolean remove(Object o)
	{
		return false;
	}

	@Override
	public void clear()
	{
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{

			@Override
			public boolean hasNext()
			{
				return false;
			}

			@Override
			public E next()
			{
				throw new NoSuchElementException();
			}

			@Override
			public void remove()
			{
			}

		};
	}

	@Override
	public int size()
	{
		return 0;
	}

}
