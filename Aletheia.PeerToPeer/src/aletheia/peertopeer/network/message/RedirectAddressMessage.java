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
package aletheia.peertopeer.network.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = RedirectAddressMessage.SubProtocol.class)
public class RedirectAddressMessage extends NonPersistentMessage
{
	private final NodeAddress nodeAddress;

	public RedirectAddressMessage(NodeAddress nodeAddress)
	{
		this.nodeAddress = nodeAddress;
	}

	public NodeAddress getNodeAddress()
	{
		return nodeAddress;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<RedirectAddressMessage>
	{
		private final NodeAddress.Protocol nodeAddressProtocol = new NodeAddress.Protocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, RedirectAddressMessage m) throws IOException
		{
			nodeAddressProtocol.send(out, m.getNodeAddress());
		}

		@Override
		public RedirectAddressMessage recv(DataInput in) throws IOException, ProtocolException
		{
			NodeAddress nodeAddress = nodeAddressProtocol.recv(in);
			return new RedirectAddressMessage(nodeAddress);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			nodeAddressProtocol.skip(in);
		}
	}

}
