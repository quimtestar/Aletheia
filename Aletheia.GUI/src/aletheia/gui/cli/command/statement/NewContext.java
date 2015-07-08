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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import aletheia.gui.cli.CliJPanel;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement.StatementException;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "ctx", factory = NewContext.Factory.class)
public class NewContext extends NewStatement
{
	private final Term term;
	private final Map<ParameterVariableTerm, Identifier> parameterIdentifiers;

	public NewContext(CliJPanel from, Transaction transaction, Identifier identifier, Term term, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
	{
		super(from, transaction, identifier);
		this.term = term;
		this.parameterIdentifiers = parameterIdentifiers;
	}

	protected Term getTerm()
	{
		return term;
	}

	protected Context openSubContext() throws StatementException
	{
		return getFrom().getActiveContext().openSubContext(getTransaction(), term);
	}

	private static final Identifier underscore;
	static
	{
		try
		{
			underscore = Identifier.parse("_");
		}
		catch (InvalidNameException e)
		{
			throw new Error(e);
		}
	}

	@Override
	protected RunNewStatementReturnData runNewStatement() throws Exception
	{
		if (getFrom().getActiveContext() == null)
			throw new NotActiveContextException();
		Context context = openSubContext();
		Term body = term;
		Iterator<Assumption> assumptionIterator = context.assumptions(getTransaction()).iterator();
		Map<Identifier, Assumption> identifyAssumptions = new HashMap<Identifier, Assumption>();
		while (body instanceof FunctionTerm)
		{
			if (!assumptionIterator.hasNext())
				break;
			Assumption assumption = assumptionIterator.next();
			FunctionTerm function = (FunctionTerm) body;
			Identifier identifier = parameterIdentifiers.get(function.getParameter());
			if (identifier != null && !identifier.equals(underscore))
				identifyAssumptions.put(identifier, assumption);
			body = function.getBody();
		}
		for (Map.Entry<Identifier, Assumption> e : identifyAssumptions.entrySet())
			e.getValue().identify(getTransaction(), e.getKey());
		return new RunNewStatementReturnData(context);
	}

	public static class Factory extends AbstractNewStatementFactory<NewContext>
	{

		@Override
		public NewContext parse(CliJPanel cliJPanel, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers = new HashMap<ParameterVariableTerm, Identifier>();
			Term term = parseTerm(cliJPanel.getActiveContext(), transaction, split.get(0), parameterIdentifiers);
			return new NewContext(cliJPanel, transaction, identifier, term, parameterIdentifiers);
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
