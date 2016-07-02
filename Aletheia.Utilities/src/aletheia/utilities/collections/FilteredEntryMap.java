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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of a {@link Map} with its {@link java.util.Map.Entry}s
 * filtered out of a given {@link Map}.
 *
 * @see Filter
 *
 * @author Quim Testar
 */
public class FilteredEntryMap<K, V> extends AbstractMap<K, V>
{
	private final Filter<? super Entry<K, V>> filter;
	private final Map<K, V> inner;

	public FilteredEntryMap(Filter<? super Entry<K, V>> filter, Map<K, V> inner)
	{
		this.filter = filter;
		this.inner = inner;
	}

	protected Filter<? super Entry<K, V>> getFilter()
	{
		return filter;
	}

	protected Map<K, V> getInner()
	{
		return inner;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key)
	{
		if (!inner.containsKey(key))
			return false;
		V value = inner.get(key);
		try
		{
			return filter.filter(new SimpleEntry<>((K) key, value));
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key)
	{
		if (!inner.containsKey(key))
			return null;
		V value = inner.get(key);
		try
		{
			if (filter.filter(new SimpleEntry<>((K) key, value)))
				return value;
			else
				return null;
		}
		catch (ClassCastException e)
		{
			return null;
		}
	}

	protected class FilteredEntrySet extends FilteredSet<Entry<K, V>>
	{

		public FilteredEntrySet(Set<Entry<K, V>> innerEntrySet)
		{
			super(new Filter<Entry<K, V>>()
			{

				@Override
				public boolean filter(Entry<K, V> e)
				{
					return filter.filter(e);
				}
			}, innerEntrySet);
		}

	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return new FilteredEntrySet(getInner().entrySet());
	}

	@Override
	public boolean isEmpty()
	{
		return entrySet().isEmpty();
	}

}
