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
package aletheia.gui.contextjtree.node.old;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.renderer.ConsequentContextJTreeNodeRenderer;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.persistence.Transaction;

public class ConsequentTreeNode extends AbstractTreeNode
{
	private final ContextTreeModel model;
	private final Context context;

	public ConsequentTreeNode(ContextTreeModel model, Context context)
	{
		this.model = model;
		this.context = context;
	}

	public ContextTreeModel getModel()
	{
		return model;
	}

	public Context getContext()
	{
		return context;
	}

	@Override
	public ContextTreeNode getParent()
	{
		return (ContextTreeNode) model.nodeMap().get(context);
	}

	@Override
	protected ConsequentContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new ConsequentContextJTreeNodeRenderer(contextJTree, context);
	}

	@Override
	public String toString()
	{
		Transaction transaction = model.getPersistenceManager().beginDirtyTransaction();
		try
		{
			Identifier id = context.identifier(transaction);
			if (id == null)
				return null;
			return "|- " + id.toString();
		}
		catch (RuntimeException e)
		{
			return "*deleted*";
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public boolean getAllowsChildren()
	{
		return false;
	}

}
