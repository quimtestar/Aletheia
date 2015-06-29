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
 * Gives a read-only view of a {@link Collection} as a {@link Collection} with a
 * type parameter that is a superclass of the original's.
 *
 * @param <E>
 *            The type of the resulting {@link Collection} view.
 *
 * @author Quim Testar
 */
public class AdaptedCollection<E> extends AdaptedIterable<E>implements Collection<E>
{

	public AdaptedCollection(Collection<? extends E> inner)
	{
		super(inner);
	}

	/**
	 * The original collection this collection is a view of.
	 *
	 * @return The original collection.
	 */
	@Override
	protected Collection<? extends E> getInner()
	{
		return (Collection<? extends E>) super.getInner();
	}

	@Override
	public int size()
	{
		return getInner().size();
	}

	@Override
	public boolean add(E e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o)
	{
		return getInner().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return getInner().containsAll(c);
	}

	@Override
	public boolean isEmpty()
	{
		return getInner().isEmpty();
	}

	@Override
	public boolean remove(Object arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray()
	{
		return getInner().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return getInner().toArray(a);
	}

}
