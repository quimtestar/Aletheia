package aletheia.gui.contextjtree.renderer;

import java.awt.Color;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.model.statement.Statement;

public class GroupSorterContextJTreeNodeRenderer extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = -602580358340866197L;
	private final GroupSorter<? extends Statement> sorter;

	public GroupSorterContextJTreeNodeRenderer(ContextJTree contextJTree,GroupSorter<? extends Statement> sorter)
	{
		super(contextJTree);
		this.sorter=sorter;
		addTextLabel(sorter.getPrefix().qualifiedName(), Color.blue);
	}

	public GroupSorter<? extends Statement> getSorter()
	{
		return sorter;
	}
	
	

}
