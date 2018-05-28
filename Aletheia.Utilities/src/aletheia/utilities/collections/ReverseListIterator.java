/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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

import java.util.ListIterator;

public class ReverseListIterator<E> implements ListIterator<E>
{
	private final ListIterator<E> inner;

	public ReverseListIterator(ListIterator<E> inner)
	{
		super();
		this.inner = inner;
	}

	@Override
	public boolean hasNext()
	{
		return inner.hasPrevious();
	}

	@Override
	public E next()
	{
		return inner.previous();
	}

	@Override
	public boolean hasPrevious()
	{
		return inner.hasNext();
	}

	@Override
	public E previous()
	{
		return inner.next();
	}

	@Override
	public int nextIndex()
	{
		return inner.previousIndex();
	}

	@Override
	public int previousIndex()
	{
		return inner.nextIndex();
	}

	@Override
	public void remove()
	{
		inner.remove();
	}

	@Override
	public void set(E e)
	{
		inner.set(e);
	}

	@Override
	public void add(E e)
	{
		inner.add(e);
		inner.previous();
	}

}
