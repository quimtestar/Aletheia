/*******************************************************************************
 * Copyright (c) 2016, 2018 Quim Testar.
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

import java.awt.Toolkit;
import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.gui.common.datatransfer.TermTransferable;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "tcb", groupPath = "/term", factory = TermCb.Factory.class)
public class TermCb extends TransactionalCommand
{
	private final Term term;
	private final ParameterIdentification parameterIdentification;

	public TermCb(CommandSource from, Transaction transaction, Term term, ParameterIdentification parameterIdentification)
	{
		super(from, transaction);
		this.term = term;
		this.parameterIdentification = parameterIdentification;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TermTransferable(term, getActiveContext(), parameterIdentification), null);
		getOut().println(termToString(getActiveContext(), getTransaction(), term, parameterIdentification));
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermCb>
	{
		@Override
		public TermCb parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term;
			ParameterIdentification parameterIdentification = null;
			if (split.size() < 1)
			{
				if (from.getActiveContext() == null)
					throw new CommandParseException(new NotActiveContextException());
				term = from.getActiveContext().getConsequent();
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
			return new TermCb(from, transaction, term, parameterIdentification);
		}

		@Override
		protected int minParameters()
		{
			return 0;
		}

		@Override
		protected String paramSpec()
		{
			return "[<term> [<parameter identification>]]";
		}

		@Override
		public String shortHelp()
		{
			return "Copies a parsed term to the system clipboard (the active context's consequent by default).";
		}

	}

}
