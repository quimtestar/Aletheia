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

import aletheia.model.identifier.Identifier;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithType;
import aletheia.parsergenerator.semantic.ParseTreeToken;

@ProcessorProduction(left = "P", right =
{ "I" })
public class P_I_IdentifierTokenSubProcessor extends ParameterWithTypeTokenSubProcessor
{

	protected P_I_IdentifierTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterWithType subProcess(ParseTreeToken token) throws TokenProcessorException
	{
		Identifier identifier = getProcessor().processIdentifier((ParseTreeToken) token.getChildren().get(0));
		return new ParameterWithType(identifier, null);
	}

}
