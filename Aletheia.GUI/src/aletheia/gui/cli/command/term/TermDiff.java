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

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "diff", groupPath = "/term", factory = TermDiff.Factory.class)
public class TermDiff extends TransactionalCommand
{
	private final Term term0;
	private final Term term1;

	public TermDiff(CliJPanel from, Transaction transaction, Term term0, Term term1)
	{
		super(from, transaction);
		this.term0 = term0;
		this.term1 = term1;
	}

	protected Term getTerm0()
	{
		return term0;
	}

	protected Term getTerm1()
	{
		return term1;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		if (getFrom().getActiveContext() == null)
			throw new NotActiveContextException();
		Term t0 = term0;
		Term t1 = term1;
		Term.DiffInfo di = t0.diff(t1);
		if (di instanceof Term.DiffInfoEqual)
			getOut().println("Terms are equal.");
		else
		{
			String sleft = di.toStringLeft(getFrom().getActiveContext().variableToIdentifier(getTransaction()));
			getErr().print("\u2190 ");
			while (!sleft.isEmpty())
			{
				int i1 = sleft.indexOf(Term.DiffInfo.beginMark);
				if (i1 < 0)
				{
					getErr().print(sleft);
					sleft = "";
				}
				else
				{
					getErr().print(sleft.substring(0, i1));
					sleft = sleft.substring(i1);
				}
				getErr().flush();
				int i2 = sleft.indexOf(Term.DiffInfo.endMark);
				if (i2 < 0)
				{
					getErrB().print(sleft);
					sleft = "";
				}
				else
				{
					i2 += Term.DiffInfo.endMark.length();
					getErrB().print(sleft.substring(0, i2));
					sleft = sleft.substring(i2);
				}
				getErrB().flush();
			}
			getErr().println();

			String sright = di.toStringRight(getFrom().getActiveContext().variableToIdentifier(getTransaction()));
			getErr().print("\u2192 ");
			while (!sright.isEmpty())
			{
				int i1 = sright.indexOf(Term.DiffInfo.beginMark);
				if (i1 < 0)
				{
					getErr().print(sright);
					sright = "";
				}
				else
				{
					getErr().print(sright.substring(0, i1));
					sright = sright.substring(i1);
				}
				getErr().flush();
				int i2 = sright.indexOf(Term.DiffInfo.endMark);
				if (i2 < 0)
				{
					getErrB().print(sright);
					sright = "";
				}
				else
				{
					i2 += Term.DiffInfo.endMark.length();
					getErrB().print(sright.substring(0, i2));
					sright = sright.substring(i2);
				}
				getErrB().flush();
			}
			getErr().println();

		}
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermDiff>
	{

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		public TermDiff parse(CliJPanel cliJPanel, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term0 = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(0));
			Term term1 = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(1));
			return new TermDiff(cliJPanel, transaction, term0, term1);
		}

		@Override
		protected String paramSpec()
		{
			return "<term> <term>";
		}

		@Override
		public String shortHelp()
		{
			return "Show the differences between two terms.";
		}

	}

}
