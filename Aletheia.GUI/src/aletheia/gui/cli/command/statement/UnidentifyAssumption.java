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
import aletheia.model.nomenclator.Nomenclator.SignatureIsValidNomenclatorException;
import aletheia.model.nomenclator.Nomenclator.UnknownIdentifierException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "unidas", groupPath = "/statement", factory = UnidentifyAssumption.Factory.class)
public class UnidentifyAssumption extends TransactionalCommand
{
	private final Statement statement;

	public UnidentifyAssumption(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws UnknownIdentifierException, SignatureIsValidNomenclatorException
	{
		statement.unidentify(getTransaction());
		Context newActiveContext;
		if (statement instanceof Context)
			newActiveContext = (Context) statement;
		else
			newActiveContext = statement.getContext(getTransaction());
		putSelectStatement(getTransaction(), statement);
		return new RunTransactionalReturnData(newActiveContext);

	}

	public static class Factory extends AbstractVoidCommandFactory<UnidentifyAssumption>
	{

		@Override
		public UnidentifyAssumption parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				int i = Integer.parseInt(split.get(0));
				if (from.getActiveContext() == null)
					throw new NotActiveContextException();
				Statement statement = from.getActiveContext().assumptions(transaction).get(i);
				return new UnidentifyAssumption(from, transaction, statement);
			}
			catch (NotActiveContextException e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
		}

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		protected String paramSpec()
		{
			return "<assumption#>";
		}

		@Override
		public String shortHelp()
		{
			return "Delete the identificator assigned to an assumption in the active context given by its ordinal number.";
		}

	}

}
