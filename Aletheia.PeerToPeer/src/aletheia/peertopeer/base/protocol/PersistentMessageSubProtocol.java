/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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
package aletheia.peertopeer.base.protocol;

import java.io.DataOutput;
import java.io.IOException;

import aletheia.peertopeer.base.message.Message;
import aletheia.peertopeer.base.message.MessageCode;
import aletheia.peertopeer.base.message.PersistentMessage;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.protocol.PersistentExportableProtocol;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public abstract class PersistentMessageSubProtocol<M extends PersistentMessage> extends PersistentExportableProtocol<M> implements MessageSubProtocol<M>
{
	private final MessageCode messageCode;

	public PersistentMessageSubProtocol(int requiredVersion, PersistenceManager persistenceManager, Transaction transaction, MessageCode messageCode)
	{
		super(0, persistenceManager, transaction);
		checkVersionAvailability(PersistentMessageSubProtocol.class, requiredVersion);
		this.messageCode = messageCode;
	}

	protected MessageCode getMessageCode()
	{
		return messageCode;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void sendMessage(DataOutput out, Message m) throws IOException
	{
		send(out, (M) m);
	}

}
