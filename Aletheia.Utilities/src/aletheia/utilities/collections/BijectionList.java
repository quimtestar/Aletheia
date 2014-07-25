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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * A view of a {@link List} defined by a {@link Bijection}.
 *
 * @param <I>
 *            The input type.
 * @param <O>
 *            The output type.
 *
 * @author Quim Testar
 */
public class BijectionList<I, O> extends BijectionCollection<I, O> implements List<O>
{

	public BijectionList(Bijection<I, O> bijection, List<I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected List<I> getInner()
	{
		return (List<I>) super.getInner();
	}

	@Override
	public boolean addAll(int index, Collection<? extends O> c)
	{
		return getInner().addAll(index, new BijectionCollection<O, I>(invertBijection(getBijection()), new AdaptedCollection<O>(c)));

	}

	@Override
	public O get(int index)
	{
		return getBijection().forward(getInner().get(index));
	}

	@Override
	public O set(int index, O element)
	{
		return getBijection().forward(getInner().set(index, getBijection().backward(element)));
	}

	@Override
	public void add(int index, O element)
	{
		getInner().add(index, getBijection().backward(element));
	}

	@Override
	public O remove(int index)
	{
		return getBijection().forward(getInner().remove(index));
	}

	@Override
	public int indexOf(Object o)
	{
		try
		{
			return getInner().indexOf(getBijection().backward(BijectionCollection.<O> extracted(o)));
		}
		catch (aletheia.utilities.collections.BijectionCollection.ExtractionClassException e)
		{
			return -1;
		}
	}

	@Override
	public int lastIndexOf(Object o)
	{
		try
		{
			return getInner().lastIndexOf(getBijection().backward(BijectionCollection.<O> extracted(o)));
		}
		catch (aletheia.utilities.collections.BijectionCollection.ExtractionClassException e)
		{
			return -1;
		}
	}

	@Override
	public ListIterator<O> listIterator()
	{
		return new BijectionListIterator<I, O>(getBijection(), getInner().listIterator());
	}

	@Override
	public ListIterator<O> listIterator(int index)
	{
		return new BijectionListIterator<I, O>(getBijection(), getInner().listIterator(index));
	}

	@Override
	public List<O> subList(int fromIndex, int toIndex)
	{
		return new BijectionList<I, O>(getBijection(), getInner().subList(fromIndex, toIndex));
	}

}
