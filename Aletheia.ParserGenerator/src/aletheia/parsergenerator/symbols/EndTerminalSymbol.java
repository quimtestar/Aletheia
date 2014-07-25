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
package aletheia.parsergenerator.symbols;

/**
 * And standard grammar symbol that represents the end of input. It is a
 * singleton class. Should not be used in an actual grammar, it's just intended
 * to be a mark symbol.
 */
public class EndTerminalSymbol extends TerminalSymbol
{
	private static final long serialVersionUID = 1113726528173385977L;

	private EndTerminalSymbol()
	{
	}

	@Override
	public String toString()
	{
		return "$";
	}

	public static EndTerminalSymbol instance = new EndTerminalSymbol();

	@Override
	public boolean equals(Object o)
	{
		return (o instanceof EndTerminalSymbol);
	}

	@Override
	public int hashCode()
	{
		return EndTerminalSymbol.class.hashCode() * 34357;
	}

}
