/*******************************************************************************
 * Copyright (c) 2017 Quim Testar.
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
package aletheia.parser.parameteridentification;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.semantic.F__openfun_M_closefun_TokenReducer;
import aletheia.parser.parameteridentification.semantic.I__id_TokenReducer;
import aletheia.parser.parameteridentification.semantic.M__M_comma_P_TokenReducer;
import aletheia.parser.parameteridentification.semantic.M__P_TokenReducer;
import aletheia.parser.parameteridentification.semantic.P__I_TokenReducer;
import aletheia.parser.parameteridentification.semantic.T__T__TokenReducer;
import aletheia.parser.parameteridentification.semantic.T___T_F_TokenReducer;
import aletheia.parser.parameteridentification.semantic.T___TokenReducer;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

public class ParameterIdentificationParser extends Parser
{
	private static final long serialVersionUID = 3547200583493971229L;

	public static abstract class ProductionTokenPayloadReducer<P> extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Void, P>
	{

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
		public P reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
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
		public P reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
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
					T__T__TokenReducer.class,
					T___TokenReducer.class,
					
					T___T_F_TokenReducer.class,
					
					F__openfun_M_closefun_TokenReducer.class,
					
					P__I_TokenReducer.class,
					
					M__M_comma_P_TokenReducer.class,
					M__P_TokenReducer.class,
					
					I__id_TokenReducer.class);
	//@formatter:on

	private final static ParameterIdentificationParser instance = new ParameterIdentificationParser();

	private final AutomatonSet automatonSet;
	private final ProductionManagedTokenPayloadReducer<Void, ?> tokenPayloadReducer;

	private static TransitionTable loadTransitionTable()
	{
		InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.parameterIdentificationTransitionTablePath);
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

	private ParameterIdentificationParser()
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
		finally
		{
		}
		this.tokenPayloadReducer = new ProductionManagedTokenPayloadReducer<>(reducerClasses);
	}

	public static ParameterIdentification parseParameterIdentification(Reader reader) throws AletheiaParserException
	{
		return instance.parse(reader);
	}

	private ParameterIdentification parse(Reader reader) throws AletheiaParserException
	{
		try
		{
			return (ParameterIdentification) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenPayloadReducer, null);
		}
		catch (ParserBaseException e)
		{
			throw new AletheiaParserException(e);
		}
	}

}
