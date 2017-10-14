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
import java.util.Map.Entry;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.FunctionTerm;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.FunctionTerm.NullParameterTypeException;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefList;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.ReverseList;

@ProcessorProduction(left = "F", right =
{ "openfun", "TPL", "arrow", "T", "closefun" })
public class F_openfun_TPL_arrow_T_closefun_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected F_openfun_TPL_arrow_T_closefun_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, String input, Context context, Transaction transaction,
			Map<ParameterRef, ParameterVariableTerm> tempParameterTable, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		TypedParameterRefList typedParameterRefList = getProcessor().processTypedParameterRefList((NonTerminalToken) token.getChildren().get(1), input, context,
				transaction, tempParameterTable, parameterIdentifiers);
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(3), input, context, transaction, tempParameterTable,
				parameterIdentifiers);
		for (TypedParameterRef typedParameterRef : new ReverseList<>(typedParameterRefList.getList()))
		{
			try
			{
				term = new FunctionTerm(typedParameterRef.getParameter(), term);
			}
			catch (NullParameterTypeException e)
			{
				throw new AletheiaParserException(e, token.getStartLocation(), token.getStopLocation(), input);
			}
		}
		for (Entry<ParameterRef, ParameterVariableTerm> e : typedParameterRefList.getOldParameterTable().entrySet())
		{
			ParameterRef parameterRef = e.getKey();
			if (parameterRef != null)
			{
				ParameterVariableTerm oldPar = e.getValue();
				if (oldPar != null)
					tempParameterTable.put(parameterRef, oldPar);
				else
					tempParameterTable.remove(parameterRef);
			}
		}
		return term;
	}

}
