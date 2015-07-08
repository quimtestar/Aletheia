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

import java.util.Collections;
import java.util.Iterator;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ProperStatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.ContextGroupSorter;
import aletheia.gui.contextjtree.sorter.ContextSorter;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.CombinedIterator;

public class ContextSorterContextJTreeNode extends StatementGroupSorterContextJTreeNode implements StatementContextJTreeNode,
		TopGroupSorterContextJTreeNode<Statement>
{
	private final ConsequentContextJTreeNode consequentNode;

	public ContextSorterContextJTreeNode(ContextJTreeModel model, ContextSorter contextSorter)
	{
		super(model, contextSorter.makeContextGroupSorter());
		this.consequentNode = new ConsequentContextJTreeNode(model, this);
	}

	@Override
	public ContextGroupSorter getSorter()
	{
		return (ContextGroupSorter) super.getSorter();
	}

	public Context getContext()
	{
		return getSorter().getContext();
	}

	@Override
	public Context getStatement()
	{
		return getContext();
	}

	@Override
	public Identifier getIdentifier()
	{
		return getSorter().getContextSorter().getIdentifier();
	}

	@Override
	public ContextSorter getNodeMapSorter()
	{
		return getSorter().getContextSorter();
	}

	@Override
	public GroupSorter<? extends Statement> parentSorter()
	{
		return getNodeMapSorter().getGroup();
	}

	public ConsequentContextJTreeNode getConsequentNode()
	{
		return consequentNode;
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		if (childIndex < super.getChildCount())
			return super.getChildAt(childIndex);
		else if (childIndex == super.getChildCount())
			return consequentNode;
		else
			return null;
	}

	@Override
	public int getChildCount()
	{
		return super.getChildCount() + 1;
	}

	@Override
	public int getIndex(ContextJTreeNode node)
	{
		if (consequentNode.equals(node))
			return super.getChildCount();
		else
			return super.getIndex(node);
	}

	@Override
	public Iterator<? extends ContextJTreeNode> childrenIterator()
	{
		return new CombinedIterator<ContextJTreeNode>(super.childrenIterator(), Collections.<ContextJTreeNode> singleton(consequentNode).iterator());
	}

	@Override
	protected StatementContextJTreeNodeRenderer<?> buildRenderer(ContextJTree contextJTree)
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
	protected synchronized StatementContextJTreeNodeRenderer<?> getRenderer()
	{
		return (StatementContextJTreeNodeRenderer<?>) super.getRenderer();
	}

}
