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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class TeePrintStream extends PrintStream
{
	private final PrintStream other;

	public TeePrintStream(File file, String csn, PrintStream other) throws FileNotFoundException, UnsupportedEncodingException
	{
		super(file, csn);
		this.other = other;
	}

	public TeePrintStream(File file, PrintStream other) throws FileNotFoundException
	{
		super(file);
		this.other = other;
	}

	public TeePrintStream(OutputStream out, boolean autoFlush, String encoding, PrintStream other) throws UnsupportedEncodingException
	{
		super(out, autoFlush, encoding);
		this.other = other;
	}

	public TeePrintStream(OutputStream out, boolean autoFlush, PrintStream other)
	{
		super(out, autoFlush);
		this.other = other;
	}

	public TeePrintStream(OutputStream out, PrintStream other)
	{
		super(out);
		this.other = other;
	}

	public TeePrintStream(String fileName, String csn, PrintStream other) throws FileNotFoundException, UnsupportedEncodingException
	{
		super(fileName, csn);
		this.other = other;
	}

	public TeePrintStream(String fileName, PrintStream other) throws FileNotFoundException
	{
		super(fileName);
		this.other = other;
	}

	@Override
	public boolean checkError()
	{
		return other.checkError() || super.checkError();
	}

	@Override
	public void write(int x)
	{
		other.write(x);
		super.write(x);
	}

	@Override
	public void write(byte[] x, int o, int l)
	{
		other.write(x, o, l);
		super.write(x, o, l);
	}

	@Override
	public void close()
	{
		other.close();
		super.close();
	}

	@Override
	public void flush()
	{
		other.flush();
		super.flush();
	}

}
