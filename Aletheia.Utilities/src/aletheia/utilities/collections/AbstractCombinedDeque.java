/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import java.util.Deque;
import java.util.Iterator;

/**
 * Abstract implementation of a combined {@link Deque}.
 *
 * @author Quim Testar
 *
 * @see AbstractCombinedQueue
 */
public abstract class AbstractCombinedDeque<E> extends AbstractCombinedQueue<E> implements Deque<E>
{

	private static final long serialVersionUID = -160606245218215715L;

	public AbstractCombinedDeque(Deque<E> front)
	{
		super(front);
	}

	@Override
	protected Deque<E> getFront()
	{
		return (Deque<E>) super.getFront();
	}

	@Override
	protected abstract Deque<E> getBack();

	@Override
	public void addFirst(E e)
	{
		getFront().addFirst(e);
	}

	@Override
	public void addLast(E e)
	{
		getBack().addLast(e);
	}

	@Override
	public boolean offerFirst(E e)
	{
		return getFront().offerFirst(e);
	}

	@Override
	public boolean offerLast(E e)
	{
		return getBack().offerLast(e);
	}

	@Override
	public E removeFirst()
	{
		if (!getFront().isEmpty())
			return getFront().removeFirst();
		else
			return getBack().removeFirst();
	}

	@Override
	public E removeLast()
	{
		if (!getBack().isEmpty())
			return getBack().removeLast();
		else
			return getFront().removeLast();
	}

	@Override
	public E pollFirst()
	{
		if (!getFront().isEmpty())
			return getFront().pollFirst();
		else
			return getBack().pollFirst();
	}

	@Override
	public E pollLast()
	{
		if (!getBack().isEmpty())
			return getBack().pollLast();
		else
			return getFront().pollLast();
	}

	@Override
	public E getFirst()
	{
		if (!getFront().isEmpty())
			return getFront().getFirst();
		else
			return getBack().getFirst();
	}

	@Override
	public E getLast()
	{
		if (!getBack().isEmpty())
			return getBack().getLast();
		else
			return getFront().getLast();
	}

	@Override
	public E peekFirst()
	{
		if (!getFront().isEmpty())
			return getFront().peekFirst();
		else
			return getBack().peekFirst();
	}

	@Override
	public E peekLast()
	{
		if (!getBack().isEmpty())
			return getBack().peekLast();
		else
			return getFront().peekLast();
	}

	@Override
	public boolean removeFirstOccurrence(Object o)
	{
		if (getFront().removeFirstOccurrence(o))
			return true;
		if (getBack().removeFirstOccurrence(o))
			return true;
		return false;
	}

	@Override
	public boolean removeLastOccurrence(Object o)
	{
		if (getBack().removeLastOccurrence(o))
			return true;
		if (getFront().removeLastOccurrence(o))
			return true;
		return false;
	}

	@Override
	public void push(E e)
	{
		getFront().push(e);
	}

	@Override
	public E pop()
	{
		if (!getFront().isEmpty())
			return getFront().pop();
		else
			return getBack().pop();
	}

	@Override
	public Iterator<E> descendingIterator()
	{
		return new CombinedIterator(getBack().descendingIterator(), getFront().descendingIterator());
	}

}
