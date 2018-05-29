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
package aletheia.parsergenerator.parser;

import java.io.Reader;

import aletheia.parsergenerator.Location;
import aletheia.parsergenerator.LocationInterval;
import aletheia.parsergenerator.lexer.IgnoreWhitespaceLexer;
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.tokens.EndToken;
import aletheia.parsergenerator.tokens.TerminalToken;

/**
 * <p>
 * A lexer for grammar specifications. This lexer ignores white spaces.
 * </p>
 * <p>
 * The following terminal token/symbols are recognized
 * <table border="1">
 * <tr>
 * <th>Tag</th>
 * <th>Input</th>
 * <th>Token class</th>
 * <th>Comments</th>
 * </tr>
 * <tr>
 * <td>IDENTIFIER</td>
 * <td>[a-zA-Z0-9_]+</td>
 * <td>{@link IdentifierToken}</td>
 * <td>All the input characters that match it are taken in a greedy way.</td>
 * </tr>
 * <tr>
 * <td>ARROW</td>
 * <td>-></td>
 * <td>{@link ArrowToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>SEMICOLON</td>
 * <td>;</td>
 * <td>{@link SemicolonToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table>
 * </p>
 */
public class GrammarLexer extends IgnoreWhitespaceLexer
{
	public static final TaggedTerminalSymbol sIdentifier = new TaggedTerminalSymbol("IDENTIFIER");
	public static final TaggedTerminalSymbol sArrow = new TaggedTerminalSymbol("ARROW");
	public static final TaggedTerminalSymbol sSemicolon = new TaggedTerminalSymbol("SEMICOLON");

	/**
	 * The token returned by the {@link GrammarLexer} associated to the terminal
	 * symbol tagged with the string "IDENTIFIER". Records the actual string
	 * identifier read from the input.
	 */
	public class IdentifierToken extends TerminalToken
	{
		private final String text;

		public IdentifierToken(LocationInterval locationInterval, String text)
		{
			super(sIdentifier, locationInterval);
			this.text = text;
		}

		public String getText()
		{
			return text;
		}
	}

	/**
	 * The token returned by the {@link GrammarLexer} associated to the terminal
	 * symbol tagged with the string "ARROW".
	 */
	public class ArrowToken extends TerminalToken
	{
		public ArrowToken(LocationInterval locationInterval)
		{
			super(sArrow, locationInterval);
		}
	}

	/**
	 * The token returned by the {@link GrammarLexer} associated to the terminal
	 * symbol tagged with the string "SEMICOLON".
	 */
	public class SemicolonToken extends TerminalToken
	{
		public SemicolonToken(LocationInterval locationInterval)
		{
			super(sSemicolon, locationInterval);
		}
	}

	public GrammarLexer(Reader reader) throws LexerException
	{
		super(reader);
	}

	@Override
	public TerminalToken readToken() throws LexerException
	{
		Location location = getLocation();
		try
		{
			return super.readToken();
		}
		catch (UnrecognizedInputException e)
		{
			while (getNext() == '#')
			{
				eat();
				while (!isAtEnd() && getNext() != '\n')
					eat();
				if (isAtEnd())
					return new EndToken(getLocation());
				eat();
				while (!isAtEnd() && Character.isWhitespace(getNext()))
					eat();
				if (isAtEnd())
					return new EndToken(getLocation());
			}
			if (((getNext() >= 'a') && (getNext() <= 'z')) || ((getNext() >= 'A') && (getNext() <= 'Z')) || (getNext() == '_')
					|| ((getNext() >= '0') && (getNext() <= '9')))
			{
				StringBuffer sb = new StringBuffer();
				sb.append(getNext());
				eat();
				while (!isAtEnd() && (((getNext() >= 'a') && (getNext() <= 'z')) || ((getNext() >= 'A') && (getNext() <= 'Z')) || (getNext() == '_')
						|| ((getNext() >= '0') && (getNext() <= '9'))))
				{
					sb.append(getNext());
					eat();
				}
				String text = sb.toString();
				return new IdentifierToken(new LocationInterval(location, getLocation()), text);
			}
			else if (getNext() == ';')
			{
				eat();
				return new SemicolonToken(new LocationInterval(location, getLocation()));
			}
			else if (getNext() == '-')
			{
				eat();
				if (!isAtEnd() && getNext() == '>')
				{
					eat();
					return new ArrowToken(new LocationInterval(location, getLocation()));
				}
				else
					throw new UnrecognizedInputException(new LocationInterval(location, getLocation()), getNext());
			}
			else
				throw new UnrecognizedInputException(new LocationInterval(location, getLocation()), getNext());
		}
	}
}
