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
package aletheia.parser.term.semantic;

import java.util.Iterator;
import java.util.List;

import aletheia.model.term.ParameterVariableTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "T", right =
{ "T", "B" })
public class T__T_B_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 0);
		Term tail = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		try
		{
			Term composed = term.compose(tail);
			if (globals.getParameterIdentifiers() != null)
			{
				Iterator<ParameterVariableTerm> i0 = term.parameters().iterator();
				if (i0.hasNext())
				{
					i0.next();
					for (ParameterVariableTerm p : composed.parameters())
					{
						if (!i0.hasNext())
							break;
						ParameterVariableTerm p0 = i0.next();
						if (!globals.getParameterIdentifiers().containsKey(p))
							globals.getParameterIdentifiers().put(p, globals.getParameterIdentifiers().get(p0));
					}
				}
			}
			return composed;
		}
		catch (ComposeTypeException e)
		{
			throw new SemanticException(reducees.get(0), e);
		}

	}

}
