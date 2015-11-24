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
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "id", groupPath = "/statement", factory = IdentifyStatement.Factory.class)
public class IdentifyStatement extends TransactionalCommand
{
	private final Statement statement;
	private final Identifier identifier;

	public IdentifyStatement(CommandSource from, Transaction transaction, Statement statement, Identifier identifier)
	{
		super(from, transaction);
		this.statement = statement;
		this.identifier = identifier;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Identifier oldId = statement.identifier(getTransaction());
		if ((oldId == null && identifier != null) || (oldId != null && identifier == null)
				|| (oldId != null && identifier != null && !oldId.equals(identifier)))
		{
			if (oldId != null)
				statement.unidentify(getTransaction());
			if (identifier != null)
				statement.identify(getTransaction(), identifier);
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<IdentifyStatement>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public IdentifyStatement parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Statement statement = findStatementPath(cliJPanel.getPersistenceManager(), transaction, cliJPanel.getActiveContext(), split.get(0));
				Identifier identifier = null;
				if (split.size() > 1)
					identifier = Identifier.parse(split.get(1));
				return new IdentifyStatement(cliJPanel, transaction, statement, identifier);

			}
			catch (InvalidNameException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}

		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [<identifier>]";
		}

		@Override
		public String shortHelp()
		{
			return "Gives an identifier to a statement (or clears the existing one).";
		}

	}

}
