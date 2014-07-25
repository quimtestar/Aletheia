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

import java.util.Map;

/**
 * A {@link CloseableMap} on the top of a regular {@link Map} and whose
 * {@link CloseableIterator#close()} method does nothing.
 *
 * @author Quim Testar
 */
public class TrivialCloseableMap<K, V> extends AbstractCloseableMap<K, V>
{
	private final Map<K, V> inner;

	public TrivialCloseableMap(Map<K, V> inner)
	{
		this.inner = inner;
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return inner.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return inner.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return inner.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		return inner.put(key, value);
	}

	@Override
	public V remove(Object key)
	{
		return inner.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		inner.putAll(m);
	}

	@Override
	public void clear()
	{
		inner.clear();
	}

	@Override
	public CloseableSet<K> keySet()
	{
		return new TrivialCloseableSet<K>(inner.keySet());
	}

	@Override
	public CloseableCollection<V> values()
	{
		return new TrivialCloseableCollection<V>(inner.values());
	}

	@Override
	public CloseableSet<Entry<K, V>> entrySet()
	{
		return new TrivialCloseableSet<Entry<K, V>>(inner.entrySet());
	}

}
