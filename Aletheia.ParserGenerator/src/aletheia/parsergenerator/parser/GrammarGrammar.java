/*******************************************************************************
 * Copyright (c) 2014, 2017 Quim Testar.
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

import java.util.Arrays;

import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.symbols.TerminalSymbol;

/**
 * A grammar for grammar specifications. Intended to work with the
 * {@link GrammarLexer} lexer. The production rules are:
 *
 * <pre>
 * G -> identifier Q
 * Q -> Q P semicolon
 * Q -> semicolon
 * P -> identifier arrow R
 * R -> R identifier
 * R ->
 * </pre>
 *
 * Where the start symbol is G and <i>identifier</i>, <i>semicolon</i> and
 * <i>arrow</i> are the {@linkplain TerminalSymbol terminal symbols} defined at
 * the {@linkplain GrammarLexer grammar lexer} specification.
 */
public class GrammarGrammar extends Grammar
{
	private static final long serialVersionUID = -3794599560302883680L;

	public static final TaggedNonTerminalSymbol sG = new TaggedNonTerminalSymbol("G");
	public static final TaggedNonTerminalSymbol sQ = new TaggedNonTerminalSymbol("Q");
	public static final TaggedNonTerminalSymbol sP = new TaggedNonTerminalSymbol("P");
	public static final TaggedNonTerminalSymbol sR = new TaggedNonTerminalSymbol("R");

	/*
	 * @formatter:off
	 *
	 * G -> id Q
	 * Q -> Q P ;
	 * Q -> ;
	 * P -> id -> R
	 * R -> R id
	 * R ->
	 *
	 * @formatter:on
	 */
	public static final Production prodG = new Production(sG, Arrays.<Symbol> asList(GrammarLexer.sIdentifier, sQ));
	public static final Production prodQ = new Production(sQ, Arrays.<Symbol> asList(sQ, sP, GrammarLexer.sSemicolon));
	public static final Production prodQEmpty = new Production(sQ, Arrays.<Symbol> asList(GrammarLexer.sSemicolon));
	public static final Production prodP = new Production(sP, Arrays.<Symbol> asList(GrammarLexer.sIdentifier, GrammarLexer.sArrow, sR));
	public static final Production prodR = new Production(sR, Arrays.<Symbol> asList(sR, GrammarLexer.sIdentifier));
	public static final Production prodREmpty = new Production(sR, Arrays.<Symbol> asList());

	public GrammarGrammar()
	{
		super(Arrays.asList(prodG, prodQ, prodQEmpty, prodP, prodR, prodREmpty), sG);
	}

}
