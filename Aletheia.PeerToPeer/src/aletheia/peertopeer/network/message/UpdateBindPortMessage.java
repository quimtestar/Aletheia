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

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;

@MessageSubProtocolInfo(subProtocolClass = UpdateBindPortMessage.SubProtocol.class)
public class UpdateBindPortMessage extends NonPersistentMessage
{
	private final int bindPort;

	public UpdateBindPortMessage(int bindPort)
	{
		this.bindPort = bindPort;
	}

	public int getBindPort()
	{
		return bindPort;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<UpdateBindPortMessage>
	{
		private final IntegerProtocol integerProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.integerProtocol = new IntegerProtocol(0);
		}

		@Override
		public void send(DataOutput out, UpdateBindPortMessage t) throws IOException
		{
			integerProtocol.send(out, t.getBindPort());
		}

		@Override
		public UpdateBindPortMessage recv(DataInput in) throws IOException, ProtocolException
		{
			int port = integerProtocol.recv(in);
			return new UpdateBindPortMessage(port);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			integerProtocol.skip(in);
		}
	}

}
