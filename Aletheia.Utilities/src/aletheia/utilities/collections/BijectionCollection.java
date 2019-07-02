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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import aletheia.utilities.MiscUtilities;

/**
 * A view of a {@link Collection} defined by a {@link Bijection}.
 *
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 *
 * @author Quim Testar
 */
public class BijectionCollection<I, O> extends AbstractCollection<O>
{

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

	private final Bijection<I, O> bijection;

	private final Collection<I> inner;

	public BijectionCollection(Bijection<I, O> bijection, Collection<I> inner)
	{
		super();
		this.bijection = bijection;
		this.inner = inner;
	}

	/**
	 * The bijection.
	 *
	 * @return The bijection.
	 */
	protected Bijection<I, O> getBijection()
	{
		return bijection;
	}

	/**
	 * The original collection.
	 *
	 * @return The original collection.
	 */
	protected Collection<I> getInner()
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o)
	{
		try
		{
			return inner.contains(bijection.backward((O) o));
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public Iterator<O> iterator()
	{
		return new BijectionIterator<>(bijection, inner.iterator());
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return inner.containsAll(c);
	}

	@Override
	public boolean add(O e)
	{
		return inner.add(bijection.backward(e));
	}

	@Override
	public boolean addAll(Collection<? extends O> c)
	{
		return inner.addAll(new BijectionCollection<>(invertBijection(bijection), new AdaptedCollection<>(c)));
	}

	@Override
	public void clear()
	{
		inner.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o)
	{
		try
		{
			return inner.remove(bijection.backward((O) o));
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	/**
	 * Extracts a full collection of objects into a collection of instances of
	 * the output type, by {@linkplain #extracted(Object) extracting} its
	 * elements.
	 *
	 * @param c
	 *            The collection to be extracted.
	 * @return The extracted collection.
	 */
	protected static <O> Collection<O> extractedCollection(final Collection<?> c)
	{
		return new AbstractReadOnlyCollection<>()
		{

			@Override
			public int size()
			{
				return c.size();
			}

			@Override
			public boolean isEmpty()
			{
				return c.isEmpty();
			}

			@Override
			public boolean contains(Object o)
			{
				return c.contains(o);
			}

			@Override
			public Iterator<O> iterator()
			{
				final Iterator<?> iterator = c.iterator();
				return new Iterator<>()
				{

					@Override
					public boolean hasNext()
					{
						return iterator.hasNext();
					}

					@SuppressWarnings("unchecked")
					@Override
					public O next()
					{
						while (hasNext())
						{
							try
							{
								return (O) iterator.next();
							}
							catch (ClassCastException e)
							{
							}
						}
						throw new NoSuchElementException();
					}

					@Override
					public void remove()
					{
						iterator.remove();
					}

				};

			}

			@Override
			public Object[] toArray()
			{
				return c.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a)
			{
				return c.toArray(a);
			}

			@Override
			public boolean containsAll(Collection<?> c_)
			{
				return c.containsAll(c_);
			}
		};
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return inner.removeAll(new BijectionCollection<>(invertBijection(bijection), BijectionCollection.<O> extractedCollection(c)));
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return inner.retainAll(new BijectionCollection<>(invertBijection(bijection), BijectionCollection.<O> extractedCollection(c)));
	}

	@Override
	public Object[] toArray()
	{
		return MiscUtilities.iterableToArray(this);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return MiscUtilities.iterableToArray(this, a);
	}

}
