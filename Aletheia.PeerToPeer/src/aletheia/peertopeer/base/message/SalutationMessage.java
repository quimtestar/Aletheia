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
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@MessageSubProtocolInfo(subProtocolClass = SalutationMessage.SubProtocol.class)
public class SalutationMessage extends NonPersistentMessage
{
	private final static String salutation = "\u1f08\u03bb\u03ae\u03b8\u03b5\u03b9\u03b1";
	private final int protocolVersion;
	private final PeerToPeerConnection.Gender gender;
	private final UUID nodeUuid;

	public SalutationMessage(int protocolVersion, PeerToPeerConnection.Gender gender, UUID nodeUuid)
	{
		this.protocolVersion = protocolVersion;
		this.gender = gender;
		this.nodeUuid = nodeUuid;
	}

	public int getProtocolVersion()
	{
		return protocolVersion;
	}

	public PeerToPeerConnection.Gender getGender()
	{
		return gender;
	}

	public UUID getNodeUuid()
	{
		return nodeUuid;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class AbstractSubProtocol<M extends SalutationMessage> extends NonPersistentMessage.SubProtocol<M>
	{
		private final StringProtocol stringProtocol;
		private final IntegerProtocol integerProtocol;
		private final PeerToPeerConnection.Gender.Protocol genderProtocol;
		private final UUIDProtocol uuidProtocol;

		public AbstractSubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.stringProtocol = new StringProtocol(0);
			this.integerProtocol = new IntegerProtocol(0);
			this.genderProtocol = new PeerToPeerConnection.Gender.Protocol(0);
			this.uuidProtocol = new UUIDProtocol(0);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			stringProtocol.send(out, salutation);
			integerProtocol.send(out, m.getProtocolVersion());
			genderProtocol.send(out, m.getGender());
			uuidProtocol.send(out, m.getNodeUuid());
		}

		protected abstract M recv(int protocolVersion, PeerToPeerConnection.Gender gender, UUID nodeUuid, DataInput in) throws IOException, ProtocolException;

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			String salutation = stringProtocol.recv(in);
			if (!SalutationMessage.salutation.equals(salutation))
				throw new ProtocolException();
			int protocolVersion = integerProtocol.recv(in);
			PeerToPeerConnection.Gender gender = genderProtocol.recv(in);
			UUID nodeUuid = uuidProtocol.recv(in);
			return recv(protocolVersion, gender, nodeUuid, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			stringProtocol.skip(in);
			integerProtocol.skip(in);
			genderProtocol.skip(in);
			uuidProtocol.skip(in);
		}

	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractSubProtocol<SalutationMessage>
	{

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		protected SalutationMessage recv(int protocolVersion, PeerToPeerConnection.Gender gender, UUID nodeUuid, DataInput in)
		{
			return new SalutationMessage(protocolVersion, gender, nodeUuid);
		}

	}

}
