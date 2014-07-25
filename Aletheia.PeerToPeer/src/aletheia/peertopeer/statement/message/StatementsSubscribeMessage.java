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
package aletheia.peertopeer.statement.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.ListProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = StatementsSubscribeMessage.SubProtocol.class)
public class StatementsSubscribeMessage extends NonPersistentMessage
{
	private final List<UUID> subscribeUuids;
	private final List<UUID> unsubscribeUuids;

	public StatementsSubscribeMessage(Collection<UUID> subscribedUuids, Collection<UUID> unsubscribedUuids)
	{
		super();
		this.subscribeUuids = new ArrayList<UUID>(subscribedUuids);
		this.unsubscribeUuids = new ArrayList<UUID>(unsubscribedUuids);
	}

	public List<UUID> getSubscribedUuids()
	{
		return Collections.unmodifiableList(subscribeUuids);
	}

	public List<UUID> getUnsubscribedUuids()
	{
		return Collections.unmodifiableList(unsubscribeUuids);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<StatementsSubscribeMessage>
	{
		private final UUIDProtocol uuidProtocol;
		private final ListProtocol<UUID> uuidListProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.uuidProtocol = new UUIDProtocol(0);
			this.uuidListProtocol = new ListProtocol<UUID>(0, uuidProtocol);
		}

		@Override
		public void send(DataOutput out, StatementsSubscribeMessage m) throws IOException
		{
			uuidListProtocol.send(out, m.getSubscribedUuids());
			uuidListProtocol.send(out, m.getUnsubscribedUuids());
		}

		@Override
		public StatementsSubscribeMessage recv(DataInput in) throws IOException, ProtocolException
		{
			List<UUID> subscribeUuids = uuidListProtocol.recv(in);
			List<UUID> unsubscribeUuids = uuidListProtocol.recv(in);
			return new StatementsSubscribeMessage(subscribeUuids, unsubscribeUuids);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidListProtocol.skip(in);
			uuidListProtocol.skip(in);
		}

	}

}
