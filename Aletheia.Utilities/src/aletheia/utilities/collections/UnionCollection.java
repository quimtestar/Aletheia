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
import java.util.Iterator;

import aletheia.utilities.MiscUtilities;

/**
 * A collection that is the result of the union operation on a collection of
 * collections. The union operation is the collection of the elements of the
 * elements of the collection that we are uniting.
 *
 * @param <E>
 *            The elements' type.
 *
 * @author Quim Testar
 */
public class UnionCollection<E> extends AbstractReadOnlyCollection<E>
{
	private final Collection<? extends Collection<E>> inner;

	/**
	 * Creates a new union collection.
	 *
	 * @param inner
	 *            The collection of collections to unite.
	 */
	public UnionCollection(Collection<? extends Collection<E>> inner)
	{
		this.inner = inner;
	}

	protected Collection<? extends Collection<E>> getInner()
			{
		return inner;
			}

	/**
	 * The sum of the sizes of all the element collections of the united
	 * collection.
	 */
	@Override
	public int size()
	{
		int size = 0;
		for (Collection<E> c : inner)
			size += c.size();
		return size;
	}

	/**
	 * All the element collections are empty.
	 */
	@Override
	public boolean isEmpty()
	{
		for (Collection<E> c : inner)
			if (!c.isEmpty())
				return false;
		return true;
	}

	/**
	 * One of the element collections contains the element.
	 */
	@Override
	public boolean contains(Object o)
	{
		for (Collection<E> c : inner)
			if (c.contains(o))
				return true;
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object o : c)
			if (!contains(o))
				return true;
		return false;
	}

	/**
	 * The element collections are successively iterated.
	 */
	@Override
	public Iterator<E> iterator()
	{
		return new UnionIterable<E>(inner).iterator();
	}

	@Override
	public Object[] toArray()
	{
		return MiscUtilities.iterableToArray(this);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return MiscUtilities.iterableToArray(this, a);
	}

}
