/*******************************************************************************
 * Copyright (c) 2014, 2019 Quim Testar.
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

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.nomenclator.Nomenclator.NomenclatorException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Context.StatementNotInContextException;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "idas", groupPath = "/statement", factory = IdentifyAssumption.Factory.class)
public class IdentifyAssumption extends TransactionalCommand
{
	private final Identifier identifier;
	private final Statement statement;

	protected IdentifyAssumption(CommandSource from, Transaction transaction, Identifier identifier, Statement statement)
	{
		super(from, transaction);
		this.identifier = identifier;
		this.statement = statement;
	}

	protected Identifier getIdentifier()
	{
		return identifier;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws StatementNotInContextException, NomenclatorException
	{
		statement.identify(getTransaction(), identifier);
		Context newActiveContext;
		if (statement instanceof Context)
			newActiveContext = (Context) statement;
		else
			newActiveContext = statement.getContext(getTransaction());
		putSelectStatement(getTransaction(), statement);
		return new RunTransactionalReturnData(newActiveContext);
	}

	public static class Factory extends AbstractVoidCommandFactory<IdentifyAssumption>
	{

		@Override
		public IdentifyAssumption parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Identifier identifier = Identifier.parse(split.get(0));
				int i = Integer.parseInt(split.get(1));
				if (from.getActiveContext() == null)
					throw new NotActiveContextException();
				Statement statement = from.getActiveContext().assumptions(transaction).get(i);
				return new IdentifyAssumption(from, transaction, identifier, statement);
			}
			catch (InvalidNameException | NotActiveContextException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
		}

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		protected String paramSpec()
		{
			return "<identifier> <assumption #>";
		}

		@Override
		public String shortHelp()
		{
			return "Assigns an identifier to an unidentified assumption of the active context by its number.";
		}

	}

}
