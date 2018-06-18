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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredSet;
import aletheia.utilities.collections.ReverseList;

@TaggedCommand(tag = "useless", groupPath = "/statement", factory = Useless.Factory.class)
public class Useless extends TransactionalCommand
{
	private final Context context;
	private final Comparator<Statement> pathComparator;
	private final boolean unsigned;

	public Useless(CommandSource from, Transaction transaction, Context context, boolean unsigned)
	{
		super(from, transaction);
		this.context = context;
		this.pathComparator = Statement.pathComparator(getTransaction());
		this.unsigned = unsigned;
	}

	private void processProved(Context context)
	{
		System.out.println(context.getUuid() + ": " + context);
		Set<Statement> useless = new FilteredSet<>(new Filter<Statement>()
		{

			@Override
			public boolean filter(Statement st)
			{
				return !(st instanceof Assumption) || st.getContext(getTransaction()).equals(context);
			}
		}, context.uselessDescendents(getTransaction()));
		if (!useless.isEmpty())
		{
			ArrayList<Statement> list = new ArrayList<>(useless);
			Collections.sort(list, pathComparator);
			Statement last = null;
			for (Statement st : list)
			{
				if ((!(st instanceof Assumption) || st.getContext(getTransaction()).equals(context))
						&& (last == null || !last.isDescendent(getTransaction(), st)))
				{
					getOut().println(" -> " + st.statementPathString(getTransaction(), getActiveContext()) + " " + (st.isProved() ? "\u2713" : ""));
					last = st;
				}
			}
			getOut().println();
		}
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Stack<Context> stack = new Stack<>();
		stack.push(context);
		while (!stack.isEmpty())
		{
			Context ctx = stack.pop();
			if (ctx.isProved())
			{
				if (!unsigned || !ctx.isValidSignature(getTransaction()))
					processProved(ctx);
			}
			else
			{
				ArrayList<Context> subs = new ArrayList<>(ctx.subContexts(getTransaction()));
				Collections.sort(subs, pathComparator);
				stack.addAll(new ReverseList<>(subs));
			}
		}
		getOut().println("end.");
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Useless>
	{

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public Useless parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			try
			{
				checkMinParameters(split);
				boolean unsigned = split.remove("-unsigned");
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
				return new Useless(from, transaction, context, unsigned);
			}
			catch (NotActiveContextException e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<context>] [-unsigned]";
		}

		@Override
		public String shortHelp()
		{
			return "Useless (in terms of proveness) statements descending of this context.";
		}

	}

}
