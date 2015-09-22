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
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "hc", groupPath = "/statement", factory = HighestContext.Factory.class)
public class HighestContext extends TransactionalCommand
{
	private final Statement statement;

	public HighestContext(CliJPanel from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	public class NotProvenCommandException extends CommandException
	{
		private static final long serialVersionUID = 3412924922166066315L;

		private NotProvenCommandException()
		{
			super("Not proven.");
		}

	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		if (!statement.isProved())
			throw new NotProvenCommandException();
		Context ctx = statement.highestContext(getTransaction());
		if (!ctx.equals(statement.getContext(getTransaction())))
			getOut().println(ctx.label());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<HighestContext>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public HighestContext parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
			return new HighestContext(cliJPanel, transaction, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Checks if a proved statement can be transported to a higher context (keeping its provenness).";
		}

	}

}
