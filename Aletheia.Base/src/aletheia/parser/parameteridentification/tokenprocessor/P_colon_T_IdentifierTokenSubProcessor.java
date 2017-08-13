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
package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeParameterIdentification;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "P", right =
{ "colon", "T" })
public class P_colon_T_IdentifierTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected P_colon_T_IdentifierTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException
	{
		ParameterIdentification parameterType = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(2), input);
		if (parameterType == null)
			return null;
		else
			return new ParameterWithTypeParameterIdentification(null, parameterType);
	}

}
