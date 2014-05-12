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
package aletheia.peertopeer.network.message.routeablesubmessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.BooleanProtocol;

public abstract class BitSlotRouteableSubMessage extends SlotRouteableSubMessage
{
	private final boolean bit;

	public BitSlotRouteableSubMessage(UUID origin, int sequence, int slot, boolean bit)
	{
		super(origin, sequence, slot);
		this.bit = bit;
	}

	public boolean getBit()
	{
		return bit;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends BitSlotRouteableSubMessage> extends SlotRouteableSubMessage.SubProtocol<M>
	{
		private final BooleanProtocol booleanProtocol = new BooleanProtocol(0);

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			super.send(out, m);
			booleanProtocol.send(out, m.getBit());
		}

		protected abstract M recv(UUID origin, int sequence, int slot, boolean bit, DataInput in) throws IOException, ProtocolException;

		@Override
		protected M recv(UUID origin, int sequence, int slot, DataInput in) throws IOException, ProtocolException
		{
			boolean bit = booleanProtocol.recv(in);
			return recv(origin, sequence, slot, bit, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			booleanProtocol.skip(in);
		}

	}

}
