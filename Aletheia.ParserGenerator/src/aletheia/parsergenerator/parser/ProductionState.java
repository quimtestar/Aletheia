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

import java.io.Serializable;

import aletheia.parsergenerator.symbols.Symbol;

/**
 * This immutable class represents a semi-processed {@link Production}. It's
 * defined by a {@link Production} and an integer that represents a position on
 * the right side of the production. That position must be between 0 (nothing
 * processed) to the size of the right side of the production, inclusively (the
 * full production processed).
 */
public class ProductionState implements Serializable
{
	private static final long serialVersionUID = 4862699222807870794L;

	private final Production production;
	private final int position;

	/**
	 * Creates a new production state given the production and the position.
	 *
	 * @param production
	 *            The production.
	 * @param position
	 *            The position.
	 */
	private ProductionState(Production production, int position)
	{
		this.production = production;
		this.position = position;
	}

	/**
	 * Creates a new production state at position 0 of a production.
	 *
	 * @param production
	 *            The production.
	 */
	public ProductionState(Production production)
	{
		this(production, 0);
	}

	public Production getProduction()
	{
		return production;
	}

	public int getPosition()
	{
		return position;
	}

	/**
	 * The position is greater or equal to the size of the right side of the
	 * production.
	 *
	 * @return Is this production at end?
	 */
	public boolean atEnd()
	{
		return position >= production.getRight().size();
	}

	/**
	 * If this production state is not at end, returns a new production state
	 * advancing one position to the right.
	 *
	 * @return The advanced production state.
	 */
	public ProductionState advance()
	{
		if (atEnd())
			throw new RuntimeException();
		return new ProductionState(production, position + 1);
	}

	/**
	 * The next symbol on the right side of the production, according to the
	 * position of this state.
	 *
	 * @return The symbol.
	 */
	public Symbol nextSymbol()
	{
		return production.getRight().get(position);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
		result = prime * result + ((production == null) ? 0 : production.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		ProductionState other = (ProductionState) obj;
		if (position != other.position)
			return false;
		if (production == null)
		{
			if (other.production != null)
				return false;
		}
		else if (!production.equals(other.production))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(production.getLeft().toString());
		buf.append(" -> ");
		int i = 0;
		for (Symbol s : production.getRight())
		{
			if (i == position)
				buf.append("*");
			buf.append(s.toString());
			buf.append(" ");
			i++;
		}
		if (i == position)
			buf.append("*");
		return buf.toString();
	}

}
