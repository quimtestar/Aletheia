package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
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
			return StatementContextJTreeNodeRenderer.renderer(contextJTree, getStatement().refresh(transaction));
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
