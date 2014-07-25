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
import java.util.Set;
import java.util.SortedMap;

/**
 * A {@link SortedMap} whose entries can be addressed by its position according
 * to the Map's order.
 *
 * @author Quim Testar
 */
public interface CountedSortedMap<K, V> extends SortedMap<K, V>
{
	/**
	 * Return the map's entry corresponding to a given ordinal.
	 *
	 * @param ordinal
	 *            The ordinal position.
	 */
	Entry<K, V> get(int ordinal);

	/**
	 * Remove and return the map's entry corresponding to a given ordinal.
	 *
	 * @param ordinal
	 *            The ordinal position.
	 */
	Entry<K, V> remove(int ordinal);

	/**
	 * Returns the ordinal corresponding to a given key or the smallest greater
	 * element if the key is not present.
	 */
	int ordinalOfKey(Object o);

	@Override
	CountedSortedMap<K, V> subMap(K fromKey, K toKey);

	@Override
	CountedSortedMap<K, V> headMap(K toKey);

	@Override
	CountedSortedMap<K, V> tailMap(K fromKey);

	interface CountedIteratorCollection<E> extends Collection<E>
	{
		@Override
		CountedIterator<E> iterator();
	}

	interface CountedIteratorSet<E> extends CountedIteratorCollection<E>, Set<E>
	{
	}

	@Override
	CountedIteratorSet<Entry<K, V>> entrySet();

	@Override
	CountedIteratorSet<K> keySet();

	@Override
	CountedIteratorCollection<V> values();

}
