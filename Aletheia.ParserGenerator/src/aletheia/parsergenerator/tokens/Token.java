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

import java.io.PrintStream;
import java.util.Stack;

import aletheia.parsergenerator.symbols.Symbol;
import aletheia.utilities.collections.ReverseList;

/**
 * A token is a part of the input that has been processed with the lexer/parser
 * and is associated to the a @{linkplain Symbol grammar symbol}. A token also
 * keeps record of the start location and end location of the part of the input
 * processed.
 *
 * @param <S>
 *            The symbol class that is associated to this token class.
 */
public class Token<S extends Symbol>
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
	 * The start location.
	 *
	 * @return The start location.
	 */
	public Location getStartLocation()
	{
		return startLocation;
	}

	/**
	 * The stop location.
	 *
	 * @return The stop location.
	 */
	public Location getStopLocation()
	{
		return stopLocation;
	}

	public void trace(PrintStream out)
	{
		class StackEntry
		{
			public final Token<?> token;
			public final String indent;

			public StackEntry(Token<?> token, String indent)
			{
				super();
				this.token = token;
				this.indent = indent;
			}
		}
		;

		Stack<StackEntry> stack = new Stack<StackEntry>();
		stack.push(new StackEntry(this, ""));
		while (!stack.isEmpty())
		{
			StackEntry se = stack.pop();
			out.print(se.indent + se.token.getSymbol());
			if (se.token instanceof TerminalToken)
			{
				TerminalToken ttok = (TerminalToken) se.token;
				if (ttok instanceof TaggedTerminalToken)
				{
					TaggedTerminalToken tagtok = (TaggedTerminalToken) ttok;
					out.println(": " + tagtok.toString());
				}
				else
					out.println();
			}
			else if (se.token instanceof NonTerminalToken)
			{
				NonTerminalToken nttok = (NonTerminalToken) se.token;
				out.println(": " + nttok.getProduction().getRight());
				String indent = se.indent + " ";
				for (Token<?> c : new ReverseList<>(nttok.getChildren()))
					stack.push(new StackEntry(c, indent));
			}
			else
				throw new Error();
		}

	}

}
