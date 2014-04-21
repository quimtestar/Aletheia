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

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.tokens.EndToken;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.TerminalToken;

/**
 * A lexer is simply an object that offers a sequence of {@link TerminalToken}s.
 * The input it uses to produce that sequence is left to the classes
 * implementing this interface.
 */
public interface Lexer
{
	public class LexerException extends ParserLexerException
	{
		private static final long serialVersionUID = 9168128419101166987L;

		public LexerException(Location startLocation, Location stopLocation, String message, Throwable cause)
		{
			super(startLocation, stopLocation, message, cause);
		}

		public LexerException(Location startLocation, Location stopLocation, String message)
		{
			super(startLocation, stopLocation, message);
		}

		public LexerException(Location startLocation, Location stopLocation, Throwable cause)
		{
			super(startLocation, stopLocation, cause);
		}

		public LexerException(Location startLocation, Location stopLocation)
		{
			super(startLocation, stopLocation);
		}

		@Override
		public String getGenericMessage()
		{
			return "Lexer error";
		}

	}

	/**
	 * Moves the lexer forward to the next {@link TerminalToken} and returns it.
	 * If the lexer is at the end of the input it will return an
	 * {@link EndToken} in all the subsequent calls.
	 * 
	 * @return The next token.
	 * 
	 * @throws LexerException
	 * 
	 * @see TerminalToken
	 * @see EndToken
	 */
	TerminalToken readToken() throws LexerException;

	/**
	 * Returns the {@link Location} in the input was the last token read.
	 * 
	 * @return The location.
	 * 
	 * @see Location
	 */
	Location getLocation();

}
