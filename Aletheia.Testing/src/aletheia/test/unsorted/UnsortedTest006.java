package aletheia.test.unsorted;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import aletheia.test.Test;
import aletheia.utilities.collections.AbstractCloseableList;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CloseableIterator;

public class UnsortedTest006 extends Test
{

	class CloseableList<E> extends AbstractCloseableList<E>
	{

		private final List<E> list;

		public CloseableList(List<E> list)
		{
			super();
			this.list = list;
		}

		@Override
		public int size()
		{
			return list.size();
		}

		@Override
		public boolean addAll(Collection<? extends E> c)
		{
			return list.addAll(c);
		}

		@Override
		public CloseableIterator<E> iterator()
		{
			return new CloseableIterator<>()
			{

				private final Iterator<E> iterator = list.iterator();

				@Override
				public boolean hasNext()
				{
					return iterator.hasNext();
				}

				@Override
				public E next()
				{
					return iterator.next();
				}

				@Override
				public void close()
				{
					System.out.println("close!");
				}
			};
		}

		@Override
		public E get(int index)
		{
			return list.get(index);
		}

	}

	@Override
	public void run() throws Exception
	{
		CloseableList<String> list = new CloseableList<>(Arrays.asList("hola", "adeu", "poctlla", "huatxiquei"));
		System.out.println("A:");
		try (CloseableIterator<String> i = list.iterator())
		{
			while (i.hasNext())
			{
				String s = i.next();
				System.out.println(s);
				if (s.equals("adeu"))
					break;
			}
		}
		System.out.println("B:");
		for (String s : new BufferedList<>(list))
		{
			System.out.println(s);
			if (s.equals("adeu"))
				break;
		}
		System.out.println("End");
	}

}
