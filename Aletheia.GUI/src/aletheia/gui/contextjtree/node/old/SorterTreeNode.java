package aletheia.gui.contextjtree.node.old;

import aletheia.gui.contextjtree.ContextJTreeModel;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.RootGroupSorter;
import aletheia.gui.contextjtree.sorter.Sorter;

public abstract class SorterTreeNode extends AbstractTreeNode
{
	private final ContextJTreeModel model;
	private final Sorter sorter;
	private final BranchTreeNode parent;

	public SorterTreeNode(ContextJTreeModel model, Sorter sorter)
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
			this.parent = (GroupSorterTreeNode) model.nodeMap().get(group);
	}

	public ContextJTreeModel getModel()
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
