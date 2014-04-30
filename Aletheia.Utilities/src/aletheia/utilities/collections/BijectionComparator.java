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

import java.util.Comparator;

/**
 * A {@link Comparator} that does its job using another type's
 * {@link Comparator} via a {@link Bijection}.
 * 
 * @author Quim Testar
 */
public class BijectionComparator<I, O> implements Comparator<O>
{
	private final Bijection<I, O> bijection;
	private final Comparator<I> inner;

	public BijectionComparator(Bijection<I, O> bijection, Comparator<I> inner)
	{
		this.bijection = bijection;
		this.inner = inner;
	}

	public Comparator<I> getInner()
	{
		return inner;
	}

	@Override
	public int compare(O o1, O o2)
	{
		return inner.compare(bijection.backward(o1), bijection.backward(o2));
	}

}
