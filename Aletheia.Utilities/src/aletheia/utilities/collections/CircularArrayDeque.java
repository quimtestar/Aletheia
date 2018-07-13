/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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

import java.util.ArrayDeque;

public class CircularArrayDeque<E> extends ArrayDeque<E>
{
	private static final long serialVersionUID = -2872282921978440975L;

	private final int numElements;

	public CircularArrayDeque(int numElements)
	{
		super(numElements);
		this.numElements = numElements;
	}

	public int getNumElements()
	{
		return numElements;
	}

	@Override
	public void addFirst(E e)
	{
		while (size() >= numElements)
			removeLast();
		super.addFirst(e);
	}

	@Override
	public void addLast(E e)
	{
		while (size() >= numElements)
			removeFirst();
		super.addLast(e);
	}

	public boolean isFull()
	{
		return size() >= numElements;
	}

}
