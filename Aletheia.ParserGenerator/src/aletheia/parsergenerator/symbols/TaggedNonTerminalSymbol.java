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
 * A non-terminal symbol that is identified by a string (tag). Two tagged
 * non-terminal symbols with the same tag will be considered to be one and the
 * same for all purposes.
 */
public class TaggedNonTerminalSymbol extends NonTerminalSymbol implements TaggedSymbol
{
	private static final long serialVersionUID = -3145936006599658141L;

	private final String tag;

	public TaggedNonTerminalSymbol(String tag)
	{
		this.tag = tag;
	}

	@Override
	public String getTag()
	{
		return tag;
	}

	@Override
	public String toString()
	{
		return tag;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
		TaggedNonTerminalSymbol other = (TaggedNonTerminalSymbol) obj;
		if (tag == null)
		{
			if (other.tag != null)
				return false;
		}
		else if (!tag.equals(other.tag))
			return false;
		return true;
	}

}
