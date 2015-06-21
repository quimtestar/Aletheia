/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
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

		private Element(List<E> list, int index)
		{
			this(index, list.get(index));
		}

		@Override
		public String toString()
		{
			return "[" + index + ": " + object + "]";
		}

	}

	private final ArrayList<Element> removedElements;
	private final ArrayList<Element> insertedElements;

	public ListChanges(List<E> oldList, List<E> newList)
	{
		int oldI = 0;
		int newI = 0;
		removedElements = new ArrayList<Element>(oldList.size());
		insertedElements = new ArrayList<Element>(newList.size());
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
				oldJ++;
				newJ++;
				if (oldJ >= oldList.size() || newJ >= newList.size())
					break;
				oldSt_ = oldList.get(oldJ);
				newSt_ = newList.get(newJ);
				oldMap.put(oldSt_, oldJ);
				newMap.put(newSt_, newJ);
			}
			Integer newK = newMap.get(oldSt_);
			if (newJ >= newList.size() || newK != null)
			{
				while (oldI < oldJ)
					removedElements.add(new Element(oldList, oldI++));
				if (newK != null)
					while (newI < newK)
						insertedElements.add(new Element(newList, newI++));
			}
			Integer oldK = oldMap.get(newSt_);
			if (oldJ >= oldList.size() || oldK != null)
			{
				while (newI < newJ)
					insertedElements.add(new Element(newList, newI++));
				if (oldK != null)
					while (oldI < oldK)
						removedElements.add(new Element(oldList, oldI++));
			}
		}
		while (oldI < oldList.size())
			removedElements.add(new Element(oldList, oldI++));
		while (newI < newList.size())
			insertedElements.add(new Element(newList, newI++));
	}

	public ListChanges()
	{
		this.removedElements = new ArrayList<Element>(0);
		this.insertedElements = new ArrayList<Element>(0);
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
