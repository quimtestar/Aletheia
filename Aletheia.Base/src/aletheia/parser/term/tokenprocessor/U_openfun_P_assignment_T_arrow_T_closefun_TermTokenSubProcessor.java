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
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.term.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "U", right =
{ "openfun", "P", "assignment", "T", "arrow", "T", "closefun" })
public class U_openfun_P_assignment_T_arrow_T_closefun_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected U_openfun_P_assignment_T_arrow_T_closefun_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		ParameterRef parameterRef = getProcessor().processParameterRef((NonTerminalToken) token.getChildren().get(1), input);
		Term value = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(3), input, context, transaction, tempParameterTable);
		ParameterVariableTerm parameter = new ParameterVariableTerm(value.getType());
		if (parameterIdentifiers != null && parameterRef instanceof IdentifierParameterRef)
			parameterIdentifiers.put(parameter, ((IdentifierParameterRef) parameterRef).getIdentifier());
		ParameterVariableTerm oldpar = tempParameterTable.put(parameterRef, parameter);
		try
		{
			Term body = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(5), input, context, transaction, tempParameterTable,
					parameterIdentifiers);
			try
			{
				return new FunctionTerm(parameter, body).compose(value);
			}
			catch (ComposeTypeException | NullParameterTypeException e)
			{
				throw new AletheiaParserException(e, token.getStartLocation(), token.getStopLocation(), input);
			}
		}
		finally
		{
			if (oldpar != null)
				tempParameterTable.put(parameterRef, oldpar);
			else
				tempParameterTable.remove(parameterRef);
		}
	}

}
