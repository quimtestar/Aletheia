/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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

import aletheia.gui.app.AletheiaJFrame;
import aletheia.gui.app.AletheiaJPanel;
import aletheia.gui.authority.AuthorityJPanel;
import aletheia.persistence.PersistenceManager;
import aletheia.utilities.gui.MyJSplitPane;

public class ContextJTreeJPanel extends JPanel
{
	private static final long serialVersionUID = -720616063280427500L;

	private final AletheiaJPanel aletheiaJPanel;
	private final ContextJTree contextJTree;
	private final JScrollPane contextJTreeScrollPane;
	private final AuthorityJPanel authorityJPanel;
	private final MyJSplitPane splitPane1;

	public ContextJTreeJPanel(AletheiaJPanel aletheiaJPanel)
	{
		super();
		this.aletheiaJPanel = aletheiaJPanel;
		this.contextJTree = new ContextJTree(aletheiaJPanel);
		this.contextJTreeScrollPane = new JScrollPane(this.contextJTree);
		this.authorityJPanel = new AuthorityJPanel(this);
		this.splitPane1 = new MyJSplitPane(JSplitPane.VERTICAL_SPLIT, contextJTreeScrollPane, authorityJPanel);
		this.splitPane1.setResizeWeight(1);
		this.splitPane1.setDividerLocationOrExpandWhenValid(1d);
		this.splitPane1.setOneTouchExpandable(true);
		setLayout(new BorderLayout());
		add(splitPane1, BorderLayout.CENTER);
	}

	public AletheiaJPanel getAletheiaJPanel()
	{
		return aletheiaJPanel;
	}

	public AletheiaJFrame getAletheiaJFrame()
	{
		return aletheiaJPanel.getAletheiaJFrame();
	}

	public PersistenceManager getPersistenceManager()
	{
		return aletheiaJPanel.getPersistenceManager();
	}

	public ContextJTree getContextJTree()
	{
		return contextJTree;
	}

	public void close() throws InterruptedException
	{
		contextJTree.close();
		authorityJPanel.close();
	}

	public void updateFontSize()
	{
		contextJTree.updateFontSize();
		authorityJPanel.updateFontSize();
	}

}
