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
package aletheia.peertopeer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.net.InetSocketAddressProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

public class NodeAddress implements Exportable
{
	private final UUID uuid;
	private final InetSocketAddress address;

	public NodeAddress(UUID uuid, InetSocketAddress address)
	{
		super();
		this.uuid = uuid;
		this.address = address;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	@Override
	public String toString()
	{
		return uuid + ":" + address;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeAddress other = (NodeAddress) obj;
		if (address == null)
		{
			if (other.address != null)
				return false;
		}
		else if (!address.equals(other.address))
			return false;
		if (uuid == null)
		{
			if (other.uuid != null)
				return false;
		}
		else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class Protocol extends ExportableProtocol<NodeAddress>
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		private final NullableProtocol<InetSocketAddress> nullableInetSocketAddressProtocol = new NullableProtocol<>(0, new InetSocketAddressProtocol(0));

		public Protocol(int requiredVersion)
		{
			super(0);
			checkVersionAvailability(Protocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, NodeAddress t) throws IOException
		{
			uuidProtocol.send(out, t.getUuid());
			nullableInetSocketAddressProtocol.send(out, t.getAddress());
		}

		@Override
		public NodeAddress recv(DataInput in) throws IOException, ProtocolException
		{
			UUID uuid = uuidProtocol.recv(in);
			InetSocketAddress address = nullableInetSocketAddressProtocol.recv(in);
			return new NodeAddress(uuid, address);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
			nullableInetSocketAddressProtocol.skip(in);
		}
	}
}
