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
package aletheia.protocol.net;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.StringProtocol;

@ProtocolInfo(availableVersions = 0)
public class InetAddressProtocol extends Protocol<InetAddress>
{
	private final StringProtocol stringProtocol = new StringProtocol(0);
	private final ByteArrayProtocol byteArrayProtocol = new ByteArrayProtocol(0);

	public InetAddressProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(InetAddressProtocol.class, requiredVersion);
	}

	@Override
	public void send(DataOutput out, InetAddress t) throws IOException
	{
		stringProtocol.send(out, t.getHostName());
		byteArrayProtocol.send(out, t.getAddress());
	}

	@Override
	public InetAddress recv(DataInput in) throws IOException, ProtocolException
	{
		String host = stringProtocol.recv(in);
		byte[] address = byteArrayProtocol.recv(in);
		return InetAddress.getByAddress(host, address);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		stringProtocol.skip(in);
		byteArrayProtocol.skip(in);
	}

}
