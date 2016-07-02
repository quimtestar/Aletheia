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
package aletheia.peertopeer.base.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerConnection.Gender;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = MaleSalutationMessage.SubProtocol.class)
public class MaleSalutationMessage extends SalutationMessage
{
	private final UUID expectedPeerNodeUuid;

	public MaleSalutationMessage(int protocolVersion, Gender gender, UUID nodeUuid, UUID expectedPeerNodeUuid)
	{
		super(protocolVersion, gender, nodeUuid);
		this.expectedPeerNodeUuid = expectedPeerNodeUuid;
	}

	public UUID getExpectedPeerNodeUuid()
	{
		return expectedPeerNodeUuid;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractSubProtocol<MaleSalutationMessage>
	{
		private final NullableProtocol<UUID> nullableUuidProtocol = new NullableProtocol<>(0, new UUIDProtocol(0));

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected MaleSalutationMessage recv(int protocolVersion, PeerToPeerConnection.Gender gender, UUID nodeUuid, DataInput in)
				throws IOException, ProtocolException
		{
			UUID expectedPeerNodeUuid = nullableUuidProtocol.recv(in);
			return new MaleSalutationMessage(protocolVersion, gender, nodeUuid, expectedPeerNodeUuid);
		}

		@Override
		public void send(DataOutput out, MaleSalutationMessage m) throws IOException
		{
			super.send(out, m);
			nullableUuidProtocol.send(out, m.getExpectedPeerNodeUuid());
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			nullableUuidProtocol.skip(in);
		}

	}

}
