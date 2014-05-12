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
package aletheia.gui.signaturerequestjtree;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.TransferHandler;

import aletheia.gui.common.PersistentJTreeNodeRenderer;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;

public abstract class SignatureRequestTreeNodeRenderer extends PersistentJTreeNodeRenderer
{
	private static final long serialVersionUID = -4153973298932177782L;
	private final SignatureRequestTreeNode node;

	protected class Listener implements KeyListener, MouseListener
	{

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_F3:
			{
				SignatureRequestTreeNode node = getNode();
				if (node != null)
				{
					Context context = node.getContext();
					if (context != null)
						getPersistentJTree().getAletheiaJPanel().getCliJPanel().setActiveContext(context);
				}
				break;
			}
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				getPersistentJTree().cancelEditing();
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
				getPersistentJTree().getTransferHandler().exportAsDrag(getPersistentJTree(), e, TransferHandler.COPY);
		}
	}

	public SignatureRequestTreeNodeRenderer(SignatureRequestJTree signatureRequestJTree, SignatureRequestTreeNode node)
	{
		super(signatureRequestJTree, true);
		this.node = node;
		Listener listener = makeListener();
		addKeyListener(listener);
		addMouseListener(listener);
	}

	protected Listener makeListener()
	{
		return new Listener();
	}

	@Override
	protected SignatureRequestJTree getPersistentJTree()
	{
		return (SignatureRequestJTree) super.getPersistentJTree();
	}

	protected SignatureRequestTreeNode getNode()
	{
		return node;
	}

	@Override
	protected void mouseClickedOnVariableReference(VariableTerm variable, MouseEvent ev)
	{
		Transaction transaction = beginTransaction();
		try
		{
			Statement statement = getPersistenceManager().statements(transaction).get(variable);
			if (statement != null)
				getPersistentJTree().getAletheiaJPanel().getContextJTree().selectStatement(statement, true);
		}
		finally
		{
			transaction.abort();
		}
	}

}
