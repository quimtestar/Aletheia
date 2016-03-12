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
package aletheia.gui.cli.command.statement;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Context.Match;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.DiamondPriorityDiscardingQueue;

@TaggedCommand(tag = "lu", groupPath = "/statement", factory = LookUp.Factory.class)
public class LookUp extends TransactionalCommand
{
	private final Context context;
	private final Term term;
	private final int size;

	public LookUp(CommandSource from, Transaction transaction, Context context, Term term, int size)
	{
		super(from, transaction);
		this.context = context;
		this.term = term;
		this.size = size;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Comparator<Context.Match> comparator = new Comparator<Context.Match>()
		{

			@Override
			public int compare(Match m1, Match m2)
			{
				return Integer.compare(m1.getAssignable().size() - m1.getTermMatch().getAssignMapLeft().size(),
						m2.getAssignable().size() - m2.getTermMatch().getAssignMapLeft().size());
			}

		};
		Queue<Context.Match> matches = new DiamondPriorityDiscardingQueue<Context.Match>(DiamondPriorityDiscardingQueue.heightForCapacity(size + 1),
				comparator);
		matches.addAll(context.lookupMatches(getTransaction(), term));
		int n = 0;
		while (n < size && !matches.isEmpty())
		{
			Context.Match m = matches.poll();
			getOut().print("-> " + m.getStatement().statementPathString(getTransaction(), context) + ": "
					+ m.getStatement().getTerm().toString(context.variableToIdentifier(getTransaction())) + ": ");
			int i = 0;
			for (ParameterVariableTerm p : m.getStatement().getTerm().parameters())
			{
				Term t = m.getTermMatch().getAssignMapLeft().get(p);
				getOut().print("[ " + "@" + i + " <- " + (t != null ? t.toString(context.variableToIdentifier(getTransaction())) : "?") + " ] ");
				i++;
			}
			getOut().println();
			n++;
		}
		if (!matches.isEmpty())
			getOut().println("-> ...");
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<LookUp>
	{
		public static final int defaultSize = 10;

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		public LookUp parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				Context ctx = from.getActiveContext();
				if (ctx == null)
					throw new NotActiveContextException();
				Term term = null;
				int size = defaultSize;
				if (split.size() > 0)
				{
					try
					{
						size = Integer.parseInt(split.get(0));
					}
					catch (NumberFormatException e)
					{
						term = ctx.parseTerm(transaction, split.get(0));
						if (split.size() > 1)
							size = Integer.parseInt(split.get(1));
					}
				}
				return new LookUp(from, transaction, ctx, term, size);
			}
			catch (Exception e)
			{
				throw new CommandParseException(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<term>] [<size>]";
		}

		@Override
		public String shortHelp()
		{
			return "Looks up which statements of the active context match the given term (or the active's term context) up to <size> elements (default: "
					+ defaultSize + ").";
		}

	}

}
