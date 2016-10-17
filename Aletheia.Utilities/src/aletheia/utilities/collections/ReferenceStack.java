/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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

import java.lang.ref.Reference;
import java.util.NoSuchElementException;

/**
 * A stack built on a sequence of nodes linked by abstract {@link Reference}s
 */
public abstract class ReferenceStack<E>
{

	protected abstract Reference<Node> makeReference(Node node);

	protected class Node
	{
		private E item;
		private Reference<Node> next;

		private Node(E item, Node next)
		{
			super();
			this.item = item;
			this.next = next != null ? makeReference(next) : null;
		}
	}

	private Reference<Node> top;

	public ReferenceStack()
	{
		this.top = null;
	}

	public void push(E item)
	{
		top = makeReference(new Node(item, top != null ? top.get() : null));
	}

	public E pop()
	{
		if (top == null)
			throw new NoSuchElementException();
		Node node = top.get();
		if (node == null)
			throw new NoSuchElementException();
		E item = node.item;
		top = node.next;
		return item;
	}

	public E peek()
	{
		if (top == null)
			return null;
		Node node = top.get();
		if (node == null)
			return null;
		return node.item;
	}

	public boolean isEmpty()
	{
		return top == null || top.get() == null;
	}

}
