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

import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.tokens.ParseTreeToken;

@ProcessorProduction(left = "MP", right =
{ "MP", "AN" })
public class MP_MP_AN_IntegerTokenSubProcessor extends IntegerTokenSubProcessor
{

	protected MP_MP_AN_IntegerTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected int subProcess(ParseTreeToken token) throws TokenProcessorException
	{
		int nMP = getProcessor().processInteger((ParseTreeToken) token.getChildren().get(0));
		int nAN = getProcessor().processInteger((ParseTreeToken) token.getChildren().get(1));
		return nMP + nAN;
	}

}
