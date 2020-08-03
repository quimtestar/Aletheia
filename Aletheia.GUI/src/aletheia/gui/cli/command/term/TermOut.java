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
package aletheia.gui.cli.command.term;

import java.util.List;

import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "term", groupPath = "/term", factory = TermOut.Factory.class)
public class TermOut extends TransactionalCommand
{
	private final Term term;
	private final ParameterIdentification parameterIdentification;
	private final boolean indent;

	public TermOut(CommandSource from, Transaction transaction, Term term, ParameterIdentification parameterIdentification, boolean indent)
	{
		super(from, transaction);
		this.term = term;
		this.parameterIdentification = parameterIdentification;
		this.indent = indent;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		if (indent)
			printTerm(getActiveContext(), getTransaction(), term, parameterIdentification);
		else
			getOut().println(termToString(getActiveContext(), getTransaction(), term, parameterIdentification));
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermOut>
	{

		@Override
		public TermOut parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			boolean indent = split.remove("-indent");
			checkMinParameters(split);
			Term term;
			ParameterIdentification parameterIdentification;
			if (split.size() < 1)
			{
				if (from.getActiveContext() == null)
					throw new CommandParseException(new NotActiveContextException());
				term = from.getActiveContext().getConsequent();
				parameterIdentification = from.getActiveContext().consequentParameterIdentification(transaction);
			}
			else
			{
				if (split.size() < 2)
				{
					TermParser.ParameterIdentifiedTerm parameterIdentifiedTerm = parseParameterIdentifiedTerm(from.getActiveContext(), transaction,
							split.get(0));
					term = parameterIdentifiedTerm.getTerm();
					parameterIdentification = parameterIdentifiedTerm.getParameterIdentification();
				}
				else
				{
					term = parseTerm(from.getActiveContext(), transaction, split.get(0));
					parameterIdentification = parseParameterIdentification(split.get(1));
				}
			}
			return new TermOut(from, transaction, term, parameterIdentification, indent);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "[<term> [<parameter identification>]] [-indent]";
		}

		@Override
		public String shortHelp()
		{
			return "Returns a parseable string corresponding to a given term (the active context's consequent by default).";
		}

	}

}
