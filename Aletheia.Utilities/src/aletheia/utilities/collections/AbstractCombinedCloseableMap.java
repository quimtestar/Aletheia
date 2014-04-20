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

public abstract class AbstractCombinedCloseableMap<K, V> extends AbstractCombinedMap<K, V> implements CloseableMap<K, V>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9092716975104998675L;

	public AbstractCombinedCloseableMap(CloseableMap<K, V> front)
	{
		super(front);
	}

	@Override
	protected CloseableMap<K, V> getFront()
	{
		return (CloseableMap<K, V>) super.getFront();
	}

	@Override
	protected abstract CloseableMap<K, V> getBack();

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

		protected class EntrySetIterator extends AbstractCombinedMap<K, V>.EntrySet.EntrySetIterator implements CloseableIterator<Entry<K, V>>
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
	public CombinedCloseableSet<K> keySet()
	{
		return new CombinedCloseableSet<K>(getFront().keySet(), getBack().keySet());
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

}
