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
import aletheia.model.term.VariableTerm;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.AletheiaParserGenerator;
import aletheia.parser.term.semantic.AN__asterisk_TokenReducer;
import aletheia.parser.term.semantic.AN__asterisk_number_TokenReducer;
import aletheia.parser.term.semantic.A__F_TokenReducer;
import aletheia.parser.term.semantic.A__I_TokenReducer;
import aletheia.parser.term.semantic.A__R_TokenReducer;
import aletheia.parser.term.semantic.A__atparam_TokenReducer;
import aletheia.parser.term.semantic.A__openpar_T_closepar_TokenReducer;
import aletheia.parser.term.semantic.A__tau_TokenReducer;
import aletheia.parser.term.semantic.B__B_bar_Q_TokenReducer;
import aletheia.parser.term.semantic.B__Q_TokenReducer;
import aletheia.parser.term.semantic.C__A_MP_TokenReducer;
import aletheia.parser.term.semantic.F__openfun_TPL_arrow_T_closefun_TokenReducer;
import aletheia.parser.term.semantic.I__I_dot_id_TokenReducer;
import aletheia.parser.term.semantic.I__id_TokenReducer;
import aletheia.parser.term.semantic.MP__MP_AN_TokenReducer;
import aletheia.parser.term.semantic.MP___TokenReducer;
import aletheia.parser.term.semantic.P__I_TokenReducer;
import aletheia.parser.term.semantic.P__atparam_TokenReducer;
import aletheia.parser.term.semantic.Q__C_TokenReducer;
import aletheia.parser.term.semantic.R__Rt_Sts_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_TokenReducer;
import aletheia.parser.term.semantic.S__I_TokenReducer;
import aletheia.parser.term.semantic.St__S_TokenReducer;
import aletheia.parser.term.semantic.Sts__St_TokenReducer;
import aletheia.parser.term.semantic.TPL__TPL_comma_TP_TokenReducer;
import aletheia.parser.term.semantic.TPL__TP_TokenReducer;
import aletheia.parser.term.semantic.TPL___TokenReducer;
import aletheia.parser.term.semantic.TP__P_assignment_T_TokenReducer;
import aletheia.parser.term.semantic.TP__P_colon_T_TokenReducer;
import aletheia.parser.term.semantic.TP__T_TokenReducer;
import aletheia.parser.term.semantic.T__B_TokenReducer;
import aletheia.parser.term.semantic.T__T_B_TokenReducer;
import aletheia.parser.term.tokenprocessor.parameterRef.IdentifierParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.ParameterRef;
import aletheia.parser.term.tokenprocessor.parameterRef.TypedParameterRefList;
import aletheia.parsergenerator.ParserLexerException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.Transaction;
import aletheia.utilities.collections.AdaptedMap;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionKeyMap;

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

	public static abstract class ProductionTokenPayloadReducer<P> extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Globals, P>
	{
		public enum ReferenceType
		{
			TYPE, INSTANCE, VALUE,
		}

		protected static Map<ParameterRef, VariableTerm> antecedentReferenceMap(Context context, Transaction transaction,
				List<Token<? extends Symbol>> antecedents)
		{
			TypedParameterRefList tprl = NonTerminalToken.findLastPayloadInList(antecedents, new TaggedNonTerminalSymbol("TPL"));
			if (tprl == null)
			{
				//TODO This might be computed only once per parse call
				return new AdaptedMap<>(new BijectionKeyMap<>(new Bijection<Identifier, IdentifierParameterRef>()
				{

					@Override
					public IdentifierParameterRef forward(Identifier identifier)
					{
						return new IdentifierParameterRef(identifier);
					}

					@Override
					public Identifier backward(IdentifierParameterRef parameterRef)
					{
						return parameterRef.getIdentifier();
					}
				}, context.identifierToVariable(transaction))); //slow if not counting on database cacheing.
			}
			else
				return tprl.parameterTable();
		}

		@Override
		public final P reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return reduce(globals.context, globals.transaction, antecedents, production, reducees);
		}

		public abstract P reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
				List<Token<? extends Symbol>> reducees) throws SemanticException;
	}

	public static abstract class TrivialProductionTokenPayloadReducer<P> extends ProductionTokenPayloadReducer<P>
	{
		private final int position;

		public TrivialProductionTokenPayloadReducer(int position)
		{
			this.position = position;
		}

		public TrivialProductionTokenPayloadReducer()
		{
			this(0);
		}

		@Override
		public P reduce(Context context, Transaction transaction, List<Token<? extends Symbol>> antecedents, Production production,
				List<Token<? extends Symbol>> reducees) throws SemanticException
		{
			return NonTerminalToken.getPayloadFromTokenList(reducees, position);
		}

	}

	//@formatter:off
	private final static Collection<Class<? extends ProductionTokenPayloadReducer<?>>> reducerClasses =
			Arrays.asList(
					T__T_B_TokenReducer.class,
					T__B_TokenReducer.class,
					B__B_bar_Q_TokenReducer.class,
					B__Q_TokenReducer.class,
					Q__C_TokenReducer.class,
					C__A_MP_TokenReducer.class,
					A__tau_TokenReducer.class,
					A__I_TokenReducer.class,
					A__atparam_TokenReducer.class,
					A__F_TokenReducer.class,
					A__R_TokenReducer.class,
					A__openpar_T_closepar_TokenReducer.class,
					F__openfun_TPL_arrow_T_closefun_TokenReducer.class,
					TPL___TokenReducer.class,
					TPL__TP_TokenReducer.class,
					TPL__TPL_comma_TP_TokenReducer.class,
					TP__P_colon_T_TokenReducer.class,
					TP__T_TokenReducer.class,
					TP__P_assignment_T_TokenReducer.class,
					P__I_TokenReducer.class,
					P__atparam_TokenReducer.class,
					I__I_dot_id_TokenReducer.class,
					I__id_TokenReducer.class,
					
					R__Rt_Sts_TokenReducer.class,
					Rt__ampersand_TokenReducer.class,
					Sts__St_TokenReducer.class,
					St__S_TokenReducer.class,
					S__I_TokenReducer.class,
					
					MP___TokenReducer.class,
					MP__MP_AN_TokenReducer.class,
					AN__asterisk_TokenReducer.class,
					AN__asterisk_number_TokenReducer.class);
	//@formatter:on

	private final static TermParser instance = new TermParser();

	private final AutomatonSet automatonSet;
	private final ProductionManagedTokenPayloadReducer<Globals, ?> tokenPayloadReducer;

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
		this.tokenPayloadReducer = new ProductionManagedTokenPayloadReducer<>(reducerClasses);
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
			return (Term) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenPayloadReducer, new Globals(context, transaction));
		}
		catch (ParserLexerException e)
		{
			throw new AletheiaParserException(e);
		}
	}

}
