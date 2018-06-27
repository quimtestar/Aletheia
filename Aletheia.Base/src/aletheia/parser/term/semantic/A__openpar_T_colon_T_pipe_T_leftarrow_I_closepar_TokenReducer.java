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

import aletheia.model.identifier.Identifier;
import aletheia.model.term.FoldingCastTypeTerm;
import aletheia.model.term.FoldingCastTypeTerm.FoldingCastTypeException;
import aletheia.model.term.IdentifiableVariableTerm;
import aletheia.model.term.Term;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;

@AssociatedProduction(left = "A", right =
{ "openpar", "T", "colon", "T", "pipe", "T", "leftarrow", "I", "closepar" })
public class A__openpar_T_colon_T_pipe_T_leftarrow_I_closepar_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		Term type = NonTerminalToken.getPayloadFromTokenList(reducees, 3);
		Term value = NonTerminalToken.getPayloadFromTokenList(reducees, 5);
		Identifier identifier = NonTerminalToken.getPayloadFromTokenList(reducees, 7);
		IdentifiableVariableTerm variable = globals.getContext().identifierToVariable(globals.getTransaction()).get(identifier);
		if (variable == null)
			throw new SemanticException(reducees.get(0), "Identifier:" + "'" + identifier + "'" + " not defined");
		try
		{
			return new FoldingCastTypeTerm(term, type, variable, value);
		}
		catch (FoldingCastTypeException e)
		{
			throw new SemanticException(reducees, e);
		}
	}

}
