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

import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.BooleanProtocol;

@MessageSubProtocolInfo(subProtocolClass = ValidPeerNodeUuidMessage.SubProtocol.class)
public class ValidPeerNodeUuidMessage extends NonPersistentMessage
{
	private final boolean valid;

	public ValidPeerNodeUuidMessage(boolean valid)
	{
		this.valid = valid;
	}

	public boolean isValid()
	{
		return valid;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<ValidPeerNodeUuidMessage>
	{

		private final BooleanProtocol booleanProtocol = new BooleanProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, ValidPeerNodeUuidMessage m) throws IOException
		{
			booleanProtocol.send(out, m.isValid());
		}

		@Override
		public ValidPeerNodeUuidMessage recv(DataInput in) throws IOException
		{
			boolean valid = booleanProtocol.recv(in);
			return new ValidPeerNodeUuidMessage(valid);
		}

		@Override
		public void skip(DataInput in) throws IOException
		{
			booleanProtocol.skip(in);
		}

	}

}
