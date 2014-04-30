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

/**
 * A view of a {@link CloseableSortedMap} defined by a {@link Bijection} on the
 * values.
 * 
 * @author Quim Testar
 */
public class BijectionCloseableSortedMap<K, I, O> extends BijectionSortedMap<K, I, O> implements CloseableSortedMap<K, O>
{

	public BijectionCloseableSortedMap(Bijection<I, O> bijection, CloseableSortedMap<K, I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected CloseableSortedMap<K, I> getInner()
	{
		return (CloseableSortedMap<K, I>) super.getInner();
	}

	@Override
	public CloseableSet<K> keySet()
	{
		return getInner().keySet();
	}

	@Override
	public CloseableCollection<O> values()
	{
		return new BijectionCloseableCollection<>(getBijection(), getInner().values());
	}

	@Override
	public CloseableSet<Map.Entry<K, O>> entrySet()
	{
		Bijection<Entry<K, I>, Entry<K, O>> bijection_ = new Bijection<Entry<K, I>, Entry<K, O>>()
		{

			@Override
			public Entry<K, O> forward(final Entry<K, I> input)
			{
				return new Entry<K, O>()
				{

					@Override
					public K getKey()
					{
						return input.getKey();
					}

					@Override
					public O getValue()
					{
						return getBijection().forward(input.getValue());
					}

					@Override
					public O setValue(O value)
					{
						return getBijection().forward(input.setValue(getBijection().backward(value)));
					}

				};
			}

			@Override
			public Entry<K, I> backward(final Entry<K, O> output)
			{
				return new Entry<K, I>()
				{

					@Override
					public K getKey()
					{
						return output.getKey();
					}

					@Override
					public I getValue()
					{
						return getBijection().backward(output.getValue());
					}

					@Override
					public I setValue(I value)
					{
						return getBijection().backward(output.setValue(getBijection().forward(value)));
					}

				};
			}

		};

		return new BijectionCloseableSet<Entry<K, I>, Entry<K, O>>(bijection_, getInner().entrySet());
	}

	@Override
	public CloseableSortedMap<K, O> subMap(K fromKey, K toKey)
	{
		return new BijectionCloseableSortedMap<>(getBijection(), getInner().subMap(fromKey, toKey));
	}

	@Override
	public CloseableSortedMap<K, O> headMap(K toKey)
	{
		return new BijectionCloseableSortedMap<>(getBijection(), getInner().headMap(toKey));
	}

	@Override
	public CloseableSortedMap<K, O> tailMap(K fromKey)
	{
		return new BijectionCloseableSortedMap<>(getBijection(), getInner().tailMap(fromKey));
	}

}
