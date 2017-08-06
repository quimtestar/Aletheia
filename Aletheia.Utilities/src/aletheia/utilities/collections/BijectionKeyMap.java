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
 * A view of a map defined by a bijection between the keys.
 *
 * @param <I>
 *            The input keys type.
 * @param <O>
 *            The output keys type.
 * @param <V>
 *            The values type.
 *
 * @author Quim Testar
 */
public class BijectionKeyMap<I, O, V> extends AbstractMap<O, V>
{
	private final Bijection<I, O> bijection;
	private final Map<I, V> inner;

	public BijectionKeyMap(Bijection<I, O> bijection, Map<I, V> inner)
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
	protected Map<I, V> getInner()
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

	protected static class ExtractionClassException extends Exception
	{
		private static final long serialVersionUID = -4258536611129607689L;

		public ExtractionClassException(ClassCastException e)
		{
			super(e);
		}
	}

	/**
	 * Casts an object to the output type of the bijection. If it's not possible
	 * throws an {@link ExtractionClassException}.
	 *
	 * @param o
	 *            The object to cast.
	 * @return The casted object.
	 * @throws ExtractionClassException
	 */
	@SuppressWarnings("unchecked")
	protected static <O> O extracted(Object o) throws ExtractionClassException
	{
		try
		{
			return (O) o;
		}
		catch (ClassCastException e)
		{
			throw new ExtractionClassException(e);
		}
	}

	@Override
	public boolean containsKey(Object key)
	{
		try
		{
			return inner.containsKey(bijection.backward(BijectionKeyMap.<O> extracted(key)));
		}
		catch (ExtractionClassException e)
		{
			return false;
		}
	}

	@Override
	public boolean containsValue(Object value)
	{
		return inner.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		try
		{
			return inner.get(bijection.backward(BijectionKeyMap.<O> extracted(key)));
		}
		catch (ExtractionClassException e)
		{
			return null;
		}
	}

	@Override
	public Set<O> keySet()
	{
		return new BijectionSet<>(bijection, inner.keySet());
	}

	@Override
	public Collection<V> values()
	{
		return inner.values();
	}

	@Override
	public Set<Map.Entry<O, V>> entrySet()
	{
		Bijection<Entry<I, V>, Entry<O, V>> bijection_ = new Bijection<Entry<I, V>, Entry<O, V>>()
		{

			@Override
			public Entry<O, V> forward(final Entry<I, V> input)
			{
				return new Entry<O, V>()
				{

					@Override
					public O getKey()
					{
						return bijection.forward(input.getKey());
					}

					@Override
					public V getValue()
					{
						return input.getValue();
					}

					@Override
					public V setValue(V value)
					{
						return input.setValue(value);
					}

				};
			}

			@Override
			public Entry<I, V> backward(final Entry<O, V> output)
			{
				return new Entry<I, V>()
				{

					@Override
					public I getKey()
					{
						return bijection.backward(output.getKey());
					}

					@Override
					public V getValue()
					{
						return output.getValue();
					}

					@Override
					public V setValue(V value)
					{
						return output.setValue(value);
					}

				};
			}

		};

		return new BijectionSet<>(bijection_, inner.entrySet());
	}

	@Override
	public V put(O key, V value)
	{
		return inner.put(bijection.backward(key), value);
	}

	@Override
	public V remove(Object key)
	{
		try
		{
			return inner.remove(bijection.backward(BijectionKeyMap.<O> extracted((key))));
		}
		catch (ExtractionClassException e)
		{
			return null;
		}
	}

	/**
	 * Returns the {@linkplain InverseBijection inverse bijection} of another one.
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
	public void putAll(Map<? extends O, ? extends V> m)
	{
		inner.putAll(new BijectionKeyMap<>(invertBijection(bijection), new AdaptedMap<>(m)));
	}

	@Override
	public void clear()
	{
		inner.clear();
	}

}
