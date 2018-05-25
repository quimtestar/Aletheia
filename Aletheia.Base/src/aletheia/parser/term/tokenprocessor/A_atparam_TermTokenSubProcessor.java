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
import aletheia.model.term.VariableTerm;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.term.tokenprocessor.parameterRef.NumberedParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parsergenerator.semantic.ParseTreeToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.persistence.Transaction;

@ProcessorProduction(left = "A", right =
{ "atparam" })
public class A_atparam_TermTokenSubProcessor extends TermTokenSubProcessor
{
	protected A_atparam_TermTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Term subProcess(ParseTreeToken token, Context context, Transaction transaction, Map<ParameterRef, ParameterVariableTerm> tempParameterTable,
			Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TokenProcessorException
	{
		String atParam = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
		VariableTerm variable = tempParameterTable.get(new NumberedParameterRef(atParam));
		if (variable == null)
			throw new TokenProcessorException("Parameter:" + "'" + atParam + "'" + " not defined", token.getChildren().get(0).getStartLocation(),
					token.getChildren().get(0).getStopLocation());
		return variable;
	}

}
