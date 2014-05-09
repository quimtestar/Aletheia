/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 * 
 * This file is part of the Aletheia Proof Assistant.
 * 
 * The Aletheia Proof Assistant is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * The Aletheia Proof Assistant is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.protocol.primitive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.protocol.AllocateProtocolException;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class ByteArrayProtocol extends Protocol<byte[]>
{
	private final IntegerProtocol integerProtocol;

	public ByteArrayProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(ByteArrayProtocol.class, requiredVersion);
		this.integerProtocol = new IntegerProtocol(0);
	}

	@Override
	public void send(DataOutput out, byte[] a) throws IOException
	{
		send(out, a, 0, a.length);
	}

	public void send(DataOutput out, byte[] a, int off, int len) throws IOException
	{
		integerProtocol.send(out, len);
		out.write(a, off, len);
	}

	@Override
	public byte[] recv(DataInput in) throws IOException, ProtocolException
	{
		int n = integerProtocol.recv(in);
		if (n < 0)
			throw new ProtocolException();
		try
		{
			byte[] a = new byte[n];
			in.readFully(a);
			return a;
		}
		catch (OutOfMemoryError e)
		{
			throw new AllocateProtocolException(n, e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		skipN(in);
	}

	public int skipN(DataInput in) throws IOException, ProtocolException
	{
		int n = integerProtocol.recv(in);
		if (n < 0)
			throw new ProtocolException();
		in.skipBytes(n);
		return n;
	}

}
