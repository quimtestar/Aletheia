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
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.peertopeer.network.RouterSet;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.SetProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = RouterSetMessage.SubProtocol.class)
public class RouterSetMessage extends NonPersistentMessage
{
	private final RouterSet routerSet;
	private final Set<UUID> clearing;

	public RouterSetMessage(RouterSet routerSet, Set<UUID> clearing)
	{
		this.routerSet = routerSet;
		this.clearing = clearing;
	}

	public RouterSet getRouterSet()
	{
		return routerSet;
	}

	public Set<UUID> getClearing()
	{
		return Collections.unmodifiableSet(clearing);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<RouterSetMessage>
	{
		private final RouterSet.Protocol routerSetProtocol;
		private final SetProtocol<UUID> uuidSetProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.routerSetProtocol = new RouterSet.Protocol(0);
			this.uuidSetProtocol = new SetProtocol<>(0, new UUIDProtocol(0));
		}

		@Override
		public void send(DataOutput out, RouterSetMessage routerSetMessage) throws IOException
		{
			routerSetProtocol.send(out, routerSetMessage.getRouterSet());
			uuidSetProtocol.send(out, routerSetMessage.getClearing());
		}

		@Override
		public RouterSetMessage recv(DataInput in) throws IOException, ProtocolException
		{
			RouterSet routerSet = routerSetProtocol.recv(in);
			Set<UUID> clearing = uuidSetProtocol.recv(in);
			return new RouterSetMessage(routerSet, clearing);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			routerSetProtocol.skip(in);
			uuidSetProtocol.skip(in);
		}
	}

}
