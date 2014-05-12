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
import java.util.EnumSet;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.EnumSetProtocol;

@MessageSubProtocolInfo(subProtocolClass = BeltRequestMessage.SubProtocol.class)
public class BeltRequestMessage extends NonPersistentMessage
{
	private final EnumSet<Side> sides;

	public BeltRequestMessage(EnumSet<Side> sides)
	{
		this.sides = sides;
	}

	public EnumSet<Side> getSides()
	{
		return sides;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<BeltRequestMessage>
	{
		private final EnumSetProtocol<Side> sideSetProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.sideSetProtocol = new EnumSetProtocol<>(0, new Side.Protocol(0), Side.class);
		}

		@Override
		public void send(DataOutput out, BeltRequestMessage m) throws IOException
		{
			sideSetProtocol.send(out, m.getSides());
		}

		@Override
		public BeltRequestMessage recv(DataInput in) throws IOException, ProtocolException
		{
			EnumSet<Side> sides = sideSetProtocol.recv(in);
			return new BeltRequestMessage(sides);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			sideSetProtocol.skip(in);
		}
	}

}
