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
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.semantic.ParseTree;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "B", right =
{ "B", "bar", "Q" })
public class B_B_bar_Q_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected B_B_bar_Q_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(ParseTree token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException
	{
		Term term = getProcessor().processTerm((ParseTree) token.getChildren().get(0), context, transaction, tempParameterTable);
		Term oldTerm = getProcessor().processTerm((ParseTree) token.getChildren().get(2), context, transaction, tempParameterTable);
		ParameterVariableTerm param = new ParameterVariableTerm(oldTerm.getType());
		try
		{
			return new FunctionTerm(param, term.replaceSubterm(oldTerm, param));
		}
		catch (ReplaceTypeException e)
		{
			throw new TokenProcessorException(e, token.getStartLocation(), token.getStopLocation());
		}
	}

}
