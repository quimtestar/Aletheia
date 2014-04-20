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
import java.io.OutputStream;

public abstract class SegmentedOutputStream extends OutputStream
{
	private final int segmentSize;

	private final byte[] buf;
	private int pos;

	public SegmentedOutputStream(int segmentSize)
	{
		this.segmentSize = segmentSize;
		this.buf = new byte[segmentSize];
		this.pos = 0;
	}

	protected abstract void segment(byte[] b, int off, int len) throws IOException;

	@Override
	public void write(int b) throws IOException
	{
		buf[pos] = (byte) b;
		pos++;
		if (pos >= segmentSize)
		{
			segment(buf, 0, pos);
			pos = 0;
		}
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if (pos + len > segmentSize)
		{
			if (pos > 0)
			{
				segment(buf, 0, pos);
				pos = 0;
			}
		}
		if (pos + len <= segmentSize)
		{
			System.arraycopy(b, off, buf, pos, len);
			pos += len;
			if (pos >= segmentSize)
			{
				segment(buf, 0, pos);
				pos = 0;
			}
		}
		else
			segment(b, off, len);
	}

	@Override
	public void close() throws IOException
	{
		if (pos > 0)
			segment(buf, 0, pos);
		super.close();
	}

}
