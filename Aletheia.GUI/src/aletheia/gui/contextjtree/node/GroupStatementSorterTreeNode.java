package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.statementsorter.GroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.RootContextGroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.RootGroupStatementSorter;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class GroupStatementSorterTreeNode extends StatementSorterTreeNode implements BranchTreeNode
{
	public GroupStatementSorterTreeNode(ContextTreeModel model, GroupStatementSorter<?> groupStatementSorter)
	{
		super(model,groupStatementSorter);
	}

	public GroupStatementSorter<?> getStatementSorter()
	{
		return (GroupStatementSorter<?>) super.getStatementSorter();
	}

	@Override
	public Changes changeStatementList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkStatementInsert(Statement statement)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkStatementRemove(Statement statement)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
