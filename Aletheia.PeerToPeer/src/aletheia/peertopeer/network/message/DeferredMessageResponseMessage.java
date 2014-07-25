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
import java.util.Collection;
import java.util.UUID;

import aletheia.model.peertopeer.DeferredMessage;
import aletheia.peertopeer.base.message.AbstractUUIDPersistentInfoMessage;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.peertopeer.DeferredMessageProtocol;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

@MessageSubProtocolInfo(subProtocolClass = DeferredMessageResponseMessage.SubProtocol.class)
public class DeferredMessageResponseMessage extends AbstractUUIDPersistentInfoMessage<DeferredMessage>
{

	private DeferredMessageResponseMessage(Collection<? extends AbstractUUIDPersistentInfoMessage.Entry<DeferredMessage>> entries)
	{
		super(entries);
	}

	public static class Entry extends AbstractUUIDPersistentInfoMessage.Entry<DeferredMessage>
	{
		public Entry(DeferredMessage deferredMessage)
		{
			super(deferredMessage.getUuid(), deferredMessage);
		}
	}

	public static DeferredMessageResponseMessage create(Collection<DeferredMessage> deferredMessages)
	{
		return new DeferredMessageResponseMessage(new BijectionCollection<DeferredMessage, Entry>(new Bijection<DeferredMessage, Entry>()
		{

			@Override
			public Entry forward(DeferredMessage deferredMessage)
			{
				return new Entry(deferredMessage);
			}

			@Override
			public DeferredMessage backward(Entry entry)
			{
				return entry.getValue();
			}
		}, deferredMessages));
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends AbstractUUIDPersistentInfoMessage.SubProtocol<DeferredMessage, DeferredMessageResponseMessage>
	{
		private final DeferredMessageProtocol deferredMessageProtocol;

		public SubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
		{
			super(0, persistenceManager, transaction, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.deferredMessageProtocol = new DeferredMessageProtocol(0, persistenceManager, transaction);
		}

		@Override
		protected void sendValue(UUID uuid, DataOutput out, DeferredMessage deferredMessage) throws IOException
		{
			deferredMessageProtocol.send(out, deferredMessage);
		}

		@Override
		protected DeferredMessage recvValue(UUID uuid, DataInput in) throws IOException, ProtocolException
		{
			return deferredMessageProtocol.recv(in);
		}

		@Override
		protected void skipValue(DataInput in) throws IOException, ProtocolException
		{
			deferredMessageProtocol.skip(in);
		}

		@Override
		public DeferredMessageResponseMessage recv(DataInput in) throws IOException, ProtocolException
		{
			return new DeferredMessageResponseMessage(recvEntries(in));
		}

	}

}
