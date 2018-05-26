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

import aletheia.model.statement.Context;
import aletheia.model.statement.Statement;
import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.semantic.ParseTree;
import aletheia.persistence.Transaction;

public abstract class StatementTokenSubProcessor extends TokenSubProcessor<Statement, StatementTokenSubProcessor.Parameter>
{
	public static class Parameter
	{
		private final Context context;
		private final Transaction transaction;

		public Parameter(Context context, Transaction transaction)
		{
			super();
			this.context = context;
			this.transaction = transaction;
		}
	}

	protected StatementTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Statement subProcess(ParseTree token, Parameter parameter) throws TokenProcessorException
	{
		return subProcess(token, parameter.context, parameter.transaction);
	}

	public abstract Statement subProcess(ParseTree token, Context context, Transaction transaction) throws TokenProcessorException;

}
