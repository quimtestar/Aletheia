package aletheia.gui.contextjtree.renderer;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

		public void passEvent(AWTEvent e)
		{
			if (statementRenderer != null)
				statementRenderer.dispatchEvent(e);
		}

	}

	private final CardLayout layout;
	private final JPanel panel;
	private final ExpandedRenderer expandedRenderer;
	private final CollapsedRenderer collapsedRenderer;
	private final static String cardExpanded = "expanded";
	private final static String cardCollapsed = "collapsed";
	private boolean expanded;

	private class Listener implements MouseListener, KeyListener
	{

		private void passEvent(AWTEvent e)
		{
			if (!expanded)
				collapsedRenderer.passEvent(e);
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			passEvent(e);
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			passEvent(e);
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			passEvent(e);
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			passEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			passEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			passEvent(e);
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			passEvent(e);
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			passEvent(e);
		}

	}

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
		Listener listener = new Listener();
		addKeyListener(listener);
		addMouseListener(listener);
	}

	public GroupSorter<? extends Statement> getSorter()
	{
		return sorter;
	}

	public void setExpanded(boolean expanded)
	{
		layout.show(panel, expanded ? cardExpanded : cardCollapsed);
		this.expanded = expanded;
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void setSelected(boolean selected)
	{
		super.setSelected(selected);
		expandedRenderer.setSelected(selected);
		collapsedRenderer.setSelected(selected);
	}

}
