package aletheia.gui.contextjtree.renderer;

import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JPanel;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

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

		private final StatementContextJTreeNodeRenderer statementRenderer;

		public CollapsedRenderer(ContextJTree contextJTree)
		{
			super(contextJTree);
			Transaction transaction = getContextJTree().getModel().beginTransaction();
			try
			{
				Statement statement = sorter.getStatement(transaction);
				if (statement != null)
				{
					statementRenderer = StatementContextJTreeNodeRenderer.renderer(contextJTree, statement);
					add(statementRenderer);
				}
				else
				{
					statementRenderer = null;
					addTextLabel(sorter.getPrefix().qualifiedName(), Color.blue);
				}
			}
			finally
			{
				transaction.abort();
			}
		}

		@Override
		public void setSelected(boolean selected)
		{
			super.setSelected(selected);
			if (statementRenderer != null)
				statementRenderer.setSelected(selected);
		}

	}

	private final CardLayout layout;
	private final JPanel panel;
	private final ExpandedRenderer expandedRenderer;
	private final CollapsedRenderer collapsedRenderer;
	private final static String cardExpanded = "expanded";
	private final static String cardCollapsed = "collapsed";

	public GroupSorterContextJTreeNodeRenderer(ContextJTree contextJTree, GroupSorter<? extends Statement> sorter, boolean expanded)
	{
		super(contextJTree);
		this.sorter = sorter;
		this.layout = new CardLayout();
		this.panel = new JPanel(this.layout);
		this.expandedRenderer = new ExpandedRenderer(contextJTree);
		this.panel.add(this.expandedRenderer, cardExpanded);
		this.collapsedRenderer = new CollapsedRenderer(contextJTree);
		this.panel.add(this.collapsedRenderer, cardCollapsed);
		add(this.panel);
		setExpanded(expanded);
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
