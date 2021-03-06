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
package aletheia.parsergenerator.tokens;

import java.util.List;

import aletheia.parsergenerator.parser.Production;
import aletheia.parsergenerator.semantic.SemanticException;
import aletheia.parsergenerator.semantic.TokenPayloadReducer;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;

public class NonTerminalToken<G, P> extends Token<NonTerminalSymbol>
{
	private final P payload;

	public NonTerminalToken(G globals, List<Token<? extends Symbol>> antecedents, Production production, List<Token<? extends Symbol>> reducees,
			TokenPayloadReducer<G, ? extends P> reducer) throws SemanticException
	{
		super(production.getLeft(), locationPairFromAntecedentsReducees(antecedents, reducees));
		this.payload = reducer.reduce(globals, antecedents, production, reducees);
	}

	public P getPayload()
	{
		return payload;
	}

	@Override
	public String toString()
	{
		return super.toString() + ":" + payload;
	}

	@SuppressWarnings("unchecked")
	public static <P> P getPayloadFromTokenList(List<Token<? extends Symbol>> list, int i)
	{
		return ((NonTerminalToken<?, ? extends P>) list.get(i)).getPayload();
	}

	public static <P, T extends NonTerminalToken<?, P>> T findLastInList(List<Token<? extends Symbol>> list, NonTerminalSymbol symbol)
	{
		return Token.findLastInList(list, symbol);
	}

	public static <P, T extends NonTerminalToken<?, P>> T findLastInList(List<Token<? extends Symbol>> list, NonTerminalSymbol target, Symbol stopper)
	{
		return Token.findLastInList(list, target, stopper);
	}

	public static <P, T extends NonTerminalToken<?, P>> P findLastPayloadInList(List<Token<? extends Symbol>> list, NonTerminalSymbol symbol)
	{
		T token = NonTerminalToken.<P, T> findLastInList(list, symbol);
		return token == null ? null : token.getPayload();
	}

	public static <P, T extends NonTerminalToken<?, P>> P findLastPayloadInList(List<Token<? extends Symbol>> list, NonTerminalSymbol target, Symbol stopper)
	{
		T token = NonTerminalToken.<P, T> findLastInList(list, target, stopper);
		return token == null ? null : token.getPayload();
	}

}
