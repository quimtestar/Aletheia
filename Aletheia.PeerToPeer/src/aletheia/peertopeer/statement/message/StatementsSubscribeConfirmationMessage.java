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
package aletheia.peertopeer.statement.message;

import java.io.DataInput;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import aletheia.peertopeer.base.message.AbstractUUIDMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@MessageSubProtocolInfo(subProtocolClass = StatementsSubscribeConfirmationMessage.SubProtocol.class)
public class StatementsSubscribeConfirmationMessage extends AbstractUUIDMessage
{

	public StatementsSubscribeConfirmationMessage(Collection<UUID> uuids)
	{
		super(uuids);
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDMessage.SubProtocol<StatementsSubscribeConfirmationMessage>
	{

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public StatementsSubscribeConfirmationMessage recv(DataInput in) throws IOException, ProtocolException
		{
			Collection<UUID> uuids = recvUuids(in);
			return new StatementsSubscribeConfirmationMessage(uuids);
		}

	}

}
