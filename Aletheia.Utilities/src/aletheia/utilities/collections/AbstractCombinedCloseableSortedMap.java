/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
 * An {@link AbstractCloseableSortedMap} for {@link CloseableSortedMap}s.
 *
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedCloseableSortedMap<K, V> extends AbstractCombinedSortedMap<K, V> implements CloseableSortedMap<K, V>
{
	private static final long serialVersionUID = 4434135098510257056L;

	public AbstractCombinedCloseableSortedMap(CloseableSortedMap<K, V> front)
	{
		super(front);
	}

	@Override
	protected CloseableSortedMap<K, V> getFront()
	{
		return (CloseableSortedMap<K, V>) super.getFront();
	}

	@Override
	protected abstract CloseableSortedMap<K, V> getBack();

	protected class EntrySet extends AbstractCombinedMap<K, V>.EntrySet implements CloseableSet<Entry<K, V>>
	{
		protected EntrySet(CloseableSet<K> keySet)
		{
			super(keySet);
		}

		@Override
		protected CloseableSet<K> getKeySet()
		{
			return (CloseableSet<K>) super.getKeySet();
		}

		protected class EntrySetIterator extends AbstractCombinedSortedMap<K, V>.EntrySet.EntrySetIterator implements CloseableIterator<Entry<K, V>>
		{

			protected EntrySetIterator(CloseableIterator<K> keyIterator)
			{
				super(keyIterator);
			}

			@Override
			protected CloseableIterator<K> getKeyIterator()
			{
				return (CloseableIterator<K>) super.getKeyIterator();
			}

			@Override
			public void close()
			{
				getKeyIterator().close();
			}

		}

		@Override
		public CloseableIterator<Entry<K, V>> iterator()
		{
			return new EntrySetIterator(getKeySet().iterator());
		}

	}

	@Override
	public CloseableSet<Map.Entry<K, V>> entrySet()
	{
		return new EntrySet(keySet());
	}

	@Override
	public CloseableSet<K> keySet()
	{
		return new CombinedCloseableSetSortedIterator<K>(getFront().keySet(), getBack().keySet(), resolvedComparator());
	}

	protected class Values extends AbstractCombinedMap<K, V>.Values implements CloseableSet<V>
	{

		protected Values(CloseableSet<Entry<K, V>> entrySet)
		{
			super(entrySet);
		}

		@Override
		protected CloseableSet<Entry<K, V>> getEntrySet()
		{
			return (CloseableSet<Entry<K, V>>) super.getEntrySet();
		}

		protected class ValuesIterator extends AbstractCombinedMap<K, V>.Values.ValuesIterator implements CloseableIterator<V>
		{

			protected ValuesIterator(CloseableIterator<Entry<K, V>> entrySetIterator)
			{
				super(entrySetIterator);
			}

			@Override
			protected CloseableIterator<Entry<K, V>> getEntrySetIterator()
			{
				return (CloseableIterator<Entry<K, V>>) super.getEntrySetIterator();
			}

			@Override
			public void close()
			{
				getEntrySetIterator().close();
			}

		}

		@Override
		public CloseableIterator<V> iterator()
		{
			return new ValuesIterator(getEntrySet().iterator());
		}

	}

	@Override
	public CloseableCollection<V> values()
	{
		return new Values(entrySet());
	}

	@Override
	public CloseableSortedMap<K, V> headMap(final K toKey)
	{
		return new AbstractCombinedCloseableSortedMap<K, V>(getFront().headMap(toKey))
		{
			private static final long serialVersionUID = -2512566498203919691L;

			@Override
			protected CloseableSortedMap<K, V> getBack()
			{
				return AbstractCombinedCloseableSortedMap.this.getBack().headMap(toKey);
			}
		};
	}

	@Override
	public CloseableSortedMap<K, V> subMap(final K fromKey, final K toKey)
	{
		return new AbstractCombinedCloseableSortedMap<K, V>(getFront().subMap(fromKey, toKey))
		{
			private static final long serialVersionUID = -2899448591974596379L;

			@Override
			protected CloseableSortedMap<K, V> getBack()
			{
				return AbstractCombinedCloseableSortedMap.this.getBack().subMap(fromKey, toKey);
			}

		};
	}

	@Override
	public CloseableSortedMap<K, V> tailMap(final K fromKey)
	{
		return new AbstractCombinedCloseableSortedMap<K, V>(getFront().tailMap(fromKey))
		{
			private static final long serialVersionUID = 1979312580696145790L;

			@Override
			protected CloseableSortedMap<K, V> getBack()
			{
				return AbstractCombinedCloseableSortedMap.this.getBack().tailMap(fromKey);
			}

		};
	}

}
