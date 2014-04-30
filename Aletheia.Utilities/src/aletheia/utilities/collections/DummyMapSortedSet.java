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

import java.util.Comparator;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * A {@link SortedSet} implemented as the key set of a {@link SortedMap}.
 * 
 * @author Quim Testar
 */
public class DummyMapSortedSet<E> extends DummyMapSet<E> implements SortedSet<E>
{

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

	protected DummyMapSortedSet(SortedMap<E, Dummy> map)
	{
		super(map);
	}

	@Override
	protected SortedMap<E, Dummy> getMap()
	{
		return (SortedMap<E, Dummy>) super.getMap();
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return getMap().comparator();
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement)
	{
		return new DummyMapSortedSet<E>(getMap().subMap(fromElement, toElement));
	}

	@Override
	public SortedSet<E> headSet(E toElement)
	{
		return new DummyMapSortedSet<E>(getMap().headMap(toElement));
	}

	@Override
	public SortedSet<E> tailSet(E fromElement)
	{
		return new DummyMapSortedSet<E>(getMap().tailMap(fromElement));
	}

	@Override
	public E first()
	{
		return getMap().firstKey();
	}

	@Override
	public E last()
	{
		return getMap().lastKey();
	}

	@Override
	public int size()
	{
		return getMap().size();
	}

}
