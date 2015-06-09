package aletheia.gui.contextjtree.node;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.ContextGroupSorter;
import aletheia.gui.contextjtree.sorter.ContextSorter;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextSorterContextJTreeNode extends StatementGroupSorterContextJTreeNode implements StatementContextJTreeNode
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
	public ContextSorter getNodeMapSorter()
	{
		return getSorter().getContextSorter();
	}

	@Override
	protected GroupSorter<? extends Statement> parentSorter()
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
	public Enumeration<? extends ContextJTreeNode> children()
	{
		final Enumeration<? extends ContextJTreeNode> enumeration = super.children();
		return new Enumeration<ContextJTreeNode>()
		{
			boolean pendingConsequent = true;

			@Override
			public boolean hasMoreElements()
			{
				if (enumeration.hasMoreElements())
					return true;
				else
					return pendingConsequent;
			}

			@Override
			public ContextJTreeNode nextElement()
			{
				if (enumeration.hasMoreElements())
					return enumeration.nextElement();
				else if (pendingConsequent)
				{
					pendingConsequent = false;
					return consequentNode;
				}
				else
					throw new NoSuchElementException();
			}

		};
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
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

}
