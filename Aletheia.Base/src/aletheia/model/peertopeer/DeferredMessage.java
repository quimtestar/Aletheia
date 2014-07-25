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
package aletheia.model.peertopeer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesMap;
import aletheia.persistence.entities.peertopeer.DeferredMessageEntity;
import aletheia.protocol.Exportable;
import aletheia.protocol.peertopeer.deferredmessagecontent.DeferredMessageContentProtocol;
import aletheia.protocol.primitive.DateProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.security.messagedigester.BufferedMessageDigester;
import aletheia.security.utilities.SecurityUtilities;

public class DeferredMessage implements Exportable
{
	private final static String messageDigestAlgorithm = "SHA-1";

	public static abstract class DeferredMessageException extends Exception
	{

		private static final long serialVersionUID = -6066510443051546270L;

		public DeferredMessageException()
		{
			super();
		}

		public DeferredMessageException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public DeferredMessageException(String message)
		{
			super(message);
		}

		public DeferredMessageException(Throwable cause)
		{
			super(cause);
		}

	}

	private final PersistenceManager persistenceManager;
	private final DeferredMessageEntity entity;

	public DeferredMessage(PersistenceManager persistenceManager, DeferredMessageEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	protected DeferredMessage(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateDeferredMessageEntity(DeferredMessageEntity.class);
	}

	public static DeferredMessage create(PersistenceManager persistenceManager, Transaction transaction, UUID recipientUuid, Date date,
			DeferredMessageContent content)
	{
		DeferredMessage deferredMessage = new DeferredMessage(persistenceManager);
		deferredMessage.setRecipientUuid(recipientUuid);
		deferredMessage.setDate(date);
		deferredMessage.setContent(content);
		deferredMessage.persistenceUpdate(transaction);
		return deferredMessage;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public DeferredMessageEntity getEntity()
	{
		return entity;
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		updateUuid();
		persistenceManager.putDeferredMessage(transaction, this);
	}

	public UUID getUuid()
	{
		return getEntity().getUuid();
	}

	private void setUuid(UUID uuid)
	{
		getEntity().setUuid(uuid);
	}

	public UUID getRecipientUuid()
	{
		return entity.getRecipientUuid();
	}

	private void setRecipientUuid(UUID recipientUuid)
	{
		entity.setRecipientUuid(recipientUuid);
	}

	public Date getDate()
	{
		return entity.getDate();
	}

	private void setDate(Date date)
	{
		entity.setDate(date);
	}

	public DeferredMessageContent getContent()
	{
		return getEntity().getContent();
	}

	private void setContent(DeferredMessageContent content)
	{
		getEntity().setContent(content);
	}

	private void updateUuid()
	{
		try
		{
			BufferedMessageDigester digester = new BufferedMessageDigester(messageDigestAlgorithm);
			UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			DateProtocol dateProtocol = new DateProtocol(0);
			DeferredMessageContentProtocol deferredMessageContentProtocol = new DeferredMessageContentProtocol(0);
			uuidProtocol.send(digester.dataOutput(), getRecipientUuid());
			dateProtocol.send(digester.dataOutput(), getDate());
			deferredMessageContentProtocol.send(digester.dataOutput(), getContent());
			setUuid(SecurityUtilities.instance.messageDigestDataToUUID(digester.digest()));
		}
		catch (NoSuchAlgorithmException | IOException e)
		{
			throw new Error(e);
		}
		finally
		{
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getUuid().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeferredMessage other = (DeferredMessage) obj;
		if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}

	public NodeDeferredMessagesMap nodeDeferredMessagesMap(Transaction transaction)
	{
		return persistenceManager.nodeDeferredMessagesMap(transaction, this);
	}

	public void deleteCascade(Transaction transaction)
	{
		if (!lock(transaction))
			return;
		nodeDeferredMessagesMap(transaction).clear();
		persistenceManager.deleteDeferredMessage(transaction, this);
	}

	public boolean deleteIfNoNodes(Transaction transaction)
	{
		if (!lock(transaction))
			return true;
		if (nodeDeferredMessagesMap(transaction).isEmpty())
		{
			persistenceManager.deleteDeferredMessage(transaction, this);
			return true;
		}
		return false;
	}

	public class DeletedDeferredMessageException extends DeferredMessageException
	{

		private static final long serialVersionUID = 2131906862185660262L;

	}

	public NodeDeferredMessage createNodeDeferredMessage(Transaction transaction, UUID nodeUuid) throws DeletedDeferredMessageException
	{
		if (!lock(transaction))
			throw new DeletedDeferredMessageException();
		return NodeDeferredMessage.create(persistenceManager, transaction, nodeUuid, this);
	}

	private boolean lock(Transaction transaction)
	{
		return persistenceManager.lockDeferredMessage(transaction, this);
	}

}
