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

import aletheia.model.identifier.Identifier;
import aletheia.model.term.FunctionTerm.FunctionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.AletheiaParserException;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithType;
import aletheia.parser.parameteridentification.tokenprocessor.TokenProcessor.ParameterWithTypeList;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.utilities.collections.ReverseList;

public abstract class FunctionParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected FunctionParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	protected FunctionParameterIdentification subProcess(ParameterWithTypeList parameterWithTypeList, ParameterIdentification body)
	{
		for (ParameterWithType parameterWithType : new ReverseList<>(parameterWithTypeList))
		{
			Identifier parameter = null;
			ParameterIdentification parameterType = null;
			if (parameterWithType != null)
			{
				parameter = parameterWithType.getParameter();
				parameterType = parameterWithType.getParameterType();
				if (parameter != null || parameterType != null || body != null)
					body = new FunctionParameterIdentification(parameter, parameterType, body);
			}
		}
		return (FunctionParameterIdentification) body;

	}

	@Override
	protected abstract FunctionParameterIdentification subProcess(NonTerminalToken token, String input) throws AletheiaParserException;

}
