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
package aletheia.parser.term;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.AletheiaParserGenerator;
import aletheia.parser.term.parameterRef.IdentifierParameterRef;
import aletheia.parser.term.parameterRef.ParameterRef;
import aletheia.parser.term.parameterRef.TypedParameterRefList;
import aletheia.parser.term.semantic.AN__asterisk_Number_TokenReducer;
import aletheia.parser.term.semantic.AN__asterisk_TokenReducer;
import aletheia.parser.term.semantic.A__A_SCo_Number_M_TokenReducer;
import aletheia.parser.term.semantic.A__A_apostrophe_TokenReducer;
import aletheia.parser.term.semantic.A__A_bang_I_TokenReducer;
import aletheia.parser.term.semantic.A__A_percent_TokenReducer;
import aletheia.parser.term.semantic.A__A_sharp_TokenReducer;
import aletheia.parser.term.semantic.A__A_tilde_TokenReducer;
import aletheia.parser.term.semantic.A__A_unfunctionalize_TokenReducer;
import aletheia.parser.term.semantic.A__F_TokenReducer;
import aletheia.parser.term.semantic.A__I_TokenReducer;
import aletheia.parser.term.semantic.A__R_TokenReducer;
import aletheia.parser.term.semantic.A__atparam_TokenReducer;
import aletheia.parser.term.semantic.A__equals_bang_I_TokenReducer;
import aletheia.parser.term.semantic.A__hexref_TokenReducer;
import aletheia.parser.term.semantic.A__opencur_T_closecur_TokenReducer;
import aletheia.parser.term.semantic.A__openpar_T_closepar_TokenReducer;
import aletheia.parser.term.semantic.A__openpar_T_colon_T_pipe_T_leftarrow_I_closepar_TokenReducer;
import aletheia.parser.term.semantic.A__opensq_T_closesq_TokenReducer;
import aletheia.parser.term.semantic.A__tau_TokenReducer;
import aletheia.parser.term.semantic.B__B_bar_Q_TokenReducer;
import aletheia.parser.term.semantic.B__Q_TokenReducer;
import aletheia.parser.term.semantic.C__A_MP_TokenReducer;
import aletheia.parser.term.semantic.F__openfun_TPL_arrow_T_closefun_TokenReducer;
import aletheia.parser.term.semantic.I__I_dot_id_TokenReducer;
import aletheia.parser.term.semantic.I__id_TokenReducer;
import aletheia.parser.term.semantic.MP__MP_AN_TokenReducer;
import aletheia.parser.term.semantic.MP___TokenReducer;
import aletheia.parser.term.semantic.M___TokenReducer;
import aletheia.parser.term.semantic.M__hyphen_TokenReducer;
import aletheia.parser.term.semantic.Number__number_TokenReducer;
import aletheia.parser.term.semantic.P__I_TokenReducer;
import aletheia.parser.term.semantic.P__atparam_TokenReducer;
import aletheia.parser.term.semantic.Q__C_TokenReducer;
import aletheia.parser.term.semantic.Q__C_question_C_TokenReducer;
import aletheia.parser.term.semantic.R__Rt_Sr_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_bang_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_caret_TokenReducer;
import aletheia.parser.term.semantic.SCo___TokenReducer;
import aletheia.parser.term.semantic.SCo__semicolon_TokenReducer;
import aletheia.parser.term.semantic.S__I_TokenReducer;
import aletheia.parser.term.semantic.S__hexref_TokenReducer;
import aletheia.parser.term.semantic.Sc__Sc_S_bar_TokenReducer;
import aletheia.parser.term.semantic.Sc__Uuid_bar_TokenReducer;
import aletheia.parser.term.semantic.Sc___TokenReducer;
import aletheia.parser.term.semantic.Sc__bar_TokenReducer;
import aletheia.parser.term.semantic.Sr__Sts_TokenReducer;
import aletheia.parser.term.semantic.Sr__openpar_Sc_St_closepar_TokenReducer;
import aletheia.parser.term.semantic.St__S_TokenReducer;
import aletheia.parser.term.semantic.St__turnstile_TokenReducer;
import aletheia.parser.term.semantic.Sts__St_TokenReducer;
import aletheia.parser.term.semantic.Sts__Uuid_TokenReducer;
import aletheia.parser.term.semantic.TPL__TPL_comma_TP_TokenReducer;
import aletheia.parser.term.semantic.TPL__TP_TokenReducer;
import aletheia.parser.term.semantic.TPL___TokenReducer;
import aletheia.parser.term.semantic.TP__P_assignment_T_TokenReducer;
import aletheia.parser.term.semantic.TP__P_colon_T_TokenReducer;
import aletheia.parser.term.semantic.TP__T_TokenReducer;
import aletheia.parser.term.semantic.T__B_TokenReducer;
import aletheia.parser.term.semantic.T__T_B_TokenReducer;
import aletheia.parser.term.semantic.Uuid__uuid_TokenReducer;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.lexer.Lexer;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TaggedNonTerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.persistence.PersistenceManager;
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

	public final static class Globals
	{
		private final PersistenceManager persistenceManager;
		private final Transaction transaction;
		private final Context context;
		private final Map<ParameterRef, VariableTerm> baseReferenceMap;
		private final Map<ParameterVariableTerm, Identifier> parameterIdentifiers;

		private Globals(Transaction transaction, Context context, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
		{
			this.persistenceManager = transaction.getPersistenceManager();
			this.transaction = transaction;
			this.context = context;
			if (context != null)
				this.baseReferenceMap = new AdaptedMap<>(new BijectionKeyMap<>(new Bijection<Identifier, IdentifierParameterRef>()
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
				}, context.identifierToVariable(transaction)));
			else
				this.baseReferenceMap = null;
			this.parameterIdentifiers = parameterIdentifiers;
		}

		public PersistenceManager getPersistenceManager()
		{
			return persistenceManager;
		}

		public Context getContext()
		{
			return context;
		}

		public Transaction getTransaction()
		{
			return transaction;
		}

		public Map<ParameterRef, VariableTerm> getBaseReferenceMap()
		{
			return baseReferenceMap;
		}

		public Map<ParameterVariableTerm, Identifier> getParameterIdentifiers()
		{
			return parameterIdentifiers;
		}

	}

	public static abstract class ProductionTokenPayloadReducer<P> extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Globals, P>
	{
		public enum ReferenceType
		{
			TYPE, INSTANCE, VALUE,
		}

		protected Map<ParameterRef, VariableTerm> antecedentReferenceMap(Globals globals, List<Token<? extends Symbol>> antecedents)
		{
			TypedParameterRefList tprl = NonTerminalToken.findLastPayloadInList(antecedents, new TaggedNonTerminalSymbol("TPL"));
			if (tprl == null)
				return globals.getBaseReferenceMap();
			else
				return tprl.parameterTable();
		}

		protected VariableTerm resolveReference(Globals globals, List<Token<? extends Symbol>> antecedents, ParameterRef reference)
		{
			Map<ParameterRef, VariableTerm> arm = antecedentReferenceMap(globals, antecedents);
			if (arm == null)
				return null;
			else
				return arm.get(reference);
		}

		protected static class DereferenceStatementException extends Exception
		{
			private static final long serialVersionUID = -1491724112412279812L;

			private DereferenceStatementException(String message)
			{
				super(message);
			}

		}

		protected Term dereferenceStatement(Statement statement, ReferenceType referenceType, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
				throws DereferenceStatementException
		{
			switch (referenceType)
			{
			case TYPE:
				if (parameterIdentifiers != null)
					parameterIdentifiers.putAll(statement.getTerm().parameterIdentifierMap(statement.getTermParameterIdentification()));
				return statement.getTerm();
			case INSTANCE:
			{
				if (statement instanceof Specialization)
				{
					Specialization specialization = (Specialization) statement;
					if (parameterIdentifiers != null)
						parameterIdentifiers.putAll(specialization.getInstance().parameterIdentifierMap(specialization.getInstanceParameterIdentification()));
					return specialization.getInstance();
				}
				else
					throw new DereferenceStatementException("Cannot reference the instance of a non-specialization statement");
			}
			case VALUE:
			{
				if (statement instanceof Declaration)
				{
					Declaration declaration = (Declaration) statement;
					if (parameterIdentifiers != null)
						parameterIdentifiers.putAll(declaration.getValue().parameterIdentifierMap(declaration.getValueParameterIdentification()));
					return declaration.getValue();
				}
				else
					throw new DereferenceStatementException("Cannot reference the value of a non-declaration statement");
			}
			default:
				throw new Error();
			}
		}

		protected Context antecedentContext(Globals globals, List<Token<? extends Symbol>> antecedents) throws SemanticException
		{
			NonTerminalToken<?, Context> ctxToken = NonTerminalToken.findLastInList(antecedents, new TaggedNonTerminalSymbol("Sc"),
					new TaggedNonTerminalSymbol("R_t"));
			return ctxToken == null ? globals.getContext() : ctxToken.getPayload();
		}

	}

	//@formatter:off
	private final static Collection<Class<? extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Globals,?>>> reducerClasses =
			Arrays.asList(
					T__T_B_TokenReducer.class,
					T__B_TokenReducer.class,
					
					B__B_bar_Q_TokenReducer.class,
					B__Q_TokenReducer.class,
					
					Q__C_TokenReducer.class,
					Q__C_question_C_TokenReducer.class,
					
					C__A_MP_TokenReducer.class,
					
					A__tau_TokenReducer.class,
					A__I_TokenReducer.class,
					A__atparam_TokenReducer.class,
					A__F_TokenReducer.class,
					A__R_TokenReducer.class,
					A__hexref_TokenReducer.class,
					A__A_tilde_TokenReducer.class,
					A__A_percent_TokenReducer.class,
					A__A_sharp_TokenReducer.class,
					A__A_apostrophe_TokenReducer.class,
					A__A_unfunctionalize_TokenReducer.class,
					A__A_bang_I_TokenReducer.class,
					A__equals_bang_I_TokenReducer.class,
					A__openpar_T_closepar_TokenReducer.class,
					A__A_SCo_Number_M_TokenReducer.class,
					A__opensq_T_closesq_TokenReducer.class,
					A__opencur_T_closecur_TokenReducer.class,
					A__openpar_T_colon_T_pipe_T_leftarrow_I_closepar_TokenReducer.class,

					SCo__semicolon_TokenReducer.class,
					SCo___TokenReducer.class,
					
					M___TokenReducer.class,
					M__hyphen_TokenReducer.class,
					
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

					R__Rt_Sr_TokenReducer.class,
					Sr__Sts_TokenReducer.class,
					Sr__openpar_Sc_St_closepar_TokenReducer.class,
					Rt__ampersand_TokenReducer.class,
					Rt__ampersand_caret_TokenReducer.class,
					Rt__ampersand_bang_TokenReducer.class,
					Sts__Uuid_TokenReducer.class,
					Sts__St_TokenReducer.class,
					St__S_TokenReducer.class,
					St__turnstile_TokenReducer.class,
					Sc___TokenReducer.class,
					Sc__bar_TokenReducer.class,
					Sc__Uuid_bar_TokenReducer.class,
					Sc__Sc_S_bar_TokenReducer.class,
					S__I_TokenReducer.class,
					S__hexref_TokenReducer.class,
					
					MP___TokenReducer.class,
					MP__MP_AN_TokenReducer.class,
					AN__asterisk_TokenReducer.class,
					AN__asterisk_Number_TokenReducer.class,
					
					Number__number_TokenReducer.class,
					Uuid__uuid_TokenReducer.class);
	//@formatter:on

	private final static TermParser instance = new TermParser();

	private final AutomatonSet automatonSet;
	private final ProductionManagedTokenPayloadReducer<Globals, ?> tokenPayloadReducer;

	private static TransitionTable loadTransitionTable()
	{
		try (InputStream is = TermParser.class.getResourceAsStream(AletheiaParserConstants.termTransitionTablePath))
		{
			return TransitionTable.load(is);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private TermParser()
	{
		super(loadTransitionTable());
		try (InputStream is = TermParser.class.getResourceAsStream(AletheiaParserConstants.automatonSetPath))
		{
			automatonSet = AutomatonSet.load(is);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new RuntimeException(e);
		}
		this.tokenPayloadReducer = new ProductionManagedTokenPayloadReducer<>(reducerClasses);
	}

	public static Term parseTerm(Transaction transaction, Context context, Reader reader, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws ParserBaseException
	{
		return instance._parseTerm(transaction, context, reader, parameterIdentifiers);
	}

	public static Term parseTerm(Transaction transaction, Context context, Reader reader) throws ParserBaseException
	{
		return parseTerm(transaction, context, reader, null);
	}

	private Term _parseTerm(Transaction transaction, Context context, Reader reader, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws ParserBaseException
	{
		return (Term) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenPayloadReducer, new Globals(transaction, context, parameterIdentifiers));
	}

	public static class ParameterIdentifiedTerm
	{
		private final Term term;
		private final ParameterIdentification parameterIdentification;

		private ParameterIdentifiedTerm(Term term, ParameterIdentification parameterIdentification)
		{
			super();
			this.term = term;
			this.parameterIdentification = parameterIdentification;
		}

		public Term getTerm()
		{
			return term;
		}

		public ParameterIdentification getParameterIdentification()
		{
			return parameterIdentification;
		}
	}

	public static ParameterIdentifiedTerm parseParameterIdentifiedTerm(Transaction transaction, Context context, Reader reader) throws ParserBaseException
	{
		return instance._parseParameterIdentifiedTerm(transaction, context, reader);
	}

	private ParameterIdentifiedTerm _parseParameterIdentifiedTerm(Transaction transaction, Context context, Reader reader) throws ParserBaseException
	{
		Map<ParameterVariableTerm, Identifier> parameterIdentifiers = new HashMap<>();
		Term term = _parseTerm(transaction, context, reader, parameterIdentifiers);
		ParameterIdentification parameterIdentification = term.makeParameterIdentification(parameterIdentifiers);
		return new ParameterIdentifiedTerm(term, parameterIdentification);
	}

}
