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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import aletheia.utilities.NaturalComparator;
import aletheia.utilities.SuperComparator;

/**
 * A {@link SortedSet} with a single entry.
 *
 * @param <E>
 *            The elements type.
 *
 * @author Quim Testar
 */
public class SingletonSortedSet<E> implements SortedSet<E>
{
	private final E element;
	private final Comparator<? super E> comparator;
	private final Set<E> singletonSet;
	private final Comparator<E> actualComparator;

	public SingletonSortedSet(E element, Comparator<? super E> comparator)
	{
		this.element = element;
		this.comparator = comparator;
		this.singletonSet = Collections.singleton(element);
		if (comparator != null)
			actualComparator = new SuperComparator<>(comparator);
		else
			actualComparator = new NaturalComparator<E>();
	}

	public SingletonSortedSet(E element)
	{
		this(element, null);
	}

	public E getElement()
	{
		return element;
	}

	@Override
	public int size()
	{
		return singletonSet.size();
	}

	@Override
	public boolean isEmpty()
	{
		return singletonSet.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return singletonSet.contains(o);
	}

	@Override
	public Iterator<E> iterator()
	{
		return singletonSet.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return singletonSet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return singletonSet.toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		return singletonSet.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		return singletonSet.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return singletonSet.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		return singletonSet.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return singletonSet.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return singletonSet.removeAll(c);
	}

	@Override
	public void clear()
	{
		singletonSet.clear();
	}

	@Override
	public boolean equals(Object o)
	{
		return singletonSet.equals(o);
	}

	@Override
	public int hashCode()
	{
		return singletonSet.hashCode();
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement)
	{
		if (actualComparator.compare(element, fromElement) >= 0 && actualComparator.compare(element, toElement) < 0)
			return this;
		else
			return new EmptySortedSet<E>(comparator);
	}

	@Override
	public SortedSet<E> headSet(E toElement)
	{
		if (actualComparator.compare(element, toElement) < 0)
			return this;
		else
			return new EmptySortedSet<E>(comparator);
	}

	@Override
	public SortedSet<E> tailSet(E fromElement)
	{
		if (actualComparator.compare(element, fromElement) >= 0)
			return this;
		else
			return new EmptySortedSet<E>(comparator);
	}

	@Override
	public E first()
	{
		return element;
	}

	@Override
	public E last()
	{
		return element;
	}

}
