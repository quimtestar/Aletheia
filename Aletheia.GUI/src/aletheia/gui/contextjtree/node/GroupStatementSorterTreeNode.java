package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.model.statement.Statement;

public class GroupStatementSorterTreeNode extends StatementSorterTreeNode implements BranchTreeNode
{
	public GroupStatementSorterTreeNode(ContextTreeModel model, GroupSorter<?> groupSorter)
	{
		super(model, groupSorter);
	}

	@Override
	public GroupSorter<?> getSorter()
	{
		return (GroupSorter<?>) super.getSorter();
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
