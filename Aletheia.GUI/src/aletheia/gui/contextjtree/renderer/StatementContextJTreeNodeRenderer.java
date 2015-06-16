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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import aletheia.model.statement.Declaration;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.statement.UnfoldingContext;
import aletheia.persistence.Transaction;

public abstract class StatementContextJTreeNodeRenderer extends ContextJTreeNodeRenderer
{
	private static final long serialVersionUID = 8638570524486699014L;
	private static final Logger logger = LoggerManager.instance.logger();

	private final Statement statement;
	private final StatementAuthority statementAuthority;
	private final StatementLocal statementLocal;

	protected class EditableStatementIdentifierComponent extends EditableTextLabelComponent implements EditableComponent
	{
		private static final long serialVersionUID = -4548549900954285255L;

		public EditableStatementIdentifierComponent()
		{
			super(getDefaultColor());
			Transaction transaction = beginTransaction();
			try
			{
				Identifier id = statement.identifier(transaction);
				String idString;
				if (id != null)
					idString = id.toString();
				else
					idString = statement.getVariable().toString();
				setLabelText(idString);
				if (id != null)
					setFieldText(id.toString());
			}
			finally
			{
				transaction.abort();
			}
		}

		private void resetTextField()
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				Identifier id = statement.identifier(transaction);
				if (id != null)
					setFieldText(id.toString());
				else
					setFieldText("");
			}
			finally
			{
				transaction.abort();
			}
		}

		@Override
		public void keyPressed(KeyEvent ev)
		{
			switch (ev.getKeyCode())
			{
			case KeyEvent.VK_ENTER:
				try
				{
					getContextJTree().editStatementName(statement, getFieldText().trim());
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
		return jLabel;
	}

	private final Listener listener;

	protected StatementContextJTreeNodeRenderer(ContextJTree contextJTree, Statement statement, EditableTextLabelComponent editableTextLabelComponent)
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
			this.editableTextLabelComponent = addEditableTextLabelComponent(editableTextLabelComponent);
			addColonLabel();
			addTerm(statement.parentVariableToIdentifier(transaction), statement.getTerm());

			this.listener = new Listener();
			addKeyListener(listener);
			addMouseListener(listener);
		}
		finally
		{
			transaction.abort();
		}
	}

	public Statement getStatement()
	{
		return statement;
	}

	protected EditableTextLabelComponent addEditableTextLabelComponent(EditableTextLabelComponent editableTextLabelComponent)
	{
		if (editableTextLabelComponent == null)
			editableTextLabelComponent = new EditableStatementIdentifierComponent();
		addEditableComponent(editableTextLabelComponent);
		add(editableTextLabelComponent);
		return editableTextLabelComponent;
	}

	public static StatementContextJTreeNodeRenderer renderer(ContextJTree contextJTree, Statement statement)
	{
		return renderer(contextJTree, statement, null);
	}

	protected static StatementContextJTreeNodeRenderer renderer(ContextJTree contextJTree, Statement statement,
			EditableTextLabelComponent editableTextLabelComponent)
	{
		if (statement == null)
			return null;
		else if (statement instanceof Assumption)
			return new AssumptionContextJTreeNodeRenderer(contextJTree, (Assumption) statement, editableTextLabelComponent);
		else if (statement instanceof UnfoldingContext)
			return new UnfoldingContextContextJTreeNodeRenderer(contextJTree, (UnfoldingContext) statement, editableTextLabelComponent);
		else if (statement instanceof RootContext)
			return new RootContextContextJTreeNodeRenderer(contextJTree, (RootContext) statement, editableTextLabelComponent);
		else if (statement instanceof Context)
			return new ContextContextJTreeNodeRenderer(contextJTree, (Context) statement, editableTextLabelComponent);
		else if (statement instanceof Declaration)
			return new DeclarationContextJTreeNodeRenderer(contextJTree, (Declaration) statement, editableTextLabelComponent);
		else if (statement instanceof Specialization)
			return new SpecializationContextJTreeNodeRenderer(contextJTree, (Specialization) statement, editableTextLabelComponent);
		else
			throw new Error();
	}

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
