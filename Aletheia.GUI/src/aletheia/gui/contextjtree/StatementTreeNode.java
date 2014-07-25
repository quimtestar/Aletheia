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

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementTreeNode extends AbstractTreeNode
{
	private final ContextTreeModel model;
	private final BranchTreeNode parent;
	private final Statement statement;

	public StatementTreeNode(ContextTreeModel model, Statement statement)
	{
		this.model = model;
		if (statement instanceof RootContext)
			this.parent = model.getRootTreeNode();
		else
		{
			Transaction transaction = model.beginTransaction();
			try
			{
				this.parent = (ContextTreeNode) model.nodeMap().get(statement.getContext(transaction));
			}
			finally
			{
				transaction.abort();
			}
		}
		this.statement = statement;
	}

	public ContextTreeModel getModel()
	{
		return model;
	}

	public Statement getStatement()
	{
		return statement;
	}

	@Override
	public BranchTreeNode getParent()
	{
		return parent;
	}

	@Override
	protected StatementContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		Transaction transaction = model.beginTransaction();
		try
		{
			return StatementContextJTreeNodeRenderer.renderer(contextJTree, statement.refresh(transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	public String toString()
	{
		Transaction transaction = model.getPersistenceManager().beginDirtyTransaction();
		try
		{
			Identifier id = statement.identifier(transaction);
			if (id == null)
				return statement.getVariable().toString();
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
