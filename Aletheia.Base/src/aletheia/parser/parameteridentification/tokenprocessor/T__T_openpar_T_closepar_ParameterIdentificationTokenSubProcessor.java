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

import aletheia.model.term.CompositionTerm.CompositionParameterIdentification;
import aletheia.model.term.Term.ParameterIdentification;
import aletheia.parser.TokenProcessorException;
import aletheia.parsergenerator.tokens.NonTerminalToken;

@ProcessorProduction(left = "T_", right =
{ "T", "openpar", "T", "closepar" })
public class T__T_openpar_T_closepar_ParameterIdentificationTokenSubProcessor extends ParameterIdentificationTokenSubProcessor
{

	protected T__T_openpar_T_closepar_ParameterIdentificationTokenSubProcessor(TokenProcessor processor)
	{
		super(processor);
	}

	@Override
	protected ParameterIdentification subProcess(NonTerminalToken token) throws TokenProcessorException
	{
		ParameterIdentification head = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(0));
		ParameterIdentification tail = getProcessor().processParameterIdentification((NonTerminalToken) token.getChildren().get(2));
		if (head instanceof CompositionParameterIdentification || head == null)
			return new CompositionParameterIdentification((CompositionParameterIdentification) head, tail);
		else
			return new CompositionParameterIdentification(null,
					new CompositionParameterIdentification(new CompositionParameterIdentification(null, head), tail));
	}

}
