/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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

import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.TaggedCommand;
import aletheia.model.identifier.Identifier;
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Assumption;
import aletheia.model.statement.Context;
import aletheia.model.statement.Statement.StatementException;
import aletheia.parser.term.TermParser;
import aletheia.persistence.Transaction;

@TaggedCommand(tag = "ctx", factory = NewContext.Factory.class)
public class NewContext extends NewStatement
{
	private final TermParser.ParameterIdentifiedTerm parameterIdentifiedTerm;

	public NewContext(CommandSource from, Transaction transaction, Identifier identifier, TermParser.ParameterIdentifiedTerm parameterIdentifiedTerm)
	{
		super(from, transaction, identifier);
		this.parameterIdentifiedTerm = parameterIdentifiedTerm;
	}

	protected TermParser.ParameterIdentifiedTerm getParameterIdentifiedTerm()
	{
		return parameterIdentifiedTerm;
	}

	protected Context openSubContext() throws StatementException, NotActiveContextException
	{
		Context ctx = getActiveContext();
		if (ctx == null)
			throw new NotActiveContextException();
		return ctx.openSubContext(getTransaction(), parameterIdentifiedTerm.getTerm());
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
		Context context = openSubContext();
		ParameterIdentification parameterIdentification = parameterIdentifiedTerm.getParameterIdentification();
		Iterator<Assumption> assumptionIterator = context.assumptions(getTransaction()).iterator();
		Map<Identifier, Assumption> identifyAssumptions = new HashMap<>();
		while (parameterIdentification instanceof FunctionParameterIdentification)
		{
			if (!assumptionIterator.hasNext())
				break;
			Assumption assumption = assumptionIterator.next();
			FunctionParameterIdentification functionParameterIdentification = (FunctionParameterIdentification) parameterIdentification;
			Identifier identifier = functionParameterIdentification.getParameter();
			if (identifier != null && !identifier.equals(underscore))
				identifyAssumptions.put(identifier, assumption);
			assumption.updateTermParameterIdentification(getTransaction(), functionParameterIdentification.getDomain());
			parameterIdentification = functionParameterIdentification.getBody();
		}
		context.updateConsequentParameterIdentification(getTransaction(), parameterIdentification);
		for (Map.Entry<Identifier, Assumption> e : identifyAssumptions.entrySet())
			e.getValue().identify(getTransaction(), e.getKey());
		return new RunNewStatementReturnData(context);
	}

	public static class Factory extends AbstractNewStatementFactory<NewContext>
	{

		@Override
		public NewContext parse(CommandSource from, Transaction transaction, Identifier identifier, List<String> split) throws CommandParseException
		{
			checkMinParameters(split);
			TermParser.ParameterIdentifiedTerm parameterIdentifiedTerm = parseParameterIdentifiedTerm(from.getActiveContext(), transaction, split.get(0));
			return new NewContext(from, transaction, identifier, parameterIdentifiedTerm);
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
