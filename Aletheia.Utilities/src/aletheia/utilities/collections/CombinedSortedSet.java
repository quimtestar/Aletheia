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
import java.util.SortedSet;

/**
 * A combined sorted set of any two other sorted sets. The elements in the front
 * set will shadow the elements on the back set.
 *
 * @param <E>
 *            The elements' type.
 *
 * @author Quim Testar
 */
public class CombinedSortedSet<E> extends AbstractCombinedSortedSet<E> implements SortedSet<E>
{
	private static final long serialVersionUID = 5196602047719292556L;

	private final SortedSet<E> back;
	private final Comparator<? super E> comparator;

	/**
	 * The comparators of front and back set must be the same, and will be used as a
	 * the comparator of this set.
	 *
	 * @param front
	 *            The front sorted set.
	 * @param back
	 *            The back sorted set.
	 */
	public CombinedSortedSet(SortedSet<E> front, SortedSet<E> back)
	{
		super(front);
		try
		{
			if ((front.comparator() != back.comparator()) && !front.comparator().equals(back.comparator()))
				throw new Error("Comparators differ");
		}
		catch (NullPointerException e)
		{
			throw new Error("Comparators differ");
		}
		this.back = back;
		this.comparator = front.comparator();
	}

	@Override
	protected SortedSet<E> getFront()
	{
		return super.getFront();
	}

	@Override
	protected SortedSet<E> getBack()
	{
		return back;
	}

	@Override
	public Comparator<? super E> comparator()
	{
		return comparator;
	}

}
