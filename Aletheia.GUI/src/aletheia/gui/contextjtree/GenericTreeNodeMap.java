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
package aletheia.gui.contextjtree;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import aletheia.utilities.collections.SoftCacheWithCleanerMap;

public abstract class GenericTreeNodeMap<K, N> extends AbstractMap<K, N>
{
	private final SoftCacheWithCleanerMap<K, N> map;

	private class CacheListener implements SoftCacheWithCleanerMap.Listener<K>
	{

		@Override
		public void keyCleaned(K key)
		{
			synchronized (map)
			{
				keyRemoved(key);
			}
		}

	}

	public GenericTreeNodeMap()
	{
		this.map = new SoftCacheWithCleanerMap<K, N>();
		this.map.addListener(new CacheListener());
	}

	public synchronized boolean cachedKey(K key)
	{
		return map.containsKey(key);
	}

	protected abstract N buildNode(K key);

	protected abstract void keyRemoved(K key);

	@Override
	public synchronized N get(Object oKey)
	{
		try
		{
			@SuppressWarnings("unchecked")
			K key = (K) oKey;
			synchronized (map)
			{
				N node = map.get(key);
				if (node == null)
				{
					node = buildNode(key);
					map.put(key, node);
				}
				return node;
			}

		}
		catch (ClassCastException e)
		{
			return null;
		}

	}

	@Override
	public boolean containsKey(Object key)
	{
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized Set<Map.Entry<K, N>> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return map.keySet();
	}

	@Override
	public synchronized int size()
	{
		return map.size();
	}

	@Override
	public synchronized Collection<N> values()
	{
		return map.values();
	}

	@Override
	public synchronized N remove(Object oKey)
	{
		try
		{
			@SuppressWarnings("unchecked")
			K key = (K) oKey;
			synchronized (map)
			{
				N node = map.remove(key);
				if (node != null)
					keyRemoved(key);
				return node;
			}
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}

	@Override
	public synchronized void clear()
	{
		for (K k : keySet())
			keyRemoved(k);
		map.clear();
	}

	@Override
	public final N put(K key, N value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void putAll(Map<? extends K, ? extends N> m)
	{
		throw new UnsupportedOperationException();
	}

}
