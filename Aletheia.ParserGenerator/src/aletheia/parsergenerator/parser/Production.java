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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import aletheia.parsergenerator.symbols.NonTerminalSymbol;
import aletheia.parsergenerator.symbols.Symbol;

/**
 * A production is any of the rewrite rules a {@linkplain Grammar grammar} has.
 * It's structure consists of
 * <ul>
 * <li><b>The left side:</b> A {@linkplain NonTerminalSymbol non terminal
 * symbol}.</li>
 * <li><b>The right side:</b> A sequence of {@linkplain Symbol symbols}.</li>
 * </ul>
 * A production is textually represented as:
 *
 * <pre>
 * A -> x<sub>1</sub> x<sub>2</sub> x<sub>3</sub> ... x<sub>n</sub>
 * </pre>
 *
 * Where A is the {@linkplain NonTerminalSymbol non terminal symbol} on the left
 * side and x<sub>i</sub> are the {@linkplain Symbol symbols} on the right side.
 *
 */
public class Production implements Serializable
{
	private static final long serialVersionUID = 1495196072672798162L;

	private final NonTerminalSymbol left;
	private final List<Symbol> right;

	/**
	 * Creates a new production with the specified left and right sides.
	 *
	 * @param left
	 *            The left side.
	 * @param right
	 *            The right side.
	 */
	public Production(NonTerminalSymbol left, List<Symbol> right)
	{
		this.left = left;
		this.right = new ArrayList<Symbol>(right);
	}

	/**
	 * The left side.
	 *
	 * @return The left side.
	 */
	public NonTerminalSymbol getLeft()
	{
		return left;
	}

	/**
	 * The right side.
	 *
	 * @return The right side.
	 */
	public List<Symbol> getRight()
	{
		return Collections.unmodifiableList(right);
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(left.toString());
		buf.append(" -> ");
		for (Symbol s : right)
		{
			buf.append(s.toString());
			buf.append(" ");
		}
		return buf.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		Production other = (Production) obj;
		if (left == null)
		{
			if (other.left != null)
				return false;
		}
		else if (!left.equals(other.left))
			return false;
		if (right == null)
		{
			if (other.right != null)
				return false;
		}
		else if (!right.equals(other.right))
			return false;
		return true;
	}

}
