/*******************************************************************************
 * Copyright (c) 2017 Quim Testar
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
package aletheia.parser.parameteridentification.tokenprocessor;

import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.TokenProcessorException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.ParseTreeToken;

@ProcessorProduction(left = "F", right =
{ "openfun", "M", "arrow", "T_", "closefun" })
public class F_openfun_M_arrow_T__closefun_FunctionParameterIdentificationTokenSubProcessor extends FunctionParameterIdentificationTokenSubProcessor
{

	protected F_openfun_M_arrow_T__closefun_FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected FunctionParameterIdentification subProcess(ParseTreeToken token) throws TokenProcessorException
	{
		ParameterWithTypeList parameterWithTypeList = getProcessor().processParameterWithTypeList((ParseTreeToken) token.getChildren().get(1));
		ParameterIdentification body = getProcessor().processParameterIdentification((ParseTreeToken) token.getChildren().get(3));
		return subProcess(parameterWithTypeList, body);
	}

}
