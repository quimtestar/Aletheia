/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
import aletheia.model.term.ProjectedCastTypeTerm;
import aletheia.model.term.ProjectedCastTypeTerm.ProjectedCastTypeException;
import aletheia.model.term.Term;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "opensq", "T", "closesq" })
public class A_opensq_T_closesq_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_opensq_T_closesq_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException
	{
		try
		{
			return new ProjectedCastTypeTerm(
					getProcessor().processTerm((NonTerminalToken) token.getChildren().get(1), context, transaction, tempParameterTable));
		}
		catch (ProjectedCastTypeException e)
		{
			throw new TokenProcessorException(e, token.getChildren().get(1).getStartLocation(), token.getChildren().get(1).getStopLocation());
		}
	}

}
