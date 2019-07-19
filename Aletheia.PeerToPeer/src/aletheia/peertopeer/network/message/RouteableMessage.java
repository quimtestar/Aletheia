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
package aletheia.peertopeer.network.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.network.protocol.RouteableSubMessageProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;

@MessageSubProtocolInfo(subProtocolClass = RouteableMessage.SubProtocol.class)
public class RouteableMessage extends NonPersistentMessage
{
	private final Collection<RouteableSubMessage> routeableSubMessages;

	public RouteableMessage(Collection<RouteableSubMessage> routeableSubMessages)
	{
		this.routeableSubMessages = routeableSubMessages;
	}

	public Collection<RouteableSubMessage> getRouteableSubMessages()
	{
		return routeableSubMessages;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<RouteableMessage>
	{
		private final RouteableSubMessageProtocol routeableSubMessageProtocol;
		private final CollectionProtocol<RouteableSubMessage> routeableSubMessageListProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.routeableSubMessageProtocol = new RouteableSubMessageProtocol(0);
			this.routeableSubMessageListProtocol = new CollectionProtocol<>(0, routeableSubMessageProtocol);
		}

		@Override
		public void send(DataOutput out, RouteableMessage routeableMessage) throws IOException
		{
			routeableSubMessageListProtocol.send(out, routeableMessage.getRouteableSubMessages());
		}

		@Override
		public RouteableMessage recv(DataInput in) throws IOException, ProtocolException
		{
			Collection<RouteableSubMessage> routeableSubMessages = routeableSubMessageListProtocol.recv(in);
			return new RouteableMessage(routeableSubMessages);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			routeableSubMessageListProtocol.skip(in);
		}
	}

}
