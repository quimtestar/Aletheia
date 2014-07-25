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
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.peertopeer.network.CumulationSet.Cumulation;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

public abstract class CumulationValueMessage extends NonPersistentMessage
{
	private final Cumulation.Value<?> cumulationValue;

	public CumulationValueMessage(Cumulation.Value<?> cumulationValue)
	{
		this.cumulationValue = cumulationValue;
	}

	public Cumulation.Value<?> getCumulationValue()
	{
		return cumulationValue;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends CumulationValueMessage> extends NonPersistentMessage.SubProtocol<M>
	{
		private final Cumulation.Value.Protocol cumulationValueProtocol = new Cumulation.Value.Protocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			cumulationValueProtocol.send(out, m.getCumulationValue());
		}

		protected abstract M recv(Cumulation.Value<?> cumulationValue, DataInput in) throws IOException;

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			Cumulation.Value<?> cumulationValue = cumulationValueProtocol.recv(in);
			return recv(cumulationValue, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			cumulationValueProtocol.skip(in);
		}
	}

}
