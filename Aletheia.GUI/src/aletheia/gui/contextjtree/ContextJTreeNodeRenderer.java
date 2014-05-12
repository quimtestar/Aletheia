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

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import aletheia.gui.common.PersistentJTreeNodeRenderer;
import aletheia.model.statement.Statement;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;

public abstract class ContextJTreeNodeRenderer extends PersistentJTreeNodeRenderer
{
	private static final long serialVersionUID = -5450326554560954995L;

	private class TreeNodeRendererKeyListener implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				getContextJTree().cancelEditing();
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ev);
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent ev)
		{
		}

		@Override
		public void keyTyped(KeyEvent ev)
		{
		}

	}

	public ContextJTreeNodeRenderer(ContextJTree contextJTree)
	{
		super(contextJTree, true);
		addKeyListener(new TreeNodeRendererKeyListener());
	}

	@Override
	protected ContextJTree getPersistentJTree()
	{
		return (ContextJTree) super.getPersistentJTree();
	}

	protected ContextJTree getContextJTree()
	{
		return getPersistentJTree();
	}

	@Override
	protected void mouseClickedOnVariableReference(VariableTerm variable, MouseEvent ev)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			Statement statement = getPersistenceManager().statements(transaction).get(variable);
			if (statement != null)
				getContextJTree().selectStatement(statement, true);
		}
		finally
		{
			transaction.abort();
		}

	}

}
