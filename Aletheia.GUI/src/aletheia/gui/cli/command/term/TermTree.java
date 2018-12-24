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
package aletheia.gui.cli.command.term;

import java.util.List;
import java.util.ListIterator;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.term.CompositionTerm;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.ProjectionTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "termtree", groupPath = "/term", factory = TermTree.Factory.class)
public class TermTree extends TransactionalCommand
{
	private final String label;
	private final Term term;
	private final int maxDepth;

	public TermTree(CommandSource from, Transaction transaction, String label, Term term, int maxDepth)
	{
		super(from, transaction);
		this.label = label;
		this.term = term;
		this.maxDepth = maxDepth;
	}

	protected Term getTerm()
	{
		return term;
	}

	protected String getLabel()
	{
		return label;
	}

	protected int getMaxDepth()
	{
		return maxDepth;
	}

	private boolean hasChildren(Term term)
	{
		return (term instanceof CompositionTerm) || (term instanceof FunctionTerm) || (term instanceof ProjectionTerm);
	}

	private void showTree(String label, Term term)
	{
		if (hasChildren(term) && maxDepth > 0)
			getOutB().print("\u250c " + label + ": ");
		else
			getOutB().print("\u2022 " + label + ": ");
		showTree("", "", label, false, term, term.parameterNumerator(), maxDepth);
	}

	private void showTree(String prefixA, String prefixB, String label, boolean compLabel, Term term, Term.ParameterNumerator numerator, int depth)
	{
		getOut().println(term.toString(getTransaction(), getActiveContext(), numerator));
		if (depth > 0)
		{
			String atomicLabel = compLabel ? "(" + label + ")" : label;
			if (term instanceof CompositionTerm)
			{
				ListIterator<Term> iterator = ((CompositionTerm) term).components().listIterator();
				while (iterator.hasNext())
				{
					int i = iterator.nextIndex();
					Term component = iterator.next();
					String prefixA_, prefixB_;
					if (iterator.hasNext())
					{
						if (hasChildren(component) && depth > 1)
							prefixA_ = prefixB + "\u251c\u2500\u252c";
						else
							prefixA_ = prefixB + "\u251c\u2500\u2500";
						prefixB_ = prefixB + "\u2502 ";
					}
					else
					{
						if (hasChildren(component) && depth > 1)
							prefixA_ = prefixB + "\u2514\u2500\u252c";
						else
							prefixA_ = prefixB + "\u2514\u2500\u2500";
						prefixB_ = prefixB + "  ";
					}
					String label_ = atomicLabel + (";" + i);
					getOutB().format("%s %s: ", prefixA_, label_);
					showTree(prefixA_, prefixB_, label_, false, component, numerator, depth - 1);
				}
			}
			else
			{
				while (term instanceof ProjectionTerm)
					term = ((ProjectionTerm) term).getFunction();
				if (term instanceof FunctionTerm)
				{
					FunctionTerm functionTerm = (FunctionTerm) term;
					ParameterVariableTerm param = functionTerm.getParameter();
					Term body = functionTerm.getBody();
					{
						Term parType = param.getType();
						String prefixA_;
						if (hasChildren(parType) && depth > 1)
							prefixA_ = prefixB + "\u251c\u2500\u252c";
						else
							prefixA_ = prefixB + "\u251c\u2500\u2500";
						String prefixB_ = prefixB + "\u2502 ";
						String label_ = atomicLabel + "%";
						getOutB().format("%s %s: ", prefixA_, label_);
						showTree(prefixA_, prefixB_, label_, false, parType, numerator, depth - 1);
					}
					{
						int parNum = numerator.numberParameter(param);
						String prefixA_;
						if (hasChildren(body) && depth > 1)
							prefixA_ = prefixB + "\u2514\u2500\u252c";
						else
							prefixA_ = prefixB + "\u2514\u2500\u2500";
						String prefixB_ = prefixB + "  ";
						String label_;
						boolean compLabel_;
						if (body.isFreeVariable(functionTerm.getParameter()))
						{
							label_ = label + " @" + parNum;
							compLabel_ = true;
						}
						else
						{
							label_ = atomicLabel + "'";
							compLabel_ = false;
						}
						getOutB().format("%s %s: ", prefixA_, label_);
						showTree(prefixA_, prefixB_, label_, compLabel_, body, numerator, depth - 1);
						numerator.unNumberParameter();
					}
				}
			}
		}
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		showTree(label, term);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermTree>
	{

		@Override
		public TermTree parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			int maxDepth = Integer.MAX_VALUE;
			{
				int i = split.indexOf("-d");
				if (i >= 0)
				{
					split.remove(i);
					if (i >= split.size())
						throw new CommandParseException("No maximum depth specified.");
					try
					{
						maxDepth = Integer.parseInt(split.get(i));
					}
					catch (NumberFormatException e)
					{
						throw new CommandParseException("Specified maximum depth '" + split.get(i) + "' is not a number.", e);
					}
					split.remove(i);
				}
			}
			Term term;
			String label;
			if (split.size() < 1)
			{
				if (from.getActiveContext() == null)
					throw new CommandParseException(new NotActiveContextException());
				label = "&|-";
				term = from.getActiveContext().getConsequent();
			}
			else
			{
				label = "(" + split.get(0) + ")";
				term = parseTerm(from.getActiveContext(), transaction, split.get(0));
			}
			return new TermTree(from, transaction, label, term, maxDepth);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "[<term>] [-d <maxdepth> ]";
		}

		@Override
		public String shortHelp()
		{
			return "Shows the component tree of a term.";
		}

	}

}
