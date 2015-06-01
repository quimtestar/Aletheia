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

import aletheia.gui.contextjtree.node.ContextGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.RootContextGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.sorter.ContextGroupSorter;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootContextGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.gui.contextjtree.sorter.StatementGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;

public class SorterTreeNodeMap extends GenericTreeNodeMap<Sorter, SorterContextJTreeNode>
{
	public SorterTreeNodeMap(ContextJTreeModel model)
	{
		super(model);
	}

	@Override
	protected synchronized SorterContextJTreeNode buildNode(Sorter sorter)
	{
		if (sorter instanceof GroupSorter)
		{
			if (sorter instanceof StatementGroupSorter)
				return new StatementGroupSorterContextJTreeNode(getModel(), (StatementGroupSorter) sorter);
			else if (sorter instanceof RootContextGroupSorter)
				return new RootContextGroupSorterContextJTreeNode(getModel(), (RootContextGroupSorter) sorter);
			else
				throw new Error();
		}
		else if (sorter instanceof StatementSorter)
		{
			StatementSorter statementSorter = (StatementSorter) sorter;
			Statement statement = statementSorter.getStatement();
			statement.addStateListener(getModel().getStatementListener());
			statement.addAuthorityStateListener(getModel().getStatementListener());
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.addNomenclatorListener(getModel().getStatementListener());
				ctx.addLocalStateListener(getModel().getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.addRootNomenclatorListener(getModel().getStatementListener());
				}
				return new ContextGroupSorterContextJTreeNode(getModel(), new ContextGroupSorter(ctx));
			}
			else
				return new StatementSorterContextJTreeNode(getModel(), statementSorter);

		}
		else
			throw new Error();
	}

	@Override
	protected synchronized void keyRemoved(Sorter sorter)
	{
		if (sorter instanceof StatementSorter)
		{
			Statement statement = ((StatementSorter) sorter).getStatement();
			statement.removeStateListener(getModel().getStatementListener());
			statement.removeAuthorityStateListener(getModel().getStatementListener());
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.removeNomenclatorListener(getModel().getStatementListener());
				ctx.removeLocalStateListener(getModel().getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.removeRootNomenclatorListener(getModel().getStatementListener());
				}
			}
		}
	}

}
