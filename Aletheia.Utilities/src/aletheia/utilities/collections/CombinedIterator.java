package aletheia.utilities.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CombinedIterator<E> extends AbstractReadOnlyIterator<E>
{
	private final Iterator<E> frontIterator;
	private final Iterator<E> backIterator;

	private E next;
	private boolean hasNext;

	protected CombinedIterator(Iterator<E> frontIterator, Iterator<E> backIterator)
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

	protected Iterator<E> getFrontIterator()
	{
		return frontIterator;
	}

	protected Iterator<E> getBackIterator()
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