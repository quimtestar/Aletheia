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

import java.util.HashMap;
import java.util.Map;

import aletheia.gui.contextjtree.node.ContextSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.RootContextGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.SorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementSorterContextJTreeNode;
import aletheia.gui.contextjtree.node.StatementGroupSorterContextJTreeNode;
import aletheia.gui.contextjtree.sorter.ContextSorter;
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
	private final ContextJTreeModel model;
	private final Map<Statement, StatementContextJTreeNode> statementMap;

	public SorterTreeNodeMap(ContextJTreeModel model)
	{
		super();
		this.model = model;
		this.statementMap = new HashMap<Statement, StatementContextJTreeNode>();
	}

	@Override
	protected synchronized SorterContextJTreeNode buildNode(Sorter sorter)
	{
		if (sorter instanceof GroupSorter)
		{
			if (sorter instanceof StatementGroupSorter)
				return new StatementGroupSorterContextJTreeNode(model, (StatementGroupSorter) sorter);
			else if (sorter instanceof RootContextGroupSorter)
				return new RootContextGroupSorterContextJTreeNode(model, (RootContextGroupSorter) sorter);
			else
				throw new Error();
		}
		else if (sorter instanceof StatementSorter)
		{
			StatementSorter statementSorter = (StatementSorter) sorter;
			SorterContextJTreeNode node;
			Statement statement = statementSorter.getStatement();
			statement.addStateListener(model.getStatementListener());
			statement.addAuthorityStateListener(model.getStatementListener());
			if (statementSorter instanceof ContextSorter)
			{
				Context ctx = (Context) statement;
				ctx.addNomenclatorListener(model.getStatementListener());
				ctx.addLocalStateListener(model.getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.addRootNomenclatorListener(model.getStatementListener());
				}
				node = new ContextSorterContextJTreeNode(model, (ContextSorter) statementSorter);
			}
			else
				node = new StatementSorterContextJTreeNode(model, statementSorter);
			statementMap.put(statement, (StatementContextJTreeNode) node);
			return node;
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
			statement.removeStateListener(model.getStatementListener());
			statement.removeAuthorityStateListener(model.getStatementListener());
			if (statement instanceof Context)
			{
				Context ctx = (Context) statement;
				ctx.removeNomenclatorListener(model.getStatementListener());
				ctx.removeLocalStateListener(model.getStatementListener());
				if (ctx instanceof RootContext)
				{
					RootContext rootCtx = (RootContext) ctx;
					rootCtx.removeRootNomenclatorListener(model.getStatementListener());
				}
			}
			statementMap.remove(statement);
		}
	}

	public synchronized StatementContextJTreeNode getStatementContextJTreeNode(Statement statement)
	{
		return statementMap.get(statement);
	}

}
