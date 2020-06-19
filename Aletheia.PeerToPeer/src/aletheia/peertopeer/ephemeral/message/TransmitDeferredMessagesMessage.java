/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.model.peertopeer.deferredmessagecontent.protocol.DeferredMessageContentProtocol;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.MessageSubProtocolInfo;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.collection.CollectionProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;

@MessageSubProtocolInfo(subProtocolClass = TransmitDeferredMessagesMessage.SubProtocol.class)
public class TransmitDeferredMessagesMessage extends NonPersistentMessage
{
	public static class Entry implements Exportable
	{
		private final UUID recipientUuid;
		private final Date date;
		private final DeferredMessageContent content;

		private Entry(UUID recipientUuid, Date date, DeferredMessageContent content)
		{
			super();
			this.recipientUuid = recipientUuid;
			this.date = date;
			this.content = content;
		}

		private Entry(DeferredMessage deferredMessage)
		{
			this(deferredMessage.getRecipientUuid(), deferredMessage.getDate(), deferredMessage.getContent());
		}

		public UUID getRecipientUuid()
		{
			return recipientUuid;
		}

		public Date getDate()
		{
			return date;
		}

		public DeferredMessageContent getContent()
		{
			return content;
		}

		@ProtocolInfo(availableVersions = 0)
		public static class Protocol extends ExportableProtocol<Entry>
		{
			private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			private final DateProtocol dateProtocol = new DateProtocol(0);
			private final DeferredMessageContentProtocol deferredMessageContentProtocol = new DeferredMessageContentProtocol(0);

			protected Protocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(Protocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, Entry e) throws IOException
			{
				uuidProtocol.send(out, e.getRecipientUuid());
				dateProtocol.send(out, e.getDate());
				deferredMessageContentProtocol.send(out, e.getContent());
			}

			@Override
			public Entry recv(DataInput in) throws IOException, ProtocolException
			{
				UUID recipientUuid = uuidProtocol.recv(in);
				Date date = dateProtocol.recv(in);
				DeferredMessageContent content = deferredMessageContentProtocol.recv(in);
				return new Entry(recipientUuid, date, content);
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				uuidProtocol.skip(in);
				dateProtocol.skip(in);
				deferredMessageContentProtocol.skip(in);
			}

		}

	}

	private final Collection<Entry> entries;

	private TransmitDeferredMessagesMessage(Collection<Entry> entries)
	{
		super();
		this.entries = entries;
	}

	public static TransmitDeferredMessagesMessage fromDeferredMessages(Collection<DeferredMessage> deferredMessages)
	{
		return new TransmitDeferredMessagesMessage(new BijectionCollection<>(new Bijection<DeferredMessage, Entry>()
		{

			@Override
			public Entry forward(DeferredMessage deferredMessage)
			{
				return new Entry(deferredMessage);
			}

			@Override
			public DeferredMessage backward(Entry entry)
			{
				throw new UnsupportedOperationException();
			}
		}, deferredMessages));

	}

	public Collection<Entry> getEntries()
	{
		return entries;
	}

	@ProtocolInfo(availableVersions = 0)
	public static class SubProtocol extends NonPersistentMessage.SubProtocol<TransmitDeferredMessagesMessage>
	{
		private final CollectionProtocol<Entry> entryCollectionProtocol;

		public SubProtocol(int requiredVersion, MessageCode messageCode)
		{
			super(0, messageCode);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
			this.entryCollectionProtocol = new CollectionProtocol<>(0, new Entry.Protocol(0));
		}

		@Override
		public void send(DataOutput out, TransmitDeferredMessagesMessage m) throws IOException
		{
			entryCollectionProtocol.send(out, m.getEntries());
		}

		@Override
		public TransmitDeferredMessagesMessage recv(DataInput in) throws IOException, ProtocolException
		{
			Collection<Entry> entries = entryCollectionProtocol.recv(in);
			return new TransmitDeferredMessagesMessage(entries);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			entryCollectionProtocol.skip(in);
		}
	}

}
