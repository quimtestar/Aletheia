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

import aletheia.parser.TokenProcessorException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.semantic.ParseTree;

@ProcessorProduction(left = "M", right =
{ "M", "comma", "P" })
public class M_M_comma_P_ParameterIdentificationTokenSubProcessor extends ParameterWithTypeListTokenSubProcessor
{

	protected M_M_comma_P_ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterWithTypeList subProcess(ParseTree token) throws TokenProcessorException
	{
		ParameterWithTypeList list = getProcessor().processParameterWithTypeList((ParseTree) token.getChildren().get(0));
		list.add(getProcessor().processParameterWithType((ParseTree) token.getChildren().get(2)));
		return list;
	}

}
