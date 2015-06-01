package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.StatementGroupSorter;
import aletheia.model.statement.Statement;

public class StatementGroupSorterContextJTreeNode extends GroupSorterContextJTreeNode<Statement>
{

	public StatementGroupSorterContextJTreeNode(ContextJTreeModel model, StatementGroupSorter sorter)
	{
		super(model, sorter);
	}

	@Override
	public StatementGroupSorter getSorter()
	{
		return (StatementGroupSorter) super.getSorter();
	}

}
