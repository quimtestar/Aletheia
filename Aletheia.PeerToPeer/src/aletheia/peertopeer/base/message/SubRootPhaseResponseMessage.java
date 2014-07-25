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
import java.util.List;

import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.BooleanProtocol;

@MessageSubProtocolInfo(subProtocolClass = SubRootPhaseResponseMessage.SubProtocol.class)
public class SubRootPhaseResponseMessage extends SubRootPhaseMessage
{
	private final boolean acknowledged;

	public SubRootPhaseResponseMessage(SubRootPhaseType subRootPhaseType, boolean acknowledge)
	{
		super(subRootPhaseType);
		this.acknowledged = acknowledge;
	}

	public boolean isAcknowledged()
	{
		return acknowledged;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends SubRootPhaseMessage.SubProtocol<SubRootPhaseResponseMessage>
	{
		private final BooleanProtocol booleanProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.booleanProtocol = new BooleanProtocol(0);
		}

		@Override
		public void send(DataOutput out, SubRootPhaseResponseMessage m) throws IOException
		{
			super.send(out, m);
			booleanProtocol.send(out, m.isAcknowledged());
		}

		@Override
		protected void recvArgs(List<Object> args, DataInput in) throws IOException, ProtocolException
		{
			super.recvArgs(args, in);
			args.add(booleanProtocol.recv(in));
		}

		@Override
		protected SubRootPhaseResponseMessage makeMessage(List<Object> args)
		{
			return new SubRootPhaseResponseMessage((SubRootPhaseType) args.get(0), (boolean) args.get(1));
		}
	}

}
