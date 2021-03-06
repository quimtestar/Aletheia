/*******************************************************************************
 * Copyright (c) 2016, 2017 Quim Testar.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "unused", groupPath = "/statement", factory = Unused.Factory.class)
public class Unused extends TransactionalCommand
{
	private final Context context;

	public Unused(CommandSource from, Transaction transaction, Context context)
	{
		super(from, transaction);
		this.context = context;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Stack<Queue<Statement>> stack = new Stack<>();
		stack.push(new LinkedList<>(context.localSortedStatements(getTransaction())));
		loop: while (!stack.isEmpty())
		{
			Queue<Statement> queue = stack.peek();
			while (!queue.isEmpty())
			{
				Statement st = queue.poll();
				if (st.dependents(getTransaction()).isEmpty()
						&& st.getContext(getTransaction()).descendantContextsByConsequent(getTransaction(), st.getTerm()).isEmpty())
					getOut().println(" -> " + st.statementPathString(getTransaction(), getActiveContext()) + " " + (st.isProved() ? "\u2713" : ""));
				if (st instanceof Context && !st.isProved())
				{
					stack.push(new LinkedList<>(((Context) st).localSortedStatements(getTransaction())));
					continue loop;
				}
			}
			stack.pop();
		}
		getOut().println("end.");
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Unused>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Unused parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
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
				return new Unused(from, transaction, context);
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
			return "Unused statements descending of this context.";
		}

	}

}
