package aletheia.gui.contextjtree.renderer;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.sorter.GroupSorter;
import aletheia.gui.contextjtree.sorter.StatementGroupSorter;
import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class GroupSorterContextJTreeNodeRenderer extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = -602580358340866197L;

	private static final Logger logger = LoggerManager.instance.logger();

	private final GroupSorter<? extends Statement> sorter;

	protected class EditableSorterPrefixComponent extends EditableTextLabelComponent implements EditableComponent
	{

		private static final long serialVersionUID = -8199917497226360702L;

		public EditableSorterPrefixComponent()
		{
			super(getGroupSorterColor());
			String qualifiedName = sorter.getPrefix().qualifiedName();
			setLabelText(qualifiedName);
			setFieldText(qualifiedName);
		}

		private void resetTextField()
		{
			setFieldText(sorter.getPrefix().qualifiedName());
		}

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_ENTER:
				try
				{
					getContextJTree().editSorterPrefix(sorter, getFieldText().trim());
				}
				catch (Exception e)
				{
					try
					{
						getContextJTree().getAletheiaJPanel().getCliJPanel().exception(e);
					}
					catch (InterruptedException e1)
					{
						logger.error(e1.getMessage(), e1);
					}
				}
				break;
			case KeyEvent.VK_ESCAPE:
				resetTextField();
				break;
			}
		}

	}

	private class ExpandedRenderer extends ContextJTreeNodeRenderer
	{
		private static final long serialVersionUID = 7681227180403731319L;

		private final EditableSorterPrefixComponent editableSorterPrefixComponent;

		public ExpandedRenderer(ContextJTree contextJTree)
		{
			super(contextJTree);
			addGroupSorterLabel();
			addSpaceLabel(3);
			this.editableSorterPrefixComponent = new EditableSorterPrefixComponent();
			addEditableComponent(editableSorterPrefixComponent);
			add(editableSorterPrefixComponent);
		}

		public void editName()
		{
			editableSorterPrefixComponent.edit();
		}

	}

	private final CardLayout layout;
	private final JPanel panel;
	private final ExpandedRenderer expandedRenderer;
	private final EditableSorterPrefixComponent collapsedEditableSorterPrefixComponent;
	private final StatementContextJTreeNodeRenderer collapsedRenderer;
	private final static String cardExpanded = "expanded";
	private final static String cardCollapsed = "collapsed";
	private boolean expanded;

	private class Listener implements MouseListener, KeyListener
	{

		private void passEvent(AWTEvent e)
		{
			if (!expanded && collapsedRenderer != null)
				collapsedRenderer.dispatchEvent(e);
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			passEvent(e);
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_F2:
				editName();
				break;
			case KeyEvent.VK_DELETE:
				try
				{
					if (e.isShiftDown())
						deleteCascade();
					else
						delete();
				}
				catch (InterruptedException e1)
				{
					logger.error(e1.getMessage(), e1);
				}
				break;
			case KeyEvent.VK_F3:
			{
				CliJPanel cliJPanel = getContextJTree().getAletheiaJPanel().getCliJPanel();
				if (sorter instanceof StatementGroupSorter)
					cliJPanel.setActiveContext(((StatementGroupSorter) sorter).getContext());
				break;
			}
			}
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
		Transaction transaction = getContextJTree().getModel().beginTransaction();
		try
		{
			Statement statement = sorter.getStatement(transaction);
			if (statement != null)
			{
				this.collapsedEditableSorterPrefixComponent = new EditableSorterPrefixComponent();
				this.collapsedRenderer = StatementContextJTreeNodeRenderer.renderer(contextJTree, statement, collapsedEditableSorterPrefixComponent);
				this.panel.add(this.collapsedRenderer, cardCollapsed);
			}
			else
			{
				this.collapsedEditableSorterPrefixComponent = null;
				this.collapsedRenderer = null;
			}
		}
		finally
		{
			transaction.abort();
		}
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
		layout.show(panel, expanded || cardCollapsed == null ? cardExpanded : cardCollapsed);
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
		if (collapsedRenderer != null)
			collapsedRenderer.setSelected(selected);
	}

	public void editName()
	{
		if (expanded)
			expandedRenderer.editName();
		else if (collapsedEditableSorterPrefixComponent != null)
			collapsedEditableSorterPrefixComponent.edit();
	}

	private void delete() throws InterruptedException
	{
		getContextJTree().deleteSorter(sorter);
	}

	private void deleteCascade() throws InterruptedException
	{
		getContextJTree().deleteSorterCascade(sorter);
	}

}
