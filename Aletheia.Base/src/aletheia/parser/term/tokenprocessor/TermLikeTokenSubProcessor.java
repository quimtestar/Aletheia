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
package aletheia.parser.term.tokenprocessor;

import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.ParseTreeToken;
import aletheia.persistence.Transaction;

public abstract class TermLikeTokenSubProcessor<R> extends TokenSubProcessor<R, TermLikeTokenSubProcessor.Parameter>
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

	public TermLikeTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected R subProcess(ParseTreeToken token, Parameter parameter) throws TokenProcessorException
	{
		return subProcess(token, parameter.context, parameter.transaction, parameter.tempParameterTable, parameter.parameterIdentifiers);
	}

	protected abstract R subProcess(ParseTreeToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException;

	protected R subProcess(ParseTreeToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable)
			throws TokenProcessorException
	{
		return subProcess(token, context, transaction, tempParameterTable, null);
	}

}
