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
 * A view of a {@link SortedMap} defined by a {@link Bijection} between the
 * values.
 *
 * @param <K>
 *            The keys type.
 * @param <I>
 *            The input values type.
 * @param <O>
 *            The output values type.
 *
 * @author Quim Testar
 */

public class BijectionSortedMap<K, I, O> extends BijectionMap<K, I, O>implements SortedMap<K, O>
{

	public BijectionSortedMap(Bijection<I, O> bijection, SortedMap<K, I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected SortedMap<K, I> getInner()
	{
		return (SortedMap<K, I>) super.getInner();
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return getInner().comparator();
	}

	@Override
	public SortedMap<K, O> subMap(K fromKey, K toKey)
	{
		return new BijectionSortedMap<K, I, O>(getBijection(), getInner().subMap(fromKey, toKey));
	}

	@Override
	public SortedMap<K, O> headMap(K toKey)
	{
		return new BijectionSortedMap<K, I, O>(getBijection(), getInner().headMap(toKey));
	}

	@Override
	public SortedMap<K, O> tailMap(K fromKey)
	{
		return new BijectionSortedMap<K, I, O>(getBijection(), getInner().tailMap(fromKey));
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

}
