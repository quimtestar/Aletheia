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
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = SendDeferredMessageUuidMessage.SubProtocol.class)
public class SendDeferredMessageUuidMessage extends NonPersistentMessage
{
	private final UUID recipientUuid;

	public SendDeferredMessageUuidMessage(UUID recipientUuid)
	{
		this.recipientUuid = recipientUuid;
	}

	public UUID getRecipientUuid()
	{
		return recipientUuid;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<SendDeferredMessageUuidMessage>
	{
		private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, SendDeferredMessageUuidMessage m) throws IOException
		{
			uuidProtocol.send(out, m.getRecipientUuid());
		}

		@Override
		public SendDeferredMessageUuidMessage recv(DataInput in) throws IOException, ProtocolException
		{
			UUID recipientUuid = uuidProtocol.recv(in);
			return new SendDeferredMessageUuidMessage(recipientUuid);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
		}
	}

}
