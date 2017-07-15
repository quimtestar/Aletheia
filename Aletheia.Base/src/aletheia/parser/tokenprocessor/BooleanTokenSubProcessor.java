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
package aletheia.parser.tokenprocessor;

import aletheia.parser.TermParserException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

public abstract class BooleanTokenSubProcessor extends TokenSubProcessor<Boolean, Void>
{

	protected BooleanTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected Boolean subProcess(NonTerminalToken token, String input, Void parameter) throws TermParserException
	{
		return subProcess(token, input);
	}

	protected abstract boolean subProcess(NonTerminalToken token, String input) throws TermParserException;

}