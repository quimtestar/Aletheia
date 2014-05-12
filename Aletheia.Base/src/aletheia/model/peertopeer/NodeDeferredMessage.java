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

import java.util.Date;
import java.util.UUID;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.peertopeer.NodeDeferredMessageEntity;

public class NodeDeferredMessage
{
	private final PersistenceManager persistenceManager;
	private final NodeDeferredMessageEntity entity;

	public NodeDeferredMessage(PersistenceManager persistenceManager, NodeDeferredMessageEntity entity)
	{
		this.persistenceManager = persistenceManager;
		this.entity = entity;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public NodeDeferredMessageEntity getEntity()
	{
		return entity;
	}

	protected NodeDeferredMessage(PersistenceManager persistenceManager, UUID nodeUuid, UUID deferredMessageUuid, UUID deferredMessageRecipientUuid,
			Date deferredMessageDate)
	{
		this.persistenceManager = persistenceManager;
		this.entity = persistenceManager.instantiateNodeDeferredMessageEntity(NodeDeferredMessageEntity.class);
		setNodeUuid(nodeUuid);
		setDeferredMessageUuid(deferredMessageUuid);
		setDeferredMessageRecipientUuid(deferredMessageRecipientUuid);
		setDeferredMessageDate(deferredMessageDate);
	}

	protected static NodeDeferredMessage create(PersistenceManager persistenceManager, Transaction transaction, UUID nodeUuid, UUID deferredMessageUuid,
			UUID deferredMessageRecipientUuid, Date deferredMessageDate)
	{
		NodeDeferredMessage nodeDeferredMessage = new NodeDeferredMessage(persistenceManager, nodeUuid, deferredMessageUuid, deferredMessageRecipientUuid,
				deferredMessageDate);
		nodeDeferredMessage.persistenceUpdate(transaction);
		return nodeDeferredMessage;
	}

	protected static NodeDeferredMessage create(PersistenceManager persistenceManager, Transaction transaction, UUID nodeUuid, DeferredMessage deferredMessage)
	{
		return create(persistenceManager, transaction, nodeUuid, deferredMessage.getUuid(), deferredMessage.getRecipientUuid(), deferredMessage.getDate());
	}

	public UUID getNodeUuid()
	{
		return getEntity().getNodeUuid();
	}

	private void setNodeUuid(UUID nodeUuid)
	{
		getEntity().setNodeUuid(nodeUuid);
	}

	public UUID getDeferredMessageUuid()
	{
		return getEntity().getDeferredMessageUuid();
	}

	private void setDeferredMessageUuid(UUID deferredMessageUuid)
	{
		getEntity().setDeferredMessageUuid(deferredMessageUuid);
	}

	public UUID getDeferredMessageRecipientUuid()
	{
		return getEntity().getDeferredMessageRecipientUuid();
	}

	private void setDeferredMessageRecipientUuid(UUID deferredMessageRecipientUuid)
	{
		getEntity().setDeferredMessageRecipientUuid(deferredMessageRecipientUuid);
	}

	public Date getDeferredMessageDate()
	{
		return getEntity().getDeferredMessageDate();
	}

	private void setDeferredMessageDate(Date deferredMessageDate)
	{
		getEntity().setDeferredMessageDate(deferredMessageDate);
	}

	protected void persistenceUpdate(Transaction transaction)
	{
		persistenceManager.putNodeDeferredMessage(transaction, this);
	}

	protected boolean persistenceUpdateNoOverwrite(Transaction transaction)
	{
		return persistenceManager.putNodeDeferredMessageNoOverwrite(transaction, this);
	}

	public DeferredMessage getDeferredMessage(Transaction transaction)
	{
		return persistenceManager.getDeferredMessage(transaction, getDeferredMessageUuid());
	}

	public void delete(Transaction transaction)
	{
		persistenceManager.deleteNodeDeferredMessage(transaction, this);
	}

}
