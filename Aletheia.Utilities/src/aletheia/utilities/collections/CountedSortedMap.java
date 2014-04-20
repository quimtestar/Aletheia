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

public interface CountedSortedMap<K, V> extends SortedMap<K, V>
{
	Entry<K, V> get(int ordinal);

	Entry<K, V> remove(int ordinal);

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
