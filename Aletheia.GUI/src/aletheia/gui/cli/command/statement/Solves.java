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

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

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
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		Term term = this.term;
		for (Context st : ctx.descendantContextsByConsequent(getTransaction(), term))
		{
			List<? extends Statement> path = st.statementPath(getTransaction(), ctx);
			StringBuffer sbpath = new StringBuffer();
			for (Statement st2 : path)
			{
				if (!(st2 instanceof RootContext))
				{
					Identifier id = st2.identifier(getTransaction());
					if (id != null)
						sbpath.append(id.toString());
					else
						sbpath.append(st2.getVariable().toString());
				}
				if (!st2.equals(st))
					sbpath.append("/");
			}
			getOut().println(" -> " + sbpath);
		}
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
		public Solves parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(0));
			return new Solves(cliJPanel, transaction, term);
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
