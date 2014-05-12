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

@TaggedCommand(tag = "abspath", groupPath = "/statement", factory = StatementAbsolutePath.Factory.class)
public class StatementAbsolutePath extends TransactionalCommand
{
	private final Statement statement;

	public StatementAbsolutePath(CliJPanel from, Transaction transaction, Statement statement)
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
		getOut().println(statement.statementPathString(getTransaction()));
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<StatementAbsolutePath>
	{

		@Override
		public StatementAbsolutePath parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement;
			if (split.size() > 0)
			{
				statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
				if (statement == null)
					throw new CommandParseException("Bad statement path: " + split.get(0));
			}
			else
				statement = cliJPanel.getActiveContext();
			return new StatementAbsolutePath(cliJPanel, transaction, statement);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Returns the absolute path of the given statement.";
		}

	}

}
