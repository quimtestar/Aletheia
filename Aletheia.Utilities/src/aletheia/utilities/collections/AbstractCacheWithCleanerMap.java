/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Abstract implementation of cache map, independent of the actual subclass of
 * {@link Reference} used to store the values.
 *
 * @param <K>
 *            The map keys' type.
 * @param <V>
 *            The map value's type.
 * @param <R>
 *            The reference type to the values.
 *
 * @author Quim Testar
 */
public abstract class AbstractCacheWithCleanerMap<K, V, R extends Reference<V>> extends AbstractCacheMap<K, V, R> implements CacheWithCleanerMap<K, V>
{
	private final ReferenceQueue<V> queue;
	private final Map<R, K> reverseMap;

	private final Set<Listener<K>> listeners = new HashSet<>();

	public AbstractCacheWithCleanerMap()
	{
		super();
		queue = new ReferenceQueue<>();
		reverseMap = new HashMap<>();
	}

	private synchronized void cleanReferences()
	{
		while (true)
		{
			@SuppressWarnings("unchecked")
			R ref = (R) queue.poll();
			if (ref == null)
				break;
			cleanReference(ref);
		}
	}

	private synchronized void cleanReference(R ref)
	{
		K key = reverseMap.remove(ref);
		if (key != null)
		{
			getInnerMap().remove(key);
			for (Listener<K> listener : listeners)
				listener.keyCleaned(key);
		}
	}

	@Override
	public synchronized void addListener(Listener<K> listener)
	{
		listeners.add(listener);
	}

	@Override
	public synchronized void removeListener(Listener<K> listener)
	{
		listeners.remove(listener);
	}

	@Override
	public synchronized void clear()
	{
		super.clear();
		reverseMap.clear();
	}

	@Override
	public synchronized boolean containsKey(Object key)
	{
		return get(key) != null;
	}

	@Override
	public synchronized V remove(Object key)
	{
		R ref = getInnerMap().remove(key);
		V v = null;
		if (ref != null)
		{
			reverseMap.remove(ref);
			v = ref.get();
		}
		cleanReferences();
		return v;
	}

	/**
	 * Creates a reference to be used for storing a value in the map. Subclasses
	 * must override this method.
	 *
	 * @param value
	 *            The value to store.
	 * @param queue
	 *            The reference queue to register the reference.
	 * @return The reference.
	 */
	protected abstract R makeRef(V value, ReferenceQueue<V> queue);

	@Override
	protected R makeRef(V value)
	{
		return makeRef(value, queue);
	}

	@Override
	public synchronized V put(K key, V value)
	{
		PutRef putRef = putRef(key, value);
		reverseMap.put(putRef.new_, key);
		V v = null;
		if (putRef.old != null)
		{
			reverseMap.remove(putRef.old);
			v = putRef.old.get();
		}
		cleanReferences();
		return v;
	}

	protected class EntrySet extends AbstractCacheMap<K, V, R>.EntrySet
	{
		protected class MyIterator extends AbstractCacheMap<K, V, R>.EntrySet.MyIterator
		{

			protected class MyEntry extends AbstractCacheMap<K, V, R>.EntrySet.MyIterator.MyEntry
			{
				@Override
				public V setValue(V value)
				{
					synchronized (AbstractCacheWithCleanerMap.this)
					{
						PutRef putRef = setValueRef(value);
						reverseMap.put(putRef.new_, present.getKey());
						return putRef.old.get();
					}
				}

			}

			@Override
			protected MyEntry makeEntry()
			{
				return new MyEntry();
			}
		}

		@Override
		public Iterator<Entry<K, V>> iterator()
		{
			return new MyIterator();
		}

		@Override
		public int size()
		{
			synchronized (AbstractCacheWithCleanerMap.this)
			{
				return getInnerMap().size();
			}
		}

	}

	@Override
	public synchronized Set<Entry<K, V>> entrySet()
	{
		return new EntrySet();
	}

}
