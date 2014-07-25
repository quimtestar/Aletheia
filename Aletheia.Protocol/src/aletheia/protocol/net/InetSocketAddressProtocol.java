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
import java.net.InetSocketAddress;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;

@ProtocolInfo(availableVersions = 0)
public class InetSocketAddressProtocol extends SocketAddressProtocol<InetSocketAddress>
{
	private final InetAddressProtocol inetAddressProtocol = new InetAddressProtocol(0);
	private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

	public InetSocketAddressProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(InetSocketAddressProtocol.class, requiredVersion);
	}

	@Override
	public void send(DataOutput out, InetSocketAddress t) throws IOException
	{
		inetAddressProtocol.send(out, t.getAddress());
		integerProtocol.send(out, t.getPort());
	}

	@Override
	public InetSocketAddress recv(DataInput in) throws IOException, ProtocolException
	{
		InetAddress inetAddress = inetAddressProtocol.recv(in);
		int port = integerProtocol.recv(in);
		return new InetSocketAddress(inetAddress, port);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		inetAddressProtocol.skip(in);
		integerProtocol.skip(in);
	}

}
