package aletheia.utilities.collections;

import java.util.ListIterator;

public class ReverseListIterator<E> implements ListIterator<E>
{
	private final ListIterator<E> inner;

	public ReverseListIterator(ListIterator<E> inner)
	{
		super();
		this.inner = inner;
	}

	@Override
	public boolean hasNext()
	{
		return inner.hasPrevious();
	}

	@Override
	public E next()
	{
		return inner.previous();
	}

	@Override
	public boolean hasPrevious()
	{
		return inner.hasNext();
	}

	@Override
	public E previous()
	{
		return inner.next();
	}

	@Override
	public int nextIndex()
	{
		return inner.previousIndex();
	}

	@Override
	public int previousIndex()
	{
		return inner.nextIndex();
	}

	@Override
	public void remove()
	{
		inner.remove();
	}

	@Override
	public void set(E e)
	{
		inner.set(e);
	}

	@Override
	public void add(E e)
	{
		inner.add(e);
		inner.previous();
	}

}
