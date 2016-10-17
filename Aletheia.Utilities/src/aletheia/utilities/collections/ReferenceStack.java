package aletheia.utilities.collections;

import java.lang.ref.Reference;
import java.util.NoSuchElementException;

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
			this.next = next!=null?makeReference(next):null;
		}
	}
	
	private Reference<Node> top;
	
	public ReferenceStack()
	{
		this.top=null;
	}
	
	public void push(E item)
	{
		top=makeReference(new Node(item,top!=null?top.get():null));
	}
	
	public E pop()
	{
		if (top==null)
			throw new NoSuchElementException();
		Node node=top.get();
		if (node==null)
			throw new NoSuchElementException();
		E item=node.item;
		top=node.next;
		return item;
	}
	
	public E peek()
	{
		if (top==null)
			return null;
		Node node=top.get();
		if (node==null)
			return null;
		return node.item;
	}

	public boolean isEmpty()
	{
		return top==null || top.get()==null;
	}
	
	
}
