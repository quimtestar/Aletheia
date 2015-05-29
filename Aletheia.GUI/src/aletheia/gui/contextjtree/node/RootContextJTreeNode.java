package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.renderer.ContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.renderer.EmptyContextJTreeNodeRenderer;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootContextRootGroupSorter;
import aletheia.gui.contextjtree.sorter.StatementRootGroupSorter;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class RootContextJTreeNode extends RootGroupSorterContextJTreeNode<RootContext>
{

	public RootContextJTreeNode(ContextJTreeModel model, RootContextRootGroupSorter rootContextRootGroupSorter)
	{
		super(model, rootContextRootGroupSorter);
	}
	
	
	
	@Override
	public RootContextRootGroupSorter getSorter()
	{
		return (RootContextRootGroupSorter)super.getSorter();
	}



	@Override
	protected ContextJTreeNodeRenderer buildRenderer(ContextJTree contextJTree)
	{
		return new EmptyContextJTreeNodeRenderer(contextJTree);
	}


}
