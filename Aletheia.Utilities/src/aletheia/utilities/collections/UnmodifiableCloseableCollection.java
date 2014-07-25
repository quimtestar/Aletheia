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

import java.util.Collection;

/**
 * A read-only view of a {@link CloseableCollection}.
 *
 * @author Quim Testar
 */
public class UnmodifiableCloseableCollection<E> implements CloseableCollection<E>
{
	private final CloseableCollection<E> inner;

	public UnmodifiableCloseableCollection(CloseableCollection<E> inner)
	{
		super();
		this.inner = inner;
	}

	protected CloseableCollection<E> getInner()
	{
		return inner;
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return inner.contains(o);
	}

	@Override
	public CloseableIterator<E> iterator()
	{
		return new UnmodifiableCloseableIterator<>(inner.iterator());
	}

	@Override
	public Object[] toArray()
	{
		return inner.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return inner.toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return inner.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o)
	{
		return inner.equals(o);
	}

	@Override
	public int hashCode()
	{
		return inner.hashCode();
	}

}
