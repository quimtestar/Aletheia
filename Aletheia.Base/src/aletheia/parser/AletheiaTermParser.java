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
package aletheia.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.tokenprocessor.TokenProcessor;
import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.lexer.LexerLexer;
import aletheia.parsergenerator.lexer.LexerParser;
import aletheia.parsergenerator.parser.Grammar;
import aletheia.parsergenerator.parser.GrammarParser;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.parser.TransitionTable.ConflictException;
import aletheia.parsergenerator.parser.TransitionTableLalr1;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

/**
 * Implementation of the {@link TermParser term parser} for the aletheia system.
 * This {@linkplain Parser parser} (and {@link Lexer lexer}) loads the actual
 * {@linkplain TransitionTable transition table} for the parser and the
 * automaton set for the lexer from the files "aletheia.ttb" and "aletheia.ast"
 * that must be in the same path than the class file of this class. Those files
 * are generated using the {@link #generate()} method.
 *
 *
 * @see aletheia.parsergenerator.parser
 * @see aletheia.parsergenerator.lexer
 * @see AletheiaTermParserGenerator
 */
public class AletheiaTermParser extends Parser
{
	private static final long serialVersionUID = -4016748422579759655L;

	private final static String grammarPath = "aletheia/parser/aletheia.gra";
	private final static String lexerPath = "aletheia/parser/aletheia.lex";
	private final static String transitionTablePath = "aletheia/parser/aletheia.ttb";
	private final static String automatonSetPath = "aletheia/parser/aletheia.ast";

	private final static AletheiaTermParser instance = new AletheiaTermParser();

	private final AutomatonSet automatonSet;
	private final TokenProcessor tokenProcessor;

	private AletheiaTermParser()
	{
		super(loadTransitionTable());
		try
		{
			{
				InputStream is = ClassLoader.getSystemResourceAsStream(automatonSetPath);
				try
				{
					automatonSet = AutomatonSet.load(is);
				}
				finally
				{
					if (is != null)
						is.close();
				}
			}
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
		catch (ClassNotFoundException e)
		{
			throw new Error(e);
		}
		finally
		{
		}
		this.tokenProcessor = new TokenProcessor(getGrammar());
	}

	public static Term parseTerm(Context context, Transaction transaction, String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TermParserException
	{
		return instance.parse(context, transaction, input, parameterIdentifiers);
	}

	public static Term parseTerm(Context context, Transaction transaction, String input) throws TermParserException
	{
		return parseTerm(context, transaction, input, null);
	}

	public static Term parseTerm(String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers) throws TermParserException
	{
		return parseTerm(null, null, input, parameterIdentifiers);
	}

	public static Term parseTerm(String input) throws TermParserException
	{
		return parseTerm(input, null);
	}

	private Term parse(Context context, Transaction transaction, String input, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws TermParserException
	{
		try
		{
			NonTerminalToken token = parseToken(new AutomatonSetLexer(automatonSet, new StringReader(input)));
			return tokenProcessor.process(token, input, context, transaction, parameterIdentifiers);
		}
		catch (ParserLexerException e)
		{
			throw new TermParserException(e, input);
		}
	}

	public static AutomatonSet createAutomatonSet() throws ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(lexerPath));
		try
		{
			LexerLexer lexLex = new LexerLexer(reader);
			LexerParser lexParser = new LexerParser();
			AutomatonSet automatonSet = lexParser.parse(lexLex);
			return automatonSet;
		}
		finally
		{
			reader.close();
		}
	}

	public static TransitionTable createTransitionTable() throws ConflictException, ParserLexerException, IOException
	{
		Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(grammarPath));
		try
		{
			GrammarParser gp = new GrammarParser();
			Grammar g = gp.parse(reader);
			TransitionTable table = new TransitionTableLalr1(g);
			return table;
		}
		finally
		{
			reader.close();
		}
	}

	private static TransitionTable loadTransitionTable()
	{
		InputStream is = ClassLoader.getSystemResourceAsStream(transitionTablePath);
		try
		{
			return TransitionTable.load(is);
		}
		catch (ClassNotFoundException e)
		{
			throw new Error(e);
		}
		catch (IOException e)
		{
			throw new Error(e);
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
			}
			catch (IOException e)
			{
				throw new Error(e);
			}
		}
	}

	protected static void generate() throws ParserLexerException, IOException, ConflictException
	{
		AutomatonSet automatonSet = createAutomatonSet();
		automatonSet.save(new File("src/" + automatonSetPath));

		TransitionTable table = createTransitionTable();
		table.save(new File("src/" + transitionTablePath));

	}

}
