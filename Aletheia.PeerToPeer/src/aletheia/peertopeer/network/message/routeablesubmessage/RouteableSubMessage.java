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

import aletheia.peertopeer.network.protocol.RouteableSubMessageSubProtocol;
import aletheia.protocol.Exportable;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

public abstract class RouteableSubMessage implements Exportable
{
	private final UUID origin;
	private final int sequence;

	public RouteableSubMessage(UUID origin, int sequence)
	{
		super();
		this.origin = origin;
		this.sequence = sequence;
	}

	public UUID getOrigin()
	{
		return origin;
	}

	public int getSequence()
	{
		return sequence;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends RouteableSubMessage> extends RouteableSubMessageSubProtocol<M>
	{
		private final UUIDProtocol uuidProtocol;
		private final IntegerProtocol integerProtocol;

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.uuidProtocol = new UUIDProtocol(0);
			this.integerProtocol = new IntegerProtocol(0);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			uuidProtocol.send(out, m.getOrigin());
			integerProtocol.send(out, m.getSequence());
		}

		protected abstract M recv(UUID origin, int sequence, DataInput in) throws IOException, ProtocolException;

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			UUID origin = uuidProtocol.recv(in);
			int sequence = integerProtocol.recv(in);
			return recv(origin, sequence, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			uuidProtocol.skip(in);
			integerProtocol.skip(in);
		}

	}

}
