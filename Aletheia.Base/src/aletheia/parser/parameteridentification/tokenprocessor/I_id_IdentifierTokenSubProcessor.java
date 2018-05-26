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
import aletheia.model.identifier.NodeNamespace.InvalidNameException;
import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.semantic.ParseTree;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;

@ProcessorProduction(left = "I", right =
{ "id" })
public class I_id_IdentifierTokenSubProcessor extends IdentifierTokenSubProcessor
{

	protected I_id_IdentifierTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Identifier subProcess(ParseTree token) throws TokenProcessorException
	{
		String name = ((TaggedTerminalToken) token.getChildren().get(0)).getText();
		try
		{
			return new Identifier(name);
		}
		catch (InvalidNameException e)
		{
			throw new TokenProcessorException(e, token.getChildren().get(0).getStartLocation(), token.getChildren().get(0).getStopLocation());
		}
	}

}
