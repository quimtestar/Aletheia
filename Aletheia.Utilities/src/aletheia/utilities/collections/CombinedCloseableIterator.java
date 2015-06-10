package aletheia.utilities.collections;

public class CombinedCloseableIterator<E> extends CombinedIterator<E> implements CloseableIterator<E>
{
	public CombinedCloseableIterator(CloseableIterator<? extends E> frontIterator, CloseableIterator<? extends E> backIterator)
	{
		super(frontIterator, backIterator);
	}

	@Override
	protected CloseableIterator<? extends E> getFrontIterator()
	{
		return (CloseableIterator<? extends E>) super.getFrontIterator();
	}

	@Override
	protected CloseableIterator<? extends E> getBackIterator()
	{
		return (CloseableIterator<? extends E>) super.getBackIterator();
	}

	@Override
	public void close()
	{
		getFrontIterator().close();
		getBackIterator().close();
	}

}