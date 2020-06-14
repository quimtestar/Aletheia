/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.util.Stack;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.authority.PrivatePerson;
import aletheia.model.authority.StatementAuthority;
import aletheia.model.authority.StatementAuthority.AuthorityCreationException;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "auth", groupPath = "/authority", factory = Auth.Factory.class)
public class Auth extends TransactionalCommand
{
	private final PrivatePerson author;
	private final Statement statement;

	public Auth(CommandSource from, Transaction transaction, PrivatePerson author, Statement statement)
	{
		super(from, transaction);
		this.author = author;
		this.statement = statement;
	}

	protected PrivatePerson getAuthor()
	{
		return author;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	protected void createAuthority() throws AuthorityCreationException
	{
		statement.createAuthority(getTransaction(), author);
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		if (!(statement instanceof RootContext))
		{
			Context ctx = statement.getContext(getTransaction());
			Stack<Context> stack = new Stack<>();
			while (true)
			{
				StatementAuthority auth = ctx.getAuthority(getTransaction());
				if (auth != null)
					break;
				stack.push(ctx);
				if (ctx instanceof RootContext)
					break;
				ctx = ctx.getContext(getTransaction());
			}
			while (!stack.isEmpty())
			{
				ctx = stack.pop();
				ctx.createAuthority(getTransaction(), author);
			}
		}
		createAuthority();
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Auth>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		protected Auth makeAuth(CommandSource from, Transaction transaction, PrivatePerson author, Statement statement) throws CommandParseException
		{
			return new Auth(from, transaction, author, statement);
		}

		@Override
		public Auth parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				PrivatePerson author = from.getPersistenceManager().privatePersonsByNick(transaction).get(split.get(0));
				if (author == null)
					throw new CommandParseException("Invalid nick");
				Statement statement;
				if (split.size() > 1)
				{
					statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(1));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
				}
				else
				{
					statement = from.getActiveContext();
					if (statement == null)
						throw new NotActiveContextException();
				}
				return makeAuth(from, transaction, author, statement);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "<nick> [<statement>]";
		}

		@Override
		public String shortHelp()
		{
			return "Creates the authority of a statement.";
		}

	}

}
