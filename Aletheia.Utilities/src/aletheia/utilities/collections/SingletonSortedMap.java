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
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * A {@link SortedMap} with a single entry.
 * 
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 */
public class SingletonSortedMap<K, V> implements SortedMap<K, V>
{
	private final K key;
	private final V value;
	private final Map<K, V> singletonMap;

	/**
	 * Creates a new singleton sorted map.
	 * 
	 * @param key
	 *            The key.
	 * @param value
	 *            The value.
	 */
	public SingletonSortedMap(K key, V value)
	{
		this.key = key;
		this.value = value;
		this.singletonMap = Collections.singletonMap(key, value);
	}

	/**
	 * @return The key.
	 */
	protected K getKey()
	{
		return key;
	}

	/**
	 * @return The value.
	 */
	protected V getValue()
	{
		return value;
	}

	/**
	 * @return The singleton map.
	 */
	protected Map<K, V> getSingletonMap()
	{
		return singletonMap;
	}

	@Override
	public void clear()
	{
		singletonMap.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return singletonMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return singletonMap.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return singletonMap.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return singletonMap.isEmpty();
	}

	@Override
	public V put(K key, V value)
	{
		return singletonMap.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		singletonMap.putAll(m);
	}

	@Override
	public V remove(Object key)
	{
		return singletonMap.remove(key);
	}

	@Override
	public int size()
	{
		return singletonMap.size();
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return singletonMap.entrySet();
	}

	@Override
	public K firstKey()
	{
		return key;
	}

	@Override
	public SortedMap<K, V> headMap(K toKey)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet()
	{
		return singletonMap.keySet();
	}

	@Override
	public K lastKey()
	{
		return key;
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<V> values()
	{
		return singletonMap.values();
	}

}
