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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.lexer.LexerLexer.CharToken;
import aletheia.parsergenerator.lexer.LexerLexer.NumberToken;
import aletheia.parsergenerator.lexer.LexerLexer.TagToken;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;
import aletheia.parsergenerator.semantic.ParseTree;
import aletheia.parsergenerator.parser.TransitionTableLalr1;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;

/**
 * A parser for lexers.
 */
public class LexerParser extends Parser
{
	private static final long serialVersionUID = -4090653108442737302L;

	//@formatter:off
	private final static Automaton blankAutomaton=Automaton.charset(Arrays.asList(new Character[]{
			'\u2028','\u2029',
			'\u0020','\u1680','\u180e','\u2000','\u2001','\u2002','\u2003','\u2004','\u2005','\u2006','\u2007','\u2008','\u2009',
			'\u200a','\u202f','\u205f','\u3000',
			'\u00A0', '\u2007', '\u202F',
			'\u0009', '\n', '\u000B', '\u000C', '\r', '\u001C', '\u001D', '\u001E', '\u001F',
	}));

	private final static Collection<Character> eolChars=new HashSet<>(Arrays.asList(new Character[]{
			'\n','\r','\u000b','\u000c','\u0085',
			'\u2028','\u2029',
	}));

	//@formatter:off
	private final static Automaton eolAutomaton=Automaton.union(
			Automaton.charset(eolChars),
			Automaton.string("\r\n"));
	//@formatter:on

	private final static Automaton dotAutomaton;

	static
	{
		Collection<Character> col = new ArrayList<>();
		for (char c = 1; c < 128; c++)
		{
			if (!eolChars.contains(c))
				col.add(c);
		}
		dotAutomaton = Automaton.charset(col);
	}

	private static TransitionTable lexerTransitionTable()
	{
		try
		{
			return new TransitionTableLalr1(LexerGrammarFactory.getLexerGrammar());
		}
		catch (ConflictException e)
		{
			throw new Error(e);
		}
	}

	public LexerParser()
	{
		super(lexerTransitionTable());
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
		ParseTree parseTree = parseToken(lexerLexer);
		return processTokenLexer(parseTree);
	}

	public AutomatonSet processTokenLexer(ParseTree parseTree)
	{
		AutomatonSet automatonSet;
		if (parseTree.getProduction().getRight().size() == 3)
		{
			automatonSet = processTokenLexer(parseTree.getChildParseTree(0));
			AutomatonTag at = processTokenAutomatonTag(parseTree.getChildParseTree(1));
			automatonSet.addAutomatonTag(at.automaton, at.tag);
		}
		else if (parseTree.getProduction().getRight().size() == 2)
		{
			automatonSet = new AutomatonSet();
			AutomatonTag at = processTokenAutomatonTag(parseTree.getChildParseTree(0));
			automatonSet.addAutomatonTag(at.automaton, at.tag);
		}
		else
			throw new Error();
		return automatonSet;
	}

	private class AutomatonTag
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

	public AutomatonTag processTokenAutomatonTag(ParseTree token)
	{
		Automaton a = processTokenRegExp(token.getChildParseTree(1));
		TaggedTerminalSymbol tag = null;
		if (token.getProduction().getRight().size() > 4)
		{
			TagToken tagToken = (TagToken) token.getChildren().get(4);
			tag = new TaggedTerminalSymbol(tagToken.getTag());
		}
		return new AutomatonTag(a.determinize().minimize(), tag);
	}

	private class Interval
	{
		public final int from;
		public final int to;

		public Interval(int from, int to)
		{
			this.from = from;
			this.to = to;
		}
	}

	private Automaton processTokenRegExp(ParseTree parseTree)
	{
		if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("E")))
		{
			if (parseTree.getProduction().getRight().size() == 1)
				return processTokenRegExp(parseTree.getChildParseTree(0));
			else if ((parseTree.getProduction().getRight().size() == 3)
					&& (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("UNION"))))
			{
				Automaton a1 = processTokenRegExp(parseTree.getChildParseTree(0));
				Automaton a2 = processTokenRegExp(parseTree.getChildParseTree(2));
				return a1.union(a2);
			}
			else if ((parseTree.getProduction().getRight().size() == 3)
					&& (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("CARET"))))
			{
				Automaton a1 = processTokenRegExp(parseTree.getChildParseTree(0));
				Automaton a2 = processTokenRegExp(parseTree.getChildParseTree(2));
				return a1.intersection(a2);
			}
			else if ((parseTree.getProduction().getRight().size() == 3)
					&& (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("HYPHEN"))))
			{
				Automaton a1 = processTokenRegExp(parseTree.getChildParseTree(0));
				Automaton a2 = processTokenRegExp(parseTree.getChildParseTree(2));
				return a1.subtraction(a2);
			}
			else
				throw new Error();
		}
		else if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("T")))
		{
			if (parseTree.getProduction().getRight().size() == 0)
				return Automaton.emptyString();
			else if ((parseTree.getProduction().getRight().size() == 2))
			{
				Automaton a1 = processTokenRegExp(parseTree.getChildParseTree(0));
				Automaton a2 = processTokenRegExp(parseTree.getChildParseTree(1));
				return a1.concatenate(a2);
			}
			else
				throw new Error();
		}
		else if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("F")))
		{
			if (parseTree.getProduction().getRight().size() == 1)
			{
				if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("CHAR")))
				{
					char c = ((CharToken) parseTree.getChildren().get(0)).getC();
					return Automaton.singleton(c);
				}
				else if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("BLANK")))
				{
					return blankAutomaton;
				}
				else if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("EOL")))
				{
					return eolAutomaton;
				}
				else if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("DOT")))
				{
					return dotAutomaton;
				}
				else
					throw new Error();
			}
			else if ((parseTree.getProduction().getRight().size() == 2))
			{
				if (parseTree.getProduction().getRight().get(0).equals(new TaggedNonTerminalSymbol("F")))
				{
					if (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("KLEENE")))
					{
						Automaton a = processTokenRegExp(parseTree.getChildParseTree(0));
						return a.kleene();
					}
					else if (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("PLUS")))
					{
						Automaton a = processTokenRegExp(parseTree.getChildParseTree(0));
						return a.concatenate(a.kleene());
					}
					else if (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("QUESTION")))
					{
						Automaton a = processTokenRegExp(parseTree.getChildParseTree(0));
						return a.union(Automaton.emptyString());
					}
					else if (parseTree.getProduction().getRight().get(1).equals(new TaggedNonTerminalSymbol("R")))
					{
						Automaton a = processTokenRegExp(parseTree.getChildParseTree(0));
						Interval i = processTokenInterval(parseTree.getChildParseTree(1));
						Automaton b = Automaton.emptyString();
						for (int j = 0; j < i.from; j++)
							b = b.concatenate(a);
						if (i.to >= Integer.MAX_VALUE)
							return b.concatenate(a.kleene());
						else
						{
							Automaton c = Automaton.empty();
							for (int j = i.from; j <= i.to; j++)
							{
								c = c.union(b);
								b = b.concatenate(a);
							}
							return c;
						}
					}
					else
						throw new Error();
				}
				else if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("HASH")))
				{
					int n = ((NumberToken) parseTree.getChildren().get(1)).getN();
					return Automaton.singleton((char) n);
				}
				else
					throw new Error();

			}
			else if ((parseTree.getProduction().getRight().size() == 3))
			{
				if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("OP")))
					return processTokenRegExp(parseTree.getChildParseTree(1));
				if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("OB")))
				{
					Set<Character> set = new TreeSet<>();
					processParseTreeCharset(parseTree.getChildParseTree(1), set);
					return Automaton.charset(set);
				}
				else
					throw new Error();

			}
			else
				throw new Error();
		}
		else
			throw new Error();
	}

	private Interval processTokenInterval(ParseTree parseTree)
	{
		if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("R")))
		{
			return processTokenInterval(parseTree.getChildParseTree(1));
		}
		else if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("I")))
		{
			if (parseTree.getProduction().getRight().size() == 1)
			{
				int n = ((NumberToken) parseTree.getChildren().get(0)).getN();
				return new Interval(n, n);
			}
			else if (parseTree.getProduction().getRight().size() == 2)
			{
				if (parseTree.getProduction().getRight().get(0).equals(new TaggedTerminalSymbol("COMMA")))
				{
					int n = ((NumberToken) parseTree.getChildren().get(1)).getN();
					return new Interval(0, n);
				}
				else if (parseTree.getProduction().getRight().get(1).equals(new TaggedTerminalSymbol("COMMA")))
				{
					int n = ((NumberToken) parseTree.getChildren().get(0)).getN();
					return new Interval(n, Integer.MAX_VALUE);
				}
				else
					throw new Error();
			}
			else if (parseTree.getProduction().getRight().size() == 3)
			{
				int n = ((NumberToken) parseTree.getChildren().get(0)).getN();
				int m = ((NumberToken) parseTree.getChildren().get(2)).getN();
				return new Interval(n, m);
			}
			else
				throw new Error();
		}
		else
			throw new Error();

	}

	private void processParseTreeCharset(ParseTree parseTree, Set<Character> set)
	{
		if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("D")))
		{
			for (ParseTree pt : parseTree.getChildParseTrees())
				processParseTreeCharset(pt, set);
		}
		else if (parseTree.getProduction().getLeft().equals(new TaggedNonTerminalSymbol("C")))
		{
			if ((parseTree.getProduction().getRight().size() == 1) && (parseTree.getChildren().get(0).getSymbol().equals(new TaggedTerminalSymbol("CHAR"))))
				set.add(((CharToken) parseTree.getChildren().get(0)).getC());
			else if ((parseTree.getProduction().getRight().size() == 3)
					&& (parseTree.getChildren().get(1).getSymbol().equals(new TaggedTerminalSymbol("HYPHEN"))))
			{
				char c0 = ((CharToken) parseTree.getChildren().get(0)).getC();
				char c1 = ((CharToken) parseTree.getChildren().get(2)).getC();
				for (char c = c0; c <= c1; c++)
					set.add(c);
			}
			else
				throw new Error();
		}
		else
			throw new Error();
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
