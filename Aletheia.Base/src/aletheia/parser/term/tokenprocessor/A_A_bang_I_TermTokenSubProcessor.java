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
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ReplaceTypeException;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "A", "bang", "I" })
public class A_A_bang_I_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_A_bang_I_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(NonTerminalToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException
	{
		Term term = getProcessor().processTerm((NonTerminalToken) token.getChildren().get(0), context, transaction, tempParameterTable);
		Identifier identifier = getProcessor().processIdentifier((NonTerminalToken) token.getChildren().get(2));
		Statement statement = context.identifierToStatement(transaction).get(identifier);
		if (statement instanceof Declaration)
		{
			Declaration declaration = (Declaration) statement;
			try
			{
				return term.replace(declaration.getVariable(), declaration.getValue());
			}
			catch (ReplaceTypeException e)
			{
				throw new TokenProcessorException(e, token.getStartLocation(), token.getStopLocation());
			}
		}
		else
			throw new TokenProcessorException("Referenced statement: '" + identifier + "' after the bang must be a declaration",
					token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation());
	}

}
