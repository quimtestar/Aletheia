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

import aletheia.parsergenerator.symbols.TaggedTerminalSymbol;

/**
 * A terminal token associated to a {@link TaggedTerminalSymbol}. It records
 * also the actual text string from the input processed.
 */
public class TaggedTerminalToken extends TerminalToken
{
	private final String text;

	/**
	 * Creates a new tagged terminal token.
	 *
	 * @param symbol
	 *            The tagged terminal symbol.
	 * @param startLocation
	 *            The start location.
	 * @param stopLocation
	 *            The stop location.
	 * @param text
	 *            The text processed.
	 */
	public TaggedTerminalToken(TaggedTerminalSymbol symbol, Location startLocation, Location stopLocation, String text)
	{
		super(symbol, startLocation, stopLocation);
		this.text = text;
	}

	@Override
	public TaggedTerminalSymbol getSymbol()
	{
		return (TaggedTerminalSymbol) super.getSymbol();
	}

	/**
	 * The text of the input processed.
	 *
	 * @return The text.
	 */
	public String getText()
	{
		return text;
	}

	@Override
	public String toString()
	{
		return super.toString() + ":[" + text + "]";
	}

}
