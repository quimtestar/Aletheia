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
import java.util.UUID;

import aletheia.model.identifier.Identifier;
import aletheia.model.statement.Context;
import aletheia.model.statement.Declaration;
import aletheia.model.statement.Specialization;
import aletheia.model.statement.Statement;
import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.VariableTerm;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.AletheiaParserGenerator;
import aletheia.parser.term.parameterRef.IdentifierParameterRef;
import aletheia.parser.term.parameterRef.ParameterRef;
import aletheia.parser.term.parameterRef.TypedParameterRefList;
import aletheia.parser.term.semantic.AN__asterisk_TokenReducer;
import aletheia.parser.term.semantic.AN__asterisk_number_TokenReducer;
import aletheia.parser.term.semantic.A__A_apostrophe_TokenReducer;
import aletheia.parser.term.semantic.A__A_bang_I_TokenReducer;
import aletheia.parser.term.semantic.A__A_percent_TokenReducer;
import aletheia.parser.term.semantic.A__A_OS_number_M_TokenReducer;
import aletheia.parser.term.semantic.A__A_sharp_TokenReducer;
import aletheia.parser.term.semantic.A__A_tilde_TokenReducer;
import aletheia.parser.term.semantic.A__F_TokenReducer;
import aletheia.parser.term.semantic.A__I_TokenReducer;
import aletheia.parser.term.semantic.A__R_TokenReducer;
import aletheia.parser.term.semantic.A__atparam_TokenReducer;
import aletheia.parser.term.semantic.A__equals_bang_I_TokenReducer;
import aletheia.parser.term.semantic.A__hexref_TokenReducer;
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
import aletheia.parser.term.semantic.M___TokenReducer;
import aletheia.parser.term.semantic.M__hyphen_TokenReducer;
import aletheia.parser.term.semantic.OS___TokenReducer;
import aletheia.parser.term.semantic.OS__semicolon_TokenReducer;
import aletheia.parser.term.semantic.P__I_TokenReducer;
import aletheia.parser.term.semantic.P__atparam_TokenReducer;
import aletheia.parser.term.semantic.Q__C_TokenReducer;
import aletheia.parser.term.semantic.Q__C_question_C_TokenReducer;
import aletheia.parser.term.semantic.R__Rt_Sts_TokenReducer;
import aletheia.parser.term.semantic.R__Rt_openpar_Sps_closepar_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_bang_TokenReducer;
import aletheia.parser.term.semantic.Rt__ampersand_caret_TokenReducer;
import aletheia.parser.term.semantic.S__I_TokenReducer;
import aletheia.parser.term.semantic.S__hexref_TokenReducer;
import aletheia.parser.term.semantic.Sp__S_bar_Sp_TokenReducer;
import aletheia.parser.term.semantic.Sp__St_TokenReducer;
import aletheia.parser.term.semantic.Sps__Sp_TokenReducer;
import aletheia.parser.term.semantic.Sps__Sr_bar_Sp_TokenReducer;
import aletheia.parser.term.semantic.Sps__uuid_bar_Sp_TokenReducer;
import aletheia.parser.term.semantic.St__S_TokenReducer;
import aletheia.parser.term.semantic.St__turnstile_TokenReducer;
import aletheia.parser.term.semantic.Sts__St_TokenReducer;
import aletheia.parser.term.semantic.Sts__uuid_TokenReducer;
import aletheia.parser.term.semantic.Sr___TokenReducer;
import aletheia.parser.term.semantic.TPL__TPL_comma_TP_TokenReducer;
import aletheia.parser.term.semantic.TPL__TP_TokenReducer;
import aletheia.parser.term.semantic.TPL___TokenReducer;
import aletheia.parser.term.semantic.TP__P_assignment_T_TokenReducer;
import aletheia.parser.term.semantic.TP__P_colon_T_TokenReducer;
import aletheia.parser.term.semantic.TP__T_TokenReducer;
import aletheia.parser.term.semantic.T__B_TokenReducer;
import aletheia.parser.term.semantic.T__T_B_TokenReducer;
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
import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;
import aletheia.parsergenerator.symbols.TerminalSymbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
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

		protected Term dereferenceStatement(Statement statement, ReferenceType referenceType) throws DereferenceStatementException
		{
			switch (referenceType)
			{
			case TYPE:
				return statement.getTerm();
			case INSTANCE:
			{
				if (!(statement instanceof Specialization))
					throw new DereferenceStatementException("Cannot reference the instance of a non-specialization statement");
				return ((Specialization) statement).getInstance();
			}
			case VALUE:
			{
				if (!(statement instanceof Declaration))
					throw new DereferenceStatementException("Cannot reference the value of a non-declaration statement");
				return ((Declaration) statement).getValue();
			}
			default:
				throw new Error();
			}
		}

		protected Context antecedentContext(Globals globals, List<Token<? extends Symbol>> antecedents) throws SemanticException
		{
			//TODO: should find a better way of doing all this.
			NonTerminalToken<?, Statement> lastS = NonTerminalToken.findLastInList(antecedents, new TaggedNonTerminalSymbol("S"),
					new TaggedNonTerminalSymbol("R_t"));
			if (lastS == null)
			{
				TaggedTerminalToken uuidToken = Token.<TerminalSymbol, TaggedTerminalToken> findLastInList(antecedents, new TaggedTerminalSymbol("uuid"),
						new TaggedNonTerminalSymbol("R_t"));
				if (uuidToken != null)
				{
					UUID uuid = UUID.fromString(uuidToken.getText());
					Context context = globals.getPersistenceManager().getContext(globals.getTransaction(), uuid);
					if (context == null)
						throw new SemanticException(uuidToken, "Context not found with UUID: " + uuid);
					return context;
				}
				else if (NonTerminalToken.findLastInList(antecedents, new TaggedNonTerminalSymbol("S_r"), new TaggedNonTerminalSymbol("R_t")) == null)
					return globals.getContext();
				else
					return null;
			}
			else if (lastS.getPayload() instanceof Context)
				return (Context) lastS.getPayload();
			else
				throw new SemanticException(lastS, "Referenced statement in path not a context");
		}

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
		public P reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return NonTerminalToken.getPayloadFromTokenList(reducees, position);
		}

	}

	public static abstract class ConstantProductionTokenPayloadReducer<P> extends ProductionTokenPayloadReducer<P>
	{
		private final P value;

		public ConstantProductionTokenPayloadReducer(P value)
		{
			super();
			this.value = value;
		}

		@Override
		public P reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
				throws SemanticException
		{
			return value;
		}

	}

	public static abstract class NullProductionTokenPayloadReducer extends ConstantProductionTokenPayloadReducer<Void>
	{
		public NullProductionTokenPayloadReducer()
		{
			super(null);
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
					A__A_bang_I_TokenReducer.class,
					A__equals_bang_I_TokenReducer.class,
					A__openpar_T_closepar_TokenReducer.class,
					A__A_OS_number_M_TokenReducer.class,

					OS__semicolon_TokenReducer.class,
					OS___TokenReducer.class,
					
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
					
					R__Rt_Sts_TokenReducer.class,
					R__Rt_openpar_Sps_closepar_TokenReducer.class,
					Rt__ampersand_TokenReducer.class,
					Rt__ampersand_caret_TokenReducer.class,
					Rt__ampersand_bang_TokenReducer.class,
					Sts__uuid_TokenReducer.class,
					Sts__St_TokenReducer.class,
					St__S_TokenReducer.class,
					St__turnstile_TokenReducer.class,
					Sps__Sr_bar_Sp_TokenReducer.class,
					Sps__uuid_bar_Sp_TokenReducer.class,
					Sps__Sp_TokenReducer.class,
					Sr___TokenReducer.class,
					Sp__S_bar_Sp_TokenReducer.class,
					Sp__St_TokenReducer.class,
					S__I_TokenReducer.class,
					S__hexref_TokenReducer.class,
					
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

	public static Term parseTerm(Transaction transaction, Context context, Reader reader, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		return instance.parse(transaction, context, reader, parameterIdentifiers);
	}

	public static Term parseTerm(Transaction transaction, Context context, Reader reader) throws AletheiaParserException
	{
		return parseTerm(transaction, context, reader, null);
	}

	private Term parse(Transaction transaction, Context context, Reader reader, Map<ParameterVariableTerm, Identifier> parameterIdentifiers)
			throws AletheiaParserException
	{
		try
		{
			return (Term) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenPayloadReducer, new Globals(transaction, context, parameterIdentifiers));
		}
		catch (ParserLexerException e)
		{
			throw new AletheiaParserException(e);
		}
	}

}
