/*******************************************************************************
 * Copyright (c) 2018, 2020 Quim Testar
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

import aletheia.model.identifier.Identifier;
import aletheia.parser.parameteridentification.ParameterIdentificationParser.ProductionTokenPayloadReducer;
import aletheia.parser.parameteridentification.ParameterWithDomain;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "P", right =
{ "I" })
public class P__I_TokenReducer extends ProductionTokenPayloadReducer<ParameterWithDomain>
{

	@Override
	public ParameterWithDomain reduce(Void globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		return new ParameterWithDomain(identifier, null);
	}

}
