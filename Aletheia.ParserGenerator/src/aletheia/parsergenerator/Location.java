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
package aletheia.parsergenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A location in the input. Line and column.
 */
public class Location
{
	private final static Pattern lineBreakPattern = Pattern.compile("\\r\\n|\\n|\\r");

	public final int line;
	public final int column;

	public Location(int line, int column)
	{
		super();
		this.line = line;
		this.column = column;
	}

	public String position()
	{
		return "Line:" + line + " Col:" + column;
	}

	@Override
	public String toString()
	{
		return position();
	}

	public int positionInText(String text)
	{
		if (line <= 1)
			return column;
		else
		{
			int l = line;
			Matcher matcher = lineBreakPattern.matcher(text);
			while (l > 1 && matcher.find())
				l--;
			return matcher.start() + column;
		}
	}

	public static final Location initial = new Location(1, 0);

}
