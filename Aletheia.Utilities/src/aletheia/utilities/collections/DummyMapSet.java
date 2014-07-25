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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import aletheia.utilities.MiscUtilities;

/**
 * A {@link Set} implemented as the key set of a {@link Map}.
 *
 * @author Quim Testar
 */
public class DummyMapSet<E> implements Set<E>
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

	private final Map<E, Dummy> map;

	protected DummyMapSet(Map<E, Dummy> map)
	{
		this.map = map;
	}

	protected Map<E, Dummy> getMap()
	{
		return map;
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
	public Iterator<E> iterator()
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
	public String toString()
	{
		return MiscUtilities.toString(this);
	}

}
