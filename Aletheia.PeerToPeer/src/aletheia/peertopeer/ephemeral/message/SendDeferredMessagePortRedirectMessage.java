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
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;

@MessageSubProtocolInfo(subProtocolClass = SendDeferredMessagePortRedirectMessage.SubProtocol.class)
public class SendDeferredMessagePortRedirectMessage extends SendDeferredMessageRedirectMessage
{
	private final int port;

	public SendDeferredMessagePortRedirectMessage(UUID uuid, int port)
	{
		super(uuid);
		this.port = port;
	}

	public int getPort()
	{
		return port;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends SendDeferredMessageRedirectMessage.SubProtocol<SendDeferredMessagePortRedirectMessage>
	{
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected SendDeferredMessagePortRedirectMessage recv(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			int port = integerProtocol.recv(in);
			return new SendDeferredMessagePortRedirectMessage(uuid, port);
		}

		@Override
		public void send(DataOutput out, SendDeferredMessagePortRedirectMessage m) throws IOException
		{
			super.send(out, m);
			integerProtocol.send(out, m.getPort());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			integerProtocol.skip(in);
		}

	}

}
