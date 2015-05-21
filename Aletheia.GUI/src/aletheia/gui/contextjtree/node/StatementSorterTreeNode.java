package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.statementsorter.GroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.RootContextGroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.RootGroupStatementSorter;
import aletheia.gui.contextjtree.statementsorter.StatementSorter;

public abstract class StatementSorterTreeNode extends AbstractTreeNode
{
	private final ContextTreeModel model;
	private final StatementSorter<?> statementSorter;
	private final BranchTreeNode parent;
	
	public StatementSorterTreeNode(ContextTreeModel model, StatementSorter<?> statementSorter)
	{
		super();
		this.model = model;
		this.statementSorter = statementSorter;
		GroupStatementSorter<?> group=statementSorter.getGroup();
		if (group instanceof RootGroupStatementSorter)
		{
			if (group instanceof RootContextGroupStatementSorter)
				this.parent=model.getRootTreeNode();
			else
				this.parent=(ContextTreeNode) model.nodeMap().get(group.getContext());
		}
		else
			this.parent= (GroupStatementSorterTreeNode) model.nodeMap().get(group);
	}

	public ContextTreeModel getModel()
	{
		return model;
	}
	
	public StatementSorter<?> getStatementSorter()
	{
		return statementSorter;
	}

	@Override
	public BranchTreeNode getParent()
	{
		return parent;
	}

}
