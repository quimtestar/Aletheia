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
package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ConsequentContextJTreeNodeRenderer;
import aletheia.model.statement.Context;

public class ConsequentContextJTreeNode extends ContextJTreeNode
{
	private final ContextSorterContextJTreeNode parent;

	public ConsequentContextJTreeNode(ContextJTreeModel model, ContextSorterContextJTreeNode parent)
	{
		super(model);
		this.parent = parent;
	}

	@Override
	public ContextSorterContextJTreeNode getParent()
	{
		return parent;
	}

	public Context getContext()
	{
		return getParent().getContext();
	}

	@Override
	protected ConsequentContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new ConsequentContextJTreeNodeRenderer(contextJTree, parent.getContext());
	}

	@Override
	protected synchronized ConsequentContextJTreeNodeRenderer getRenderer()
	{
		return (ConsequentContextJTreeNodeRenderer) super.getRenderer();
	}

	@Override
	public String toString()
	{
		return super.toString() + "[Consequent: " + getContext().label() + "]";
	}

}
