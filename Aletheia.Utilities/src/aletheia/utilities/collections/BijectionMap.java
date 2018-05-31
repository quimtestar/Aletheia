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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A view of a {@link Map} defined by a {@link Bijection} between the values.
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
public class BijectionMap<K, I, O> extends AbstractMap<K, O>
{
	private final Bijection<I, O> bijection;
	private final Map<K, I> inner;

	public BijectionMap(Bijection<I, O> bijection, Map<K, I> inner)
	{
		this.bijection = bijection;
		this.inner = inner;
	}

	/**
	 * The bijection.
	 *
	 * @return The bijection.
	 */
	public Bijection<I, O> getBijection()
	{
		return bijection;
	}

	/**
	 * The original map.
	 *
	 * @return The original map.
	 */
	protected Map<K, I> getInner()
	{
		return inner;
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return inner.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsValue(Object value)
	{
		try
		{
			return inner.containsValue(bijection.backward((O) value));
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public O get(Object key)
	{
		return bijection.forward(inner.get(key));
	}

	@Override
	public Set<K> keySet()
	{
		return inner.keySet();
	}

	@Override
	public Collection<O> values()
	{
		return new BijectionCollection<>(bijection, inner.values());
	}

	@Override
	public Set<Map.Entry<K, O>> entrySet()
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
						return bijection.forward(input.getValue());
					}

					@Override
					public O setValue(O value)
					{
						return bijection.forward(input.setValue(bijection.backward(value)));
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
						return bijection.backward(output.getValue());
					}

					@Override
					public I setValue(I value)
					{
						return bijection.backward(output.setValue(bijection.forward(value)));
					}

				};
			}

		};

		return new BijectionSet<>(bijection_, inner.entrySet());
	}

	@Override
	public O put(K key, O value)
	{
		return bijection.forward(inner.put(key, bijection.backward(value)));
	}

	@Override
	public O remove(Object key)
	{
		return bijection.forward(inner.remove(key));
	}

	/**
	 * Returns the {@linkplain InverseBijection inverse bijection} of another
	 * one.
	 *
	 * @param b
	 *            The input bijection.
	 * @return The inverted bijection.
	 */
	protected static <I, O> Bijection<O, I> invertBijection(final Bijection<I, O> b)
	{
		return new InverseBijection<>(b);
	}

	@Override
	public void putAll(Map<? extends K, ? extends O> m)
	{
		inner.putAll(new BijectionMap<>(invertBijection(bijection), new AdaptedMap<>(m)));
	}

	@Override
	public void clear()
	{
		inner.clear();
	}

}
