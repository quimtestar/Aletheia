package aletheia.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListChanges<E>
{
	public class Element
	{
		public final int index;
		public final E object;
		
		
		private Element(int index, E object)
		{
			super();
			this.index = index;
			this.object = object;
		}
		
		private Element(List<E> list,int index)
		{
			this(index,list.get(index));
		}
		
	}
	
	private final ArrayList<Element> removedElements;
	private final ArrayList<Element> insertedElements;
	
	public ListChanges(List<E> oldList, List<E> newList)
	{
		int oldI = 0;
		int newI = 0;
		removedElements=new ArrayList<Element>(oldList.size());
		insertedElements=new ArrayList<Element>(newList.size());
		while (oldI < oldList.size() && newI < newList.size())
		{
			Object oldSt = oldList.get(oldI);
			Object newSt = newList.get(newI);
			while (oldSt.equals(newSt))
			{
				oldI++;
				newI++;
				if (oldI >= oldList.size() || newI >= newList.size())
					break;
				oldSt = oldList.get(oldI);
				newSt = newList.get(newI);
			}
			if (oldI >= oldList.size() || newI >= newList.size())
				break;
			int oldJ = oldI;
			int newJ = newI;
			Object oldSt_ = oldList.get(oldJ);
			Object newSt_ = newList.get(newJ);
			Map<Object, Integer> oldMap = new HashMap<Object, Integer>();
			oldMap.put(oldSt, oldI);
			Map<Object, Integer> newMap = new HashMap<Object, Integer>();
			newMap.put(newSt, newI);
			while (!oldMap.containsKey(newSt_) && !newMap.containsKey(oldSt_))
			{
				oldMap.put(oldSt_, oldJ);
				newMap.put(newSt_, newJ);
				oldJ++;
				newJ++;
				if (oldJ >= oldList.size() || newJ >= newList.size())
					break;
				oldSt_ = oldList.get(oldJ);
				newSt_ = newList.get(newJ);
			}
			Integer newK = newMap.get(oldSt_);
			if (newJ >= newList.size() || newK != null)
			{
				while (oldI < oldJ)
					removedElements.add(new Element(oldList,oldI++));
				if (newK != null)
					while (newI < newK)
						insertedElements.add(new Element(newList,newI++));
			}
			Integer oldK = oldMap.get(newSt_);
			if (oldJ >= oldList.size() || oldK != null)
			{
				while (newI < newJ)
					insertedElements.add(new Element(newList,newI++));
				if (oldK != null)
					while (oldI < oldK)
						removedElements.add(new Element(oldList,oldI++));
			}
		}
		while (oldI < oldList.size())
			removedElements.add(new Element(oldList,oldI++));
		while (newI < newList.size())
			insertedElements.add(new Element(newList,newI++));
	}

	public ListChanges()
	{
		this.removedElements=new ArrayList<Element>(0);
		this.insertedElements=new ArrayList<Element>(0);
	}

	public List<Element> removedElements()
	{
		return Collections.unmodifiableList(removedElements);
	}
	
	public List<Element> insertedElements()
	{
		return Collections.unmodifiableList(insertedElements);
	}
	
}
