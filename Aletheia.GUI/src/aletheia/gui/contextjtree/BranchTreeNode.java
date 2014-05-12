/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.gui.contextjtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.model.statement.Statement;

interface BranchTreeNode extends MyTreeNode
{
	public class Changes
	{
		private final int[] removedIndexes;
		private final Object[] removedObjects;
		private final int[] insertedIndexes;
		private final Object[] insertedObjects;

		protected Changes(List<?> oldList, List<?> newList)
		{
			int oldI = 0;
			int newI = 0;
			ArrayList<Integer> removedIndexes = new ArrayList<Integer>(oldList.size());
			ArrayList<Integer> insertedIndexes = new ArrayList<Integer>(newList.size());
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
						removedIndexes.add(oldI++);
					if (newK != null)
						while (newI < newK)
							insertedIndexes.add(newI++);
				}
				Integer oldK = oldMap.get(newSt_);
				if (oldJ >= oldList.size() || oldK != null)
				{
					while (newI < newJ)
						insertedIndexes.add(newI++);
					if (oldK != null)
						while (oldI < oldK)
							removedIndexes.add(oldI++);
				}
			}
			while (oldI < oldList.size())
				removedIndexes.add(oldI++);
			while (newI < newList.size())
				insertedIndexes.add(newI++);
			this.removedIndexes = new int[removedIndexes.size()];
			this.removedObjects = new Object[removedIndexes.size()];
			{
				int i = 0;
				for (int idx : removedIndexes)
				{
					this.removedIndexes[i] = idx;
					this.removedObjects[i] = oldList.get(idx);
					i++;
				}
			}
			this.insertedIndexes = new int[insertedIndexes.size()];
			this.insertedObjects = new Object[insertedIndexes.size()];
			{
				int i = 0;
				for (int idx : insertedIndexes)
				{
					this.insertedIndexes[i] = idx;
					this.insertedObjects[i] = newList.get(idx);
					i++;
				}
			}
		}

		protected Changes()
		{
			this.removedIndexes = new int[0];
			this.removedObjects = new Object[0];
			this.insertedIndexes = new int[0];
			this.insertedObjects = new Object[0];
		}

		public int[] getRemovedIndexes()
		{
			return removedIndexes;
		}

		public Object[] getRemovedObjects()
		{
			return removedObjects;
		}

		public int[] getInsertedIndexes()
		{
			return insertedIndexes;
		}

		public Object[] getInsertedObjects()
		{
			return insertedObjects;
		}

	}

	public Changes changeStatementList();

	public boolean checkStatementInsert(Statement statement);

	public boolean checkStatementRemove(Statement statement);

}
