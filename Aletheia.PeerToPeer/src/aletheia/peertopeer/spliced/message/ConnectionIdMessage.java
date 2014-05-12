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
package aletheia.peertopeer.spliced.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;

@MessageSubProtocolInfo(subProtocolClass = ConnectionIdMessage.SubProtocol.class)
public class ConnectionIdMessage extends NonPersistentMessage
{
	private final int connectionId;

	public ConnectionIdMessage(int connectionId)
	{
		this.connectionId = connectionId;
	}

	public int getConnectionId()
	{
		return connectionId;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<ConnectionIdMessage>
	{
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(requiredVersion, messageCode);
		}

		@Override
		public void send(DataOutput out, ConnectionIdMessage m) throws IOException
		{
			integerProtocol.send(out, m.getConnectionId());
		}

		@Override
		public ConnectionIdMessage recv(DataInput in) throws IOException, ProtocolException
		{
			int connectionId = integerProtocol.recv(in);
			return new ConnectionIdMessage(connectionId);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			integerProtocol.skip(in);
		}

	}

}
