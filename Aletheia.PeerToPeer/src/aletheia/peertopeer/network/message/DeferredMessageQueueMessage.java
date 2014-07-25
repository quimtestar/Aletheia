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
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = DeferredMessageQueueMessage.SubProtocol.class)
public class DeferredMessageQueueMessage extends NonPersistentMessage
{
	private final UUID recipientUuid;
	private final int distance;

	public DeferredMessageQueueMessage(UUID recipientUuid, int distance)
	{
		super();
		this.recipientUuid = recipientUuid;
		this.distance = distance;
	}

	public UUID getRecipientUuid()
	{
		return recipientUuid;
	}

	public int getDistance()
	{
		return distance;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<DeferredMessageQueueMessage>
	{
		private final UUIDProtocol uuidProtocol;
		private final IntegerProtocol integerProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.uuidProtocol = new UUIDProtocol(0);
			this.integerProtocol = new IntegerProtocol(0);
		}

		@Override
		public void send(DataOutput out, DeferredMessageQueueMessage m) throws IOException
		{
			uuidProtocol.send(out, m.getRecipientUuid());
			integerProtocol.send(out, m.getDistance());
		}

		@Override
		public DeferredMessageQueueMessage recv(DataInput in) throws IOException, ProtocolException
		{
			UUID recipientUuid = uuidProtocol.recv(in);
			int distance = integerProtocol.recv(in);
			return new DeferredMessageQueueMessage(recipientUuid, distance);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
			integerProtocol.skip(in);
		}

	}

}
