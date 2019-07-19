/*******************************************************************************
 * Copyright (c) 2015, 2018 Quim Testar.
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
import java.util.NoSuchElementException;

public class CombinedIterator<E> extends AbstractReadOnlyIterator<E>
{
	private final Iterator<? extends E> frontIterator;
	private final Iterator<? extends E> backIterator;

	private E next;
	private boolean hasNext;

	public CombinedIterator(Iterator<? extends E> frontIterator, Iterator<? extends E> backIterator)
	{
		this.frontIterator = frontIterator;
		this.backIterator = backIterator;
		try
		{
			next = forward();
			hasNext = true;
		}
		catch (NoSuchElementException e)
		{
			hasNext = false;
		}
	}

	/**
	 * If the front iterator still can be moved forward, we move it and return
	 * the element found; if not we use the back iterator. we advance it one
	 * position
	 *
	 * @return The next element found in either iterator.
	 */
	private E forward()
	{
		if (frontIterator.hasNext())
			return frontIterator.next();
		E next = backIterator.next();
		return next;
	}

	protected Iterator<? extends E> getFrontIterator()
	{
		return frontIterator;
	}

	protected Iterator<? extends E> getBackIterator()
	{
		return backIterator;
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public E next()
	{
		if (!hasNext)
			throw new NoSuchElementException();
		E pre = next;
		try
		{
			next = forward();
		}
		catch (NoSuchElementException e)
		{
			hasNext = false;
		}
		return pre;
	}

}
