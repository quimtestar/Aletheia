/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
package aletheia.parsergenerator.parser;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import aletheia.parsergenerator.symbols.EndTerminalSymbol;
import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;
import aletheia.parsergenerator.symbols.TerminalSymbol;

/**
 * An immutable state class used when parsing an input with a grammar. Defined
 * by a {@link Grammar}, a set of {@link ProductionState}s made of productions
 * of the same grammar and their correspondence to sets of follower terminal
 * symbols.
 */
public class ParserItem implements Serializable
{
	private static final long serialVersionUID = 873594274702267971L;

	private final Grammar grammar;
	private final Map<ProductionState, Set<TerminalSymbol>> followerMap;

	/**
	 * Creates a new grammar state given the {@link Grammar}.
	 * 
	 * @param grammar
	 *            The grammar.
	 */
	private ParserItem(Grammar grammar)
	{
		this.grammar = grammar;
		this.followerMap = new HashMap<ProductionState, Set<TerminalSymbol>>();
	}

	/**
	 * Given a symbol of the grammar, populate the set of production states with
	 * the initial production states (at position 0) that:
	 * <ul>
	 * <li>Correspond to a production whose left side is that symbol.</li>
	 * <li>For every first symbol of the right side on any of these productions,
	 * do the same processing recursively.</li>
	 * </ul>
	 * 
	 * @param start
	 *            The start symbol.
	 */
	private void populateStarters(Symbol start, Set<TerminalSymbol> followers)
	{
		class StackEntry
		{
			public final Symbol symbol;
			public final Set<TerminalSymbol> followers;

			public StackEntry(Symbol symbol, Set<TerminalSymbol> followers)
			{
				super();
				this.symbol = symbol;
				this.followers = followers;
			}
		}
		;

		Stack<StackEntry> stack = new Stack<StackEntry>();
		stack.push(new StackEntry(start, followers));
		while (!stack.isEmpty())
		{
			StackEntry se = stack.pop();
			Set<Production> set = grammar.getProductions().get(se.symbol);
			if (set != null)
			{
				for (Production prod : set)
				{
					ProductionState ps = new ProductionState(prod);
					Set<TerminalSymbol> fws = followerMap.get(ps);
					if (fws == null)
					{
						fws = new HashSet<TerminalSymbol>();
						followerMap.put(ps, fws);
					}
					boolean change = fws.addAll(se.followers);
					if (change && !ps.atEnd())
						stack.push(new StackEntry(ps.nextSymbol(), nextFollowers(ps, se.followers)));
				}
			}
		}
	}

	/**
	 * The set of possible terminal symbol followers of the next symbol of the
	 * production state.
	 * 
	 * @param ps
	 *            The production state.
	 * @param prodFollowers
	 *            The set of followers of this production. If the rest of the
	 *            production is nullable, this set will be added to the result.
	 * @return The set of symbol followers.
	 */
	private Set<TerminalSymbol> nextFollowers(ProductionState ps, Set<TerminalSymbol> prodFollowers)
	{
		Set<TerminalSymbol> set = new HashSet<TerminalSymbol>();
		for (int pos = ps.getPosition() + 1; pos < ps.getProduction().getRight().size(); pos++)
		{
			Symbol s = ps.getProduction().getRight().get(pos);
			if (s instanceof TerminalSymbol)
			{
				set.add((TerminalSymbol) s);
				return set;
			}
			else
			{
				NonTerminalSymbol nts = (NonTerminalSymbol) s;
				set.addAll(grammar.firstSymbols(nts));
				if (!grammar.nullableSymbol(nts))
					return set;
			}
		}
		set.addAll(prodFollowers);
		return set;
	}

	/**
	 * Creates the initial grammar state of a grammar. The set of production
	 * states of the initial grammar state is computed by just calling to the
	 * {@link #populateStarters(Symbol, Set)} method with the start symbol of
	 * the grammar.
	 * 
	 * @param grammar
	 *            The grammar.
	 * 
	 * @see #populateStarters(Symbol, Set)
	 */
	public static ParserItem initial(Grammar grammar)
	{
		ParserItem parserItem = new ParserItem(grammar);
		parserItem.populateStarters(grammar.getStartSymbol(), Collections.<TerminalSymbol> singleton(EndTerminalSymbol.instance));
		return parserItem;
	}

	/**
	 * The grammar.
	 * 
	 * @return The grammar.
	 */
	public Grammar getGrammar()
	{
		return grammar;
	}

	/**
	 * The set of {@link ProductionState}s of this grammar state.
	 * 
	 * @return The set of production states.
	 */
	public Set<ProductionState> getProductionStates()
	{
		return Collections.unmodifiableSet(followerMap.keySet());
	}

	/**
	 * The set of {@link TerminalSymbol}s that may follow a
	 * {@link ProductionState} on this item.
	 * 
	 * @param productionState
	 *            The production state;
	 * @return The set of followers;
	 */
	public Set<TerminalSymbol> getFollowers(ProductionState productionState)
	{
		Set<TerminalSymbol> set = followerMap.get(productionState);
		if (set == null)
			return null;
		else
			return Collections.unmodifiableSet(set);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((followerMap == null) ? 0 : followerMap.hashCode());
		result = prime * result + ((grammar == null) ? 0 : grammar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParserItem other = (ParserItem) obj;
		if (followerMap == null)
		{
			if (other.followerMap != null)
				return false;
		}
		else if (!followerMap.equals(other.followerMap))
			return false;
		if (grammar == null)
		{
			if (other.grammar != null)
				return false;
		}
		else if (!grammar.equals(other.grammar))
			return false;
		return true;
	}

	/**
	 * A grammar state is empty when it has no production states.
	 * 
	 * @return Is this grammar state empty?
	 */
	private boolean isEmpty()
	{
		return followerMap.isEmpty();
	}

	/**
	 * Computes the transition of this grammar state with a given symbol. The
	 * next grammar state is computed by advancing every production state whose
	 * next symbol is this parameter symbol and then calling the
	 * {@link #populateStarters(Symbol, Set)} method for the next symbol of the
	 * advanced production state.
	 * 
	 * @param symbol
	 *            The symbol.
	 * @return The new grammar state.
	 * 
	 * @see #populateStarters(Symbol, Set)
	 */
	public ParserItem next(Symbol symbol)
	{
		ParserItem next = new ParserItem(grammar);
		for (Map.Entry<ProductionState, Set<TerminalSymbol>> fme : followerMap.entrySet())
		{
			ProductionState ps = fme.getKey();
			Set<TerminalSymbol> ts = fme.getValue();
			if (!ps.atEnd() && ps.nextSymbol().equals(symbol))
			{
				ProductionState psa = ps.advance();
				Set<TerminalSymbol> fws = next.followerMap.get(psa);
				if (fws == null)
				{
					fws = new HashSet<TerminalSymbol>();
					next.followerMap.put(psa, fws);
				}
				fws.addAll(ts);
				if (!psa.atEnd())
					next.populateStarters(psa.nextSymbol(), nextFollowers(psa, ts));
			}
		}
		return next;
	}

	/**
	 * Computes the map of next grammar states for every possible symbol.
	 * 
	 * @return The map.
	 */
	public Map<Symbol, ParserItem> next()
	{
		Map<Symbol, ParserItem> map = new HashMap<Symbol, ParserItem>();
		for (Symbol s : grammar.getSymbols())
		{
			ParserItem next = next(s);
			if (!next.isEmpty())
				map.put(s, next);
		}
		return map;
	}

	/**
	 * Computes the map of productions to terminal symbols that are at end in
	 * the set of production states of this grammar state.
	 * 
	 * @return The map.
	 * 
	 * @see ProductionState#atEnd()
	 */
	public Map<Production, Set<TerminalSymbol>> endingProductions()
	{
		Map<Production, Set<TerminalSymbol>> map = new HashMap<Production, Set<TerminalSymbol>>();
		for (Map.Entry<ProductionState, Set<TerminalSymbol>> fme : followerMap.entrySet())
		{
			if (fme.getKey().atEnd())
				map.put(fme.getKey().getProduction(), fme.getValue());
		}
		return map;
	}

	@Override
	public String toString()
	{
		return followerMap.toString();
	}

	public ParserItem fusion(ParserItem item)
	{
		if (!grammar.equals(item.grammar))
			throw new RuntimeException("Different grammars");
		ParserItem res = new ParserItem(grammar);
		res.followerMap.putAll(followerMap);
		for (Map.Entry<ProductionState, Set<TerminalSymbol>> e : item.followerMap.entrySet())
		{
			Set<TerminalSymbol> fws = res.followerMap.get(e.getKey());
			if (fws == null)
				res.followerMap.put(e.getKey(), e.getValue());
			else
				res.followerMap.get(e.getKey()).addAll(e.getValue());
		}
		return res;
	}

	public boolean isFollowerSubsetOf(ParserItem other)
	{
		for (Map.Entry<ProductionState, Set<TerminalSymbol>> e : followerMap.entrySet())
		{
			if (!e.getValue().isEmpty())
			{
				Set<TerminalSymbol> set = other.followerMap.get(e.getKey());
				if (set == null)
					return false;
				if (!set.containsAll(e.getValue()))
					return false;
			}
		}
		return true;
	}
}
