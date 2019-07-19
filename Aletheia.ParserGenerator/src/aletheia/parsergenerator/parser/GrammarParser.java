/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;

/**
 * A parser of grammars.
 */
public class GrammarParser extends Parser
{
	private static final long serialVersionUID = -4943421743994955277L;

	private static final GrammarTransitionTable grammarTransitionTable;
	static
	{
		try
		{
			grammarTransitionTable = new GrammarTransitionTable();
		}
		catch (ConflictException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final GrammarTokenPayLoadReducer grammarTokenPayloadReducer = new GrammarTokenPayLoadReducer();

	public GrammarParser()
	{
		super(grammarTransitionTable);
	}

	/**
	 * Parses a grammar object from a grammar lexer.
	 *
	 * @param lexer
	 *            The lexer to extract the tokens.
	 * @return The parsed grammar.
	 * @throws ParserBaseException
	 */
	protected Grammar parse(GrammarLexer lexer) throws ParserBaseException
	{
		return (Grammar) parseToken(lexer, grammarTokenPayloadReducer);
	}

	/**
	 * Parses a grammar object from a generic {@link Reader}.
	 *
	 * @param reader
	 *            The reader to read from.
	 * @return The parsed grammar.
	 * @throws ParserBaseException
	 */
	public Grammar parse(Reader reader) throws ParserBaseException
	{
		return parse(new GrammarLexer(reader));
	}

}
