/*******************************************************************************
 * Copyright (c) 2015 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.gui.contextjtree.renderer;

import java.awt.CardLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.gui.contextjtree.node.StatementContextJTreeNode;
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

	private class EditableSorterPrefixComponent extends EditableTextLabelComponent implements EditableComponent
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
			addSpaceLabel(3);
			addExpandedGroupSorterLabel();
			this.editableSorterPrefixComponent = new EditableSorterPrefixComponent();
			addEditableComponent(editableSorterPrefixComponent);
			add(editableSorterPrefixComponent);
		}

		public void editName()
		{
			editableSorterPrefixComponent.edit();
		}

	}

	private class CollapsedRenderer extends ContextJTreeNodeRenderer
	{
		private static final long serialVersionUID = -1379926465808260318L;

		private final EditableSorterPrefixComponent editableSorterPrefixComponent;
		private final StatementContextJTreeNodeRenderer statementContextJTreeNodeRenderer;
		@SuppressWarnings("unused")
		private final StatementContextJTreeNode statementContextJTreeNode;

		private class MyStatementContextJTreeNodeRenderer extends StatementContextJTreeNodeRenderer
		{
			private static final long serialVersionUID = 2182247478964360918L;

			protected MyStatementContextJTreeNodeRenderer(ContextJTree contextJTree, Statement statement)
			{
				super(contextJTree, statement);
			}

			@Override
			protected EditableSorterPrefixComponent addEditableTextLabelComponent()
			{
				addCollapsedGroupSorterLabel();
				addEditableComponent(editableSorterPrefixComponent);
				add(editableSorterPrefixComponent);
				return editableSorterPrefixComponent;

			}
		}

		public CollapsedRenderer(ContextJTree contextJTree)
		{
			super(contextJTree);
			this.editableSorterPrefixComponent = new EditableSorterPrefixComponent();
			Transaction transaction = getContextJTree().getModel().beginTransaction();
			try
			{
				Statement statement = sorter.getStatement(transaction);
				if (statement != null)
				{
					this.statementContextJTreeNodeRenderer = new MyStatementContextJTreeNodeRenderer(contextJTree, statement);
					statementContextJTreeNode = contextJTree.getModel().getNodeMap().getByStatement(statement);
					add(statementContextJTreeNodeRenderer);
				}
				else
				{
					this.statementContextJTreeNodeRenderer = null;
					statementContextJTreeNode = null;
					addSpaceLabel(3);
					addCollapsedGroupSorterLabel();
					addEditableComponent(editableSorterPrefixComponent);
					add(editableSorterPrefixComponent);
				}
			}
			finally
			{
				transaction.abort();
			}
		}

		public void editName()
		{
			editableSorterPrefixComponent.edit();
		}

		@Override
		public void setSelected(boolean selected)
		{
			super.setSelected(selected);
			if (statementContextJTreeNodeRenderer != null)
				statementContextJTreeNodeRenderer.setSelected(selected);
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

		@Override
		public void keyTyped(KeyEvent e)
		{
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
		}

		boolean draggable = false;

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			draggable = true;
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			draggable = false;
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			draggable = false;
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			if ((draggable && (e.getModifiers() & MouseEvent.MOUSE_PRESSED) != 0))
				getContextJTree().getTransferHandler().exportAsDrag(getContextJTree(), e, TransferHandler.COPY);
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

	public void editName()
	{
		if (expanded)
			expandedRenderer.editName();
		else
			collapsedRenderer.editName();
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
