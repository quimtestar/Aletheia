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
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

@TaggedCommand(tag = "auto", groupPath = "/statement", factory = NewAuto.Factory.class)
public class NewAuto extends NewStatement
{
	private final Context context;
	private final Statement general;
	private final Term term;

	public NewAuto(CommandSource from, Transaction transaction, Identifier identifier, Context context, Statement general, Term term)
	{
		super(from, transaction, identifier);
		this.context = context;
		this.general = general;
		this.term = term;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		Context.Match m = context.match(general, term);
		if (m == null)
			throw new Exception("No match");
		Statement statement = general;
		int i = -1;

		Term term = general.getTerm();
		while (term instanceof FunctionTerm)
		{
			FunctionTerm functionTerm = (FunctionTerm) term;
			ParameterVariableTerm parameter = functionTerm.getParameter();
			Term type = parameter.getType();
			Term body = functionTerm.getBody();
			Term t = m.getTermMatch().getAssignMapLeft().get(parameter);
			if (t == null && body.freeVariables().contains(parameter))
				break;
			if (i >= 0)
				statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i)));
			i++;
			Statement st_;
			if (t != null)
			{
				st_ = context.specialize(getTransaction(), statement, t);
				body = body.replace(parameter, t);
			}
			else
			{
				Statement solver = null;
				for (Statement stsol : new BufferedList<>(context.statementsByTerm(getTransaction()).get(type)))
				{
					if (stsol.isProved())
					{
						solver = stsol;
						break;
					}
				}
				if (solver == null)
				{
					for (Statement stsol : new BufferedList<>(context.localStatementsByTerm(getTransaction()).get(type)))
					{
						solver = stsol;
						break;
					}
				}
				if (solver != null)
					st_ = context.specialize(getTransaction(), statement, solver.getVariable());
				else
				{
					Context subctx = context.openSubContext(getTransaction(), type);
					subctx.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i++)));
					st_ = context.specialize(getTransaction(), statement, subctx.getVariable());
				}
			}
			statement = st_;
			term = body;
		}
		if (statement == general)
			throw new Exception("Statement not automatically boundable to the target term");
		return new RunNewStatementReturnData(statement);
	}

	public static class Factory extends AbstractNewStatementFactory<NewAuto>
	{
		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public NewAuto parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
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
			return new NewAuto(from, transaction, identifier, ctx, statement, term);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [<term>]";
		}

		@Override
		public String shortHelp()
		{
			return "Automatically specialize the given statement matching it to produce the active context's consequent or the given term.";
		}

	}

}
