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
import aletheia.peertopeer.network.CumulationSet.Cumulation;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = NeighbourCumulationValueMessage.SubProtocol.class)
public class NeighbourCumulationValueMessage extends CumulationValueMessage
{

	public NeighbourCumulationValueMessage(Cumulation.Value<?> cumulationValue)
	{
		super(cumulationValue);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends CumulationValueMessage.SubProtocol<NeighbourCumulationValueMessage>
	{

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, NeighbourCumulationValueMessage m) throws IOException
		{
			super.send(out, m);
		}

		@Override
		protected NeighbourCumulationValueMessage recv(Cumulation.Value<?> cumulationValue, DataInput in)
		{
			return new NeighbourCumulationValueMessage(cumulationValue);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			super.skip(in);
		}
	}

}
