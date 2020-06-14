/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.BufferedList;

@TaggedCommand(tag = "solves", groupPath = "/statement", factory = Solves.Factory.class)
public class Solves extends TransactionalCommand
{
	private final Term term;

	public Solves(CommandSource from, Transaction transaction, Term term)
	{
		super(from, transaction);
		this.term = term;
	}

	protected Term getTerm()
	{
		return term;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws Exception
	{
		final Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Term term = this.term;
		List<Context> list = new BufferedList<>(ctx.descendantContextsByConsequent(getTransaction(), term));
		Comparator<Context> comparator = new Comparator<>()
		{

			@Override
			public int compare(Context ctx1, Context ctx2)
			{
				Iterator<? extends Context> i1 = ctx1.statementPath(getTransaction(), ctx).iterator();
				Iterator<? extends Context> i2 = ctx2.statementPath(getTransaction(), ctx).iterator();
				while (i1.hasNext() && i2.hasNext())
				{
					Context cp1 = i1.next();
					Context cp2 = i2.next();
					Identifier id1 = cp1.getIdentifier();
					Identifier id2 = cp2.getIdentifier();
					int c;
					c = Boolean.compare(id1 == null, id2 == null);
					if (c != 0)
						return c;
					if (id1 != null)
					{
						c = id1.compareTo(id2);
						if (c != 0)
							return c;
					}
				}
				return Boolean.compare(i1.hasNext(), i2.hasNext());
			}
		};
		Collections.<Context> sort(list, comparator);
		for (Context st : list)
			getOut().println(" -> " + st.statementPathString(getTransaction(), ctx) + " " + (st.isProved() ? "\u2713" : ""));
		getOut().println("end.");
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<Solves>
	{

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		public Solves parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term = parseTerm(from.getActiveContext(), transaction, split.get(0));
			return new Solves(from, transaction, term);
		}

		@Override
		protected String paramSpec()
		{
			return "<term>";
		}

		@Override
		public String shortHelp()
		{
			return "Lists all the contexts descending from the active one that have the given term as a consequent.";
		}

	}

}
