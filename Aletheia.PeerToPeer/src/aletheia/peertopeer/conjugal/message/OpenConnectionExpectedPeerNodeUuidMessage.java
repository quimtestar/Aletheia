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
package aletheia.peertopeer.conjugal.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = OpenConnectionExpectedPeerNodeUuidMessage.SubProtocol.class)
public class OpenConnectionExpectedPeerNodeUuidMessage extends NonPersistentMessage
{
	private final UUID expectedPeerNodeUuid;

	public OpenConnectionExpectedPeerNodeUuidMessage(UUID expectedPeerNodeUuid)
	{
		this.expectedPeerNodeUuid = expectedPeerNodeUuid;
	}

	public UUID getExpectedPeerNodeUuid()
	{
		return expectedPeerNodeUuid;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<OpenConnectionExpectedPeerNodeUuidMessage>
	{
		private final NullableProtocol<UUID> uuidNullableProtocol = new NullableProtocol<>(0, new UUIDProtocol(0));

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(requiredVersion, messageCode);
		}

		@Override
		public void send(DataOutput out, OpenConnectionExpectedPeerNodeUuidMessage m) throws IOException
		{
			uuidNullableProtocol.send(out, m.getExpectedPeerNodeUuid());
		}

		@Override
		public OpenConnectionExpectedPeerNodeUuidMessage recv(DataInput in) throws IOException, ProtocolException
		{
			UUID expectedPeerNodeUuid = uuidNullableProtocol.recv(in);
			return new OpenConnectionExpectedPeerNodeUuidMessage(expectedPeerNodeUuid);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidNullableProtocol.skip(in);
		}

	}

}
