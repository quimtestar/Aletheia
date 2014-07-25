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

import java.util.Queue;

/**
 * Abstract implementation of a combined {@link Queue}. When polling, the front
 * {@link Queue} elements are returned first.
 *
 * @author Quim Testar
 */
public abstract class AbstractCombinedQueue<E> extends AbstractCombinedCollection<E> implements Queue<E>
{

	private static final long serialVersionUID = 5726542391975305992L;

	public AbstractCombinedQueue(Queue<E> front)
	{
		super(front);
	}

	@Override
	protected Queue<E> getFront()
	{
		return (Queue<E>) super.getFront();
	}

	@Override
	protected abstract Queue<E> getBack();

	@Override
	public boolean offer(E e)
	{
		return getBack().offer(e);
	}

	@Override
	public E remove()
	{
		if (!getFront().isEmpty())
			return getFront().remove();
		else
			return getBack().remove();
	}

	@Override
	public E poll()
	{
		if (!getFront().isEmpty())
			return getFront().poll();
		else
			return getBack().poll();
	}

	@Override
	public E element()
	{
		if (!getFront().isEmpty())
			return getFront().element();
		else
			return getBack().element();
	}

	@Override
	public E peek()
	{
		if (!getFront().isEmpty())
			return getFront().peek();
		else
			return getBack().peek();
	}

}
