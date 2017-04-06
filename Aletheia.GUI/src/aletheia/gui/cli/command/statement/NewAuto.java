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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.nomenclator.Nomenclator.AlreadyUsedIdentifierException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.TermParserException;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

@TaggedCommand(tag = "auto", factory = NewAuto.Factory.class)
public class NewAuto extends NewStatement
{
	private final Context context;
	private final Statement general;
	private final Term target;
	private final List<Term> hints;

	public NewAuto(CommandSource from, Transaction transaction, Identifier identifier, Context context, Statement general, Term target, List<Term> hints)
	{
		super(from, transaction, identifier);
		this.context = context;
		this.general = general;
		this.target = target;
		this.hints = hints;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		Context.Match m = null;
		if (target != null)
		{
			m = general.match(target);
			if (m == null)
				throw new Exception("No match");
		}
		Statement statement = general;
		int i = -1;

		Statement unidentified = null;
		if (getIdentifier().equals(statement.getIdentifier()) && context.equals(statement.getContext(getTransaction())))
		{
			i = 0;
			statement.unidentify(getTransaction());
			unidentified = statement;
			getErr().println("Warning: Appending statements to identifier's parent.");
		}

		Term term = general.getTerm();
		Iterator<ParameterVariableTerm> oParamIt = term.parameters().iterator();
		while (term instanceof FunctionTerm)
		{
			FunctionTerm functionTerm = (FunctionTerm) term;
			ParameterVariableTerm parameter = functionTerm.getParameter();
			Term type = parameter.getType();
			Term body = functionTerm.getBody();
			Term t = m != null && oParamIt.hasNext() ? m.getTermMatch().getAssignMapLeft().get(oParamIt.next()) : null;
			if (t == null && body.freeVariables().contains(parameter))
			{
				{
					Iterator<Term> hi = hints.iterator();
					while (hi.hasNext())
					{
						Term hint = hi.next();
						if (hint.getType().equals(type))
						{
							t = hint;
							hi.remove();
							break;
						}
					}
				}
				if (t == null)
				{
					Set<VariableTerm> assignable = new HashSet<>();
					assignable.add(parameter);
					l: for (ParameterVariableTerm p : body.parameters())
					{
						for (Term hi : hints)
						{
							if (hi instanceof VariableTerm)
							{
								Statement sthi = context.statements(getTransaction()).get(hi);
								if (sthi != null)
								{
									Term.Match m2 = p.getType().match(assignable, sthi.getTerm());
									if (m2 != null)
									{
										t = m2.getAssignMapLeft().get(parameter);
										if (t != null)
										{
											if (m != null)
											{
												for (Entry<VariableTerm, Term> e : m2.getAssignMapLeft().entrySet())
												{
													Term t2 = m.getTermMatch().getAssignMapLeft().get(e.getKey());
													if (t2 != null && !t2.equals(e.getValue()))
													{
														t = null;
														break l;
													}
												}
											}
											break l;
										}
									}
								}
							}
						}
						assignable.add(p);
					}
				}

				if (t == null)
					break;
			}
			if (i >= 0)
			{
				while (true)
				{
					try
					{
						statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i)));
						break;
					}
					catch (AlreadyUsedIdentifierException e)
					{
						i++;
					}
				}
			}
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
				Term type_ = type.unproject();
				{
					Iterator<Term> hi = hints.iterator();
					while (hi.hasNext())
					{
						Term hint = hi.next();
						if ((hint instanceof IdentifiableVariableTerm) && hint.getType().equals(type_))
						{
							solver = context.statements(getTransaction()).get(hint);
							if (solver != null)
							{
								hi.remove();
								break;
							}
						}
					}
				}
				if (solver == null)
				{
					for (Context ctx : context.statementPath(getTransaction()))
					{
						List<Statement> solvers = new BufferedList<>(ctx.localStatementsByTerm(getTransaction()).get(type_));
						Collections.sort(solvers, new Comparator<Statement>()
						{

							@Override
							public int compare(Statement st1, Statement st2)
							{
								Identifier id1 = st1.getIdentifier();
								Identifier id2 = st2.getIdentifier();
								int c;
								c = Boolean.compare(id1 == null, id2 == null);
								if (c != 0)
									return c;
								if (id1 == null || id2 == null)
									return 0;
								c = Integer.compare(id1.length(), id2.length());
								if (c != 0)
									return c;
								return c;
							}
						});
						for (Statement stsol : solvers)
						{
							if (stsol.isProved())
							{
								solver = stsol;
								break;
							}
						}
						if (solver == null && ctx.equals(context))
							for (Statement stsol : solvers)
							{
								solver = stsol;
								break;
							}
						if (solver != null)
							break;
					}
				}
				if (solver != null)
					st_ = context.specialize(getTransaction(), statement, solver.getVariable());
				else
				{
					Context subctx = context.openSubContext(getTransaction(), type);
					while (true)
					{
						try
						{
							subctx.identify(getTransaction(), new Identifier(getIdentifier(), String.format("sub_%02d", i++)));
							break;
						}
						catch (AlreadyUsedIdentifierException e)
						{
						}
					}
					st_ = context.specialize(getTransaction(), statement, subctx.getVariable());
				}
			}
			statement = st_;
			term = body;
			if (term instanceof ProjectionTerm)
				term = ((ProjectionTerm) term).getFunction();
		}
		if (statement == general)
			throw new Exception("Nothing boundable :(");
		if (!hints.isEmpty())
		{
			getErr().println("Warning: unused hints.");
			for (Term h : hints)
				getErr().println(" -> " + termToString(context, getTransaction(), h));
		}
		if (unidentified != null)
			getErr().println("Warning: Previously existing statement has been re-identified to '" + unidentified.refresh(getTransaction()).label() + "'.");
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
				throw new CommandParseException("Statement not found: " + split.get(0));
			Term term = null;
			List<Term> hints = new LinkedList<>();
			if (split.size() > 1)
				try
				{
					term = ctx.parseTerm(transaction, split.get(1));
					for (String s : split.subList(2, split.size()))
						hints.add(ctx.parseTerm(transaction, s));
				}
				catch (TermParserException e)
				{
					throw new CommandParseTermParserException(e);
				}
			else
				term = ctx.getConsequent();
			return new NewAuto(from, transaction, identifier, ctx, statement, term, hints);
		}

		@Override
		protected String paramSpec()
		{
			return "<statement> [<term> <hint>*]";
		}

		@Override
		public String shortHelp()
		{
			return "Automatically specialize the given statement matching it to produce the active context's consequent or the given term.";
		}

		@Override
		public void longHelp(PrintStream out)
		{
			super.longHelp(out);
			out.println("A list of hint terms might be provided to be assigned to the parameters left undetermined.");
		}

	}

}
