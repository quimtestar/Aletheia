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

/**
 * A view of a {@link CloseableSet} defined by a {@link Bijection}.
 *
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 *
 * @author Quim Testar
 */

public class BijectionCloseableSet<I, O> extends BijectionSet<I, O>implements CloseableSet<O>
{

	public BijectionCloseableSet(Bijection<I, O> bijection, CloseableSet<I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected CloseableSet<I> getInner()
	{
		return (CloseableSet<I>) super.getInner();
	}

	@Override
	public CloseableIterator<O> iterator()
	{
		final CloseableIterator<I> inner = getInner().iterator();
		return new CloseableIterator<O>()
		{

			@Override
			public boolean hasNext()
			{
				return inner.hasNext();
			}

			@Override
			public O next()
			{
				return getBijection().forward(inner.next());
			}

			@Override
			public void remove()
			{
				inner.remove();

			}

			@Override
			public void close()
			{
				inner.close();
			}
		};
	}

}
