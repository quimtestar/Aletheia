/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import java.util.Iterator;

/**
 * A view of a {@link Iterable} defined by a {@link Bijection}.
 *
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 *
 * @author Quim Testar
 */
public class BijectionIterable<I, O> implements Iterable<O>
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

	private final Iterable<I> inner;

	public BijectionIterable(Bijection<I, O> bijection, Iterable<I> inner)
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
	 * The original iterable.
	 *
	 * @return The original iterable.
	 */
	protected Iterable<I> getInner()
	{
		return inner;
	}

	@Override
	public Iterator<O> iterator()
	{
		return new BijectionIterator<>(bijection, inner.iterator());
	}

}
