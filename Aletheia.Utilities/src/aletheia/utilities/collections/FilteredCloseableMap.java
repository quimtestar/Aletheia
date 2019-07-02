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

import aletheia.utilities.MiscUtilities;

/**
 * A {@link FilteredMap} that is also a {@link CloseableMap}.
 *
 * @author Quim Testar
 */
public class FilteredCloseableMap<K, V> extends FilteredMap<K, V> implements CloseableMap<K, V>
{

	public FilteredCloseableMap(Filter<? super V> filter, CloseableMap<K, V> inner)
	{
		super(filter, inner);
	}

	@Override
	protected CloseableMap<K, V> getInner()
	{
		return (CloseableMap<K, V>) super.getInner();
	}

	protected class FilteredCloseableEntrySet extends FilteredEntrySet implements CloseableSet<Entry<K, V>>
	{

		public FilteredCloseableEntrySet(CloseableSet<Entry<K, V>> innerEntrySet)
		{
			super(innerEntrySet);
		}

		@Override
		protected CloseableSet<Entry<K, V>> getInner()
		{
			return (CloseableSet<Entry<K, V>>) super.getInner();
		}

		protected class FilteredCloseableIterator extends FilteredIterator implements CloseableIterator<Entry<K, V>>
		{

			public FilteredCloseableIterator(CloseableIterator<Entry<K, V>> innerIterator)
			{
				super(innerIterator);
			}

			@Override
			protected CloseableIterator<Entry<K, V>> getInnerIterator()
			{
				return (CloseableIterator<Entry<K, V>>) super.getInnerIterator();
			}

			@Override
			public void close()
			{
				getInnerIterator().close();
			}
		}

		@Override
		public CloseableIterator<Entry<K, V>> iterator()
		{
			return new FilteredCloseableIterator(getInner().iterator());
		}

		@Override
		public boolean isEmpty()
		{
			CloseableIterator<Entry<K, V>> iterator = iterator();
			try
			{
				return !iterator.hasNext();
			}
			finally
			{
				iterator.close();
			}
		}

	}

	@Override
	public CloseableSet<Entry<K, V>> entrySet()
	{
		return new FilteredCloseableEntrySet(getInner().entrySet());
	}

	@Override
	public CloseableSet<K> keySet()
	{
		return new CloseableSet<>()
		{

			@Override
			public int size()
			{
				return FilteredCloseableMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return FilteredCloseableMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				return FilteredCloseableMap.this.containsKey(o);
			}

			@Override
			public CloseableIterator<K> iterator()
			{
				final CloseableIterator<Entry<K, V>> iterator = entrySet().iterator();
				return new CloseableIterator<>()
				{

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public K next()
					{
						return iterator.next().getKey();
					}

					@Override
					public void remove()
					{
						iterator.remove();
					}

					@Override
					public void close()
					{
						iterator.close();
					}

				};
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

			@Override
			public boolean add(K e)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(Object o)
			{
				return FilteredCloseableMap.this.remove(o) != null;
			}

			@Override
			public boolean containsAll(Collection<?> c)
			{
				for (Object o : c)
					if (!contains(o))
						return false;
				;
				return true;
			}

			@Override
			public boolean addAll(Collection<? extends K> c)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean retainAll(Collection<?> c)
			{
				boolean ret = false;
				CloseableIterator<K> iterator = iterator();
				try
				{
					while (iterator.hasNext())
					{
						K k = iterator.next();
						if (!c.contains(k))
						{
							ret = true;
							iterator.remove();
						}
					}
				}
				finally
				{
					iterator.close();
				}
				return ret;
			}

			@Override
			public boolean removeAll(Collection<?> c)
			{
				boolean ret = false;
				for (Object o : c)
					if (remove(o))
						ret = true;
				return ret;
			}

			@Override
			public void clear()
			{
				FilteredCloseableMap.this.clear();
			}

		};
	}

	@Override
	public CloseableCollection<V> values()
	{
		return new CloseableCollection<>()
		{
			@Override
			public int size()
			{
				return FilteredCloseableMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return FilteredCloseableMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				return FilteredCloseableMap.this.containsValue(o);
			}

			@Override
			public CloseableIterator<V> iterator()
			{
				final CloseableIterator<Entry<K, V>> iterator = entrySet().iterator();
				return new CloseableIterator<>()
				{

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@Override
					public V next()
					{
						return iterator.next().getValue();
					}

					@Override
					public void remove()
					{
						iterator.remove();
					}

					@Override
					public void close()
					{
						iterator.close();
					}

				};
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

			@Override
			public boolean add(V e)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(Object o)
			{
				CloseableIterator<V> iterator = iterator();
				try
				{
					while (iterator.hasNext())
					{
						if (o == null)
						{
							if (iterator.next() == null)
							{
								iterator.remove();
								return true;
							}
						}
						else if (iterator.next().equals(o))
						{
							iterator.remove();
							return true;
						}
					}
					return false;
				}
				finally
				{
					iterator.close();
				}
			}

			@Override
			public boolean containsAll(Collection<?> c)
			{
				for (Object o : c)
					if (!contains(o))
						return false;
				;
				return true;
			}

			@Override
			public boolean addAll(Collection<? extends V> c)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean removeAll(Collection<?> c)
			{
				boolean ret = false;
				for (Object o : c)
					if (remove(o))
						ret = true;
				return ret;
			}

			@Override
			public boolean retainAll(Collection<?> c)
			{
				boolean ret = false;
				CloseableIterator<V> iterator = iterator();
				try
				{
					while (iterator.hasNext())
					{
						V v = iterator.next();
						if (!c.contains(v))
						{
							ret = true;
							iterator.remove();
						}
					}
				}
				finally
				{
					iterator.close();
				}
				return ret;
			}

			@Override
			public void clear()
			{
				FilteredCloseableMap.this.clear();
			}

		};

	}

}
