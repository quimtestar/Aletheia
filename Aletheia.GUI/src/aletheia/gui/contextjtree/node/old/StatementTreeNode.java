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
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.SingletonSorter;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementTreeNode extends SorterTreeNode
{
	public StatementTreeNode(ContextTreeModel model, SingletonSorter singletonSorter)
	{
		super(model, singletonSorter);
	}

	@Override
	public SingletonSorter getSorter()
	{
		return (SingletonSorter) super.getSorter();
	}

	public Statement getStatement()
	{
		return getSorter().getStatement();
	}

	@Override
	protected StatementContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return StatementContextJTreeNodeRenderer.renderer(contextJTree, getStatement().refresh(transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public String toString()
	{
		Transaction transaction = getModel().getPersistenceManager().beginDirtyTransaction();
		try
		{
			Identifier id = getStatement().identifier(transaction);
			if (id == null)
				return getStatement().getVariable().toString();
			return id.toString();
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
