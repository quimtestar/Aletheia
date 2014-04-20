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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import aletheia.utilities.MiscUtilities;

public class DummyMapCountedSortedSet<E> implements CountedSortedSet<E>
{
	protected static class Dummy
	{
	}

	private final static Dummy dummy = new Dummy();

	protected static class SetToDummyMap<E> extends AbstractMap<E, Dummy>
	{
		private final Set<E> set;

		protected SetToDummyMap(Set<E> set)
		{
			this.set = set;
		}

		protected Set<E> getSet()
		{
			return set;
		}

		@Override
		public Set<Entry<E, Dummy>> entrySet()
		{
			return new BijectionSet<E, Entry<E, Dummy>>(new Bijection<E, Entry<E, Dummy>>()
			{

				@Override
				public Entry<E, Dummy> forward(E input)
				{
					return new SimpleEntry<E, Dummy>(input, dummy);
				}

				@Override
				public E backward(Entry<E, Dummy> output)
				{
					return output.getKey();
				}
			}, set);
		}

	}

	protected static class SortedSetToDummyMap<E> extends SetToDummyMap<E> implements SortedMap<E, Dummy>
	{

		protected SortedSetToDummyMap(SortedSet<E> set)
		{
			super(set);
		}

		@Override
		protected SortedSet<E> getSet()
		{
			return getSet();
		}

		@Override
		public Comparator<? super E> comparator()
		{
			return getSet().comparator();
		}

		@Override
		public SortedMap<E, Dummy> subMap(E fromKey, E toKey)
		{
			return new SortedSetToDummyMap<E>(getSet().subSet(fromKey, toKey));
		}

		@Override
		public SortedMap<E, Dummy> headMap(E toKey)
		{
			return new SortedSetToDummyMap<E>(getSet().headSet(toKey));
		}

		@Override
		public SortedMap<E, Dummy> tailMap(E fromKey)
		{
			return new SortedSetToDummyMap<E>(getSet().tailSet(fromKey));
		}

		@Override
		public E firstKey()
		{
			return getSet().first();
		}

		@Override
		public E lastKey()
		{
			return getSet().last();
		}
	}

	private final CountedSortedMap<E, Dummy> map;

	protected DummyMapCountedSortedSet(CountedSortedMap<E, Dummy> map)
	{
		this.map = map;
	}

	protected CountedSortedMap<E, Dummy> getMap()
	{
		return map;
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return map.comparator();
	}

	@Override
	public CountedSortedSet<E> subSet(E fromElement, E toElement)
	{
		return new DummyMapCountedSortedSet<E>(map.subMap(fromElement, toElement));
	}

	@Override
	public CountedSortedSet<E> headSet(E toElement)
	{
		return new DummyMapCountedSortedSet<E>(map.headMap(toElement));
	}

	@Override
	public CountedSortedSet<E> tailSet(E fromElement)
	{
		return new DummyMapCountedSortedSet<E>(map.tailMap(fromElement));
	}

	@Override
	public E first()
	{
		return map.firstKey();
	}

	@Override
	public E last()
	{
		return map.lastKey();
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return map.containsKey(o);
	}

	@Override
	public CountedIterator<E> iterator()
	{
		return map.keySet().iterator();
	}

	@Override
	public Object[] toArray()
	{
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return map.keySet().toArray(a);
	}

	@Override
	public boolean add(E e)
	{
		return map.put(e, dummy) == null;
	}

	@Override
	public boolean remove(Object o)
	{
		return map.remove(o) != null;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return map.keySet().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		boolean change = false;
		for (E e : c)
			if (add(e))
				change = true;
		return change;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return map.keySet().retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return map.keySet().removeAll(c);
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public E get(int ordinal)
	{
		return map.get(ordinal).getKey();
	}

	@Override
	public E remove(int ordinal)
	{
		return map.remove(ordinal).getKey();
	}

	@Override
	public int ordinalOf(Object o)
	{
		return map.ordinalOfKey(o);
	}

	@Override
	public String toString()
	{
		return MiscUtilities.toString(this);
	}

}
