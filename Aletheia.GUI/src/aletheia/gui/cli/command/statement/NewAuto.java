/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.term.TermParser.ParameterIdentifiedTerm;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "auto", factory = NewAuto.Factory.class)
public class NewAuto extends NewStatement
{
	private final Statement general;
	private final Term target;
	private final List<ParameterIdentifiedTerm> hints;

	public NewAuto(CommandSource from, Transaction transaction, Identifier identifier, Statement general, Term target, List<ParameterIdentifiedTerm> hints)
	{
		super(from, transaction, identifier);
		this.general = general;
		this.target = target;
		this.hints = hints;
	}

	private Statement suitableFromHints(Context context, Term term)
	{
		Statement statement = null;
		Iterator<ParameterIdentifiedTerm> hi = hints.iterator();
		while (hi.hasNext())
		{
			ParameterIdentifiedTerm hint = hi.next();
			if ((hint.getTerm() instanceof IdentifiableVariableTerm) && hint.getTerm().getType().equals(term))
			{
				statement = context.statements(getTransaction()).get(hint.getTerm());
				if (statement != null)
				{
					hi.remove();
					break;
				}
			}
		}
		return statement;
	}

	private void removeIdentifiableVariableTermFromHints(IdentifiableVariableTerm variable)
	{
		for (Iterator<ParameterIdentifiedTerm> iterator = hints.iterator(); iterator.hasNext();)
			if (iterator.next().getTerm().equals(variable))
			{
				iterator.remove();
				break;
			}
	}

	private Statement suitable(Context context, Term term)
	{
		Statement statement = null;
		statement = context.suitableForInstanceProofStatementByTerm(getTransaction(), term);
		if (statement != null)
		{
			removeIdentifiableVariableTermFromHints(statement.getVariable());
			return statement;
		}
		statement = suitableFromHints(context, term);
		if (statement != null)
			return statement;
		return statement;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		Context context = getActiveContext();
		if (context == null)
			throw new NotActiveContextException();

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
			ParameterIdentification pi = null;
			if (t == null && body.isFreeVariable(parameter))
			{
				{
					Iterator<ParameterIdentifiedTerm> hi = hints.iterator();
					while (hi.hasNext())
					{
						ParameterIdentifiedTerm hint = hi.next();
						if (hint.getTerm().getType().equals(type))
						{
							t = hint.getTerm();
							pi = hint.getParameterIdentification();
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
						for (ParameterIdentifiedTerm hi : hints)
						{
							if (hi.getTerm() instanceof VariableTerm)
							{
								Statement sthi = context.statements(getTransaction()).get(hi.getTerm());
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
						if (i >= subStatementOverflow)
							throw new Exception("Substatement identifier numerator overflowed.");
						statement.identify(getTransaction(), new Identifier(getIdentifier(), String.format(subStatementFormat, i)));
						break;
					}
					catch (AlreadyUsedIdentifierException e)
					{
						i++;
					}
				}
			}
			i++;

			Statement instanceProof = context.statements(getTransaction()).get(t);
			if (instanceProof == null)
				instanceProof = suitable(context, type);
			if (instanceProof == null)
			{
				instanceProof = context.openSubContext(getTransaction(), type);
				while (true)
				{
					try
					{
						if (i >= subStatementOverflow)
							throw new Exception("Substatement identifier numerator overflowed.");
						instanceProof.identify(getTransaction(), new Identifier(getIdentifier(), String.format(subStatementFormat, i++)));
						break;
					}
					catch (AlreadyUsedIdentifierException e)
					{
					}
				}
			}
			Specialization st_;
			if (t != null)
			{
				st_ = context.specialize(getTransaction(), statement, t, instanceProof);
				body = body.replace(parameter, t);
			}
			else
				st_ = context.specialize(getTransaction(), statement, instanceProof.getVariable(), instanceProof);
			st_.updateInstanceParameterIdentification(getTransaction(), pi);
			statement = st_;
			term = body.unproject();
		}
		if (statement == general)
			throw new Exception("Nothing boundable :(");
		if (!hints.isEmpty())
		{
			getErr().println("Warning: unused hints.");
			for (ParameterIdentifiedTerm h : hints)
				getErr().println(" -> " + termToString(context, getTransaction(), h.getTerm(), h.getParameterIdentification()));
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
			List<ParameterIdentifiedTerm> hints = new LinkedList<>();
			if (split.size() > 1)
			{
				term = parseTerm(ctx, transaction, split.get(1));
				for (String s : split.subList(2, split.size()))
					hints.add(parseParameterIdentifiedTerm(ctx, transaction, s));
			}
			else
				term = ctx.getConsequent();
			return new NewAuto(from, transaction, identifier, statement, term, hints);
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
