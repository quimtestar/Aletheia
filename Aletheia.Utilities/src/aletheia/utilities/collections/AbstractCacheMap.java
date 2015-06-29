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

import java.lang.ref.Reference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
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
public abstract class AbstractCacheMap<K, V, R extends Reference<V>> extends AbstractMap<K, V>implements CacheMap<K, V>
{
	private final Map<K, R> innerMap;

	public AbstractCacheMap()
	{
		super();
		innerMap = new HashMap<K, R>();
	}

	protected Map<K, R> getInnerMap()
	{
		return innerMap;
	}

	@Override
	public synchronized void clear()
	{
		innerMap.clear();
	}

	@Override
	public synchronized V get(Object key)
	{
		R ref = innerMap.get(key);
		if (ref != null)
			return ref.get();
		else
			return null;
	}

	@Override
	public synchronized boolean containsKey(Object key)
	{
		return get(key) != null;
	}

	@Override
	public synchronized V remove(Object key)
	{
		R ref = innerMap.remove(key);
		if (ref != null)
			return ref.get();
		return null;
	}

	/**
	 * Creates a reference to be used for storing a value in the map. Subclasses
	 * must override this method.
	 *
	 * @param value
	 *            The value to store.
	 *
	 * @return The reference.
	 */
	protected abstract R makeRef(V value);

	protected class PutRef
	{
		protected final R old;
		protected final R new_;

		public PutRef(R old, R new_)

		{
			super();
			this.old = old;
			this.new_ = new_;
		}
	}

	protected synchronized PutRef putRef(K key, V value)
	{
		R new_ = makeRef(value);
		R old = innerMap.put(key, new_);
		return new PutRef(old, new_);
	}

	@Override
	public synchronized V put(K key, V value)
	{
		PutRef putRef = putRef(key, value);
		if (putRef.old != null)
			return putRef.old.get();
		else
			return null;
	}

	protected class EntrySet extends AbstractSet<Entry<K, V>>
	{
		protected class MyIterator implements Iterator<Entry<K, V>>
		{
			final Iterator<Entry<K, R>> iterator;
			Entry<K, R> next = null;

			protected MyIterator()
			{
				synchronized (AbstractCacheMap.this)
				{
					iterator = innerMap.entrySet().iterator();
					next = null;
					while (true)
					{
						if (!iterator.hasNext())
						{
							next = null;
							break;
						}
						next = iterator.next();
						if (next.getValue().get() != null)
							break;
					}
				}
			}

			@Override
			public boolean hasNext()
			{
				return next != null;
			}

			protected class MyEntry implements Entry<K, V>
			{
				Entry<K, R> present = next;

				@Override
				public K getKey()
				{
					return present.getKey();
				}

				@Override
				public V getValue()
				{
					return present.getValue().get();
				}

				protected PutRef setValueRef(V value)
				{
					synchronized (AbstractCacheMap.this)
					{
						R new_ = makeRef(value);
						R old = present.setValue(new_);
						return new PutRef(old, new_);
					}
				}

				@Override
				public V setValue(V value)
				{
					synchronized (AbstractCacheMap.this)
					{
						PutRef putRef = setValueRef(value);
						return putRef.old.get();
					}
				}

			}

			protected MyEntry makeEntry()
			{
				return new MyEntry();
			}

			@Override
			public Entry<K, V> next()
			{
				Entry<K, V> e = makeEntry();

				synchronized (AbstractCacheMap.this)
				{
					while (true)
					{
						if (!iterator.hasNext())
						{
							next = null;
							break;
						}
						next = iterator.next();
						if (next.getValue().get() != null)
							break;
					}
				}

				return e;

			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
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
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public synchronized Set<Entry<K, V>> entrySet()
	{
		return new EntrySet();
	}

}
