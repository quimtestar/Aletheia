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
import aletheia.peertopeer.network.CumulationSet;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;

@MessageSubProtocolInfo(subProtocolClass = RouterCumulationMessage.SubProtocol.class)
public class RouterCumulationMessage extends CumulationMessage
{
	private final int index;

	public RouterCumulationMessage(int index, CumulationSet.Cumulation<?> cumulation)
	{
		super(cumulation);
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends CumulationMessage.SubProtocol<RouterCumulationMessage>
	{
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, RouterCumulationMessage m) throws IOException
		{
			super.send(out, m);
			integerProtocol.send(out, m.getIndex());
		}

		@Override
		protected RouterCumulationMessage recv(CumulationSet.Cumulation<?> cumulation, DataInput in) throws IOException
		{
			int index = integerProtocol.recv(in);
			return new RouterCumulationMessage(index, cumulation);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
			integerProtocol.skip(in);
		}
	}

}
