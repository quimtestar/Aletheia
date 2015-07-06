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
package aletheia.gui.contextjtree.renderer;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.TransferHandler;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.contextjtree.ContextJTree;
import aletheia.log4j.LoggerManager;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.identifier.Identifier;
import aletheia.model.local.ContextLocal;
import aletheia.model.local.StatementLocal;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.CombinedMap;

public abstract class StatementContextJTreeNodeRenderer<S extends Statement> extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = 8638570524486699014L;
	private static final Logger logger = LoggerManager.instance.logger();

	private final S statement;
	private final StatementAuthority statementAuthority;
	private final StatementLocal statementLocal;

	private final EditableTextLabelComponent editableTextLabelComponent;
	@SuppressWarnings("unused")
	private final JLabel localLabel;

	private class Listener implements KeyListener, MouseListener
	{

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_F2:
				editName();
				break;
			case KeyEvent.VK_DELETE:
				try
				{
					if (ev.isShiftDown())
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
				if (statement instanceof RootContext)
					cliJPanel.setActiveContext((Context) statement);
				else
				{
					Transaction transaction = statement.getPersistenceManager().beginTransaction();
					try
					{
						cliJPanel.setActiveContext(statement.getContext(transaction));
					}
					finally
					{
						transaction.abort();
					}
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
			if ((draggable && (e.getModifiers() & MouseEvent.MOUSE_PRESSED) != 0))
				getContextJTree().getTransferHandler().exportAsDrag(getContextJTree(), e, TransferHandler.COPY);
		}
	}

	private JLabel addProofLabel()
	{
		JLabel jLabel;
		if (statement.isProved())
			jLabel = addTickLabel();
		else
			jLabel = addQuestionMarkLabel();
		return jLabel;
	}

	private JLabel addAuthorityLabel()
	{
		JLabel jLabel;
		if (statementAuthority != null)
			jLabel = addSignatureStatusSymbolLabel(statementAuthority.signatureStatus());
		else
			jLabel = addSpaceLabel();
		return jLabel;
	}

	private JLabel addLocalLabel()
	{
		JLabel jLabel;
		if (statementLocal != null)
		{
			if (statementLocal.isSubscribeProof())
				jLabel = addSubscribeProofSymbolLabel();
			else if ((statementLocal instanceof ContextLocal) && ((ContextLocal) statementLocal).isSubscribeStatements())
				jLabel = addSubscribeStatementsSymbolLabel();
			else
				jLabel = addSpaceLabel();
		}
		else
			jLabel = addSpaceLabel();
		jLabel.addMouseListener(new MouseListener()
		{

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() >= 2)
				{
					Transaction transaction = getContextJTree().getModel().beginTransaction();
					try
					{
						int l = 0;
						if (statementLocal != null)
						{
							if (statementLocal instanceof ContextLocal)
							{
								ContextLocal contextLocal = (ContextLocal) statementLocal;
								if (contextLocal.isSubscribeStatements())
									l = 1;
							}
							if (statementLocal.isSubscribeProof())
								l = 2;
						}

						StatementLocal statementLocal_ = getStatement().getOrCreateLocal(transaction);
						if (statementLocal_ instanceof ContextLocal)
						{
							ContextLocal contextLocal_ = (ContextLocal) statementLocal_;
							if (l == 0)
							{
								contextLocal_.setSubscribeStatements(true);
								contextLocal_.setSubscribeProof(false);
							}
							else if (l == 1)
							{
								contextLocal_.setSubscribeStatements(false);
								contextLocal_.setSubscribeProof(true);
							}
							else if (l == 2)
							{
								contextLocal_.setSubscribeStatements(false);
								contextLocal_.setSubscribeProof(false);
							}
							else
								throw new Error();
						}
						else
						{
							if (l == 0)
								statementLocal_.setSubscribeProof(true);
							else if (l == 2)
								statementLocal_.setSubscribeProof(false);
							else
								throw new Error();
						}
						statementLocal_.persistenceUpdate(transaction);
						transaction.commit();
					}
					finally
					{
						transaction.abort();
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}
		});
		jLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return jLabel;
	}

	private final Listener listener;

	private Map<? extends VariableTerm, Identifier> variableToIdentifier(Transaction transaction, S statement)
	{
		Map<? extends VariableTerm, Identifier> parentVariableToIdentifier = statement.parentVariableToIdentifier(transaction);
		if (statement instanceof Context)
		{
			Map<ParameterVariableTerm, Identifier> localVariableToIdentifier = new HashMap<ParameterVariableTerm, Identifier>();
			{
				Term body = statement.getTerm();
				Iterator<Assumption> assumptionIterator = ((Context) statement).assumptions(transaction).iterator();
				while (body instanceof FunctionTerm)
				{
					FunctionTerm function = (FunctionTerm) body;
					Assumption assumption = null;
					if (assumptionIterator.hasNext())
						assumption = assumptionIterator.next();
					if (function.getBody().freeVariables().contains(function.getParameter()) && assumption != null && assumption.getIdentifier() != null)
						localVariableToIdentifier.put(function.getParameter(), assumption.getIdentifier());
					body = function.getBody();
				}
			}
			return new CombinedMap<VariableTerm, Identifier>(new AdaptedMap<VariableTerm, Identifier>(localVariableToIdentifier),
					new AdaptedMap<VariableTerm, Identifier>(parentVariableToIdentifier));
		}
		return parentVariableToIdentifier;
	}

	protected StatementContextJTreeNodeRenderer(ContextJTree contextJTree, S statement)
	{
		super(contextJTree);
		Transaction transaction = contextJTree.getModel().beginTransaction();
		try
		{
			this.statement = statement;
			this.statementAuthority = statement.getAuthority(transaction);
			this.statementLocal = statement.getLocal(transaction);
			addProofLabel();
			addAuthorityLabel();
			this.localLabel = addLocalLabel();
			addSpaceLabel();
			this.editableTextLabelComponent = addEditableTextLabelComponent();
			addColonLabel();
			addTerm(variableToIdentifier(transaction, statement), statement.getTerm());

			this.listener = new Listener();
			addKeyListener(listener);
			addMouseListener(listener);
		}
		finally
		{
			transaction.abort();
		}
	}

	public S getStatement()
	{
		return statement;
	}

	protected abstract EditableTextLabelComponent addEditableTextLabelComponent();

	public void editName()
	{
		editableTextLabelComponent.edit();
	}

	private void delete() throws InterruptedException
	{
		getContextJTree().deleteStatement(statement);
	}

	private void deleteCascade() throws InterruptedException
	{
		getContextJTree().deleteStatementCascade(statement);
	}

}
