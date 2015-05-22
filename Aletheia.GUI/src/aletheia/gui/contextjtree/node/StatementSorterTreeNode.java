package aletheia.gui.contextjtree.node;

import aletheia.gui.contextjtree.ContextTreeModel;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootContextRootGroupSorter;
import aletheia.gui.contextjtree.sorter.RootGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;

public abstract class StatementSorterTreeNode extends AbstractTreeNode
{
	private final ContextTreeModel model;
	private final Sorter sorter;
	private final BranchTreeNode parent;

	public StatementSorterTreeNode(ContextTreeModel model, Sorter sorter)
	{
		super();
		this.model = model;
		this.sorter = sorter;
		GroupSorter<?> group = sorter.getGroup();
		if (group instanceof RootGroupSorter)
		{
			if (group instanceof RootContextRootGroupSorter)
				this.parent = model.getRootTreeNode();
			else
				this.parent = (ContextTreeNode) model.nodeMap().get(group.getContext());
		}
		else
			this.parent = (GroupStatementSorterTreeNode) model.nodeMap().get(group);
	}

	public ContextTreeModel getModel()
	{
		return model;
	}

	public Sorter getSorter()
	{
		return sorter;
	}

	@Override
	public BranchTreeNode getParent()
	{
		return parent;
	}

}
