package aletheia.utilities.collections;

public class BijectionCloseableList<I, O> extends BijectionList<I, O>implements CloseableList<O>
{

	public BijectionCloseableList(Bijection<I, O> bijection, CloseableList<I> inner)
	{
		super(bijection, inner);
	}

	@Override
	protected CloseableList<I> getInner()
	{
		return (CloseableList<I>) super.getInner();
	}

	@Override
	public CloseableIterator<O> iterator()
	{
		final CloseableIterator<I> inner = getInner().iterator();
		return new CloseableIterator<O>()
		{

			@Override
			public boolean hasNext()
			{
				return inner.hasNext();
			}

			@Override
			public O next()
			{
				return getBijection().forward(inner.next());
			}

			@Override
			public void remove()
			{
				inner.remove();

			}

			@Override
			public void close()
			{
				inner.close();
			}
		};

	}

}
