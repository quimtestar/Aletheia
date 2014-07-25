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

/**
 * A {@link CountedSortedSet} implemented as the key set of a
 * {@link CountedSortedMap}.
 *
 * @author Quim Testar
 */
public class DummyMapCountedSortedSet<E> extends DummyMapSortedSet<E> implements CountedSortedSet<E>
{

	protected DummyMapCountedSortedSet(CountedSortedMap<E, Dummy> map)
	{
		super(map);
	}

	@Override
	protected CountedSortedMap<E, Dummy> getMap()
	{
		return (CountedSortedMap<E, Dummy>) super.getMap();
	}

	@Override
	public CountedSortedSet<E> subSet(E fromElement, E toElement)
	{
		return new DummyMapCountedSortedSet<E>(getMap().subMap(fromElement, toElement));
	}

	@Override
	public CountedSortedSet<E> headSet(E toElement)
	{
		return new DummyMapCountedSortedSet<E>(getMap().headMap(toElement));
	}

	@Override
	public CountedSortedSet<E> tailSet(E fromElement)
	{
		return new DummyMapCountedSortedSet<E>(getMap().tailMap(fromElement));
	}

	@Override
	public CountedIterator<E> iterator()
	{
		return getMap().keySet().iterator();
	}

	@Override
	public E get(int ordinal)
	{
		return getMap().get(ordinal).getKey();
	}

	@Override
	public E remove(int ordinal)
	{
		return getMap().remove(ordinal).getKey();
	}

	@Override
	public int ordinalOf(Object o)
	{
		return getMap().ordinalOfKey(o);
	}

}
