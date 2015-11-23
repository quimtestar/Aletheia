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
package aletheia.gui.cli.command.statement;

import java.util.List;
import java.util.UUID;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.cli.command.gui.SelectContext;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "sel", groupPath = "/statement", factory = SelectStatement.Factory.class)
public class SelectStatement extends TransactionalCommand
{
	private final Statement statement;

	public SelectStatement(CliJPanel from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		pushSelectStatement(getTransaction(), statement);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<SelectStatement>
	{

		@Override
		public SelectStatement parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			Statement statement;
			if (split.size() > 0)
			{
				if (split.get(0).startsWith("$"))
					statement = cliJPanel.getActiveContext().getStatementByHexRef(transaction, split.get(0));
				else
					try
					{
						statement = cliJPanel.getPersistenceManager().getStatement(transaction, UUID.fromString(split.get(0)));
					}
					catch (IllegalArgumentException e)
					{
						statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
					}
			}
			else
				statement = cliJPanel.getActiveContext();
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			if (statement instanceof Context)
				return new SelectContext(cliJPanel, transaction, (Context) statement);
			else
				return new SelectStatement(cliJPanel, transaction, statement);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "[<statement> | <UUID>]";
		}

		@Override
		public String shortHelp()
		{
			return "Selects a statement (the given one or the active context) on the context tree panel.";
		}

	}

}
