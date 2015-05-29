package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementRootGroupSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public abstract class RootGroupSorterContextJTreeNode<S extends Statement> extends GroupSorterContextJTreeNode<S>
{

	public RootGroupSorterContextJTreeNode(ContextJTreeModel model, RootGroupSorter<S> rootGroupSorter)
	{
		super(model, rootGroupSorter);
	}
	
	
	
	@Override
	public RootGroupSorter<S> getSorter()
	{
		return (RootGroupSorter<S>)super.getSorter();
	}


}
