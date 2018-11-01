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
package aletheia.gui.cli.command.parameteridentification;

import java.util.List;

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.AbstractVoidCommandFactory;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.gui.cli.command.TransactionalCommand;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.parser.term.TermParser;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "tpi", groupPath = "/pi", factory = TermParameterIdentification.Factory.class)
public class TermParameterIdentification extends TransactionalCommand
{
	private final ParameterIdentification parameterIdentification;

	public TermParameterIdentification(CommandSource from, Transaction transaction, ParameterIdentification parameterIdentification)
	{
		super(from, transaction);
		this.parameterIdentification = parameterIdentification;
	}

	@Override
	protected RunTransactionalReturnData runTransactional() throws NotActiveContextException
	{
		if (parameterIdentification != null)
			getOut().println(parameterIdentification);
		return null;
	}

	public static class Factory extends AbstractVoidCommandFactory<TermParameterIdentification>
	{

		@Override
		public TermParameterIdentification parse(CommandSource from, Transaction transaction, Void extra, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			TermParser.ParameterIdentifiedTerm parameterIdentifiedTerm = parseParameterIdentifiedTerm(from.getActiveContext(), transaction, split.get(0));
			return new TermParameterIdentification(from, transaction, parameterIdentifiedTerm.getParameterIdentification());
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
			return "Returns the parsed parameter identification of a given term.";
		}

	}

}
