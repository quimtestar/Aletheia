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
import aletheia.peertopeer.network.CumulationSet;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

public abstract class CumulationMessage extends NonPersistentMessage
{
	private final CumulationSet.Cumulation<?> cumulation;

	public CumulationMessage(CumulationSet.Cumulation<?> cumulation)
	{
		this.cumulation = cumulation;
	}

	public CumulationSet.Cumulation<?> getCumulation()
	{
		return cumulation;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<M extends CumulationMessage> extends NonPersistentMessage.SubProtocol<M>
	{
		private final CumulationSet.Cumulation.Protocol cumulationProtocol = new CumulationSet.Cumulation.Protocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, M m) throws IOException
		{
			cumulationProtocol.send(out, m.getCumulation());
		}

		protected abstract M recv(CumulationSet.Cumulation<?> cumulation, DataInput in) throws IOException;

		@Override
		public M recv(DataInput in) throws IOException, ProtocolException
		{
			CumulationSet.Cumulation<?> cumulation = cumulationProtocol.recv(in);
			return recv(cumulation, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			cumulationProtocol.skip(in);
		}
	}

}
