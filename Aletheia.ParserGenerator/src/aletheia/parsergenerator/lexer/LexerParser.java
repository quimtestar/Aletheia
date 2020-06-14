/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.lexer.LexerLexer.CharToken;
import aletheia.parsergenerator.lexer.LexerLexer.NumberToken;
import aletheia.parsergenerator.lexer.LexerLexer.TagToken;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.GrammarParser;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;
import aletheia.parsergenerator.parser.TransitionTableLalr1;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.ConstantProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.TrivialProductionTokenPayloadReducer;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

/**
 * A parser for lexers.
 */
public class LexerParser extends Parser
{
	private static final long serialVersionUID = -4090653108442737302L;

	private static final TransitionTable lexerTransitionTable;
	static
	{
		try
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
					"F -> HASH NUMBER;" +
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
			Grammar lexerGrammar = gP.parse(new StringReader(sg));
			lexerTransitionTable = new TransitionTableLalr1(lexerGrammar);
		}
		catch (ConflictException | ParserBaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	//@formatter:off
	private final static Collection<Class<? extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Void,?>>> reducerClasses =
			Arrays.asList(
					L__L_S_SEMICOLON_TokenReducer.class,
					L__S_SEMICOLON_TokenReducer.class,
					S__QUOTE_E_QUOTE_COLON_TAG_TokenReducer.class,
					S__QUOTE_E_QUOTE_COLON_TokenReducer.class,
					E__E_UNION_T_TokenReducer.class,
					E__E_CARET_T_TokenReducer.class,
					E__E_HYPHEN_T_TokenReducer.class,
					E__T_TokenReducer.class,
					T__T_F_TokenReducer.class,
					T___TokenReducer.class,
					F__CHAR_TokenReducer.class,
					F__BLANK_TokenReducer.class,
					F__EOL_TokenReducer.class,
					F__DOT_TokenReducer.class,
					F__F_KLEENE_TokenReducer.class,
					F__F_PLUS_TokenReducer.class,
					F__F_QUESTION_TokenReducer.class,
					F__F_R_TokenReducer.class,
					F__OP_E_CP_TokenReducer.class,
					F__OB_D_CB_TokenReducer.class,
					F__HASH_NUMBER_TokenReducer.class,
					R__OC_I_CC_TokenReducer.class,
					I__NUMBER_TokenReducer.class,
					I__COMMA_NUMBER_TokenReducer.class,
					I__NUMBER_COMMA_TokenReducer.class,
					I__NUMBER_COMMA_NUMBER_TokenReducer.class,
					D__D_C_TokenReducer.class,
					D___TokenReducer.class,
					C__CHAR_TokenReducer.class,
					C__CHAR_HYPHEN_CHAR_TokenReducer.class);
	//@formatter:on

	private static class AutomatonTag
	{
		public final DeterministicAutomaton automaton;
		public final TaggedTerminalSymbol tag;

		public AutomatonTag(DeterministicAutomaton automaton, TaggedTerminalSymbol tag)
		{
			super();
			this.automaton = automaton;
			this.tag = tag;
		}
	}

	@AssociatedProduction(left = "L", right =
	{ "L", "S", "SEMICOLON" })
	public final static class L__L_S_SEMICOLON_TokenReducer extends ProductionTokenPayloadReducer<Void, AutomatonSet>
	{

		@Override
		public AutomatonSet reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			AutomatonSet automatonSet = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			AutomatonTag at = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			automatonSet.addAutomatonTag(at.automaton, at.tag);
			return automatonSet;
		}
	}

	@AssociatedProduction(left = "L", right =
	{ "S", "SEMICOLON" })
	public final static class L__S_SEMICOLON_TokenReducer extends ProductionTokenPayloadReducer<Void, AutomatonSet>
	{

		@Override
		public AutomatonSet reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			AutomatonSet automatonSet = new AutomatonSet();
			AutomatonTag at = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			automatonSet.addAutomatonTag(at.automaton, at.tag);
			return automatonSet;
		}
	}

	@AssociatedProduction(left = "S", right =
	{ "QUOTE", "E", "QUOTE", "COLON", "TAG" })
	public final static class S__QUOTE_E_QUOTE_COLON_TAG_TokenReducer extends ProductionTokenPayloadReducer<Void, AutomatonTag>
	{

		@Override
		public AutomatonTag reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			TagToken tagToken = (TagToken) reducees.get(4);
			TaggedTerminalSymbol tag = new TaggedTerminalSymbol(tagToken.getTag());
			return new AutomatonTag(a.determinize().minimize(), tag);
		}
	}

	@AssociatedProduction(left = "S", right =
	{ "QUOTE", "E", "QUOTE", "COLON" })
	public final static class S__QUOTE_E_QUOTE_COLON_TokenReducer extends ProductionTokenPayloadReducer<Void, AutomatonTag>
	{

		@Override
		public AutomatonTag reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			return new AutomatonTag(a.determinize().minimize(), null);
		}
	}

	@AssociatedProduction(left = "E", right =
	{ "E", "UNION", "T" })
	public final static class E__E_UNION_T_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{
		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a1 = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			Automaton a2 = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
			return a1.union(a2);
		}
	}

	@AssociatedProduction(left = "E", right =
	{ "E", "CARET", "T" })
	public final static class E__E_CARET_T_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{
		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a1 = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			Automaton a2 = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
			return a1.intersection(a2);
		}
	}

	@AssociatedProduction(left = "E", right =
	{ "E", "HYPHEN", "T" })
	public final static class E__E_HYPHEN_T_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{
		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a1 = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			Automaton a2 = NonTerminalToken.getPayloadFromTokenList(reducees, 2);
			return a1.subtraction(a2);
		}
	}

	@AssociatedProduction(left = "E", right =
	{ "T" })
	public final static class E__T_TokenReducer extends TrivialProductionTokenPayloadReducer<Void, Automaton>
	{
	}

	@AssociatedProduction(left = "T", right =
	{ "T", "F" })
	public final static class T__T_F_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a1 = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			Automaton a2 = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			return a1.concatenate(a2);
		}
	}

	@AssociatedProduction(left = "T", right = {})
	public final static class T___TokenReducer extends ConstantProductionTokenPayloadReducer<Void, Automaton>
	{
		public T___TokenReducer()
		{
			super(Automaton.emptyString());
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "CHAR" })
	public final static class F__CHAR_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return Automaton.singleton(((CharToken) reducees.get(0)).getC());
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "BLANK" })
	public final static class F__BLANK_TokenReducer extends ConstantProductionTokenPayloadReducer<Void, Automaton>
	{
		private final static Collection<Character> blankChars = Arrays.asList('\u2028', '\u2029', '\u0020', '\u1680', '\u180e', '\u2000', '\u2001', '\u2002',
				'\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200a', '\u202f', '\u205f', '\u3000', '\u00A0', '\u2007', '\u202F',
				'\u0009', '\n', '\u000B', '\u000C', '\r', '\u001C', '\u001D', '\u001E', '\u001F');
		private final static Automaton blankAutomaton = Automaton.charset(blankChars);

		public F__BLANK_TokenReducer()
		{
			super(blankAutomaton);
		}

	}

	@AssociatedProduction(left = "F", right =
	{ "EOL" })
	public final static class F__EOL_TokenReducer extends ConstantProductionTokenPayloadReducer<Void, Automaton>
	{
		private final static Collection<Character> eolChars = Arrays.asList('\n', '\r', '\u000b', '\u000c', '\u0085', '\u2028', '\u2029');
		private final static Automaton eolAutomaton = Automaton.charset(eolChars).union(Automaton.string("\r\n"));

		public F__EOL_TokenReducer()
		{
			super(eolAutomaton);
		}

	}

	@AssociatedProduction(left = "F", right =
	{ "DOT" })
	public final static class F__DOT_TokenReducer extends ConstantProductionTokenPayloadReducer<Void, Automaton>
	{
		private final static Collection<Character> dotChars = new ArrayList<>();
		static
		{
			for (char c = 1; c < 128; c++)
				dotChars.add(c);
		}
		private final static Automaton dotAutomaton = Automaton.charset(dotChars).subtraction(F__EOL_TokenReducer.eolAutomaton);;

		public F__DOT_TokenReducer()
		{
			super(dotAutomaton);
		}

	}

	@AssociatedProduction(left = "F", right =
	{ "F", "KLEENE" })
	public final static class F__F_KLEENE_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			return a.kleene();
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "F", "PLUS" })
	public final static class F__F_PLUS_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			return a.concatenate(a.kleene());
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "F", "QUESTION" })
	public final static class F__F_QUESTION_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			return a.union(Automaton.emptyString());
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "F", "R" })
	public final static class F__F_R_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Automaton a = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			int[] interval = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			Automaton b = Automaton.emptyString();
			for (int j = 0; j < interval[0]; j++)
				b = b.concatenate(a);
			if (interval[1] >= Integer.MAX_VALUE)
				return b.concatenate(a.kleene());
			else
			{
				Automaton c = Automaton.empty();
				for (int j = interval[0]; j <= interval[1]; j++)
				{
					c = c.union(b);
					b = b.concatenate(a);
				}
				return c;
			}
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "OP", "E", "CP" })
	public final static class F__OP_E_CP_TokenReducer extends TrivialProductionTokenPayloadReducer<Void, Automaton>
	{
		public F__OP_E_CP_TokenReducer()
		{
			super(1);
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "OB", "D", "CB" })
	public final static class F__OB_D_CB_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Set<Character> set = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			return Automaton.charset(set);
		}
	}

	@AssociatedProduction(left = "F", right =
	{ "HASH", "NUMBER" })
	public final static class F__HASH_NUMBER_TokenReducer extends ProductionTokenPayloadReducer<Void, Automaton>
	{

		@Override
		public Automaton reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			int n = ((NumberToken) reducees.get(1)).getN();
			return Automaton.singleton((char) n);
		}
	}

	@AssociatedProduction(left = "R", right =
	{ "OC", "I", "CC" })
	public final static class R__OC_I_CC_TokenReducer extends TrivialProductionTokenPayloadReducer<Void, int[]>
	{
		public R__OC_I_CC_TokenReducer()
		{
			super(1);
		}
	}

	@AssociatedProduction(left = "I", right =
	{ "NUMBER" })
	public final static class I__NUMBER_TokenReducer extends ProductionTokenPayloadReducer<Void, int[]>
	{
		@Override
		public int[] reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			int n = ((NumberToken) reducees.get(0)).getN();
			return new int[]
			{ n, n };
		}
	}

	@AssociatedProduction(left = "I", right =
	{ "COMMA", "NUMBER" })
	public final static class I__COMMA_NUMBER_TokenReducer extends ProductionTokenPayloadReducer<Void, int[]>
	{
		@Override
		public int[] reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			int n = ((NumberToken) reducees.get(1)).getN();
			return new int[]
			{ 0, n };
		}
	}

	@AssociatedProduction(left = "I", right =
	{ "NUMBER", "COMMA" })
	public final static class I__NUMBER_COMMA_TokenReducer extends ProductionTokenPayloadReducer<Void, int[]>
	{
		@Override
		public int[] reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			int n = ((NumberToken) reducees.get(0)).getN();
			return new int[]
			{ n, Integer.MAX_VALUE };
		}
	}

	@AssociatedProduction(left = "I", right =
	{ "NUMBER", "COMMA", "NUMBER" })
	public final static class I__NUMBER_COMMA_NUMBER_TokenReducer extends ProductionTokenPayloadReducer<Void, int[]>
	{
		@Override
		public int[] reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			int n = ((NumberToken) reducees.get(0)).getN();
			int m = ((NumberToken) reducees.get(1)).getN();
			return new int[]
			{ n, m };
		}
	}

	@AssociatedProduction(left = "D", right = {})
	public final static class D___TokenReducer extends ProductionTokenPayloadReducer<Void, Set<Character>>
	{

		@Override
		public Set<Character> reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return new HashSet<>();
		}
	}

	@AssociatedProduction(left = "D", right =
	{ "D", "C" })
	public final static class D__D_C_TokenReducer extends ProductionTokenPayloadReducer<Void, Set<Character>>
	{

		@Override
		public Set<Character> reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			Set<Character> set = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
			char[] interval = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
			for (char c = interval[0]; c <= interval[1]; c++)
				set.add(c);
			return set;
		}
	}

	@AssociatedProduction(left = "C", right =
	{ "CHAR" })
	public final static class C__CHAR_TokenReducer extends ProductionTokenPayloadReducer<Void, char[]>
	{

		@Override
		public char[] reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			char c = ((CharToken) reducees.get(0)).getC();
			return new char[]
			{ c, c };
		}
	}

	@AssociatedProduction(left = "C", right =
	{ "CHAR", "HYPHEN", "CHAR" })
	public final static class C__CHAR_HYPHEN_CHAR_TokenReducer extends ProductionTokenPayloadReducer<Void, char[]>
	{

		@Override
		public char[] reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			char c0 = ((CharToken) reducees.get(0)).getC();
			char c1 = ((CharToken) reducees.get(2)).getC();
			return new char[]
			{ c0, c1 };
		}
	}

	private final static ProductionManagedTokenPayloadReducer<Void, ?> tokenPayloadReducer = new ProductionManagedTokenPayloadReducer<>(reducerClasses);

	public LexerParser()
	{
		super(lexerTransitionTable);
	}

	/**
	 * Parses an automaton set object from a {@link LexerLexer}
	 *
	 * @param lexerLexer
	 *            The lexer of lexers.
	 * @return The automaton set.
	 * @throws ParserBaseException
	 */
	public AutomatonSet parse(LexerLexer lexerLexer) throws ParserBaseException
	{
		return (AutomatonSet) parseToken(lexerLexer, tokenPayloadReducer);
	}

	/**
	 * Parses an automaton set object from a {@link Reader}.
	 *
	 * @param reader
	 *            The reader to read from.
	 * @return The automaton set.
	 * @throws ParserBaseException
	 */
	public AutomatonSet parse(Reader reader) throws ParserBaseException
	{
		return parse(new LexerLexer(reader));
	}

}
