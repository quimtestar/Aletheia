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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public class ConsequentContextJTreeNodeRenderer extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = 8932737516919384939L;

	private final Context context;

	private class MyKeyListener implements KeyListener
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

	public ConsequentContextJTreeNodeRenderer(ContextJTree contextJTree, Context context)
	{
		super(contextJTree);
		Transaction transaction = contextJTree.getModel().beginTransaction();
		try
		{
			this.context = context;
			addTurnstileLabel();
			addSpaceLabel();
			addTerm(context.variableToIdentifier(transaction), context.getConsequent());
			setActiveFont(getItalicFont());
			addSpaceLabel();
			addOpenBracket();
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
			addCloseBracket();
			addKeyListener(new MyKeyListener());
		}
		finally
		{
			transaction.abort();
		}
	}

}
