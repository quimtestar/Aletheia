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

@RouteableSubMessageSubProtocolInfo(subProtocolClass = ComplementingInvitationRouteableSubMessage.SubProtocol.class)
public class ComplementingInvitationRouteableSubMessage extends BitSlotRouteableSubMessage
{
	private InetSocketAddress address;

	public ComplementingInvitationRouteableSubMessage(UUID origin, int sequence, int slot, boolean bit)
	{
		super(origin, sequence, slot, bit);
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
		return new NodeAddress(getOrigin(), getAddress());
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends BitSlotRouteableSubMessage.SubProtocol<ComplementingInvitationRouteableSubMessage>
	{
		private final NullableProtocol<InetSocketAddress> nullableInetSocketAddress = new NullableProtocol<>(0, new InetSocketAddressProtocol(0));

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, ComplementingInvitationRouteableSubMessage m) throws IOException
		{
			super.send(out, m);
			nullableInetSocketAddress.send(out, m.getAddress());
		}

		@Override
		protected ComplementingInvitationRouteableSubMessage recv(UUID origin, int sequence, int slot, boolean bit, DataInput in) throws IOException,
		ProtocolException
		{
			ComplementingInvitationRouteableSubMessage m = new ComplementingInvitationRouteableSubMessage(origin, sequence, slot, bit);
			InetSocketAddress address = nullableInetSocketAddress.recv(in);
			m.setAddress(address);
			return m;
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			nullableInetSocketAddress.skip(in);
		}

	}

}
