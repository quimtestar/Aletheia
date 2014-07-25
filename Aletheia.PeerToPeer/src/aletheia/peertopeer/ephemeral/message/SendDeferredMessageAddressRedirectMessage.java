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
package aletheia.peertopeer.ephemeral.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.net.InetSocketAddressProtocol;

@MessageSubProtocolInfo(subProtocolClass = SendDeferredMessageAddressRedirectMessage.SubProtocol.class)
public class SendDeferredMessageAddressRedirectMessage extends SendDeferredMessageRedirectMessage
{
	private final InetSocketAddress address;

	public SendDeferredMessageAddressRedirectMessage(UUID uuid, InetSocketAddress address)
	{
		super(uuid);
		this.address = address;
	}

	public SendDeferredMessageAddressRedirectMessage(NodeAddress nodeAddress)
	{
		this(nodeAddress.getUuid(), nodeAddress.getAddress());
	}

	public InetSocketAddress getAddress()
	{
		return address;
	}

	public NodeAddress nodeAddress()
	{
		return new NodeAddress(getUuid(), getAddress());
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends SendDeferredMessageRedirectMessage.SubProtocol<SendDeferredMessageAddressRedirectMessage>
	{
		private final InetSocketAddressProtocol inetSocketAddressProtocol = new InetSocketAddressProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected SendDeferredMessageAddressRedirectMessage recv(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			InetSocketAddress address = inetSocketAddressProtocol.recv(in);
			return new SendDeferredMessageAddressRedirectMessage(uuid, address);
		}

		@Override
		public void send(DataOutput out, SendDeferredMessageAddressRedirectMessage m) throws IOException
		{
			super.send(out, m);
			inetSocketAddressProtocol.send(out, m.getAddress());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			inetSocketAddressProtocol.skip(in);
		}

	}

}
