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

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "delete", groupPath = "/statement", factory = DeleteStatement.Factory.class)
public class DeleteStatement extends TransactionalCommand
{
	private final Statement statement;

	public DeleteStatement(CliJPanel from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	protected boolean isCascade()
	{
		return false;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		statement.delete(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeleteStatement>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public DeleteStatement parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			if (split.size() > 1 && split.get(1).equals("cascade"))
				return new DeleteStatementCascade(cliJPanel, transaction, statement);
			else
				return new DeleteStatement(cliJPanel, transaction, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [cascade]";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes a statement from its context.";
		}

	}

}
