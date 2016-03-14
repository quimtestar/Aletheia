/*******************************************************************************
 * Copyright (c) 2016 Quim Testar.
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
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class WriterOutputStream extends OutputStream
{
	private final Writer writer;
	private final Charset charset;

	public WriterOutputStream(Writer writer, Charset charset)
	{
		this.writer = writer;
		this.charset = charset;
	}

	public WriterOutputStream(Writer writer)
	{
		this(writer, Charset.defaultCharset());
	}

	public Writer getWriter()
	{
		return writer;
	}

	@Override
	public void write(int b) throws IOException
	{
		write(new byte[]
		{ (byte) b });
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		CharBuffer buf = charset.decode(ByteBuffer.wrap(b, off, len));
		writer.write(buf.array());
	}

	@Override
	public void flush() throws IOException
	{
		writer.flush();
	}

	@Override
	public void close() throws IOException
	{
		writer.close();
	}

}
