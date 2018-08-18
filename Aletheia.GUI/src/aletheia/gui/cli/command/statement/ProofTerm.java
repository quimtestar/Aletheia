/*******************************************************************************
 * Copyright (c) 2018 Quim Testar.
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
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "proofterm", groupPath = "/statement", factory = ProofTerm.Factory.class)
public class ProofTerm extends TransactionalCommand
{
	private final Statement statement;

	public ProofTerm(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		if (statement instanceof Assumption)
			throw new Exception("Statement is a assumption");
		if (!statement.isProved())
			throw new Exception("Not proven");
		Term proofTerm = statement.proofTerm(getTransaction());
		if (proofTerm == null)
			throw new Exception("No proof");
		printTerm(getActiveContext(), getTransaction(), proofTerm);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<ProofTerm>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public ProofTerm parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Statement statement;
				if (split.size() > 0)
					statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
				else
				{
					statement = from.getActiveContext();
					if (statement == null)
						throw new NotActiveContextException();
				}
				return new ProofTerm(from, transaction, statement);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<statement>]";
		}

		@Override
		public String shortHelp()
		{
			return "Computes the proof term of this statement.";
		}

	}

}
