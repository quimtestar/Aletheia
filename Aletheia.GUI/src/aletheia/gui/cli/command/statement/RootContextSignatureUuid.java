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

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "rcsu", groupPath = "/statement", factory = RootContextSignatureUuid.Factory.class)
public class RootContextSignatureUuid extends TransactionalCommand
{
	private final RootContext rootContext;

	public RootContextSignatureUuid(CommandSource from, Transaction transaction, RootContext rootContext)
	{
		super(from, transaction);
		this.rootContext = rootContext;
	}

	public class NotSignedCommandException extends CommandException
	{
		private static final long serialVersionUID = -5413482467035136175L;

		private NotSignedCommandException()
		{
			super("Root context not signed");
		}
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		UUID signatureUuid = rootContext.getSignatureUuid(getTransaction());
		if (signatureUuid == null)
			throw new NotSignedCommandException();
		getOut().println(rootContext.getSignatureUuid(getTransaction()));
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<RootContextSignatureUuid>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public RootContextSignatureUuid parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Bad statement path: " + split.get(0));
			if (!(statement instanceof RootContext))
				throw new CommandParseException("Not a root context: " + split.get(0));
			return new RootContextSignatureUuid(from, transaction, (RootContext) statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<root context>";
		}

		@Override
		public String shortHelp()
		{
			return "Returns the signature UUID of a given root context.";
		}

	}

}
