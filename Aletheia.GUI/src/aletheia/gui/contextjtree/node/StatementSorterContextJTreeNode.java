package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
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

	public Statement getStatement()
	{
		return getSorter().getStatement();
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
