/*******************************************************************************
 * Copyright (c) 2020 Quim Testar.
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
import java.util.Set;
import java.util.Stack;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "delul", groupPath = "/statement", factory = DeleteUseless.Factory.class)
public class DeleteUseless extends TransactionalCommand
{
	private final Context context;

	public DeleteUseless(CommandSource from, Transaction transaction, Context context)
	{
		super(from, transaction);
		this.context = context;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		if (!context.refresh(getTransaction()).isProved())
			throw new Exception("Not proved.");
		Stack<Statement> stack = new Stack<>();
		Set<Statement> useless = context.uselessDescendents(getTransaction());
		stack.addAll(useless);
		while (!stack.isEmpty())
		{
			Statement st = stack.peek().refresh(getTransaction());
			if (st != null && !(st instanceof Assumption))
			{
				boolean hasDeps = false;
				for (Statement dep : st.dependents(getTransaction()))
				{
					if (!useless.contains(dep))
						throw new Exception("(!) Used dependent of a useless statement.");
					stack.push(dep);
					hasDeps = true;
				}
				if (!hasDeps)
				{
					stack.pop();
					getErr().println(" <- " + st.statementPathString(getTransaction(), context));
					st.delete(getTransaction());
					if (!context.refresh(getTransaction()).isProved())
						throw new Exception("(!) Deleted statement affected proveness");
				}
			}
			else
				stack.pop();
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<DeleteUseless>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public DeleteUseless parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
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
				return new DeleteUseless(from, transaction, context);
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
			return "Delete useless (in terms of proveness) statements descending of this context.";
		}

	}

}
