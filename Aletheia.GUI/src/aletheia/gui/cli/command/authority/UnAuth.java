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
package aletheia.gui.cli.command.authority;

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "unauth", groupPath = "/authority", factory = UnAuth.Factory.class)
public class UnAuth extends TransactionalCommand
{
	private final Statement statement;
	private final boolean force;

	public UnAuth(CommandSource from, Transaction transaction, Statement statement, boolean force)
	{
		super(from, transaction);
		this.statement = statement;
		this.force = force;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		statement.deleteAuthority(getTransaction(), force);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<UnAuth>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public UnAuth parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Statement statement = findStatementPath(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			boolean force = split.size() > 1 && split.get(1).equals("force");
			return new UnAuth(from, transaction, statement, force);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [force]";
		}

		@Override
		public String shortHelp()
		{
			return "Deletes the authority of an statement.";
		}

	}

}
