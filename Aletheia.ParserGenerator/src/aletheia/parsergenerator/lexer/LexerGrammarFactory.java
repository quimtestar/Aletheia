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

import java.io.StringReader;

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.GrammarParser;

/**
 * Constructs the {@link Grammar} of the language used to specify lexers.
 */
public class LexerGrammarFactory
{

	private static Grammar lexerGrammar;

	static
	{
		//@formatter:off
		String sg = "L;" +  
			"L -> L S SEMICOLON;" +
			"L -> S SEMICOLON;" +
			"S -> QUOTE E QUOTE COLON TAG;" +
			"S -> QUOTE E QUOTE COLON;" + 
			"E -> E UNION T;" + 
			"E -> E CARET T;" +
			"E -> E HYPHEN T;" +
			"E -> T;" + 
			"T -> T F;" + 
			"T -> ;" + 
			"F -> CHAR;" + 
			"F -> BLANK;" + 
			"F -> EOL;" +
			"F -> DOT;" +
			"F -> F KLEENE;" + 
			"F -> F PLUS;" + 
			"F -> F QUESTION;" + 
			"F -> F R;" + 
			"F -> OP E CP;" + 
			"F -> OB D CB;" + 
			"R -> OC I CC;" + 
			"I -> NUMBER;" + 
			"I -> COMMA NUMBER;" + 
			"I -> NUMBER COMMA;" + 
			"I -> NUMBER COMMA NUMBER;" + 
			"D -> D C;" + 
			"D -> ;" + 
			"C -> CHAR;" + 
			"C -> CHAR HYPHEN CHAR;";
		//@formatter:on
		GrammarParser gP = new GrammarParser();
		try
		{
			lexerGrammar = gP.parse(new StringReader(sg));
		}
		catch (ParserLexerException e)
		{
			throw new Error(e);
		}

	}

	/**
	 * The constructed lexer grammar.
	 * 
	 * @return The lexer grammar.
	 */
	public static Grammar getLexerGrammar()
	{
		return lexerGrammar;
	}

}
