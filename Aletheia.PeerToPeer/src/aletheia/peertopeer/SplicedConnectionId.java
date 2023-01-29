/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
package aletheia.peertopeer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;

import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.net.InetAddressProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;

public class SplicedConnectionId implements Exportable
{
	private final int connectionId;
	private final InetAddress remoteAddress;

	public SplicedConnectionId(int connectionId, InetAddress remoteAddress)
	{
		super();
		this.connectionId = connectionId;
		this.remoteAddress = remoteAddress;
	}

	public SplicedConnectionId(int connectionId)
	{
		this(connectionId, null);
	}

	public int getConnectionId()
	{
		return connectionId;
	}

	public InetAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + connectionId;
		result = prime * result + ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		SplicedConnectionId other = (SplicedConnectionId) obj;
		if (connectionId != other.connectionId)
			return false;
		if (remoteAddress == null)
		{
			if (other.remoteAddress != null)
				return false;
		}
		else if (!remoteAddress.equals(other.remoteAddress))
			return false;
		return true;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ExportableProtocol<SplicedConnectionId>
	{
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);
		private final NullableProtocol<InetAddress> nullableInetAddressProtocol = new NullableProtocol<>(0, new InetAddressProtocol(0));

		public Protocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, SplicedConnectionId t) throws IOException
		{
			integerProtocol.send(out, t.getConnectionId());
			nullableInetAddressProtocol.send(out, t.getRemoteAddress());
		}

		@Override
		public SplicedConnectionId recv(DataInput in) throws IOException, ProtocolException
		{
			int connectionId = integerProtocol.recv(in);
			InetAddress remoteAddress = nullableInetAddressProtocol.recv(in);
			return new SplicedConnectionId(connectionId, remoteAddress);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			integerProtocol.skip(in);
			nullableInetAddressProtocol.skip(in);
		}

	}
}
