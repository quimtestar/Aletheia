package aletheia.utilities.collections;

public class CombinedCloseableIterator<E> extends CombinedIterator<E> implements CloseableIterator<E>
{
	protected CombinedCloseableIterator(CloseableIterator<E> frontIterator, CloseableIterator<E> backIterator)
	{
		super(frontIterator, backIterator);
	}

	@Override
	protected CloseableIterator<E> getFrontIterator()
	{
		return (CloseableIterator<E>) super.getFrontIterator();
	}

	@Override
	protected CloseableIterator<E> getBackIterator()
	{
		return (CloseableIterator<E>) super.getBackIterator();
	}

	@Override
	public void close()
	{
		getFrontIterator().close();
		getBackIterator().close();
	}

}