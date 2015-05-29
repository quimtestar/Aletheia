package aletheia.gui.contextjtree.node;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.tree.TreeNode;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.StatementContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.old.GroupSorter;
import aletheia.gui.contextjtree.sorter.old.StatementRootGroupSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ContextContextJTreeNode extends RootGroupSorterContextJTreeNode<Statement>
{
	private final ConsequentContextJTreeNode consequentContextJTreeNode;

	public ContextContextJTreeNode(ContextJTreeModel model, StatementRootGroupSorter statementRootGroupSorter)
	{
		super(model, statementRootGroupSorter);
		this.consequentContextJTreeNode=new ConsequentContextJTreeNode(model, this);
	}
	
	@Override
	public StatementRootGroupSorter getSorter()
	{
		return (StatementRootGroupSorter)super.getSorter();
	}

	public Context getContext()
	{
		return getSorter().getContext();
	}

	public ConsequentContextJTreeNode getConsequentContextJTreeNode()
	{
		return consequentContextJTreeNode;
	}

	@Override
	public ContextJTreeNode getChildAt(int childIndex)
	{
		if (childIndex<super.getChildCount())
			return super.getChildAt(childIndex);
		else if (childIndex==super.getChildCount())
			return consequentContextJTreeNode;
		else
			return null;
	}

	@Override
	public int getChildCount()
	{
		return super.getChildCount() + 1;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		if (consequentContextJTreeNode.equals(node))
			return super.getChildCount();
		else
			return super.getIndex(node);
	}

	@Override
	public Enumeration<? extends ContextJTreeNode> children()
	{
		final Enumeration<? extends ContextJTreeNode> enumeration=super.children();
		return new Enumeration<ContextJTreeNode>()
				{
					boolean pendingConsequent=true;

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
							pendingConsequent=false;
							return consequentContextJTreeNode;
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
			return StatementContextJTreeNodeRenderer.renderer(contextJTree, getContext().refresh(transaction));
		}
		finally
		{
			transaction.abort();
		}
	}

	

}
