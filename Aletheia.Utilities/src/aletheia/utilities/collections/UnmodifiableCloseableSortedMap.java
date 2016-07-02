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

import java.util.Comparator;

public class UnmodifiableCloseableSortedMap<K, V> extends UnmodifiableCloseableMap<K, V> implements CloseableSortedMap<K, V>
{

	public UnmodifiableCloseableSortedMap(CloseableSortedMap<K, V> inner)
	{
		super(inner);
	}

	@Override
	protected CloseableSortedMap<K, V> getInner()
	{
		return (CloseableSortedMap<K, V>) super.getInner();
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
	public K lastKey()
	{
		return getInner().lastKey();
	}

	@Override
	public UnmodifiableCloseableSortedMap<K, V> subMap(K fromKey, K toKey)
	{
		return new UnmodifiableCloseableSortedMap<>(getInner().subMap(fromKey, toKey));
	}

	@Override
	public CloseableSortedMap<K, V> headMap(K toKey)
	{
		return new UnmodifiableCloseableSortedMap<>(getInner().headMap(toKey));
	}

	@Override
	public CloseableSortedMap<K, V> tailMap(K fromKey)
	{
		return new UnmodifiableCloseableSortedMap<>(getInner().tailMap(fromKey));
	}

}
