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
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "termtree", groupPath = "/term", factory = TermTree.Factory.class)
public class TermTree extends TransactionalCommand
{
	private final Term term;

	public TermTree(CommandSource from, Transaction transaction, Term term)
	{
		super(from, transaction);
		this.term = term;
	}

	protected Term getTerm()
	{
		return term;
	}

	private void showTree(Term term)
	{
		if (term instanceof CompositionTerm)
			getOutB().print("\u250c ");
		else
			getOutB().print("\u2022 ");
		showTree("", "", term);
	}

	private void showTree(String prefixA, String prefixB, Term term)
	{
		getOut().println(termToString(getActiveContext(), getTransaction(), term));
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
					if (component instanceof CompositionTerm)
						prefixA_ = prefixB + "\u251c\u2500\u252c";
					else
						prefixA_ = prefixB + "\u251c\u2500\u2500";
					prefixB_ = prefixB + "\u2502 ";
				}
				else
				{
					if (component instanceof CompositionTerm)
						prefixA_ = prefixB + "\u2514\u2500\u252c";
					else
						prefixA_ = prefixB + "\u2514\u2500\u2500";
					prefixB_ = prefixB + "  ";
				}

				getOutB().format("%s%2d: ", prefixA_, i);
				showTree(prefixA_, prefixB_, component);
			}
		}
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		showTree(term);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermTree>
	{

		@Override
		public TermTree parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term;
			if (split.size() < 1)
			{
				if (from.getActiveContext() == null)
					throw new CommandParseException(new NotActiveContextException());
				term = from.getActiveContext().getConsequent();
			}
			else
				term = parseTerm(from.getActiveContext(), transaction, split.get(0));
			return new TermTree(from, transaction, term);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "[<term>]";
		}

		@Override
		public String shortHelp()
		{
			return "Shows the component tree of a term.";
		}

	}

}
