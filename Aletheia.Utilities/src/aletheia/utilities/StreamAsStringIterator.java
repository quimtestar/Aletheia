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
package aletheia.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import aletheia.utilities.collections.CloseableIterator;

/**
 * Iterate across the lines of an {@link InputStream} as {@link String}s.
 *
 * @author Quim Testar
 */
public class StreamAsStringIterator implements CloseableIterator<String>
{
	private final BufferedReader inputStreamReader;

	private String next;

	public StreamAsStringIterator(InputStream inputStream)
	{
		this.inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));
		this.next = advance();
	}

	private String advance()
	{
		try
		{
			String next = inputStreamReader.readLine();
			if (next == null)
				inputStreamReader.close();
			return next;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext()
	{
		return next != null;
	}

	@Override
	public String next()
	{
		String s = next;
		next = advance();
		return s;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void close()
	{
		try
		{
			inputStreamReader.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
