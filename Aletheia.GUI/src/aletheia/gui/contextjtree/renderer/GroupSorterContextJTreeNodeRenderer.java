package aletheia.gui.contextjtree.renderer;

import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JPanel;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.model.statement.Statement;

public class GroupSorterContextJTreeNodeRenderer extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = -602580358340866197L;
	private final GroupSorter<? extends Statement> sorter;

	private class ExpandedRenderer extends ContextJTreeNodeRenderer
	{
		private static final long serialVersionUID = 7681227180403731319L;

		public ExpandedRenderer(ContextJTree contextJTree)
		{
			super(contextJTree);
			addTextLabel(sorter.getPrefix().qualifiedName(), Color.blue);
		}

	}

	private class CollapsedRenderer extends ContextJTreeNodeRenderer
	{

		private static final long serialVersionUID = -4978670531069281792L;

		public CollapsedRenderer(ContextJTree contextJTree)
		{
			super(contextJTree);
			addTextLabel(sorter.getPrefix().qualifiedName(), Color.red);
		}

	}

	private final CardLayout layout;
	private final JPanel panel;
	private final ExpandedRenderer expandedRenderer;
	private final CollapsedRenderer collapsedRenderer;
	private final static String cardExpanded = "expanded";
	private final static String cardCollapsed = "collapsed";

	public GroupSorterContextJTreeNodeRenderer(ContextJTree contextJTree, GroupSorter<? extends Statement> sorter)
	{
		super(contextJTree);
		this.sorter = sorter;
		this.layout = new CardLayout();
		this.panel = new JPanel(this.layout);
		this.expandedRenderer = new ExpandedRenderer(contextJTree);
		this.panel.add(this.expandedRenderer, cardExpanded);
		this.collapsedRenderer = new CollapsedRenderer(contextJTree);
		this.panel.add(this.collapsedRenderer, cardCollapsed);
		this.layout.show(this.panel, cardCollapsed);
		add(this.panel);
	}

	public GroupSorter<? extends Statement> getSorter()
	{
		return sorter;
	}

	public void setExpanded(boolean expanded)
	{
		layout.show(panel, expanded ? cardExpanded : cardCollapsed);
	}

	@Override
	public void setSelected(boolean selected)
	{
		super.setSelected(selected);
		expandedRenderer.setSelected(selected);
		collapsedRenderer.setSelected(selected);
	}

}
