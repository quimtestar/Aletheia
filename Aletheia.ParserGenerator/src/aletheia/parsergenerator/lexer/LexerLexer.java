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

import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.symbols.TerminalSymbol;
import aletheia.parsergenerator.tokens.EndToken;
import aletheia.parsergenerator.tokens.Location;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.TerminalToken;

/**
 * <p>
 * A lexer for lexer specifications.
 * </p>
 * <p>
 * This lexer works in two distinct states, and recognizes different sets of
 * tokens depending on which state it is working. In the <b>initial state</b>
 * the following terminal token/symbols are recognized:
 * <table border="1">
 * <tr>
 * <th>Tag</th>
 * <th>Input</th>
 * <th>Token class</th>
 * <th>Comments</th>
 * </tr>
 * <tr>
 * <td>QUOTE</td>
 * <td>'</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>Switches the lexer to the <b>regular expression state</b>.</td>
 * </tr>
 * <tr>
 * <td>COLON</td>
 * <td>:</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>SEMICOLON</td>
 * <td>;</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>TAG</td>
 * <td>[a-zA-Z0-9_]+</td>
 * <td>{@link TagToken}</td>
 * <td>All the input characters that match it are taken in a greedy way.</td>
 * </tr>
 * </table>
 * In this state, the blank spaces are skipped.
 * </p>
 * <p>
 * In the <b>regular expression state</b> the following terminal token/symbols
 * are recognized:
 * <table border="1">
 * <tr>
 * <th>Tag</th>
 * <th>Input</th>
 * <th>Token class</th>
 * <th>Comments</th>
 * </tr>
 * <tr>
 * <td>CHAR</td>
 * <td>\c, or any character that hasn't matched in any other entry.</td>
 * <td>{@link CharToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>QUOTE</td>
 * <td>'</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>Switches the lexer to the <b>initial state</b>.</td>
 * </tr>
 * <tr>
 * <td>UNION</td>
 * <td>|</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>CARET</td>
 * <td>^</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>KLEENE</td>
 * <td>*</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>PLUS</td>
 * <td>+</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>QUESTION</td>
 * <td>?</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>OP</td>
 * <td>(</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>CP</td>
 * <td>)</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>OB</td>
 * <td>[</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>CB</td>
 * <td>]</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>OC</td>
 * <td>{</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>CC</td>
 * <td></td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>CC</td>
 * <td></td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>HYPHEN</td>
 * <td>-</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>COMMA</td>
 * <td>,</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>BLANK</td>
 * <td>_</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>EOL</td>
 * <td>$</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>DOT</td>
 * <td>.</td>
 * <td>{@link TaggedTerminalToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>NUMBER</td>
 * <td>[0-9]+</td>
 * <td>{@link NumberToken}</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table>
 * Since the blank space is just another token in this state, blank spaces
 * cannot be skipped in this state.
 * </p>
 */
public class LexerLexer extends AbstractLexer
{

	public static final TerminalSymbol sChar = new TaggedTerminalSymbol("CHAR");
	public static final TerminalSymbol sUnion = new TaggedTerminalSymbol("UNION");
	public static final TerminalSymbol sKleene = new TaggedTerminalSymbol("KLEENE");
	public static final TerminalSymbol sPlus = new TaggedTerminalSymbol("PLUS");
	public static final TerminalSymbol sOP = new TaggedTerminalSymbol("OP");
	public static final TerminalSymbol sCP = new TaggedTerminalSymbol("CP");
	public static final TerminalSymbol sOB = new TaggedTerminalSymbol("OB");
	public static final TerminalSymbol sCB = new TaggedTerminalSymbol("CB");
	public static final TerminalSymbol sOC = new TaggedTerminalSymbol("OC");
	public static final TerminalSymbol sCC = new TaggedTerminalSymbol("CC");
	public static final TerminalSymbol sComma = new TaggedTerminalSymbol("COMMA");
	public static final TerminalSymbol sNumber = new TaggedTerminalSymbol("NUMBER");
	public static final TerminalSymbol sHyphen = new TaggedTerminalSymbol("HYPHEN");
	public static final TerminalSymbol sQuestion = new TaggedTerminalSymbol("QUESTION");
	public static final TerminalSymbol sBlank = new TaggedTerminalSymbol("BLANK");
	public static final TerminalSymbol sEOL = new TaggedTerminalSymbol("EOL");
	public static final TerminalSymbol sDot = new TaggedTerminalSymbol("DOT");
	public static final TerminalSymbol sCaret = new TaggedTerminalSymbol("CARET");

	public static final TerminalSymbol sQuote = new TaggedTerminalSymbol("QUOTE");
	public static final TerminalSymbol sColon = new TaggedTerminalSymbol("COLON");
	public static final TerminalSymbol sSemiColon = new TaggedTerminalSymbol("SEMICOLON");
	public static final TerminalSymbol sTag = new TaggedTerminalSymbol("TAG");

	/**
	 * The token returned by the {@link LexerLexer} associated to the terminal
	 * symbol tagged with the string "CHAR". Records the actual character read
	 * from the input.
	 */
	public class CharToken extends TerminalToken
	{
		private final char c;

		/**
		 * Creates a new character token with a given character.
		 *
		 * @param startLocation
		 *            The start location.
		 * @param stopLocation
		 *            The stop location.
		 * @param c
		 *            The char read.
		 */
		public CharToken(Location startLocation, Location stopLocation, char c)
		{
			super(sChar, startLocation, stopLocation);
			this.c = c;
		}

		/**
		 * The read character.
		 *
		 * @return The character.
		 */
		public char getC()
		{
			return c;
		}

		@Override
		public String toString()
		{
			return super.toString() + ":[" + c + "]";
		}
	}

	/**
	 * The token returned by the {@link LexerLexer} associated to the terminal
	 * symbol tagged with the string "NUMBER". Records the actual number read
	 * from the input.
	 */
	public class NumberToken extends TerminalToken
	{
		private final int n;

		/**
		 * Creates a new number token with a given number.
		 *
		 * @param startLocation
		 *            The start location.
		 * @param stopLocation
		 *            The stop location.
		 * @param n
		 *            The number.
		 */
		public NumberToken(Location startLocation, Location stopLocation, int n)
		{
			super(sNumber, startLocation, stopLocation);
			this.n = n;
		}

		/**
		 * The number.
		 *
		 * @return The number.
		 */
		public int getN()
		{
			return n;
		}

		@Override
		public String toString()
		{
			return super.toString() + ":[" + n + "]";
		}
	}

	/**
	 * The token returned by the {@link LexerLexer} associated to the terminal
	 * symbol tagged with the string "TAG". Records the actual string read from
	 * the input.
	 */
	public class TagToken extends TerminalToken
	{
		private final String tag;

		/**
		 * Creates a new tag token with a given string tag.
		 *
		 * @param startLocation
		 *            The start location.
		 * @param stopLocation
		 *            The stop location.
		 * @param tag
		 *            The string tag.
		 */
		public TagToken(Location startLocation, Location stopLocation, String tag)
		{
			super(sTag, startLocation, stopLocation);
			this.tag = tag;
		}

		/**
		 * The tag.
		 *
		 * @return The tag.
		 */
		public String getTag()
		{
			return tag;
		}

		@Override
		public String toString()
		{
			return super.toString() + ":[" + tag + "]";
		}
	}

	private boolean inRegExp;

	public LexerLexer(Reader reader) throws LexerException
	{
		super(reader);
		inRegExp = false;
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
			if (inRegExp)
			{
				switch (getNext())
				{
				case '\\':
				{
					eat();
					if (isAtEnd())
						throw new LexerException(location, getLocation());
					char c = getNext();
					eat();
					return new CharToken(location, getLocation(), c);
				}
				case '\'':
					eat();
					inRegExp = false;
					return new TerminalToken(sQuote, location, getLocation());
				case '|':
					eat();
					return new TerminalToken(sUnion, location, getLocation());
				case '*':
					eat();
					return new TerminalToken(sKleene, location, getLocation());
				case '+':
					eat();
					return new TerminalToken(sPlus, location, getLocation());
				case '?':
					eat();
					return new TerminalToken(sQuestion, location, getLocation());
				case '(':
					eat();
					return new TerminalToken(sOP, location, getLocation());
				case ')':
					eat();
					return new TerminalToken(sCP, location, getLocation());
				case '[':
					eat();
					return new TerminalToken(sOB, location, getLocation());
				case ']':
					eat();
					return new TerminalToken(sCB, location, getLocation());
				case '{':
					eat();
					return new TerminalToken(sOC, location, getLocation());
				case '}':
					eat();
					return new TerminalToken(sCC, location, getLocation());
				case '-':
					eat();
					return new TerminalToken(sHyphen, location, getLocation());
				case ',':
					eat();
					return new TerminalToken(sComma, location, getLocation());
				case '_':
					eat();
					return new TerminalToken(sBlank, location, getLocation());
				case '$':
					eat();
					return new TerminalToken(sEOL, location, getLocation());
				case '.':
					eat();
					return new TerminalToken(sDot, location, getLocation());
				case '^':
					eat();
					return new TerminalToken(sCaret, location, getLocation());
				default:
				{
					if (getNext() >= '0' && getNext() <= '9')
					{
						int num = 0;
						while (!isAtEnd() && (getNext() >= '0' && getNext() <= '9'))
						{
							num *= 10;
							num += getNext() - '0';
							if (num < 0)
								throw new LexerException(location, getLocation(), "Number overflow");
							eat();
						}
						return new NumberToken(location, getLocation(), num);
					}
					else
					{
						char c = getNext();
						eat();
						return new CharToken(location, getLocation(), c);
					}
				}
				}
			}
			else
			{
				while (!isAtEnd() && Character.isWhitespace(getNext()))
					eat();
				if (isAtEnd())
					return new EndToken(location);
				location = getLocation();
				switch (getNext())
				{
				case '\'':
					eat();
					inRegExp = true;
					return new TerminalToken(sQuote, location, getLocation());
				case ':':
					eat();
					return new TerminalToken(sColon, location, getLocation());
				case ';':
					eat();
					return new TerminalToken(sSemiColon, location, getLocation());
				default:
				{
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
						return new TagToken(location, getLocation(), text);
					}
					else
						throw e;

				}
				}
			}
		}
	}
}
