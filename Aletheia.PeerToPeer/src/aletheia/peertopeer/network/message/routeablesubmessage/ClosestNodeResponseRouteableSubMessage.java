/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
package aletheia.peertopeer.network.message.routeablesubmessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import aletheia.peertopeer.NodeAddress;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.net.InetSocketAddressProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@RouteableSubMessageSubProtocolInfo(subProtocolClass = ClosestNodeResponseRouteableSubMessage.SubProtocol.class)
public class ClosestNodeResponseRouteableSubMessage extends ResponseRouteableSubMessage
{
	private final UUID node;

	private InetSocketAddress address;

	public ClosestNodeResponseRouteableSubMessage(UUID origin, int sequence, UUID target, int sequenceResponse, UUID node)
	{
		super(origin, sequence, target, sequenceResponse);
		this.node = node;
	}

	public UUID getNode()
	{
		return node;
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	public void setAddress(InetSocketAddress address)
	{
		this.address = address;
	}

	public NodeAddress nodeAddress()
	{
		return new NodeAddress(getNode(), getAddress());
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends ResponseRouteableSubMessage.SubProtocol<ClosestNodeResponseRouteableSubMessage>
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		private final NullableProtocol<InetSocketAddress> nullableInetSocketAddressProtocol = new NullableProtocol<>(0, new InetSocketAddressProtocol(0));

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, ClosestNodeResponseRouteableSubMessage m) throws IOException
		{
			super.send(out, m);
			uuidProtocol.send(out, m.getNode());
			nullableInetSocketAddressProtocol.send(out, m.getAddress());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			uuidProtocol.skip(in);
			nullableInetSocketAddressProtocol.skip(in);
		}

		@Override
		protected ClosestNodeResponseRouteableSubMessage recv(UUID origin, int sequence, UUID target, int sequenceResponse, DataInput in)
				throws IOException, ProtocolException
		{
			UUID node = uuidProtocol.recv(in);
			InetSocketAddress address = nullableInetSocketAddressProtocol.recv(in);
			ClosestNodeResponseRouteableSubMessage m = new ClosestNodeResponseRouteableSubMessage(origin, sequence, target, sequenceResponse, node);
			m.setAddress(address);
			return m;
		}

	}

}
