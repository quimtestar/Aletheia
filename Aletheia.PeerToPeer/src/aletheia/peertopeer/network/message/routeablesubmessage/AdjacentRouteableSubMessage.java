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

import aletheia.peertopeer.network.message.Side;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.UUIDProtocol;

public abstract class AdjacentRouteableSubMessage extends RouteableSubMessage
{
	private final UUID target;
	private final Side side;

	public AdjacentRouteableSubMessage(UUID origin, int sequence, UUID target, Side side)
	{
		super(origin, sequence);
		this.target = target;
		this.side = side;
	}

	public UUID getTarget()
	{
		return target;
	}

	public Side getSide()
	{
		return side;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends AdjacentRouteableSubMessage> extends RouteableSubMessage.SubProtocol<M>
	{
		private final UUIDProtocol uuidProtocol;
		private final Side.Protocol sideProtocol;

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.uuidProtocol = new UUIDProtocol(0);
			this.sideProtocol = new Side.Protocol(0);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			super.send(out, m);
			uuidProtocol.send(out, m.getTarget());
			sideProtocol.send(out, m.getSide());
		}

		protected abstract M recv(UUID origin, int sequence, UUID target, Side side, DataInput in) throws IOException, ProtocolException;

		@Override
		protected M recv(UUID origin, int sequence, DataInput in) throws IOException, ProtocolException
		{
			UUID target = uuidProtocol.recv(in);
			Side side = sideProtocol.recv(in);
			return recv(origin, sequence, target, side, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			uuidProtocol.skip(in);
			sideProtocol.skip(in);
		}

	}

}
