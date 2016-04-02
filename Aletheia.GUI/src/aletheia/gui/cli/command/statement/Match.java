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

import java.util.List;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "m", groupPath = "/statement", factory = Match.Factory.class)
public class Match extends TransactionalCommand
{
	private final Context context;
	private final Statement statement;
	private final Term term;

	public Match(CommandSource from, Transaction transaction, Context context, Statement statement, Term term)
	{
		super(from, transaction);
		this.context = context;
		this.statement = statement;
		this.term = term;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		getOut().println(statement.getTerm().toString(context.variableToIdentifier(getTransaction())));
		Context.Match m = context.match(statement, term);
		if (m == null)
			throw new Exception("No match.");
		int i = 0;
		for (ParameterVariableTerm p : m.getStatement().getTerm().parameters())
		{
			Term t = m.getTermMatch().getAssignMapLeft().get(p);
			getOut().println("@" + i + " <- " + (t != null ? t.toString(context.variableToIdentifier(getTransaction())) : "?"));
			i++;
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Match>
	{
		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Match parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Context ctx = from.getActiveContext();
			if (ctx == null)
				throw new CommandParseException(new NotActiveContextException());
			Statement statement = findStatementSpec(from.getPersistenceManager(), transaction, ctx, split.get(0));
			if (statement == null)
				throw new CommandParseException("Invalid statement");
			Term term = null;
			if (split.size() > 1)
				try
				{
					term = ctx.parseTerm(transaction, split.get(1));
				}
				catch (TermParserException e)
				{
					throw new CommandParseException(e);
				}
			return new Match(from, transaction, ctx, statement, term);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [<term>]";
		}

		@Override
		public String shortHelp()
		{
			return "Matches the given term (or the active's term context) to an statement and computes the values the parameters should take.";
		}

	}

}
