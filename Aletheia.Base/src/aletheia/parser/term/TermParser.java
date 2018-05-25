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
package aletheia.parser.term;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.AletheiaParserGenerator;
import aletheia.parser.term.semantic.I__id_TokenReducer;
import aletheia.parser.term.tokens.TermToken;
import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer.ProductionTokenReducer;
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
 * @see AletheiaParserGenerator
 */
public class TermParser extends Parser
{
	private static final long serialVersionUID = -4016748422579759655L;

	//@formatter:off
	private final static Collection<Class<? extends ProductionTokenReducer<? extends NonTerminalToken>>> reducerClasses =
			Arrays.asList(
					I__id_TokenReducer.class);
	//@formatter:on

	private final static TermParser instance = new TermParser();

	private final AutomatonSet automatonSet;
	private final ProductionManagedTokenReducer<NonTerminalToken> tokenReducer;

	private static TransitionTable loadTransitionTable()
	{
		InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.termTransitionTablePath);
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

	private TermParser()
	{
		super(loadTransitionTable());
		try
		{
			{
				InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.automatonSetPath);
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
		this.tokenReducer = new ProductionManagedTokenReducer<>(reducerClasses);
	}

	public static Term parseTerm(Context context, Transaction transaction, Reader reader, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		return instance.parse(context, transaction, reader, parameterIdentifiers);
	}

	public static Term parseTerm(Context context, Transaction transaction, Reader reader) throws AletheiaParserException
	{
		return parseTerm(context, transaction, reader, null);
	}

	private Term parse(Context context, Transaction transaction, Reader reader, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		try
		{
			TermToken token = (TermToken) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenReducer);
			return token.getTerm();
		}
		catch (ParserLexerException e)
		{
			throw new AletheiaParserException(e);
		}
	}

}
