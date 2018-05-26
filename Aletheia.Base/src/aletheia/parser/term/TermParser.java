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
import java.util.List;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.AletheiaParserGenerator;
import aletheia.parser.term.semantic.A__I_TokenReducer;
import aletheia.parser.term.semantic.A__tau_TokenReducer;
import aletheia.parser.term.semantic.B__Q_TokenReducer;
import aletheia.parser.term.semantic.C__A_MP_TokenReducer;
import aletheia.parser.term.semantic.I__I_dot_id_TokenReducer;
import aletheia.parser.term.semantic.I__id_TokenReducer;
import aletheia.parser.term.semantic.MP___TokenReducer;
import aletheia.parser.term.semantic.P__I_TokenReducer;
import aletheia.parser.term.semantic.Q__C_TokenReducer;
import aletheia.parser.term.semantic.T__B_TokenReducer;
import aletheia.parser.term.semantic.T__T_B_TokenReducer;
import aletheia.parser.term.tokens.TermToken;
import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.semantic.ProductionManagedTokenReducer;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.ValuedNonTerminalToken;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
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

	private final static class Globals
	{
		final Context context;
		final Transaction transaction;

		Globals(Context context, Transaction transaction)
		{
			this.context = context;
			this.transaction = transaction;
		}

	}

	public static abstract class ProductionTokenReducer<T extends NonTerminalToken> extends ProductionManagedTokenReducer.ProductionTokenReducer<Globals, T>
	{

		@Override
		public final T reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return reduce(globals.context, globals.transaction, antecedents, production, reducees);
		}

		public abstract T reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
				List<Token<? extends Symbol>> reducees) throws SemanticException;

	}

	public static abstract class TrivialProductionTokenReducer<V, T extends ValuedNonTerminalToken<V>> extends ProductionTokenReducer<T>
	{
		private final int position;

		public TrivialProductionTokenReducer(int position)
		{
			this.position = position;
		}

		public TrivialProductionTokenReducer()
		{
			this(0);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
				List<Token<? extends Symbol>> reducees) throws SemanticException
		{
			return makeToken(production, reducees, ((ValuedNonTerminalToken<V>) reducees.get(position)).getValue());
		}

		public abstract T makeToken(Production production, List<Token<? extends Symbol>> reducees, V value);
	}

	public static abstract class TermTrivialProductionTokenReducer extends TrivialProductionTokenReducer<Term, TermToken>
	{

		@Override
		public TermToken makeToken(Production production, List<Token<? extends Symbol>> reducees, Term term)
		{
			return new TermToken(production, reducees, term);
		}

	}

	//@formatter:off
	private final static Collection<Class<? extends ProductionTokenReducer<? extends NonTerminalToken>>> reducerClasses =
			Arrays.asList(
					I__id_TokenReducer.class,
					I__I_dot_id_TokenReducer.class,
					A__I_TokenReducer.class,
					MP___TokenReducer.class,
					C__A_MP_TokenReducer.class,
					Q__C_TokenReducer.class,
					B__Q_TokenReducer.class,
					T__B_TokenReducer.class,
					T__T_B_TokenReducer.class,
					A__tau_TokenReducer.class,
					P__I_TokenReducer.class);
	//@formatter:on

	private final static TermParser instance = new TermParser();

	private final AutomatonSet automatonSet;
	private final ProductionManagedTokenReducer<Globals, NonTerminalToken> tokenReducer;

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
			TermToken token = (TermToken) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenReducer, new Globals(context, transaction));
			return token.getTerm();
		}
		catch (ParserLexerException e)
		{
			throw new AletheiaParserException(e);
		}
	}

}
