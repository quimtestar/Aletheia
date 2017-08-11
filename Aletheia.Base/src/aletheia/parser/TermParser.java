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

import java.io.IOException;
import java.io.InputStream;
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
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.persistence.Transaction;

/**
 * Implementation of the term parser for the aletheia system. This
 * {@linkplain Parser parser} (and {@link Lexer lexer}) loads the actual
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
public class TermParser extends Parser
{
	private static final long serialVersionUID = -4016748422579759655L;

	private final static TermParser instance = new TermParser();

	private final AutomatonSet automatonSet;
	private final TokenProcessor tokenProcessor;

	private TermParser()
	{
		super(loadTransitionTable());
		try
		{
			{
				InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaTermParserConstants.automatonSetPath);
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

	private static TransitionTable loadTransitionTable()
	{
		InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaTermParserConstants.termTransitionTablePath);
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

}
