/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.gui.cli.command.extra.statement;

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "rebuild", groupPath = "/statement", factory = RebuildProved.Factory.class)
public class RebuildProved extends TransactionalCommand
{
	private final Statement statement;

	public RebuildProved(CommandSource from, Transaction transaction, Statement statement)
	{
		super(from, transaction);
		this.statement = statement;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		statement.rebuildProved(getTransaction());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<RebuildProved>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public RebuildProved parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement;
			if (split.size() <= 0)
				statement = from.getActiveContext();
			else
				statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			return new RebuildProved(from, transaction, statement);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement>";
		}

		@Override
		public String shortHelp()
		{
			return "Rebuilds the proved status of all the statements that descend of the given statement and recursivelly its dependents.";
		}

	}

}
