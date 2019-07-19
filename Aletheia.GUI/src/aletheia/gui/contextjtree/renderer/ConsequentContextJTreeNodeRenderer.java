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
package aletheia.gui.contextjtree.renderer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.TransferHandler;

import org.apache.logging.log4j.Logger;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.log4j.LoggerManager;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ConsequentContextJTreeNodeRenderer extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = 8932737516919384939L;
	private static final Logger logger = LoggerManager.instance.logger();

	private final Context context;

	private class Listener implements KeyListener, MouseListener
	{

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_F3:
			{
				getContextJTree().getAletheiaJPanel().getCliJPanel().setActiveContext(context);
				break;
			}
			case KeyEvent.VK_DELETE:
			{
				try
				{
					if (ev.isControlDown())
						undelete();
					else if (ev.isShiftDown())
						deleteCascade();
					else
						delete();
				}
				catch (InterruptedException e1)
				{
					logger.error(e1.getMessage(), e1);
				}
				break;
			}
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
			if ((draggable && (e.getModifiersEx() & MouseEvent.MOUSE_PRESSED) != 0))
				getContextJTree().getTransferHandler().exportAsDrag(getContextJTree(), e, TransferHandler.COPY);
		}

	}

	private final JLabel turnstile;

	public ConsequentContextJTreeNodeRenderer(ContextJTree contextJTree, Context context)
	{
		super(true, contextJTree);
		Transaction transaction = contextJTree.getModel().beginTransaction();
		try
		{
			this.context = context;
			this.turnstile = addTurnstileLabel();
			addSpaceLabel();
			addTerm(context.variableToIdentifier(transaction), context.consequentParameterIdentification(transaction), context.getConsequent());
			setActiveFont(getItalicFont());
			addSpaceLabel();
			addOpenSquareBracket();
			boolean first = true;
			for (Statement st : context.solvers(transaction))
			{
				if (!first)
				{
					addCommaLabel();
					addSpaceLabel();
				}
				else
					first = false;
				addTerm(context.variableToIdentifier(transaction), st.getVariable());
			}
			addCloseSquareBracket();
			Listener listener = new Listener();
			addKeyListener(listener);
			addMouseListener(listener);
		}
		finally
		{
			transaction.abort();
		}
	}

	private void delete() throws InterruptedException
	{
		getContextJTree().deleteStatement(context);
	}

	private void deleteCascade() throws InterruptedException
	{
		getContextJTree().deleteStatementCascade(context);
	}

	private void undelete() throws InterruptedException
	{
		getContextJTree().undelete();
	}

	public void setActiveContext(boolean activeContext)
	{
		turnstile.setForeground(activeContext ? getActiveContextColor() : getTurnstileColor());
	}

}
