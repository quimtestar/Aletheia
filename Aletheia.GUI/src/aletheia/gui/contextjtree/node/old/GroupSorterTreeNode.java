package aletheia.gui.contextjtree.node.old;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.StatementSorter;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Statement;

public class GroupSorterTreeNode extends SorterTreeNode implements BranchTreeNode
{
	public GroupSorterTreeNode(ContextJTreeModel model, GroupSorter<?> groupSorter)
	{
		super(model, groupSorter);
	}

	@Override
	public GroupSorter<?> getSorter()
	{
		return (GroupSorter<?>) super.getSorter();
	}

	@Override
	public Changes changeSorterList()
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
