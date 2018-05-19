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
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefWithValue;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "TP", right =
{ "P", "assignment", "T" })
public class TP_P_assignment_T_ParameterRefTokenSubProcessor extends TypedParameterRefTokenSubProcessor
{

	protected TP_P_assignment_T_ParameterRefTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected TypedParameterRef subProcess(NonTerminalToken token, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TokenProcessorException
	{
		ParameterRef parameterRef = getProcessor().processParameterRef((NonTerminalToken) token.getChildren().get(0));
		Term value = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(2), context, transaction, tempParameterTable);
		Term type = value.getType();
		if (type == null)
		{
			NonTerminalToken t = (NonTerminalToken) token.getChildren().get(2);
			throw new TokenProcessorException("Typeless substitution term", t.getStartLocation(), t.getStopLocation());
		}
		ParameterVariableTerm parameter = new ParameterVariableTerm(type);
		return new TypedParameterRefWithValue(parameterRef, parameter, value);
	}

}
