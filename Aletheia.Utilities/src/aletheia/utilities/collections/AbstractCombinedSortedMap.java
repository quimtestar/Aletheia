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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import aletheia.utilities.MiscUtilities;

/**
 * Abstract implementation of a combined {@link SortedMap}. The entries in the
 * front map will shadow the entries in the back map with the same key. The
 * management of the back map is kept abstract.
 *
 * @param <K>
 *            The keys type.
 * @param <V>
 *            The values type.
 */
public abstract class AbstractCombinedSortedMap<K, V> extends AbstractCombinedMap<K, V> implements SortedMap<K, V>
{
	private static final long serialVersionUID = 7809913304487413172L;

	public AbstractCombinedSortedMap(SortedMap<K, V> front)
	{
		super(front);
	}

	@Override
	protected SortedMap<K, V> getFront()
	{
		return (SortedMap<K, V>) super.getFront();
	}

	@Override
	protected abstract SortedMap<K, V> getBack();

	@Override
	public Comparator<? super K> comparator()
	{
		return getFront().comparator();
	}

	/**
	 * The comparator that will be used in the iterator. If the front map has a
	 * comparator, use this. If not, a natural comparator is created using the
	 * {@link Comparable#compareTo(Object)} method of the elements. In that
	 * case, a {@link ClassCastException} might be thrown (not now but when
	 * using the comparator) if an element that does no implement
	 * {@link Comparable} is found.
	 *
	 * @return The comparator.
	 */
	protected Comparator<? super K> resolvedComparator()
	{
		Comparator<? super K> comp = comparator();
		if (comp != null)
			return comp;
		return new Comparator<K>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public int compare(K k1, K k2)
			{
				try
				{
					return ((Comparable<K>) k1).compareTo(k2);
				}
				catch (ClassCastException e)
				{
					throw new ClassCastException();
				}
			};
		};
	}

	@Override
	public Set<K> keySet()
	{
		return new CombinedSetSortedIterator<>(getFront().keySet(), getBack().keySet(), comparator());
	}

	@Override
	public K firstKey()
	{
		K k1;
		try
		{
			k1 = getFront().firstKey();
		}
		catch (NoSuchElementException e)
		{
			return getBack().firstKey();
		}
		K k2;
		try
		{
			k2 = getBack().firstKey();
		}
		catch (NoSuchElementException e)
		{
			return k1;
		}
		return resolvedComparator().compare(k1, k2) <= 0 ? k1 : k2;
	}

	@Override
	public K lastKey()
	{
		K k1;
		try
		{
			k1 = getFront().lastKey();
		}
		catch (NoSuchElementException e)
		{
			return getBack().lastKey();
		}
		K k2;
		try
		{
			k2 = getBack().lastKey();
		}
		catch (NoSuchElementException e)
		{
			return getFront().lastKey();
		}
		return resolvedComparator().compare(k1, k2) >= 0 ? k1 : k2;
	}

	@Override
	public SortedMap<K, V> headMap(final K toKey)
	{
		return new AbstractCombinedSortedMap<K, V>(getFront().headMap(toKey))
		{
			private static final long serialVersionUID = 7624550350393322501L;

			@Override
			protected SortedMap<K, V> getBack()
			{
				return AbstractCombinedSortedMap.this.getBack().headMap(toKey);
			}
		};
	}

	@Override
	public SortedMap<K, V> subMap(final K fromKey, final K toKey)
	{
		return new AbstractCombinedSortedMap<K, V>(getFront().subMap(fromKey, toKey))
		{
			private static final long serialVersionUID = -326776527215251305L;

			@Override
			protected SortedMap<K, V> getBack()
			{
				return AbstractCombinedSortedMap.this.getBack().subMap(fromKey, toKey);
			}

		};
	}

	@Override
	public SortedMap<K, V> tailMap(final K fromKey)
	{
		return new AbstractCombinedSortedMap<K, V>(getFront().tailMap(fromKey))
		{
			private static final long serialVersionUID = 134854101902632800L;

			@Override
			protected SortedMap<K, V> getBack()
			{
				return AbstractCombinedSortedMap.this.getBack().tailMap(fromKey);
			}

		};
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet()
	{
		final Set<K> keySet = keySet();
		return new AbstractReadOnlySet<Map.Entry<K, V>>()
		{

			@Override
			public boolean contains(Object o)
			{
				if (o instanceof Map.Entry<?, ?>)
					return keySet.contains(((Map.Entry<?, ?>) o).getKey());
				else
					return false;
			}

			@Override
			public boolean containsAll(Collection<?> col)
			{
				for (Object o : col)
					if (!contains(o))
						return false;
				return true;
			}

			@Override
			public boolean isEmpty()
			{
				return keySet.isEmpty();
			}

			@Override
			public Iterator<Map.Entry<K, V>> iterator()
			{
				final Iterator<K> iterator = keySet.iterator();
				return new AbstractReadOnlyIterator<Map.Entry<K, V>>()
				{

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public Map.Entry<K, V> next()
					{
						final K key = iterator.next();
						return new Map.Entry<K, V>()
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

							@Override
							public String toString()
							{
								return getKey().toString() + " => " + getValue().toString();
							}

						};
					}

				};
			}

			@Override
			public int size()
			{
				return keySet.size();
			}

			@Override
			public Object[] toArray()
			{
				return MiscUtilities.iterableToArray(this);
			}

			@Override
			public <T> T[] toArray(T[] a)
			{
				return MiscUtilities.iterableToArray(this, a);
			}

		};
	}

	@Override
	public Collection<V> values()
	{
		final Set<K> keySet = keySet();
		return new AbstractReadOnlyCollection<V>()
		{

			@Override
			public boolean contains(Object o)
			{
				if (o instanceof Map.Entry<?, ?>)
					return keySet.contains(((Map.Entry<?, ?>) o).getKey());
				else
					return false;
			}

			@Override
			public boolean containsAll(Collection<?> col)
			{
				for (Object o : col)
					if (!contains(o))
						return false;
				return true;
			}

			@Override
			public boolean isEmpty()
			{
				return keySet.isEmpty();
			}

			@Override
			public Iterator<V> iterator()
			{
				final Iterator<K> iterator = keySet.iterator();
				return new AbstractReadOnlyIterator<V>()
				{

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public V next()
					{
						return get(iterator.next());
					}

				};
			}

			@Override
			public int size()
			{
				return keySet.size();
			}

			@Override
			public Object[] toArray()
			{
				return MiscUtilities.iterableToArray(this);
			}

			@Override
			public <T> T[] toArray(T[] a)
			{
				return MiscUtilities.iterableToArray(this, a);
			}

		};

	}

}
