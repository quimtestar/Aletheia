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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

public class EmptySortedMap<K, V> extends AbstractReadOnlySortedMap<K, V> implements SortedMap<K, V>
{
	private final Comparator<? super K> comparator;

	public EmptySortedMap(Comparator<? super K> comparator)
	{
		this.comparator = comparator;
	}

	public EmptySortedMap()
	{
		this(null);
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public boolean containsKey(Object key)
	{
		return false;
	}

	@Override
	public boolean containsValue(Object value)
	{
		return false;
	}

	@Override
	public V get(Object key)
	{
		return null;
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return comparator;
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return this;
	}

	@Override
	public SortedMap<K, V> headMap(K toKey)
	{
		return this;
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey)
	{
		return this;
	}

	@Override
	public K firstKey()
	{
		throw new NoSuchElementException();
	}

	@Override
	public K lastKey()
	{
		throw new NoSuchElementException();
	}

	@Override
	public Set<K> keySet()
	{
		return Collections.emptySet();
	}

	@Override
	public Collection<V> values()
	{
		return Collections.emptySet();
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return Collections.emptySet();
	}

}
