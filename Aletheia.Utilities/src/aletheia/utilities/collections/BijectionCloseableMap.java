/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
 * A view of a {@link CloseableMap} defined by a {@link Bijection} on the
 * values.
 *
 * @author Quim Testar
 */
public class BijectionCloseableMap<K, I, O> extends BijectionMap<K, I, O> implements CloseableMap<K, O>
{

	public BijectionCloseableMap(Bijection<I, O> bijection, CloseableMap<K, I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected CloseableMap<K, I> getInner()
	{
		return (CloseableMap<K, I>) super.getInner();
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
		Bijection<Entry<K, I>, Entry<K, O>> bijection_ = new Bijection<>()
		{

			@Override
			public Entry<K, O> forward(final Entry<K, I> input)
			{
				return new Entry<>()
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
				return new Entry<>()
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

		return new BijectionCloseableSet<>(bijection_, getInner().entrySet());
	}

}
