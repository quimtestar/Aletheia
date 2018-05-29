/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.parsergenerator.lexer;

import java.io.Reader;

import aletheia.parsergenerator.LocationInterval;
import aletheia.parsergenerator.tokens.EndToken;
import aletheia.parsergenerator.tokens.TerminalToken;

/**
 * An abstract lexer that skips whitespace characters.
 */
public abstract class IgnoreWhitespaceLexer extends AbstractLexer
{

	public IgnoreWhitespaceLexer(Reader reader) throws LexerException
	{
		super(reader);
	}

	@Override
	public TerminalToken readToken() throws LexerException
	{
		while (!isAtEnd() && Character.isWhitespace(getNext()))
			eat();
		if (isAtEnd())
			return new EndToken(getLocation());
		throw new UnrecognizedInputException(new LocationInterval(getLocation()), getNext());
	}

}
