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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "strip", factory = NewStrip.Factory.class)
public class NewStrip extends NewStatement
{
	private final Statement statement;
	private final List<Term> hints;

	public NewStrip(CommandSource from, Transaction transaction, Identifier identifier, Statement statement, List<Term> hints)
	{
		super(from, transaction, identifier);
		this.statement = statement;
		this.hints = hints;
	}

	protected Statement getStatement()
	{
		return statement;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Statement statement = this.statement;
		Term term = statement.getTerm();
		int i = -1;
		while (term instanceof FunctionTerm)
		{
			FunctionTerm functionTerm = (FunctionTerm) term;
			ParameterVariableTerm parameter = functionTerm.getParameter();
			Term type = parameter.getType();
			Term body = functionTerm.getBody();
			Term hint = null;
			if (functionTerm.getBody().freeVariables().contains(functionTerm.getParameter()))
			{
				Iterator<Term> hi = hints.iterator();
				while (hi.hasNext())
				{
					Term t = hi.next();
					if (t.getType().equals(type))
					{
						hint = t;
						hi.remove();
						break;
					}
				}
				if (hint == null)
					break;

			}
			if (i >= 0)
				statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i)));
			i++;
			if (hint != null)
			{
				statement = ctx.specialize(getTransaction(), statement, hint);
				body = body.replace(parameter, hint);
			}
			else
			{
				Statement solver = null;
				for (Statement stsol : ctx.statementsByTerm(getTransaction()).get(type).toArray(new Statement[0]))
				{
					if (stsol.isProved())
					{
						solver = stsol;
						break;
					}
				}
				if (solver == null)
				{
					for (Statement stsol : ctx.localStatementsByTerm(getTransaction()).get(type).toArray(new Statement[0]))
					{
						solver = stsol;
						break;
					}
				}
				if (solver != null)
					statement = ctx.specialize(getTransaction(), statement, solver.getVariable());
				else
				{
					Context subctx = ctx.openSubContext(getTransaction(), type);
					subctx.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i++)));
					statement = ctx.specialize(getTransaction(), statement, subctx.getVariable());
				}
			}
			term = body;
		}
		if (statement == this.statement)
			throw new Exception("Statement not strippable");
		return new RunNewStatementReturnData(statement);
	}

	public static class Factory extends AbstractNewStatementFactory<NewStrip>
	{

		@Override
		public NewStrip parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				if (from.getActiveContext() == null)
					throw new NotActiveContextException();
				Statement statement = from.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(0)));
				if (statement == null)
					throw new CommandParseException("Bad statement: " + split.get(0));
				List<Term> hints = new LinkedList<Term>();
				for (String s : split.subList(1, split.size()))
					hints.add(from.getActiveContext().parseTerm(transaction, s));
				return new NewStrip(from, transaction, identifier, statement, hints);
			}
			catch (NotActiveContextException | InvalidNameException | TermParserException e)
			{
				throw new CommandParseException(e);
			}
			finally
			{

			}
		}

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> <hint>*";
		}

		@Override
		public String shortHelp()
		{
			return "Strips the given statement.";
		}

		@Override
		public void longHelp(PrintStream out)
		{
			super.longHelp(out);
			out.println("A list of hint terms might be provided to be assigned to the parameters left undetermined.");
		}

	}

}
