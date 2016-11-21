/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
import java.util.Stack;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.ReverseSortedSet;

@TaggedCommand(tag = "unproved", groupPath = "/authority", factory = Unproved.Factory.class)
public class Unproved extends TransactionalCommand
{
	private final Context context;

	public Unproved(CommandSource from, Transaction transaction, Context context)
	{
		super(from, transaction);
		this.context = context;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Stack<Statement> stack = new Stack<>();
		stack.push(context);
		while (!stack.isEmpty())
		{
			Statement st = stack.pop();
			if (!st.isProved())
			{
				getOut().println(" -> " + st.statementPathString(getTransaction(), getActiveContext()) + " " + (st.isProved() ? "\u2713" : ""));
				if (st instanceof Context)
					stack.addAll(new ReverseSortedSet<>(((Context) st).localSortedStatements(getTransaction())));
			}
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Unproved>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Unproved parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				Context context;
				if (split.size() > 0)
				{
					Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, from.getActiveContext(), split.get(0));
					if (statement == null)
						throw new CommandParseException("Invalid statement");
					if (!(statement instanceof Context))
						throw new CommandParseException("Not a context");
					context = (Context) statement;
				}
				else
				{
					context = from.getActiveContext();
					if (context == null)
						throw new NotActiveContextException();
				}
				return new Unproved(from, transaction, context);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<context>]";
		}

		@Override
		public String shortHelp()
		{
			return "Lists all the unproved statements descending of the specified (or the active) context.";
		}

	}

}
