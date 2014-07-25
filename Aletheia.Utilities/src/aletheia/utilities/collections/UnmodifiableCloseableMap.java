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
 * A read-only view of a {@link CloseableMap}.
 *
 * @author Quim Testar
 */
public class UnmodifiableCloseableMap<K, V> implements CloseableMap<K, V>
{
	private final CloseableMap<K, V> inner;

	public UnmodifiableCloseableMap(CloseableMap<K, V> inner)
	{
		super();
		this.inner = inner;
	}

	protected CloseableMap<K, V> getInner()
	{
		return inner;
	}

	@Override
	public CloseableSet<java.util.Map.Entry<K, V>> entrySet()
	{
		return new UnmodifiableCloseableSet<>(inner.entrySet());
	}

	@Override
	public CloseableSet<K> keySet()
	{
		return new UnmodifiableCloseableSet<>(inner.keySet());
	}

	@Override
	public CloseableCollection<V> values()
	{
		return new UnmodifiableCloseableCollection<>(inner.values());
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
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o)
	{
		return inner.equals(o);
	}

	@Override
	public int hashCode()
	{
		return inner.hashCode();
	}

}
