/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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
package aletheia.gui.contextjtree;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import aletheia.gui.app.MainAletheiaJFrame;
import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.authority.AuthorityJPanel;
import aletheia.gui.common.DraggableJScrollPane;
import aletheia.gui.common.FocusBorderManager;
import aletheia.gui.contextjtree.sorter.Sorter;
import aletheia.model.statement.Statement;
import aletheia.persistence.PersistenceManager;
import aletheia.utilities.gui.MyJSplitPane;

public class ContextJTreeJPanel extends JPanel
{
	private static final long serialVersionUID = -720616063280427500L;

	private final AletheiaJPanel aletheiaJPanel;
	private ContextJTree contextJTree;
	private JScrollPane contextJTreeScrollPane;
	private FocusBorderManager contextJTreeFocusBorderManager;
	private final AuthorityJPanel authorityJPanel;
	private final MyJSplitPane splitPane1;

	public ContextJTreeJPanel(AletheiaJPanel aletheiaJPanel)
	{
		super();
		this.aletheiaJPanel = aletheiaJPanel;
		this.contextJTree = new ContextJTree(aletheiaJPanel);
		this.contextJTreeScrollPane = new DraggableJScrollPane(this.contextJTree);
		this.contextJTreeFocusBorderManager = new FocusBorderManager(contextJTreeScrollPane, contextJTree);
		this.authorityJPanel = new AuthorityJPanel(this);
		this.splitPane1 = new MyJSplitPane(JSplitPane.VERTICAL_SPLIT, contextJTreeScrollPane, authorityJPanel);
		this.splitPane1.setResizeWeight(1);
		this.splitPane1.setDividerLocationOrCollapseWhenValid(1d);
		this.splitPane1.setOneTouchExpandable(true);
		setLayout(new BorderLayout());
		add(splitPane1, BorderLayout.CENTER);
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	public MainAletheiaJFrame getAletheiaJFrame()
	{
		return aletheiaJPanel.getAletheiaJFrame();
	}

	public PersistenceManager getPersistenceManager()
	{
		return aletheiaJPanel.getPersistenceManager();
	}

	public synchronized ContextJTree getContextJTree()
	{
		return contextJTree;
	}

	public synchronized JScrollPane getContextJTreeScrollPane()
	{
		return contextJTreeScrollPane;
	}

	public synchronized void close() throws InterruptedException
	{
		contextJTree.close();
		contextJTreeFocusBorderManager.close();
		authorityJPanel.close();
	}

	public synchronized void updateFontSize()
	{
		contextJTree.updateFontSize();
		authorityJPanel.updateFontSize();
	}

	public void selectStatement(final Statement statement)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (ContextJTreeJPanel.this)
				{
					contextJTree.selectStatement(statement, false);
					contextJTree.expandStatement(statement);
					contextJTree.scrollStatementToVisible(statement);
				}
			}

		});
	}

	public void selectSorter(final Sorter sorter)
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (ContextJTreeJPanel.this)
				{
					contextJTree.selectSorter(sorter, false);
					contextJTree.scrollSorterToVisible(sorter);
				}
			}

		});
	}

	public synchronized void resetContextJTree() throws InterruptedException
	{
		Sorter selected = contextJTree.getSelectedSorter();
		contextJTree.close();
		contextJTreeFocusBorderManager.close();
		contextJTree = new ContextJTree(aletheiaJPanel);
		if (selected != null)
			selectSorter(selected);
		contextJTreeScrollPane = new JScrollPane(contextJTree);
		contextJTreeFocusBorderManager = new FocusBorderManager(contextJTreeScrollPane, contextJTree);
		double dl = splitPane1.getProportionalDividerLocation();
		splitPane1.setTopComponent(contextJTreeScrollPane);
		splitPane1.setDividerLocationOrCollapse(dl);

		authorityJPanel.updatedContextJTree(contextJTree);
	}

}
