/*******************************************************************************
 * Copyright (c) 2017, 2020 Quim Testar.
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

import aletheia.model.identifier.Identifier;
import aletheia.model.parameteridentification.FunctionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.parser.AletheiaParserConstants;
import aletheia.parser.parameteridentification.semantic.F__openfun_M_arrow_Ts_closefun_TokenReducer;
import aletheia.parser.parameteridentification.semantic.F__openfun_M_closefun_TokenReducer;
import aletheia.parser.parameteridentification.semantic.I__I_dot_id_TokenReducer;
import aletheia.parser.parameteridentification.semantic.I__id_TokenReducer;
import aletheia.parser.parameteridentification.semantic.M__M_comma_P_TokenReducer;
import aletheia.parser.parameteridentification.semantic.M__P_TokenReducer;
import aletheia.parser.parameteridentification.semantic.P__I_TokenReducer;
import aletheia.parser.parameteridentification.semantic.P__I_colon_T_TokenReducer;
import aletheia.parser.parameteridentification.semantic.P___TokenReducer;
import aletheia.parser.parameteridentification.semantic.P__colon_T_TokenReducer;
import aletheia.parser.parameteridentification.semantic.T__Ts_TokenReducer;
import aletheia.parser.parameteridentification.semantic.T___TokenReducer;
import aletheia.parser.parameteridentification.semantic.Tk__F_TokenReducer;
import aletheia.parser.parameteridentification.semantic.Tk__openpar_T_closepar_TokenReducer;
import aletheia.parser.parameteridentification.semantic.Ts__Tk_TokenReducer;
import aletheia.parser.parameteridentification.semantic.Ts__Ts_Tk_TokenReducer;
import aletheia.parsergenerator.ParserBaseException;
import aletheia.parsergenerator.lexer.AutomatonSet;
import aletheia.parsergenerator.lexer.AutomatonSetLexer;
import aletheia.parsergenerator.parser.Parser;
import aletheia.parsergenerator.parser.TransitionTable;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer;
import aletheia.utilities.collections.ReverseList;

public class ParameterIdentificationParser extends Parser
{
	private static final long serialVersionUID = 3547200583493971229L;

	public static abstract class ProductionTokenPayloadReducer<P> extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Void, P>
	{
		protected FunctionParameterIdentification makeFunctioParameterIdentification(ParameterWithTypeList parameterWithTypeList, ParameterIdentification body)
		{
			for (ParameterWithDomain parameterWithType : new ReverseList<>(parameterWithTypeList))
			{
				Identifier parameter = null;
				ParameterIdentification domain = null;
				if (parameterWithType != null)
				{
					parameter = parameterWithType.getParameter();
					domain = parameterWithType.getDomain();
					body = FunctionParameterIdentification.make(parameter, domain, body);
				}
			}
			return (FunctionParameterIdentification) body;
		}

	}

	//@formatter:off
	private final static Collection<Class<? extends ProductionManagedTokenPayloadReducer.ProductionTokenPayloadReducer<Void, ?>>> reducerClasses =
			Arrays.asList(
					T__Ts_TokenReducer.class,
					T___TokenReducer.class,
					
					Ts__Ts_Tk_TokenReducer.class,
					Ts__Tk_TokenReducer.class,
					
					Tk__F_TokenReducer.class,
					Tk__openpar_T_closepar_TokenReducer.class,
					
					F__openfun_M_arrow_Ts_closefun_TokenReducer.class,
					F__openfun_M_closefun_TokenReducer.class,
					
					M__M_comma_P_TokenReducer.class,
					M__P_TokenReducer.class,

					P__I_TokenReducer.class,
					P___TokenReducer.class,
					P__I_colon_T_TokenReducer.class,
					P__colon_T_TokenReducer.class,

					I__I_dot_id_TokenReducer.class,
					I__id_TokenReducer.class);
	//@formatter:on

	private final static ParameterIdentificationParser instance = new ParameterIdentificationParser();

	private final AutomatonSet automatonSet;
	private final ProductionManagedTokenPayloadReducer<Void, ?> tokenPayloadReducer;

	private static TransitionTable loadTransitionTable()
	{
		try (InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.parameterIdentificationTransitionTablePath))
		{
			return TransitionTable.load(is);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private ParameterIdentificationParser()
	{
		super(loadTransitionTable());
		try (InputStream is = ClassLoader.getSystemResourceAsStream(AletheiaParserConstants.automatonSetPath))
		{
			automatonSet = AutomatonSet.load(is);
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new RuntimeException(e);
		}
		this.tokenPayloadReducer = new ProductionManagedTokenPayloadReducer<>(reducerClasses);
	}

	public static ParameterIdentification parseParameterIdentification(Reader reader) throws ParserBaseException
	{
		return instance.parse(reader);
	}

	private ParameterIdentification parse(Reader reader) throws ParserBaseException
	{
		return (ParameterIdentification) parseToken(new AutomatonSetLexer(automatonSet, reader), tokenPayloadReducer, null);
	}

}
