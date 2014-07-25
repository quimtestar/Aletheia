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
package aletheia.peertopeer.ephemeral.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.PersistentMessage;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.BooleanProtocol;

@MessageSubProtocolInfo(subProtocolClass = SignatureRequestConfirmMessage.SubProtocol.class)
public class SignatureRequestConfirmMessage extends PersistentMessage
{
	private final boolean received;

	public SignatureRequestConfirmMessage(boolean received)
	{
		this.received = received;
	}

	public boolean isReceived()
	{
		return received;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends PersistentMessage.SubProtocol<SignatureRequestConfirmMessage>
	{
		private final BooleanProtocol booleanProtocol;

		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.booleanProtocol = new BooleanProtocol(0);
		}

		@Override
		public void send(DataOutput out, SignatureRequestConfirmMessage m) throws IOException
		{
			booleanProtocol.send(out, m.isReceived());
		}

		@Override
		public SignatureRequestConfirmMessage recv(DataInput in) throws IOException, ProtocolException
		{
			boolean received = booleanProtocol.recv(in);
			return new SignatureRequestConfirmMessage(received);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			booleanProtocol.skip(in);
		}
	}

}
