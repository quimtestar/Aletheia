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
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "unf", factory = NewUnfoldingContext.Factory.class)
public class NewUnfoldingContext extends NewContext
{
	private final Declaration declaration;

	private NewUnfoldingContext(CliJPanel from, Transaction transaction, Identifier identifier, Term term,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers, Declaration declaration)
	{
		super(from, transaction, identifier, term, parameterIdentifiers);
		this.declaration = declaration;
	}

	protected Declaration getDeclaration()
	{
		return declaration;
	}

	@Override
	protected Context openSubContext() throws StatementException, NotActiveContextException
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		return ctx.openUnfoldingSubContext(getTransaction(), getTerm(), declaration);
	}

	public static class Factory extends AbstractNewStatementFactory<NewUnfoldingContext>
	{

		@Override
		public NewUnfoldingContext parse(CliJPanel cliJPanel, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			try
			{
				if (cliJPanel.getActiveContext() == null)
					throw new NotActiveContextException();
				Map<ParameterVariableTerm, Identifier> parameterIdentifiers = new HashMap<ParameterVariableTerm, Identifier>();
				Term term = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(0), parameterIdentifiers);
				Declaration declaration = (Declaration) cliJPanel.getActiveContext().identifierToStatement(transaction).get(Identifier.parse(split.get(1)));
				if (declaration == null)
					throw new CommandParseException("Bad unfolding statement:" + split.get(1));
				return new NewUnfoldingContext(cliJPanel, transaction, identifier, term, parameterIdentifiers, declaration);
			}
			catch (NotActiveContextException | InvalidNameException e)
			{
				throw new CommandParseException(e);
			}
			finally
			{

			}
		}

		@Override
		protected int minParameters()
		{
			return 2;
		}

		@Override
		protected String paramSpec()
		{
			return "<term> <declaration>";
		}

		@Override
		public String shortHelp()
		{
			return "Creates a new unfolding context statement with the given term and unfolding the given declaration.";
		}

	}

}
