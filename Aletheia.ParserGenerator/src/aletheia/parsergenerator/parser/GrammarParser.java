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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

/**
 * A parser of grammars.
 */
public class GrammarParser extends Parser
{
	private static final long serialVersionUID = -4943421743994955277L;

	public GrammarParser()
	{
		super(grammarTransitionTable());
	}

	private static GrammarTransitionTable grammarTransitionTable()
	{
		try
		{
			return new GrammarTransitionTable();
		}
		catch (ConflictException e)
		{
			throw new Error(e);
		}
	}

	/**
	 * Parses a grammar object from a grammar lexer.
	 *
	 * @param lexer
	 *            The lexer to extract the tokens.
	 * @return The parsed grammar.
	 * @throws ParserLexerException
	 */
	protected Grammar parse(GrammarLexer lexer) throws ParserLexerException
	{
		NonTerminalToken token = parseToken(lexer);
		Set<String> leftTags = new HashSet<>();
		leftTags(token, leftTags);
		Map<String, TaggedNonTerminalSymbol> mapLeft = new HashMap<>();
		for (String tag : leftTags)
			mapLeft.put(tag, new TaggedNonTerminalSymbol(tag));
		Set<String> allTags = new HashSet<>();
		rightTags(token, allTags);
		Map<String, Symbol> mapSymbols = new HashMap<>();
		for (String tag : allTags)
		{
			if (!leftTags.contains(tag))
				mapSymbols.put(tag, new TaggedTerminalSymbol(tag));
		}
		mapSymbols.putAll(mapLeft);
		String startTag = startTag(token);
		TaggedNonTerminalSymbol startSymbol = mapLeft.get(startTag);
		Set<Production> productions = new HashSet<>();
		productions(token, mapSymbols, productions);
		return new Grammar(productions, startSymbol);
	}

	/**
	 * Parses a grammar object from a generic {@link Reader}.
	 *
	 * @param reader
	 *            The reader to read from.
	 * @return The parsed grammar.
	 * @throws ParserLexerException
	 */
	public Grammar parse(Reader reader) throws ParserLexerException
	{
		return parse(new GrammarLexer(reader));
	}

	private void productions(NonTerminalToken token, Map<String, Symbol> mapSymbols, Set<Production> set)
	{
		if (token.getProduction().equals(GrammarGrammar.prodG))
			productions((NonTerminalToken) token.getChildren().get(1), mapSymbols, set);
		else if (token.getProduction().equals(GrammarGrammar.prodQ))
		{
			productions((NonTerminalToken) token.getChildren().get(0), mapSymbols, set);
			productions((NonTerminalToken) token.getChildren().get(1), mapSymbols, set);
		}
		else if (token.getProduction().equals(GrammarGrammar.prodP))
		{
			set.add(production(token, mapSymbols));
		}
	}

	private Production production(NonTerminalToken token, Map<String, Symbol> mapSymbols)
	{
		NonTerminalSymbol left = (NonTerminalSymbol) mapSymbols.get(((GrammarLexer.IdentifierToken) token.getChildren().get(0)).getText());
		List<Symbol> right = new ArrayList<>();
		rightProduction((NonTerminalToken) token.getChildren().get(2), mapSymbols, right);
		return new Production(left, right);
	}

	private void rightProduction(NonTerminalToken token, Map<String, Symbol> mapSymbols, List<Symbol> right)
	{
		if (token.getProduction().equals(GrammarGrammar.prodR))
		{
			rightProduction((NonTerminalToken) token.getChildren().get(0), mapSymbols, right);
			Symbol s = mapSymbols.get(((GrammarLexer.IdentifierToken) token.getChildren().get(1)).getText());
			right.add(s);
		}
	}

	private void leftTags(NonTerminalToken token, Set<String> set)
	{
		if (token.getProduction().equals(GrammarGrammar.prodG))
			leftTags((NonTerminalToken) token.getChildren().get(1), set);
		else if (token.getProduction().equals(GrammarGrammar.prodQ))
		{
			leftTags((NonTerminalToken) token.getChildren().get(0), set);
			leftTags((NonTerminalToken) token.getChildren().get(1), set);
		}
		else if (token.getProduction().equals(GrammarGrammar.prodP))
		{
			set.add(((GrammarLexer.IdentifierToken) token.getChildren().get(0)).getText());
		}
	}

	private void rightTags(NonTerminalToken token, Set<String> set)
	{
		if (token.getProduction().equals(GrammarGrammar.prodG))
			rightTags((NonTerminalToken) token.getChildren().get(1), set);
		else if (token.getProduction().equals(GrammarGrammar.prodQ))
		{
			rightTags((NonTerminalToken) token.getChildren().get(0), set);
			rightTags((NonTerminalToken) token.getChildren().get(1), set);
		}
		else if (token.getProduction().equals(GrammarGrammar.prodP))
		{
			rightTags((NonTerminalToken) token.getChildren().get(2), set);
		}
		else if (token.getProduction().equals(GrammarGrammar.prodR))
		{
			rightTags((NonTerminalToken) token.getChildren().get(0), set);
			set.add(((GrammarLexer.IdentifierToken) token.getChildren().get(1)).getText());
		}
	}

	private String startTag(NonTerminalToken token)
	{
		if (!token.getProduction().equals(GrammarGrammar.prodG))
			throw new Error();
		Token<?> child = token.getChildren().get(0);
		if (!(child instanceof GrammarLexer.IdentifierToken))
			throw new Error();
		return ((GrammarLexer.IdentifierToken) child).getText();
	}

}
