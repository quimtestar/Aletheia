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
package aletheia.parsergenerator.tokens;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import aletheia.parsergenerator.symbols.Symbol;
import aletheia.utilities.MiscUtilities;
import aletheia.utilities.collections.ReverseListIterator;

/**
 * A token is a part of the input that has been processed with the lexer/parser
 * and is associated to the a @{linkplain Symbol grammar symbol}. A token also
 * keeps record of the start location and end location of the part of the input
 * processed.
 *
 * @param <S>
 *            The symbol class that is associated to this token class.
 */
public abstract class Token<S extends Symbol>
{
	private final S symbol;
	private final Location startLocation;
	private final Location stopLocation;

	/**
	 * Creates a new token.
	 *
	 * @param symbol
	 *            The symbol associated to this token.
	 * @param startLocation
	 *            The start location.
	 * @param stopLocation
	 *            The stop location.
	 */
	public Token(S symbol, Location startLocation, Location stopLocation)
	{
		this.symbol = symbol;
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;
	}

	/**
	 * Creates a new token with the same start and stop location.
	 *
	 * @param symbol
	 *            The symbol associated to this token.
	 * @param location
	 *            The start and stop locations.-
	 */
	public Token(S symbol, Location location)
	{
		this(symbol, location, location);
	}

	/**
	 * The symbol.
	 *
	 * @return The symbol.
	 */
	public S getSymbol()
	{
		return symbol;
	}

	@Override
	public String toString()
	{
		return symbol.toString();
	}

	/**
	 * The start location. Null if the token is produced from the empty string.
	 *
	 * @return The start location.
	 */
	public Location getStartLocation()
	{
		return startLocation;
	}

	/**
	 * The stop location. Null if the token is produced by the empty string.
	 *
	 * @return The stop location.
	 */
	public Location getStopLocation()
	{
		return stopLocation;
	}

	public static Location startLocationFromList(List<Token<? extends Symbol>> list)
	{
		return list.isEmpty() ? null : MiscUtilities.firstFromIterable(list).getStartLocation();
	}

	public static Location stopLocationFromList(List<Token<? extends Symbol>> list)
	{
		return list.isEmpty() ? null : MiscUtilities.lastFromList(list).getStopLocation();
	}

	@SuppressWarnings("unchecked")
	public static <S extends Symbol, T extends Token<? extends S>> T findFirstInListIterator(ListIterator<Token<? extends Symbol>> iterator,
			Collection<? extends S> targets, Collection<? extends Symbol> stoppers)
	{
		while (iterator.hasNext())
		{
			Token<? extends Symbol> token = iterator.next();
			if (stoppers.contains(token.getSymbol()))
				return null;
			if (targets.contains(token.getSymbol()))
				return (T) token;
		}
		return null;
	}

	public static <S extends Symbol, T extends Token<? extends S>> T findFirstInListIterator(ListIterator<Token<? extends Symbol>> iterator, S symbol)
	{
		return findFirstInListIterator(iterator, Collections.singleton(symbol), Collections.emptyList());
	}

	public static <S extends Symbol, T extends Token<? extends S>> T findFirstInListIterator(ListIterator<Token<? extends Symbol>> iterator, S target,
			Symbol stopper)
	{
		return findFirstInListIterator(iterator, Collections.singleton(target), Collections.singleton(stopper));
	}

	public static <S extends Symbol, T extends Token<? extends S>> T findLastInList(List<Token<? extends Symbol>> list, S symbol)
	{
		return findFirstInListIterator(new ReverseListIterator<>(list.listIterator(list.size())), symbol);
	}

	public static <S extends Symbol, T extends Token<? extends S>> T findLastInList(List<Token<? extends Symbol>> list, S target, Symbol stopper)
	{
		return findFirstInListIterator(new ReverseListIterator<>(list.listIterator(list.size())), target, stopper);
	}

}
