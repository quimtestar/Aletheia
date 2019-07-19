/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Gives a read-only view of a {@link Map} as a {@link Map} with type parameters
 * that are superclasses of the original's
 *
 * @param <K>
 *            The keys type of the resulting map view.
 * @param <V>
 *            The values type of the resulting map view.
 *
 * @author Quim Testar
 */
public class AdaptedMap<K, V> implements Map<K, V>
{

	private final Map<? extends K, ? extends V> inner;

	public AdaptedMap(Map<? extends K, ? extends V> inner)
	{
		super();
		this.inner = inner;
	}

	protected Map<? extends K, ? extends V> getInner()
	{
		return inner;
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
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
	public Set<Entry<K, V>> entrySet()
	{
		final Set<K> keys = keySet();
		return new AbstractSet<>()
		{

			@Override
			public Iterator<Entry<K, V>> iterator()
			{
				final Iterator<K> iterator = keys.iterator();
				return new Iterator<>()
				{

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public Entry<K, V> next()
					{
						final K key = iterator.next();
						return new Entry<>()
						{

							@Override
							public K getKey()
							{
								return key;
							}

							@Override
							public V getValue()
							{
								return get(key);
							}

							@Override
							public V setValue(V value)
							{
								throw new UnsupportedOperationException();
							}

						};
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}

				};
			}

			@Override
			public int size()
			{
				return inner.size();
			}
		};
	}

	@Override
	public V get(Object key)
	{
		return inner.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return new AdaptedSet<>(inner.keySet());
	}

	@Override
	public V put(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public Collection<V> values()
	{
		return new AdaptedCollection<>(inner.values());
	}

}
