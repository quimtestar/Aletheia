/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
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
import aletheia.utilities.collections.AdaptedCollection;
import aletheia.utilities.collections.AdaptedSet;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionSet;
import aletheia.utilities.collections.DifferenceSet;
import aletheia.utilities.collections.ReverseList;
import aletheia.utilities.collections.UnionCollection;

/**
 * A grammar is defined by a set of {@linkplain Production productions} and a
 * start {@linkplain Symbol symbol}.
 */
public class Grammar implements Serializable
{
	private static final long serialVersionUID = -6463358111810326003L;

	private final Map<NonTerminalSymbol, Set<Production>> productions;
	private final Symbol startSymbol;
	private final Set<Symbol> symbols;
	private final Set<NonTerminalSymbol> nullableSymbols;
	private final Map<NonTerminalSymbol, Set<TerminalSymbol>> firstSymbols;
	private final Map<NonTerminalSymbol, Set<TerminalSymbol>> nextSymbols;

	/**
	 * Creates a new grammar from a collection of productions and a start
	 * symbol.
	 *
	 * @param productions
	 *            The collection of productions.
	 * @param startSymbol
	 *            The start symbol.
	 */
	public Grammar(Collection<Production> productions, Symbol startSymbol)
	{
		this.productions = new HashMap<>();
		for (Production prod : productions)
		{
			Set<Production> set = this.productions.get(prod.getLeft());
			if (set == null)
			{
				set = new HashSet<>();
				this.productions.put(prod.getLeft(), set);
			}
			set.add(prod);
		}
		this.startSymbol = startSymbol;
		this.symbols = new HashSet<>();
		for (Production prod : productions)
		{
			symbols.add(prod.getLeft());
			symbols.addAll(prod.getRight());
		}
		this.nullableSymbols = new HashSet<>();
		computeNullableSymbols();
		this.firstSymbols = new HashMap<>();
		computeFirstSymbols();
		this.nextSymbols = new HashMap<>();
		computeNextSymbols();
	}

	/**
	 * The productions of this grammar organized as a map from the non-terminal
	 * symbols that constitute their left sides to sets of productions.
	 *
	 * @return The map of productions.
	 */
	public Map<NonTerminalSymbol, Set<Production>> getProductions()
	{
		return Collections.unmodifiableMap(productions);
	}

	/**
	 * The full collection of productions.
	 *
	 * @return The collection of productions.
	 */
	public Collection<Production> productions()
	{
		return new UnionCollection<>(new AdaptedCollection<>(productions.values()));
	}

	/**
	 * The start symbol.
	 *
	 * @return The start symbol.
	 */
	public Symbol getStartSymbol()
	{
		return startSymbol;
	}

	/**
	 * The full set of symbols of this grammar. Any symbol appearing on any side
	 * of any production of this grammar is in this set.
	 *
	 * @return The set of symbols.
	 */
	public Set<Symbol> getSymbols()
	{
		return Collections.unmodifiableSet(symbols);
	}

	/**
	 * Computes the set of nullable symbols. To be called only at construction
	 * time.
	 *
	 * @see #nullableSymbol(NonTerminalSymbol)
	 */
	private void computeNullableSymbols()
	{
		nullableSymbols.clear();
		while (true)
		{
			Set<NonTerminalSymbol> added = new HashSet<>();
			b: for (Map.Entry<NonTerminalSymbol, Set<Production>> e : productions.entrySet())
			{
				if (!nullableSymbols.contains(e.getKey()))
				{
					a: for (Production prod : e.getValue())
					{
						for (Symbol s : prod.getRight())
						{
							if (!nullableSymbols.contains(s))
								continue a;
						}
						added.add(e.getKey());
						continue b;
					}
				}
			}
			if (added.isEmpty())
				break;
			nullableSymbols.addAll(added);
		}
	}

	/**
	 * Checks if a symbol is nullable. A symbol is nullable if it can be
	 * converted to the empty string using the production rules of this grammar.
	 *
	 * @param s
	 *            The symbol.
	 * @return Is it nullable?
	 */
	public boolean nullableSymbol(NonTerminalSymbol s)
	{
		return nullableSymbols.contains(s);
	}

	/**
	 * Computes the map of first symbols. To be called only at construction
	 * time.
	 *
	 * @see #firstSymbols(NonTerminalSymbol)
	 */
	private void computeFirstSymbols()
	{
		firstSymbols.clear();
		for (NonTerminalSymbol s : productions.keySet())
			firstSymbols.put(s, new HashSet<>());
		while (true)
		{
			boolean add = false;
			for (Map.Entry<NonTerminalSymbol, Set<Production>> e : productions.entrySet())
			{
				Set<TerminalSymbol> added = new HashSet<>();
				a: for (Production prod : e.getValue())
				{
					for (Symbol s_ : prod.getRight())
					{
						if (s_ instanceof TerminalSymbol)
						{
							if (!firstSymbols.get(e.getKey()).contains(s_))
								added.add((TerminalSymbol) s_);
							continue a;
						}
						else if (s_ instanceof NonTerminalSymbol)
						{
							for (TerminalSymbol s__ : firstSymbols.get(s_))
							{
								if (!firstSymbols.get(e.getKey()).contains(s__))
									added.add(s__);
							}
							if (!nullableSymbol((NonTerminalSymbol) s_))
								continue a;
						}
						else
							throw new Error();
					}
				}
				if (!added.isEmpty())
				{
					firstSymbols.get(e.getKey()).addAll(added);
					add = true;
				}
			}
			if (!add)
				break;
		}
	}

	/**
	 * The set of first symbols of a non-terminal symbol. A first symbol is any
	 * terminal symbol that can be the first one of a string derived from that
	 * non-terminal symbol using the production rules of this grammar.
	 *
	 * @param s
	 *            The non-terminal symbol.
	 * @return The set of first symbols.
	 */
	public Set<TerminalSymbol> firstSymbols(NonTerminalSymbol s)
	{
		return Collections.unmodifiableSet(firstSymbols.get(s));
	}

	/**
	 * Computes the map of next symbols. To be called only at construction time.
	 *
	 * @see #nextSymbols(NonTerminalSymbol)
	 */
	private void computeNextSymbols()
	{
		nextSymbols.clear();
		for (NonTerminalSymbol s : productions.keySet())
		{
			nextSymbols.put(s, new HashSet<>());
			if (s.equals(startSymbol))
				nextSymbols.get(s).add(EndTerminalSymbol.instance);
		}
		while (true)
		{
			boolean add = false;
			for (Map.Entry<NonTerminalSymbol, Set<Production>> e : productions.entrySet())
			{
				for (Production prod : e.getValue())
				{
					a: for (int i = 0; i < prod.getRight().size(); i++)
					{
						Symbol s = prod.getRight().get(i);
						if (s instanceof NonTerminalSymbol)
						{
							if (i + 1 < prod.getRight().size())
							{
								for (int j = i + 1; j < prod.getRight().size(); j++)
								{
									Symbol s_ = prod.getRight().get(j);
									if (s_ instanceof TerminalSymbol)
									{
										if (!nextSymbols.get(s).contains(s_))
										{
											nextSymbols.get(s).add((TerminalSymbol) s_);
											add = true;
										}
										continue a;
									}
									else if (s_ instanceof NonTerminalSymbol)
									{
										for (TerminalSymbol s__ : firstSymbols((NonTerminalSymbol) s_))
										{
											if (!nextSymbols.get(s).contains(s__))
											{
												nextSymbols.get(s).add(s__);
												add = true;
											}
										}
										if (!nullableSymbol((NonTerminalSymbol) s_))
											continue a;
									}
									else
										throw new Error();
								}
							}
							else
							{
								for (TerminalSymbol s_ : nextSymbols.get(prod.getLeft()))
								{
									if (!nextSymbols.get(s).contains(s_))
									{
										nextSymbols.get(s).add(s_);
										add = true;
									}
								}
							}
						}
					}
				}
			}
			if (!add)
				break;
		}
	}

	/**
	 * The set of next symbols of a non-terminal symbol. A next symbol is any
	 * terminal symbol that can be found following any string produced by that
	 * non-terminal symbol, including the {@linkplain EndTerminalSymbol end
	 * terminal symbol} if that string can be at the end of the input.
	 *
	 * @param s
	 *            The non-terminal symbol.
	 * @return The set of next symbols.
	 */
	public Set<TerminalSymbol> nextSymbols(NonTerminalSymbol s)
	{
		return Collections.unmodifiableSet(nextSymbols.get(s));
	}

	@Override
	public String toString()
	{
		return "start:" + startSymbol + ";" + productions.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((productions == null) ? 0 : productions.hashCode());
		result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		Grammar other = (Grammar) obj;
		if (productions == null)
		{
			if (other.productions != null)
				return false;
		}
		else if (!productions.equals(other.productions))
			return false;
		if (symbols == null)
		{
			if (other.symbols != null)
				return false;
		}
		else if (!symbols.equals(other.symbols))
			return false;
		return true;
	}

	/**
	 * The set of non terminal symbols of this grammar.
	 *
	 * @return The set.
	 */
	public Set<NonTerminalSymbol> nonTerminalSymbols()
	{
		return Collections.unmodifiableSet(productions.keySet());
	}

	/**
	 * The set of terminal symbols of this grammar.
	 *
	 * @return The set.
	 */
	public Set<TerminalSymbol> terminalSymbols()
	{
		return new BijectionSet<>(new Bijection<Symbol, TerminalSymbol>()
		{
			@Override
			public TerminalSymbol forward(Symbol input)
			{
				return (TerminalSymbol) input;
			}

			@Override
			public Symbol backward(TerminalSymbol output)
			{
				return output;
			}
		}, new DifferenceSet<>(getSymbols(), new AdaptedSet<>(nonTerminalSymbols())));
	}

	public void trace(PrintStream out)
	{
		out.println(getStartSymbol() + ";");
		Set<NonTerminalSymbol> visited = new HashSet<>();
		Stack<Symbol> stack = new Stack<>();
		stack.push(getStartSymbol());
		while (!stack.isEmpty())
		{
			Symbol s = stack.pop();
			if (s instanceof NonTerminalSymbol)
			{
				if (!visited.contains(s))
				{
					NonTerminalSymbol nts = (NonTerminalSymbol) s;
					visited.add(nts);
					Set<Production> set = getProductions().get(nts);
					if (set != null)
					{
						for (Production p : set)
						{
							out.println(p + ";");
							stack.addAll(new ReverseList<>(p.getRight()));
						}
					}
				}
			}
		}
		out.println();
	}

}
