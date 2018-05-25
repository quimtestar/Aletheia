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
import aletheia.model.term.Term;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parsergenerator.semantic.ParseTreeToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "TP", right =
{ "T" })
public class TP_T_ParameterRefTokenSubProcessor extends TypedParameterRefTokenSubProcessor
{

	protected TP_T_ParameterRefTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected TypedParameterRef subProcess(ParseTreeToken token, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TokenProcessorException
	{
		Term type = getProcessor().processTerm((ParseTreeToken) token.getChildren().get(0), context, transaction, tempParameterTable);
		ParameterVariableTerm parameter = new ParameterVariableTerm(type);
		return new TypedParameterRef(null, parameter);
	}

}
