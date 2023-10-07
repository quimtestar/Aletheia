/*******************************************************************************
 * Copyright (c) 2023 Quim Testar.
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
 *******************************************************************************/
package aletheia.test.unsorted;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import aletheia.test.Test;
import aletheia.utilities.collections.AbstractCloseableList;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CloseableIterator;

public class UnsortedTest0006 extends Test
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
