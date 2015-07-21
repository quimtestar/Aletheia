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

import java.awt.event.KeyEvent;

import org.apache.logging.log4j.Logger;

import aletheia.gui.contextjtree.ContextJTree;
import aletheia.log4j.LoggerManager;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

public abstract class ProperStatementContextJTreeNodeRenderer<S extends Statement> extends StatementContextJTreeNodeRenderer<S>
{
	private static final long serialVersionUID = -7502917941770656695L;
	private static final Logger logger = LoggerManager.instance.logger();

	private class EditableStatementIdentifierComponent extends EditableTextLabelComponent implements EditableComponent
	{
		private static final long serialVersionUID = -4548549900954285255L;

		public EditableStatementIdentifierComponent()
		{
			super(getDefaultColor());
			Transaction transaction = beginTransaction();
			try
			{
				Identifier id = getStatement().identifier(transaction);
				String idString;
				if (id != null)
					idString = id.toString();
				else
					idString = getStatement().getVariable().toString();
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
				Identifier id = getStatement().identifier(transaction);
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
					getContextJTree().editStatementName(getStatement(), getFieldText().trim());
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

	protected ProperStatementContextJTreeNodeRenderer(ContextJTree contextJTree, S statement)
	{
		super(contextJTree, statement);
	}

	@Override
	protected EditableTextLabelComponent addEditableTextLabelComponent()
	{
		addSpaceLabel();
		EditableTextLabelComponent editableTextLabelComponent = new EditableStatementIdentifierComponent();
		addEditableComponent(editableTextLabelComponent);
		add(editableTextLabelComponent);
		return editableTextLabelComponent;

	}

	public static ProperStatementContextJTreeNodeRenderer<?> renderer(ContextJTree contextJTree, Statement statement)
	{
		if (statement == null)
			return null;
		else if (statement instanceof Assumption)
			return new AssumptionContextJTreeNodeRenderer(contextJTree, (Assumption) statement);
		else if (statement instanceof Context)
			return ContextContextJTreeNodeRenderer.renderer(contextJTree, (Context) statement);
		else if (statement instanceof Specialization)
			return new SpecializationContextJTreeNodeRenderer(contextJTree, (Specialization) statement);
		else
			throw new Error();
	}

}
