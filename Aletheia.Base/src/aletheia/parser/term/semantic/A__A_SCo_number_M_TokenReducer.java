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
package aletheia.parser.term.semantic;

import java.util.List;

import aletheia.model.term.CompositionTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.TaggedTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.utilities.collections.BufferedList;

@AssociatedProduction(left = "A", right =
{ "A", "SCo", "number", "M" })
public class A__A_SCo_number_M_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		if (term instanceof CompositionTerm)
		{
			List<Term> components;
			if (NonTerminalToken.<Boolean> getPayloadFromTokenList(reducees, 3))
				components = new BufferedList<>(((CompositionTerm) term).aggregateComponents());
			else
				components = new BufferedList<>(((CompositionTerm) term).components());
			int n = Integer.parseInt(TaggedTerminalToken.getTextFromTokenList(reducees, 2));
			if (n < 0 || n >= components.size())
				throw new SemanticException(reducees.get(2), "Composition coordinate " + n + " out of bounds for term: " + "'"
						+ term.toString(globals.getTransaction(), globals.getContext()) + "'");
			return components.get(n);
		}
		else
			throw new SemanticException(reducees.get(0), "Only can use composition coordinates in compositions");
	}

}
