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
package aletheia.gui.cli.command.local;

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "subscribe", groupPath = "/local", factory = SubscribeCommand.Factory.class)
public abstract class SubscribeCommand extends TransactionalCommand
{
	private final Statement statement;

	public SubscribeCommand(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	public static class Factory extends AbstractVoidCommandFactory<SubscribeCommand>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public SubscribeCommand parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			switch (split.get(1))
			{
			case "statements":
				if (!(statement instanceof Context))
					throw new CommandParseException("Not a context");
				return new SubscribeStatementsCommand(from, transaction, (Context) statement);
			case "proof":
				return new SubscribeProofCommand(from, transaction, statement);
			case "none":
				return new SubscribeNoneCommand(from, transaction, statement);
			default:
				throw new CommandParseException("Bad subcommand");
			}
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> ( statements | proof | none )";
		}

		@Override
		public String shortHelp()
		{
			return "Subscribe a context for statements, a statement for proofs or unsubscribe.";
		}

	}

}
