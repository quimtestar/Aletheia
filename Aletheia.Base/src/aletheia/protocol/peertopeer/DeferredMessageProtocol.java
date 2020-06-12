/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.protocol.peertopeer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.peertopeer.deferredmessagecontent.DeferredMessageContentProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.UUIDProtocol;

@ProtocolInfo(availableVersions = 0)
public class DeferredMessageProtocol extends PersistentExportableProtocol<DeferredMessage>
{
	private final UUIDProtocol uuidProtocol;
	private final DateProtocol dateProtocol;
	private final DeferredMessageContentProtocol deferredMessageContentProtocol;

	public DeferredMessageProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(DeferredMessageProtocol.class, requiredVersion);
		this.uuidProtocol = new UUIDProtocol(0);
		this.dateProtocol = new DateProtocol(0);
		this.deferredMessageContentProtocol = new DeferredMessageContentProtocol(0);
	}

	@Override
	public void send(DataOutput out, DeferredMessage deferredMessage) throws IOException
	{
		uuidProtocol.send(out, deferredMessage.getUuid());
		uuidProtocol.send(out, deferredMessage.getRecipientUuid());
		dateProtocol.send(out, deferredMessage.getDate());
		deferredMessageContentProtocol.send(out, deferredMessage.getContent());
	}

	@Override
	public DeferredMessage recv(DataInput in) throws IOException, ProtocolException
	{
		UUID uuid = uuidProtocol.recv(in);
		UUID recipientUuid = uuidProtocol.recv(in);
		Date date = dateProtocol.recv(in);
		DeferredMessageContent content = deferredMessageContentProtocol.recv(in);
		DeferredMessage old = getPersistenceManager().getDeferredMessage(getTransaction(), uuid);
		if (old == null)
		{
			DeferredMessage deferredMessage = DeferredMessage.create(getPersistenceManager(), getTransaction(), recipientUuid, date, content);
			if (!uuid.equals(deferredMessage.getUuid()))
				throw new ProtocolException();
			return deferredMessage;
		}
		else
		{
			if (!recipientUuid.equals(old.getRecipientUuid()))
				throw new ProtocolException();
			if (!date.equals(old.getDate()))
				throw new ProtocolException();
			if (!content.equals(old.getContent()))
				throw new ProtocolException();
			return old;
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		uuidProtocol.skip(in);
		uuidProtocol.skip(in);
		dateProtocol.skip(in);
		deferredMessageContentProtocol.skip(in);
	}

}
