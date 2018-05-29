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

import java.io.IOException;
import java.io.Reader;

import aletheia.parsergenerator.Location;
import aletheia.parsergenerator.LocationInterval;
import aletheia.parsergenerator.tokens.EndToken;
import aletheia.parsergenerator.tokens.TerminalToken;

/**
 * <p>
 * A basic abstract {@link Lexer} that obtains its input from a {@link Reader}.
 * This implementation can only recognize the end of input, the method
 * {@link #readToken()} should be overriden, call the superclass method, catch
 * the {@link UnrecognizedInputException} and do the extra recognition job
 * there.
 * </p>
 * <p>
 * Keeps track of the position (line, column) of the input that is being
 * processed.
 * </p>
 */
public abstract class AbstractLexer implements Lexer
{

	protected final Reader reader;
	private char next;
	private boolean atEnd;
	private int line;
	private int column;

	/**
	 * Creates a new abstract lexer on a given {@link Reader}.
	 *
	 * @param reader
	 *            The reader.
	 * @throws LexerException
	 */
	public AbstractLexer(Reader reader) throws LexerException
	{
		super();
		this.reader = reader;
		this.line = Location.initial.line;
		this.column = Location.initial.column;
		eat();
	}

	/**
	 * Moves the pointer forward, skipping all the input that may be skipped.
	 *
	 * @throws LexerException
	 */
	protected void eat() throws LexerException
	{
		if (!atEnd)
		{
			int r;
			try
			{
				r = reader.read();
				if (r == '\n')
				{
					column = 1;
					line++;
				}
				else
					column++;
			}
			catch (IOException e)
			{
				throw new LexerException(new LocationInterval(getLocation()), e);
			}
			if (r >= 0)
			{
				next = (char) r;
				atEnd = false;
			}
			else
				atEnd = true;
		}
	}

	/**
	 * The next char in the input.
	 *
	 * @return The next char
	 */
	protected char getNext()
	{
		return next;
	}

	/**
	 * Is the input stream at end of input.
	 *
	 * @return Is the input stream at end.
	 */
	protected boolean isAtEnd()
	{
		return atEnd;
	}

	public class UnrecognizedInputException extends LexerException
	{
		private static final long serialVersionUID = -4974313977692162798L;

		private final char input;

		public UnrecognizedInputException(LocationInterval locationInterval, char input)
		{
			super(locationInterval, "Unrecognized input: '" + input + "'");
			this.input = input;
		}

		public char getInput()
		{
			return input;
		}
	}

	@Override
	public TerminalToken readToken() throws LexerException
	{
		if (atEnd)
			return new EndToken(new Location(line, column));
		throw new UnrecognizedInputException(new LocationInterval(getLocation()), next);
	}

	@Override
	public Location getLocation()
	{
		return new Location(line, column);
	}

}
