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
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Vector;

import aletheia.utilities.MiscUtilities;
import aletheia.utilities.SuperComparator;

/**
 * Implementation of a priority queue with limited capacity that silently
 * discards the elements with lower priority when that limit capacity is
 * reached. The capacity cannot be fixed as any integer, it must be in the form
 * of 3*2^height-2, where <b>height</b> can be any positive integer.
 *
 * @param <E>
 *            The elements' type.
 *
 * @author Quim Testar
 */
public class DiamondPriorityDiscardingQueue<E> implements Queue<E>
{
	private final Comparator<E> comparator;
	private final int height;
	private final int capacity;
	private final Vector<E> heap;
	private int size;

	/**
	 * Creates a default {@link Comparator} based on the
	 * {@link Comparable#compareTo(Object)} method. The
	 * {@link Comparator#compare(Object, Object)} method of the returned
	 * {@link Comparator} may throw a {@link ClassCastException} when it
	 * compares an object that does not implement the {@link Comparable}
	 * interface.
	 *
	 * @return The created {@link Comparator}.
	 */
	private static <E> Comparator<E> defaultComparator()
	{
		return new Comparator<E>()
				{
			@SuppressWarnings("unchecked")
			@Override
			public int compare(E e1, E e2) throws ClassCastException
			{
				return ((Comparable<? super E>) e1).compareTo(e2);
			}
				};
	}

	/**
	 * Creates a new priority discarding queue with a given height and a
	 * comparator. The resulting capacity of the queue will be 3*2^height-2.
	 *
	 * @param height
	 *            The height.
	 * @param comparator
	 *            The comparator.
	 */
	public DiamondPriorityDiscardingQueue(int height, Comparator<? super E> comparator)
	{
		this.comparator = comparator != null ? new SuperComparator<>(comparator) : DiamondPriorityDiscardingQueue.<E> defaultComparator();
		this.height = height;
		this.capacity = 3 * (1 << height) - 2;
		this.heap = new Vector<E>();
		this.size = 0;
	}

	/**
	 * Creates a new priority discarding queue with a given height and the
	 * {@link #defaultComparator()}. The resulting capacity of the queue will be
	 * 3*2^height-2.
	 *
	 * @param height
	 *            The height.
	 */
	public DiamondPriorityDiscardingQueue(int height)
	{
		this(height, null);
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		return size <= 0;
	}

	@Override
	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
				{
			int i = 0;

			@Override
			public boolean hasNext()
			{
				return (i < size);
			}

			@Override
			public E next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				return heap.get(i++);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

				};
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

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		boolean ret = false;
		for (E e : c)
			if (add(e))
				ret = true;
		return ret;
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

	private void set(int i, E e)
	{
		if (i >= heap.size())
			heap.setSize(i + 1);
		heap.set(i, e);
	}

	/**
	 * Will always return true, but the element just inserted might be
	 * inmediately discarded.
	 */
	@Override
	public boolean add(E e)
	{
		if (size >= capacity)
		{
			if (comparator.compare(e, heap.get(capacity - 1)) >= 0)
				return true;
			size = capacity - 1;
		}
		int i = size;

		while (i > 0)
		{
			if (i >= (1 << (height + 1)) - 1)
			{
				int il = 2 * i + 1 - 3 * (1 << height);
				int ir = 2 * i + 2 - 3 * (1 << height);
				E l = heap.get(il);
				E r = heap.get(ir);
				if (comparator.compare(l, r) >= 0)
				{
					if (comparator.compare(l, e) <= 0)
						break;
					set(i, l);
					i = il;
				}
				else
				{
					if (comparator.compare(r, e) <= 0)
						break;
					set(i, r);
					i = ir;
				}
			}
			else
			{
				E e_ = heap.get((i - 1) / 2);
				if (comparator.compare(e, e_) >= 0)
					break;
				set(i, e_);
				i = (i - 1) / 2;
			}
		}
		set(i, e);
		size++;
		return true;
	}

	@Override
	public boolean offer(E e)
	{
		return add(e);
	}

	@Override
	public E remove()
	{
		E e = element();
		E t = heap.get(size - 1);
		int i = 0;
		while (true)
		{
			if (i <= ((1 << height) - 1))
			{
				E l = 2 * i + 1 < size ? heap.get(2 * i + 1) : null;
				E r = 2 * i + 2 < size ? heap.get(2 * i + 2) : null;
				if (l == null)
					break;
				if (r == null || comparator.compare(l, r) <= 0)
				{
					if (comparator.compare(t, l) <= 0)
						break;
					set(i, l);
					i = 2 * i + 1;
				}
				else
				{
					if (comparator.compare(t, r) <= 0)
						break;
					set(i, r);
					i = 2 * i + 2;
				}
			}
			else
			{
				int j = (3 * (1 << height) + i - 1) / 2;
				if (j >= size)
					break;
				E e_ = heap.get(j);
				if (comparator.compare(e, e_) >= 0)
					break;
				set(i, e_);
				i = j;
			}
		}
		set(i, t);
		size--;
		return e;
	}

	@Override
	public E poll()
	{
		try
		{
			return remove();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	@Override
	public E element()
	{
		if (size <= 0)
			throw new NoSuchElementException();
		return heap.get(0);
	}

	@Override
	public E peek()
	{
		try
		{
			return element();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}

	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for (E e : this)
		{
			if (!first)
				sb.append(", ");
			first = false;
			sb.append(e.toString());
		}
		sb.append("]");
		return sb.toString();
	}

}
