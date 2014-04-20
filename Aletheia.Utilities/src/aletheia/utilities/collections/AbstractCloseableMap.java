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

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Collection;

public abstract class AbstractCloseableMap<K, V> extends AbstractMap<K, V> implements CloseableMap<K, V>
{

	@Override
	public abstract CloseableSet<Entry<K, V>> entrySet();

	@Override
	public CloseableSet<K> keySet()
	{
		return new CloseableSet<K>()
		{

			@Override
			public int size()
			{
				return AbstractCloseableMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return AbstractCloseableMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				return AbstractCloseableMap.this.containsKey(o);
			}

			@Override
			public CloseableIterator<K> iterator()
			{
				final CloseableIterator<Entry<K, V>> iterator = entrySet().iterator();
				return new CloseableIterator<K>()
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
				return toArray(new Object[0]);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T[] toArray(T[] a)
			{
				int n = size();
				if (a.length < n)
					a = (T[]) Array.newInstance(a.getClass().getComponentType(), n);
				CloseableIterator<K> iterator = iterator();
				try
				{
					int i = 0;
					while (iterator.hasNext())
					{
						a[i] = (T) iterator.next();
						i++;
					}
					for (; i < n; i++)
						a[i] = null;
				}
				finally
				{
					iterator.close();
				}
				return a;
			}

			@Override
			public boolean add(K e)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean remove(Object o)
			{
				return AbstractCloseableMap.this.remove(o) != null;
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
				AbstractCloseableMap.this.clear();
			}

		};
	}

	@Override
	public CloseableCollection<V> values()
	{
		return new CloseableCollection<V>()
		{
			@Override
			public int size()
			{
				return AbstractCloseableMap.this.size();
			}

			@Override
			public boolean isEmpty()
			{
				return AbstractCloseableMap.this.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				return AbstractCloseableMap.this.containsValue(o);
			}

			@Override
			public CloseableIterator<V> iterator()
			{
				final CloseableIterator<Entry<K, V>> iterator = entrySet().iterator();
				return new CloseableIterator<V>()
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
				return toArray(new Object[0]);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T[] toArray(T[] a)
			{
				int n = size();
				if (a.length < n)
					a = (T[]) Array.newInstance(a.getClass().getComponentType(), n);
				CloseableIterator<V> iterator = iterator();
				try
				{
					int i = 0;
					while (iterator.hasNext())
					{
						a[i] = (T) iterator.next();
						i++;
					}
					for (; i < n; i++)
						a[i] = null;
				}
				finally
				{
					iterator.close();
				}
				return a;
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
				AbstractCloseableMap.this.clear();
			}

		};

	}

}
