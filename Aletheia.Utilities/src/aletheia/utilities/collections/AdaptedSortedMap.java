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

import java.util.Comparator;
import java.util.SortedMap;

/**
 * Gives a read-only view of a {@link SortedMap} as a {@link SortedMap} with
 * type parameters that is are superclasses of the original's
 * 
 * @param <K>
 *            The keys type of the resulting {@link SortedMap} view.
 * @param <V>
 *            The values type of the resulting {@link SortedMap} view.
 * 
 * @author Quim Testar
 */
public class AdaptedSortedMap<K, V> extends AdaptedMap<K, V> implements SortedMap<K, V>
{

	public AdaptedSortedMap(SortedMap<K, ? extends V> inner)
	{
		super(inner);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SortedMap<K, ? extends V> getInner()
	{
		return (SortedMap<K, ? extends V>) super.getInner();
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return getInner().comparator();
	}

	@Override
	public K firstKey()
	{
		return getInner().firstKey();
	}

	@Override
	public SortedMap<K, V> headMap(K toKey)
	{
		return new AdaptedSortedMap<K, V>(getInner().headMap(toKey));
	}

	@Override
	public K lastKey()
	{
		return getInner().lastKey();
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return new AdaptedSortedMap<K, V>(getInner().subMap(fromKey, toKey));
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey)
	{
		return new AdaptedSortedMap<K, V>(getInner().tailMap(fromKey));
	}

}
