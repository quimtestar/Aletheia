/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * We keep track of both iterators simultaneously and take one or another next
 * elements as the next element of this iterator according to the comparison
 * between them. Note: The front and back iterators <b>must</b> have the same
 * order; if that is not the case, the result is indeterminate.
 */
public class CombinedSortedIterator<E> extends AbstractReadOnlyIterator<E>
{

	private final Iterator<? extends E> frontIterator;
	private final Iterator<? extends E> backIterator;
	private final Comparator<? super E> comparator;

	private E nextFront;
	private E nextBack;
	private boolean hasNextFront;
	private boolean hasNextBack;

	public CombinedSortedIterator(Iterator<? extends E> frontIterator, Iterator<? extends E> backIterator, Comparator<? super E> comparator)
	{
		this.frontIterator = frontIterator;
		this.backIterator = backIterator;
		this.comparator = comparator;

		try
		{
			nextFront = frontIterator.next();
			hasNextFront = true;
		}
		catch (NoSuchElementException e)
		{
			hasNextFront = false;
		}
		try
		{
			nextBack = backIterator.next();
			hasNextBack = true;
		}
		catch (NoSuchElementException e)
		{
			hasNextBack = false;
		}
	}

	protected Iterator<? extends E> getFrontIterator()
	{
		return frontIterator;
	}

	protected Iterator<? extends E> getBackIterator()
	{
		return backIterator;
	}

	protected Comparator<? super E> getComparator()
	{
		return comparator;
	}

	@Override
	public boolean hasNext()
	{
		return hasNextFront || hasNextBack;
	}

	@Override
	public E next()
	{
		if (!hasNextFront)
		{
			if (!hasNextBack)
				throw new NoSuchElementException();
			E ret = nextBack;
			try
			{
				nextBack = backIterator.next();
			}
			catch (NoSuchElementException e)
			{
				hasNextBack = false;
			}
			return ret;
		}
		else if (!hasNextBack)
		{
			E ret = nextFront;
			try
			{
				nextFront = frontIterator.next();
			}
			catch (NoSuchElementException e)
			{
				hasNextFront = false;
			}
			return ret;
		}
		else
		{
			int c = comparator.compare(nextFront, nextBack);
			if (c < 0)
			{
				E ret = nextFront;
				try
				{
					nextFront = frontIterator.next();
				}
				catch (NoSuchElementException e)
				{
					hasNextFront = false;
				}
				return ret;
			}
			else if (c > 0)
			{
				E ret = nextBack;
				try
				{
					nextBack = backIterator.next();
				}
				catch (NoSuchElementException e)
				{
					hasNextBack = false;
				}
				return ret;
			}
			else
			{
				E ret = nextFront;
				try
				{
					nextFront = frontIterator.next();
				}
				catch (NoSuchElementException e)
				{
					hasNextFront = false;
				}
				try
				{
					nextBack = backIterator.next();
				}
				catch (NoSuchElementException e)
				{
					hasNextBack = false;
				}
				return ret;
			}
		}

	}

}
