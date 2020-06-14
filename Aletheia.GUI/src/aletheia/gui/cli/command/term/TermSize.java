/*******************************************************************************
 * Copyright (c) 2019, 2020 Quim Testar.
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

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "sizeof", groupPath = "/term", factory = TermSize.Factory.class)
public class TermSize extends TransactionalCommand
{
	private final Term term;

	public TermSize(CommandSource from, Transaction transaction, Term term)
	{
		super(from, transaction);
		this.term = term;
	}

	protected Term getTerm()
	{
		return term;
	}

	private class NoTypeException extends Exception
	{
		private static final long serialVersionUID = -4102190447062432105L;

		private NoTypeException(String message)
		{
			super(message);
		}
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NoTypeException, NotActiveContextException
	{
		getOut().println(term.size());
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermSize>
	{

		@Override
		public TermSize parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term = parseTerm(from.getActiveContext(), transaction, split.get(0));
			return new TermSize(from, transaction, term);
		}

		@Override
		protected int minParameters()
		{
			return 1;
		}

		@Override
		protected String paramSpec()
		{
			return "<term>";
		}

		@Override
		public String shortHelp()
		{
			return "Computes the size of a given term.";
		}

	}

}
