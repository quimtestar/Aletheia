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
package aletheia.utilities.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} that will obtain its data by calling the method
 * {@link #segment()}, which must be implemented by a subclass.
 *
 * @author Quim Testar
 */
public abstract class SegmentedInputStream extends InputStream
{
	private byte[] segment;
	private int pos;

	public SegmentedInputStream()
	{
		this.segment = null;
		this.pos = 0;
	}

	/**
	 * Provide a byte array of data to be read. This method will be called when the
	 * {@link SegmentedInputStream} gets out of data to provide to the
	 * {@link #read()} methods.
	 */
	public abstract byte[] segment() throws IOException;

	private byte[] segmentWithData() throws IOException
	{
		while (true)
		{
			byte[] segment = segment();
			if (segment == null || segment.length > 0)
				return segment;
		}
	}

	@Override
	public int read() throws IOException
	{
		if (segment == null)
			segment = segmentWithData();
		if (segment == null)
			return -1;
		byte data = segment[pos];
		pos++;
		if (pos >= segment.length)
		{
			segment = null;
			pos = 0;
		}
		return 0xff & data;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (len < 0)
			throw new IllegalArgumentException();
		if (len == 0)
			return 0;
		if (segment == null)
			segment = segmentWithData();
		if (segment == null)
			return -1;
		int n = 0;
		while (segment != null && len >= segment.length - pos)
		{
			System.arraycopy(segment, pos, b, off + n, segment.length - pos);
			n += segment.length - pos;
			len -= segment.length - pos;
			if (len > 0)
			{
				segment = segmentWithData();
				if (segment == null || segment.length == 0)
					return n;
			}
			else
				segment = null;
			pos = 0;
		}
		if (segment != null && len > 0)
		{
			System.arraycopy(segment, pos, b, off + n, len);
			n += len;
			pos += len;
		}
		return n;
	}

}
