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

import java.util.ListIterator;

/**
 * A view of a {@link ListIterator} defined by a {@link Bijection}.
 * 
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 * 
 * @author Quim Testar
 */
public class BijectionListIterator<I, O> extends BijectionIterator<I, O> implements ListIterator<O>
{

	public BijectionListIterator(Bijection<I, O> bijection, ListIterator<I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected ListIterator<I> getInner()
	{
		return (ListIterator<I>) super.getInner();
	}

	@Override
	public boolean hasPrevious()
	{
		return getInner().hasPrevious();
	}

	@Override
	public O previous()
	{
		return getBijection().forward(getInner().previous());
	}

	@Override
	public int nextIndex()
	{
		return getInner().nextIndex();
	}

	@Override
	public int previousIndex()
	{
		return getInner().previousIndex();
	}

	@Override
	public void set(O e)
	{
		getInner().set(getBijection().backward(e));
	}

	@Override
	public void add(O e)
	{
		getInner().add(getBijection().backward(e));
	}

}
