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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.RootContext;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "root", factory = NewRootContext.Factory.class)
public class NewRootContext extends NewContext
{
	private NewRootContext(CliJPanel from, Transaction transaction, Identifier identifier, Term term,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
	{
		super(from, transaction, identifier, term, parameterIdentifiers);
	}

	@Override
	protected Context openSubContext() throws StatementException
	{
		return RootContext.create(getPersistenceManager(), getTransaction(), getTerm());
	}

	public static class Factory extends AbstractNewStatementFactory<NewRootContext>
	{

		@Override
		public NewRootContext parse(CliJPanel cliJPanel, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers = new HashMap<ParameterVariableTerm, Identifier>();
			Term term = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(0), parameterIdentifiers);
			return new NewRootContext(cliJPanel, transaction, identifier, term, parameterIdentifiers);
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
			return "Creates a new root context with the specified term.";
		}

	}

}