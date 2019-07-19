/*******************************************************************************
 * Copyright (c) 2015, 2016 Quim Testar.
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
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
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
		ConsequentContextJTreeNodeRenderer renderer = new ConsequentContextJTreeNodeRenderer(contextJTree, parent.getContext());
		renderer.setActiveContext(isActiveContext());
		return renderer;
	}

	@Override
	public String toString()
	{
		return super.toString() + "[Consequent: " + getContext().label() + "]";
	}

	public boolean isActiveContext()
	{
		return parent.isActiveContext();
	}

	@Override
	public synchronized ContextJTreeNodeRenderer renderer(ContextJTree contextJTree)
	{
		ContextJTreeNodeRenderer renderer = super.renderer(contextJTree);
		if (renderer instanceof ConsequentContextJTreeNodeRenderer)
			((ConsequentContextJTreeNodeRenderer) renderer).setActiveContext(isActiveContext());
		return renderer;
	}

	@Override
	protected synchronized ContextJTreeNodeRenderer getRenderer()
	{
		ContextJTreeNodeRenderer renderer = super.getRenderer();
		if (renderer instanceof ConsequentContextJTreeNodeRenderer)
			((ConsequentContextJTreeNodeRenderer) renderer).setActiveContext(isActiveContext());
		return renderer;
	}

}
