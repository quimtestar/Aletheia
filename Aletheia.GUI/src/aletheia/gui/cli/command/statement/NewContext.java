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
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Statement;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "ctx", factory = NewContext.Factory.class)
public class NewContext extends NewStatement
{
	private final Term term;

	public NewContext(CliJPanel from, Transaction transaction, Identifier identifier, Term term)
	{
		super(from, transaction, identifier);
		this.term = term;
	}

	protected Term getTerm()
	{
		return term;
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		if (getFrom().getActiveContext() == null)
			throw new NotActiveContextException();
		Statement statement = getFrom().getActiveContext().openSubContext(getTransaction(), term);
		return new RunNewStatementReturnData(statement);
	}

	public static class Factory extends AbstractNewStatementFactory<NewContext>
	{

		@Override
		public NewContext parse(CliJPanel cliJPanel, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Term term = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(0));
			return new NewContext(cliJPanel, transaction, identifier, term);
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
			return "Creates a context statement with the specified term.";
		}

	}

}
