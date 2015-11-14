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

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Abstract implementation of a combined map. The entries in the front map will
 * shadow the entries in the back map with the same key. The management of the
 * back map is kept abstract.
 *
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedMap<K, V> extends AbstractReadOnlyMap<K, V> implements Serializable
{
	private static final long serialVersionUID = 6151843791136257958L;

	private final Map<K, V> front;

	public AbstractCombinedMap(Map<K, V> front)
	{
		super();
		this.front = front;
	}

	protected Map<K, V> getFront()
	{
		return front;
	}

	protected abstract Map<K, V> getBack();

	@Override
	public boolean containsKey(Object key)
	{
		return front.containsKey(key) || getBack().containsKey(key);

	}

	@Override
	public boolean containsValue(Object value)
	{
		return front.containsValue(value) || getBack().containsValue(getBack());
	}

	protected class EntrySet extends AbstractSet<Entry<K, V>>
	{
		private final Set<K> keySet;

		protected EntrySet(Set<K> keySet)
		{
			this.keySet = keySet;
		}

		protected Set<K> getKeySet()
		{
			return keySet;
		}

		@Override
		public int size()
		{
			return keySet.size();
		}

		@Override
		public boolean contains(Object o)
		{
			if (!(o instanceof Entry<?, ?>))
				return false;
			Entry<?, ?> e = (Entry<?, ?>) o;
			if (!keySet.contains(e.getKey()))
				return false;
			V v = get(e.getKey());
			if (!v.equals(e.getValue()))
				return false;
			return true;
		}

		protected class EntrySetIterator implements Iterator<Entry<K, V>>
		{
			private final Iterator<K> keyIterator;

			protected EntrySetIterator(Iterator<K> keyIterator)
			{
				this.keyIterator = keyIterator;
			}

			protected Iterator<K> getKeyIterator()
			{
				return keyIterator;
			}

			@Override
			public boolean hasNext()
			{
				return keyIterator.hasNext();
			}

			@Override
			public Map.Entry<K, V> next()
			{
				final K k = keyIterator.next();
				return new Map.Entry<K, V>()
				{

					@Override
					public K getKey()
					{
						return k;
					}

					@Override
					public V getValue()
					{
						return AbstractCombinedMap.this.get(k);
					}

					@Override
					public V setValue(V value)
					{
						return AbstractCombinedMap.this.put(k, value);
					}

					@Override
					public String toString()
					{
						return getKey() + "=" + getValue();
					}

				};
			}

			@Override
			public void remove()
			{
				keyIterator.remove();
			}

		};

		@Override
		public Iterator<Entry<K, V>> iterator()
		{
			return new EntrySetIterator(keySet.iterator());
		}

	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return new EntrySet(keySet());
	}

	@Override
	public V get(Object key)
	{
		V val = front.get(key);
		if (val == null)
			val = getBack().get(key);
		return val;
	}

	@Override
	public boolean isEmpty()
	{
		return front.isEmpty() && getBack().isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		return new CombinedSet<K>(front.keySet(), getBack().keySet());
	}

	@Override
	public int size()
	{
		return keySet().size();
	}

	protected class Values extends AbstractCollection<V>
	{
		private final Set<Entry<K, V>> entrySet;

		protected Values(Set<Entry<K, V>> set)
		{
			this.entrySet = set;
		}

		protected Set<Entry<K, V>> getEntrySet()
		{
			return entrySet;
		}

		protected class ValuesIterator implements Iterator<V>
		{
			private final Iterator<Entry<K, V>> entrySetIterator;

			protected ValuesIterator(Iterator<Entry<K, V>> iterator)
			{
				this.entrySetIterator = iterator;
			}

			protected Iterator<Entry<K, V>> getEntrySetIterator()
			{
				return entrySetIterator;
			}

			@Override
			public boolean hasNext()
			{
				return entrySetIterator.hasNext();
			}

			@Override
			public V next()
			{
				return entrySetIterator.next().getValue();
			}

			@Override
			public void remove()
			{
				entrySetIterator.remove();
			}

		}

		@Override
		public Iterator<V> iterator()
		{
			return new ValuesIterator(entrySet.iterator());
		}

		@Override
		public int size()
		{
			return entrySet.size();
		}

	}

	@Override
	public Collection<V> values()
	{
		return new Values(entrySet());
	}

}
