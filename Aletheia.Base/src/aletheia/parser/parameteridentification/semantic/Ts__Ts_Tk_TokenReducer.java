/*******************************************************************************
 * Copyright (c) 2018 Quim Testar
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
package aletheia.parser.parameteridentification.semantic;

import java.util.List;

import aletheia.model.parameteridentification.CompositionParameterIdentification;
import aletheia.model.parameteridentification.ParameterIdentification;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "Ts", right =
{ "Ts", "Tk" })
public class Ts__Ts_Tk_TokenReducer extends ProductionTokenPayloadReducer<CompositionParameterIdentification>
{

	@Override
	public CompositionParameterIdentification reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production,
			List<Token<? extends Symbol>> reducees) throws SemanticException
	{
		ParameterIdentification head = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		ParameterIdentification tail = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		CompositionParameterIdentification head_;
		if (head == null || head instanceof CompositionParameterIdentification)
			head_ = (CompositionParameterIdentification) head;
		else
			head_ = CompositionParameterIdentification.make(null, head);
		return CompositionParameterIdentification.make(head_, tail);
	}

}
