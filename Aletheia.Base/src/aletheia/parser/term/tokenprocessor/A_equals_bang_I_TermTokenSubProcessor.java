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
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.tokens.ParseTreeToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "equals", "bang", "I" })
public class A_equals_bang_I_TermTokenSubProcessor extends TermTokenSubProcessor
{

	protected A_equals_bang_I_TermTokenSubProcessor(TokenProcessor tokenProcessor)
	{
		super(tokenProcessor);
	}

	@Override
	protected Term subProcess(ParseTreeToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException
	{
		Identifier identifier = getProcessor().processIdentifier((ParseTreeToken) token.getChildren().get(2));
		Statement statement = context.identifierToStatement(transaction).get(identifier);
		if (statement instanceof Declaration)
			return ((Declaration) statement).getValue();
		else
			throw new TokenProcessorException("Referenced statement: '" + identifier + "' after the bang must be a declaration",
					token.getChildren().get(2).getStartLocation(), token.getChildren().get(2).getStopLocation());
	}

}
