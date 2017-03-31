/*******************************************************************************
 * Copyright (c) 2017 Quim Testar.
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
package aletheia.parser.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.TermParserException;
import aletheia.parser.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

public abstract class TermTokenSubProcessor extends TokenSubProcessor<Term, TermTokenSubProcessor.Parameter>
{

	public static class Parameter
	{
		private final Context context;
		private final Transaction transaction;
		private final Map<ParameterRef, ParameterVariableTerm> tempParameterTable;
		private final Map<ParameterVariableTerm, Identifier> parameterIdentifiers;

		public Parameter(Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
				Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
		{
			super();
			this.context = context;
			this.transaction = transaction;
			this.tempParameterTable = tempParameterTable;
			this.parameterIdentifiers = parameterIdentifiers;
		}

	}

	public TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Parameter parameter) throws TermParserException
	{
		return subProcess(token, input, parameter.context, parameter.transaction, parameter.tempParameterTable, parameter.parameterIdentifiers);
	}

	protected abstract Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TermParserException;

}
