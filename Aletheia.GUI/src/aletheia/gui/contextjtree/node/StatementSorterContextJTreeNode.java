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
import aletheia.gui.contextjtree.renderer.ProperStatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class StatementSorterContextJTreeNode extends SorterContextJTreeNode implements StatementContextJTreeNode
{

	public StatementSorterContextJTreeNode(ContextJTreeModel model, StatementSorter statementSorter)
	{
		super(model, statementSorter);
	}

	@Override
	public StatementSorter getSorter()
	{
		return (StatementSorter) super.getSorter();
	}

	@Override
	public Statement getStatement()
	{
		return getSorter().getStatement();
	}

	@Override
	public StatementSorter getNodeMapSorter()
	{
		return getSorter();
	}

	@Override
	protected StatementContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		Transaction transaction = getModel().beginTransaction();
		try
		{
			return ProperStatementContextJTreeNodeRenderer.renderer(contextJTree, getStatement().refresh(transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	@Override
	protected synchronized StatementContextJTreeNodeRenderer getRenderer()
	{
		return (StatementContextJTreeNodeRenderer) super.getRenderer();
	}

}
