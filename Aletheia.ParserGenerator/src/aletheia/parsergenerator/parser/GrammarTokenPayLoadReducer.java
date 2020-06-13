/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import aletheia.parsergenerator.parser.GrammarLexer.IdentifierToken;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class GrammarTokenPayLoadReducer extends ProductionManagedTokenPayloadReducer<Void, Object>
{
	//@formatter:off
	private static final Collection<Class<? extends ProductionTokenPayloadReducer<Void, ?>>> productionTokenPayloadReducerClasses=
			Arrays.asList(
					G__IDENTIFIER_Q_TokenReducer.class,
					Q__Q_P_SEMICOLON_TokenReducer.class,
					Q__SEMICOLON_TokenReducer.class,
					P__IDENTIFIER_ARROW_R_TokenReducer.class,
					R__R_IDENTIFIER_TokenReducer.class,
					R___TokenReducer.class);
	//@formatter:on

	@AssociatedProduction(left = "G", right =
	{ "IDENTIFIER", "Q" })
	public static class G__IDENTIFIER_Q_TokenReducer extends ProductionTokenPayloadReducer<Void, Grammar>
	{
		@Override
		public Grammar reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			TaggedNonTerminalSymbol start = new TaggedNonTerminalSymbol(((IdentifierToken) reducees.get(0)).getText());
			Collection<Production> productions = new ArrayList<>();
			Map<String, Collection<List<String>>> preGrammar = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			for (Entry<String, Collection<List<String>>> e : preGrammar.entrySet())
			{
				TaggedNonTerminalSymbol left = new TaggedNonTerminalSymbol(e.getKey());
				for (List<String> sr : e.getValue())
				{
					List<Symbol> right = new ArrayList<>();
					for (String s : sr)
						right.add(preGrammar.containsKey(s) ? new TaggedNonTerminalSymbol(s) : new TaggedTerminalSymbol(s));
					productions.add(new Production(left, right));
				}
			}
			return new Grammar(productions, start);
		}
	}

	public static class PreProduction
	{
		private final String left;
		private final List<String> right;

		public PreProduction(String left, List<String> right)
		{
			super();
			this.left = left;
			this.right = right;
		}
	}

	@AssociatedProduction(left = "Q", right =
	{ "Q", "P", "SEMICOLON" })
	public static class Q__Q_P_SEMICOLON_TokenReducer extends ProductionTokenPayloadReducer<Void, Map<String, Collection<List<String>>>>
	{
		@Override
		public Map<String, Collection<List<String>>> reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production,
				List<Token<? extends Symbol>> reducees) throws SemanticException
		{
			Map<String, Collection<List<String>>> preGrammar = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			PreProduction preProduction = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			Collection<List<String>> rights = preGrammar.get(preProduction.left);
			if (rights == null)
			{
				rights = new ArrayList<>();
				preGrammar.put(preProduction.left, rights);
			}
			rights.add(preProduction.right);
			return preGrammar;
		}
	}

	@AssociatedProduction(left = "Q", right =
	{ "SEMICOLON" })
	public static class Q__SEMICOLON_TokenReducer extends ProductionTokenPayloadReducer<Void, Map<String, Collection<List<String>>>>
	{
		@Override
		public Map<String, Collection<List<String>>> reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production,
				List<Token<? extends Symbol>> reducees) throws SemanticException
		{
			return new HashMap<>();
		}
	}

	@AssociatedProduction(left = "P", right =
	{ "IDENTIFIER", "ARROW", "R" })
	public static class P__IDENTIFIER_ARROW_R_TokenReducer extends ProductionTokenPayloadReducer<Void, PreProduction>
	{
		@Override
		public PreProduction reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			String left = ((IdentifierToken) reducees.get(0)).getText();
			List<String> right = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
			return new PreProduction(left, right);
		}
	}

	@AssociatedProduction(left = "R", right =
	{ "R", "IDENTIFIER" })
	public static class R__R_IDENTIFIER_TokenReducer extends ProductionTokenPayloadReducer<Void, List<String>>
	{
		@Override
		public List<String> reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			List<String> right = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			String tag = ((IdentifierToken) reducees.get(1)).getText();
			right.add(tag);
			return right;
		}
	}

	@AssociatedProduction(left = "R", right = {})
	public static class R___TokenReducer extends ProductionTokenPayloadReducer<Void, List<String>>
	{
		@Override
		public List<String> reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return new ArrayList<>();
		}
	}

	public GrammarTokenPayLoadReducer() throws ProductionManagedTokenPayloadReducerException
	{
		super(productionTokenPayloadReducerClasses);
	}

}
