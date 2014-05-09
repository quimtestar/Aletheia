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

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;

/**
 * {@link Protocol} for byte data. The methods {@link DataOutput#writeByte(int)}
 * and {@link DataInput#readByte()} are used.
 */
@ProtocolInfo(availableVersions = 0)
public class ByteProtocol extends Protocol<Byte>
{
	public ByteProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(ByteProtocol.class, requiredVersion);
	}

	@Override
	public void send(DataOutput out, Byte b) throws IOException
	{
		out.writeByte(b);
	}

	@Override
	public Byte recv(DataInput in) throws IOException
	{
		return in.readByte();
	}

	@Override
	public void skip(DataInput in) throws IOException
	{
		recv(in);
	}

}
