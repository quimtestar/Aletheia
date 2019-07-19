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
 * A view of an iterator defined by a {@link Bijection}
 *
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 *
 * @author Quim Testar
 */
public class BijectionIterator<I, O> implements Iterator<O>
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

	private final Iterator<I> inner;

	public BijectionIterator(Bijection<I, O> bijection, Iterator<I> inner)
	{
		this.bijection = bijection;
		this.inner = inner;
	}

	protected Bijection<I, O> getBijection()
	{
		return bijection;
	}

	protected Iterator<I> getInner()
	{
		return inner;
	}

	@Override
	public boolean hasNext()
	{
		return inner.hasNext();
	}

	@Override
	public O next()
	{
		return bijection.forward(inner.next());
	}

	@Override
	public void remove()
	{
		inner.remove();
	}

}
