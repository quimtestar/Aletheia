/*******************************************************************************
 * Copyright (c) 2019 Quim Testar.
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
 *******************************************************************************/
package aletheia.gui.cli.command.term;

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.term.Term;
import aletheia.model.term.Term.SearchInfo;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "termsearch", groupPath = "/term", factory = TermSearch.Factory.class)
public class TermSearch extends TransactionalCommand
{
	private final Term term;
	private final Term sub;

	public TermSearch(CommandSource from, Transaction transaction, Term term, Term sub)
	{
		super(from, transaction);
		this.term = term;
		this.sub = sub;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		SearchInfo si = term.search(sub);
		if (si instanceof Term.SearchInfoNotFound)
			getErr().println("Not found.");
		else
		{
			String s = si.toString(getActiveContext().variableToIdentifier(getTransaction()));
			while (!s.isEmpty())
			{
				int i1 = s.indexOf(Term.DiffInfo.beginMark);
				if (i1 < 0)
				{
					getOut().print(s);
					s = "";
				}
				else
				{
					getOut().print(s.substring(0, i1));
					s = s.substring(i1);
				}
				getOut().flush();
				int i2 = s.indexOf(Term.DiffInfo.endMark);
				if (i2 < 0)
				{
					getOutB().print(s);
					s = "";
				}
				else
				{
					i2 += Term.DiffInfo.endMark.length();
					getOutB().print(s.substring(0, i2));
					s = s.substring(i2);
				}
				getOutB().flush();
			}
			getOut().println();
		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermSearch>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public TermSearch parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term;
			Term sub;
			if (split.size() >= 2)
			{
				term = parseTerm(from.getActiveContext(), transaction, split.get(0));
				sub = parseTerm(from.getActiveContext(), transaction, split.get(1));
			}
			else
			{
				term = from.getActiveContext().getConsequent();
				sub = parseTerm(from.getActiveContext(), transaction, split.get(0));
			}
			return new TermSearch(from, transaction, term, sub);
		}

		@Override
		protected String paramSpec()
		{
			return "[<term>] <sub>";
		}

		@Override
		public String shortHelp()
		{
			return "Search instances of a subterm inside a term.";
		}

	}

}
