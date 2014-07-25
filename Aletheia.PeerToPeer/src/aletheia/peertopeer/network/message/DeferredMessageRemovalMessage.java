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
import java.util.Collection;
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = DeferredMessageRemovalMessage.SubProtocol.class)
public class DeferredMessageRemovalMessage extends NonPersistentMessage
{
	private final UUID recipientUuid;
	private final Collection<UUID> deferredMessageUuids;

	public DeferredMessageRemovalMessage(UUID recipientUuid, Collection<UUID> deferredMessageUuids)
	{
		super();
		this.recipientUuid = recipientUuid;
		this.deferredMessageUuids = deferredMessageUuids;
	}

	public UUID getRecipientUuid()
	{
		return recipientUuid;
	}

	public Collection<UUID> getDeferredMessageUuids()
	{
		return deferredMessageUuids;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<DeferredMessageRemovalMessage>
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
		private final CollectionProtocol<UUID> uuidCollectionProtocol = new CollectionProtocol<UUID>(0, uuidProtocol);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, DeferredMessageRemovalMessage m) throws IOException
		{
			uuidProtocol.send(out, m.getRecipientUuid());
			uuidCollectionProtocol.send(out, m.getDeferredMessageUuids());
		}

		@Override
		public DeferredMessageRemovalMessage recv(DataInput in) throws IOException, ProtocolException
		{
			UUID recipientUuid = uuidProtocol.recv(in);
			Collection<UUID> deferredMessageUuids = uuidCollectionProtocol.recv(in);
			return new DeferredMessageRemovalMessage(recipientUuid, deferredMessageUuids);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
			uuidCollectionProtocol.skip(in);
		}

	}

}
