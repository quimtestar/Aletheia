/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.util.ArrayList;
import java.util.List;

import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

public abstract class SubRootPhaseMessage extends NonPersistentMessage
{
	private final SubRootPhaseType subRootPhaseType;

	public SubRootPhaseMessage(SubRootPhaseType subRootPhaseType)
	{
		this.subRootPhaseType = subRootPhaseType;
	}

	public SubRootPhaseType getSubRootPhaseType()
	{
		return subRootPhaseType;
	}

	@ProtocolInfo(availableVersions = 0)
	public abstract static class SubProtocol<M extends SubRootPhaseMessage> extends NonPersistentMessage.SubProtocol<M>
	{
		private final SubRootPhaseType.Protocol subRootPhaseTypeProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.subRootPhaseTypeProtocol = new SubRootPhaseType.Protocol(0);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			subRootPhaseTypeProtocol.send(out, m.getSubRootPhaseType());
		}

		protected abstract M makeMessage(List<Object> args);

		protected void recvArgs(List<Object> args, DataInput in) throws IOException, ProtocolException
		{
			args.add(subRootPhaseTypeProtocol.recv(in));
		}

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			List<Object> args = new ArrayList<>();
			recvArgs(args, in);
			return makeMessage(args);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			subRootPhaseTypeProtocol.skip(in);
		}

	}

}
