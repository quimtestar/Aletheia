/*******************************************************************************
 * Copyright (c) 2015, 2023 Quim Testar.
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
package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.ContextGroupSorter;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Statement;

public abstract class SorterContextJTreeNode extends ContextJTreeNode
{
	private final Sorter sorter;

	public SorterContextJTreeNode(ContextJTreeModel model, Sorter sorter)
	{
		super(model);
		this.sorter = sorter;
	}

	public Sorter getSorter()
	{
		return sorter;
	}

	public GroupSorter<? extends Statement> parentSorter()
	{
		return getSorter().getGroup();
	}

	public abstract Sorter getNodeMapSorter();

	@SuppressWarnings("unchecked")
	@Override
	public GroupSorterContextJTreeNode<? extends Statement> getParent()
	{
		Sorter parentSorter = parentSorter();
		if (parentSorter instanceof RootGroupSorter)
			return getModel().getRootTreeNode();
		if (parentSorter instanceof ContextGroupSorter)
			parentSorter = ((ContextGroupSorter) parentSorter).getContextSorter();
		return (GroupSorterContextJTreeNode<? extends Statement>) getModel().getNodeMap().get(parentSorter);
	}

	@Override
	public String toString()
	{
		return super.toString() + "[Sorter: " + getSorter() + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sorter == null) ? 0 : sorter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		SorterContextJTreeNode other = (SorterContextJTreeNode) obj;
		if (sorter == null)
		{
			if (other.sorter != null)
				return false;
		}
		else if (!sorter.equals(other.sorter))
			return false;
		return true;
	}

}
