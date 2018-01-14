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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.common.NamespacePattern;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.CloseableCollection;
import aletheia.utilities.collections.DiamondPriorityDiscardingQueue;
import aletheia.utilities.collections.Filter;
import aletheia.utilities.collections.FilteredCloseableCollection;

@TaggedCommand(tag = "lu", groupPath = "/statement", factory = LookUp.Factory.class)
public class LookUp extends TransactionalCommand
{
	private final Context context;
	private final Term term;
	private final NamespacePattern namespacePattern;
	private final int size;

	public LookUp(CommandSource from, Transaction transaction, Context context, Term term, NamespacePattern namespacePattern, int size)
	{
		super(from, transaction);
		this.context = context;
		this.term = term;
		this.namespacePattern = namespacePattern;
		this.size = size;
	}

	private CloseableCollection<Statement.Match> lookupMatchesWithPattern()
	{
		return Statement.filterMatches(new BijectionCloseableCollection<>(new Bijection<Map.Entry<Identifier, Statement>, Statement>()
		{

			@Override
			public Statement forward(Entry<Identifier, Statement> entry)
			{
				return entry.getValue();
			}

			@Override
			public Entry<Identifier, Statement> backward(Statement statement)
			{
				throw new UnsupportedOperationException();
			}
		},

				new FilteredCloseableCollection<>(new Filter<Map.Entry<Identifier, Statement>>()
				{

					@Override
					public boolean filter(Map.Entry<Identifier, Statement> entry)
					{
						return namespacePattern.matches(entry.getKey());
					}
				}, context.identifierToStatement(getTransaction()).tailMap(namespacePattern.fromKey()).entrySet()))

				, term != null ? term : context.getConsequent());

	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		Comparator<Statement.Match> comparator = new Comparator<Statement.Match>()
		{

			@Override
			public int compare(Statement.Match m1, Statement.Match m2)
			{
				return Integer.compare(m1.getAssignable().size() - m1.getTermMatch().getAssignMapLeft().size(),
						m2.getAssignable().size() - m2.getTermMatch().getAssignMapLeft().size());
			}

		};
		Queue<Context.Match> matches = new DiamondPriorityDiscardingQueue<>(DiamondPriorityDiscardingQueue.heightForCapacity(size + 1), comparator);
		if (namespacePattern == null)
			matches.addAll(context.lookupMatches(getTransaction(), term));
		else
			matches.addAll(lookupMatchesWithPattern());
		int n = 0;
		while (n < size && !matches.isEmpty())
		{
			Context.Match m = matches.poll();
			Term t = m.getStatement().getTerm();
			Term.ParameterNumerator pn = t.parameterNumerator();
			getOut().println("-> " + m.getStatement().statementPathString(getTransaction(), context) + ": "
					+ t.toString(context.variableToIdentifier(getTransaction()), pn) + ": ");
			for (ParameterVariableTerm p : m.getAssignable())
			{
				Term t_ = m.getTermMatch().getAssignMapLeft().get(p);
				if (t_ != null)
					getOut().println(
							"\t[ " + "@" + pn.unNumberedParameterNumber(p) + " <- " + t_.toString(context.variableToIdentifier(getTransaction())) + " ]");
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
				int size = defaultSize;
				int is = split.indexOf("-s");
				if (is >= 0)
				{
					split.remove(is);
					if (is >= split.size())
						throw new CommandParseException("Must specify a size.");
					size = Integer.parseInt(split.get(is));
					split.remove(is);

				}
				Term term = null;
				NamespacePattern namespacePattern = null;
				if (split.size() > 0)
				{
					term = ctx.parseTerm(transaction, split.get(0));
					if (split.size() > 1)
					{
						namespacePattern = NamespacePattern.instantiate(split.get(1));
					}
				}
				return new LookUp(from, transaction, ctx, term, namespacePattern, size);
			}
			catch (Exception e)
			{
				throw CommandParseEmbeddedException.embed(e);
			}
		}

		@Override
		protected String paramSpec()
		{
			return "[<term> [<search pattern expression>]] [-s <size>]";
		}

		@Override
		public String shortHelp()
		{
			return "Looks up which statements of the active context match the given term (or the active's term context) up to <size> elements (default: "
					+ defaultSize + ").";
		}

	}

}
