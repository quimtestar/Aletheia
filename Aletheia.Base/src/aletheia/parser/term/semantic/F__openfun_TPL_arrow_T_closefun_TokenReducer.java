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

import aletheia.model.term.FunctionTerm;
import aletheia.model.term.Term;
import aletheia.model.term.Term.ComposeTypeException;
import aletheia.parser.term.TermParser.Globals;
import aletheia.parser.term.TermParser.ProductionTokenPayloadReducer;
import aletheia.parser.term.parameterRef.TypedParameterRef;
import aletheia.parser.term.parameterRef.TypedParameterRefList;
import aletheia.parser.term.parameterRef.TypedParameterRefWithValue;
import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.ProductionManagedTokenPayloadReducer.AssociatedProduction;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.tokens.NonTerminalToken;
import aletheia.parsergenerator.tokens.Token;
import aletheia.utilities.collections.ReverseList;

@AssociatedProduction(left = "F", right =
{ "openfun", "TPL", "arrow", "T", "closefun" })
public class F__openfun_TPL_arrow_T_closefun_TokenReducer extends ProductionTokenPayloadReducer<Term>
{

	@Override
	public Term reduce(Globals globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees)
			throws SemanticException
	{
		TypedParameterRefList typedParameterRefList = NonTerminalToken.getPayloadFromTokenList(reducees, 1);
		Term term = NonTerminalToken.getPayloadFromTokenList(reducees, 3);
		for (TypedParameterRef typedParameterRef : new ReverseList<>(typedParameterRefList.list()))
		{
			term = new FunctionTerm(typedParameterRef.getParameter(), term);
			if (typedParameterRef instanceof TypedParameterRefWithValue)
				try
				{
					term = term.compose(((TypedParameterRefWithValue) typedParameterRef).getValue());
				}
				catch (ComposeTypeException e)
				{
					throw new SemanticException(reducees, e);
				}
		}
		return term;
	}

}
