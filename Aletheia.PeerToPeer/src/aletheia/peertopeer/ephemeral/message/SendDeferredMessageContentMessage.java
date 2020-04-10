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

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.model.peertopeer.deferredmessagecontent.protocol.DeferredMessageContentProtocol;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = SendDeferredMessageContentMessage.SubProtocol.class)
public class SendDeferredMessageContentMessage extends NonPersistentMessage
{
	private final DeferredMessageContent content;

	public SendDeferredMessageContentMessage(DeferredMessageContent content)
	{
		this.content = content;
	}

	public DeferredMessageContent getContent()
	{
		return content;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<SendDeferredMessageContentMessage>
	{
		private final DeferredMessageContentProtocol deferredMessageContentProtocol = new DeferredMessageContentProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, SendDeferredMessageContentMessage m) throws IOException
		{
			deferredMessageContentProtocol.send(out, m.getContent());
		}

		@Override
		public SendDeferredMessageContentMessage recv(DataInput in) throws IOException, ProtocolException
		{
			DeferredMessageContent content = deferredMessageContentProtocol.recv(in);
			return new SendDeferredMessageContentMessage(content);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			deferredMessageContentProtocol.skip(in);
		}
	}

}
