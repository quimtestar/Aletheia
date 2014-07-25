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
import aletheia.protocol.primitive.IntegerProtocol;

public abstract class ResponseRouteableSubMessage extends TargetRouteableSubMessage
{
	private final int sequenceResponse;

	public ResponseRouteableSubMessage(UUID origin, int sequence, UUID target, int sequenceResponse)
	{
		super(origin, sequence, target);
		this.sequenceResponse = sequenceResponse;
	}

	public int getSequenceResponse()
	{
		return sequenceResponse;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends ResponseRouteableSubMessage> extends TargetRouteableSubMessage.SubProtocol<M>
	{
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

		public SubProtocol(int requiredVersion, RouteableSubMessageCode routeableSubMessageCode)
		{
			super(0, routeableSubMessageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			super.send(out, m);
			integerProtocol.send(out, m.getSequenceResponse());
		}

		protected abstract M recv(UUID origin, int sequence, UUID target, int sequenceResponse, DataInput in) throws IOException, ProtocolException;

		@Override
		protected M recv(UUID origin, int sequence, UUID target, DataInput in) throws IOException, ProtocolException
		{
			int sequenceResponse = integerProtocol.recv(in);
			return recv(origin, sequence, target, sequenceResponse, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			integerProtocol.skip(in);
		}

	}

}
