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
package aletheia.peertopeer.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.DeferredMessage.DeletedDeferredMessageException;
import aletheia.model.peertopeer.NodeDeferredMessage;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.network.ResourceTreeNodeSet.ResourceTreeNode;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.peertopeer.resource.PrivatePersonResource;
import aletheia.peertopeer.resource.Resource;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.collections.peertopeer.NodeDeferredMessagesByRecipientCollection;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCloseableCollection;
import aletheia.utilities.collections.BijectionMap;
import aletheia.utilities.collections.BufferedList;
import aletheia.utilities.collections.CloseableCollection;

public class DeferredMessageSet
{
	private final static Logger logger = LoggerManager.instance.logger();

	public final static int maxDistance = 1;
	public final static long maxAge = 7 * 24 * 60 * 60 * 1000; //millis

	private final PeerToPeerNode peerToPeerNode;
	private final LocalRouterSet localRouterSet;
	private final ResourceTreeNodeSet resourceTreeNodeSet;

	private class LocalRouterSetListener implements LocalRouterSet.Listener
	{

		@Override
		public void changed()
		{
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{
					localRouterSetChange();
				}
			});
		}
	}

	private class ResourceTreeNodeSetListener implements ResourceTreeNodeSet.Listener
	{
		@Override
		public void setLocalMetadata(Resource.Metadata localMetadata)
		{
			Resource resource = localMetadata.getResource();
			if (resource instanceof PrivatePersonResource)
				asynchronouslyTransmitDeferredMessages((PrivatePersonResource) resource);
		}

		@Override
		public void clearLocalMetadata(Resource resource)
		{
		}

		@Override
		public void updatedClosestLocation(ResourceTreeNodeSet.Location closestLocation)
		{
			Resource resource = closestLocation.getResourceMetadata().getResource();
			if (resource instanceof PrivatePersonResource)
				asynchronouslyTransmitDeferredMessages((PrivatePersonResource) resource);
		}

		@Override
		public void removedClosestLocation(Resource resource)
		{
			if (resource instanceof PrivatePersonResource)
				asynchronouslyTransmitDeferredMessages((PrivatePersonResource) resource);
		}

		@Override
		public void updatedNextLocation(ResourceTreeNodeSet.Location location)
		{
		}

		@Override
		public void removedNextLocation(Resource resource)
		{
		}

	}

	private class RecipientData
	{
		private int distance;
		private final Map<NetworkPhase, Integer> neighbourDistance;

		private RecipientData()
		{
			this.distance = Integer.MAX_VALUE;
			this.neighbourDistance = new HashMap<>();
		}

	}

	private final Map<UUID, RecipientData> recipientDataMap;

	public DeferredMessageSet(PeerToPeerNode peerToPeerNode, LocalRouterSet localRouterSet, ResourceTreeNodeSet resourceTreeNodeSet)
	{
		this.peerToPeerNode = peerToPeerNode;
		this.localRouterSet = localRouterSet;
		localRouterSet.addListener(new LocalRouterSetListener());
		this.resourceTreeNodeSet = resourceTreeNodeSet;
		resourceTreeNodeSet.addListener(new ResourceTreeNodeSetListener());
		this.recipientDataMap = new HashMap<>();
		clearDeferredMessages();
	}

	private UUID getNodeUuid()
	{
		return peerToPeerNode.getNodeUuid();
	}

	private PersistenceManager getPersistenceManager()
	{
		return peerToPeerNode.getPersistenceManager();
	}

	public synchronized int distance(UUID recipientUuid)
	{
		RecipientData recipientData = recipientDataMap.get(recipientUuid);
		if (recipientData == null)
			return Integer.MAX_VALUE;
		return recipientData.distance;
	}

	public synchronized Map<UUID, Integer> recipientDistanceMap()
	{
		return Collections.unmodifiableMap(new BijectionMap<>(new Bijection<RecipientData, Integer>()
		{

			@Override
			public Integer forward(RecipientData recipientData)
			{
				return recipientData.distance;
			}

			@Override
			public RecipientData backward(Integer output)
			{
				throw new UnsupportedOperationException();
			}
		}, recipientDataMap));
	}

	private void clearDeferredMessages(Transaction transaction)
	{
		for (NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection : getPersistenceManager()
				.nodeDeferredMessagesByNodeMap(transaction, getNodeUuid()).values())
			clearDeferredMessagesByRecipient(transaction, nodeDeferredMessagesByRecipientCollection);
	}

	private void clearDeferredMessages()
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			clearDeferredMessages(transaction);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	public void close()
	{
		clearDeferredMessages();
	}

	private DeferredMessage createDeferredMessage(UUID recipientUuid, DeferredMessageContent content)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			DeferredMessage deferredMessage = DeferredMessage.create(getPersistenceManager(), transaction, recipientUuid, new Date(), content);
			transaction.commit();
			return deferredMessage;
		}
		finally
		{
			transaction.abort();
		}
	}

	private void clearDeferredMessagesByRecipient(Transaction transaction, NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection)
	{
		for (NodeDeferredMessage nodeDeferredMessage : nodeDeferredMessagesByRecipientCollection)
			deleteDeferredMessageNode(transaction, nodeDeferredMessage);
	}

	private void clearDeferredMessagesByRecipient(Transaction transaction, UUID recipientUuid)
	{
		clearDeferredMessagesByRecipient(transaction,
				getPersistenceManager().nodeDeferredMessagesByRecipientCollection(transaction, getNodeUuid(), recipientUuid));
	}

	private void clearDeferredMessagesByRecipient(UUID recipientUuid)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			clearDeferredMessagesByRecipient(transaction, recipientUuid);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	private void deleteDeferredMessageNode(Transaction transaction, NodeDeferredMessage nodeDeferredMessage)
	{
		deleteDeferredMessageNode(transaction, nodeDeferredMessage.getDeferredMessage(transaction), nodeDeferredMessage);
	}

	private void deleteDeferredMessageNode(Transaction transaction, DeferredMessage deferredMessage, NodeDeferredMessage nodeDeferredMessage)
	{
		nodeDeferredMessage.delete(transaction);
		deferredMessage.deleteIfNoNodes(transaction);
	}

	private void deleteDeferredMessageNode(DeferredMessage deferredMessage, NodeDeferredMessage nodeDeferredMessage)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			deleteDeferredMessageNode(transaction, deferredMessage, nodeDeferredMessage);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	public synchronized void seedDeferredMessage(UUID recipientUuid, DeferredMessageContent content)
	{
		DeferredMessage deferredMessage;
		NodeDeferredMessage nodeDeferredMessage;
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			deferredMessage = createDeferredMessage(recipientUuid, content);
			try
			{
				nodeDeferredMessage = deferredMessage.createNodeDeferredMessage(transaction, getNodeUuid());
			}
			catch (DeletedDeferredMessageException e)
			{
				throw new Error(e);
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
		addDeferredMessage(deferredMessage, nodeDeferredMessage);
		asynchronouslyTransmitDeferredMessages(new PrivatePersonResource(recipientUuid));
	}

	public synchronized void addDeferredMessage(DeferredMessage deferredMessage, NodeDeferredMessage nodeDeferredMessage)
	{
		cleanExpiredNodeDeferredMessages(deferredMessage.getRecipientUuid());
		RecipientData recipientData = recipientDataMap.get(deferredMessage.getRecipientUuid());
		if (recipientData != null)
		{
			for (NetworkPhase neighbour : recipientData.neighbourDistance.keySet())
				neighbour.propagateDeferredMessages(deferredMessage);
		}
		else
		{
			synchronized (localRouterSet)
			{
				NetworkPhase neighbour = localRouterSet.pathStep(deferredMessage.getRecipientUuid());
				if (neighbour == null)
				{
					recipientData = new RecipientData();
					recipientDataMap.put(deferredMessage.getRecipientUuid(), recipientData);
					updateRecipientData(deferredMessage.getRecipientUuid(), recipientData);
					if (recipientData.distance != 0)
						throw new Error();
				}
				else
				{
					neighbour.propagateDeferredMessages(deferredMessage);
					deleteDeferredMessageNode(deferredMessage, nodeDeferredMessage);
				}
			}

		}
	}

	private void cleanExpiredNodeDeferredMessages(UUID recipientUuid)
	{
		Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			for (NodeDeferredMessage nodeDeferredMessage : getPersistenceManager().nodeDeferredMessagesByRecipientCollection(transaction, getNodeUuid(),
					recipientUuid, null, fromDate()))
				deleteDeferredMessageNode(transaction, nodeDeferredMessage);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

	private NodeDeferredMessagesByRecipientCollection propagableNodeDeferredMessagesByRecipientCollection(Transaction transaction, UUID recipientUuid)
	{
		return getPersistenceManager().nodeDeferredMessagesByRecipientCollection(transaction, getNodeUuid(), recipientUuid, fromDate());
	}

	private CloseableCollection<DeferredMessage> propagableDeferredMessagesByRecipientCollection(final Transaction transaction, UUID recipientUuid)
	{
		return new BijectionCloseableCollection<>(new Bijection<NodeDeferredMessage, DeferredMessage>()
		{

			@Override
			public DeferredMessage forward(NodeDeferredMessage nodeDeferredMessage)
			{
				return nodeDeferredMessage.getDeferredMessage(transaction);
			}

			@Override
			public NodeDeferredMessage backward(DeferredMessage deferredMessage)
			{
				throw new UnsupportedOperationException();
			}
		}, propagableNodeDeferredMessagesByRecipientCollection(transaction, recipientUuid));
	}

	private Date fromDate()
	{
		return new Date(System.currentTimeMillis() - maxAge);
	}

	private Collection<DeferredMessage> propagableDeferredMessagesByRecipientCollection(UUID recipientUuid)
	{
		final Transaction transaction = getPersistenceManager().beginTransaction();
		try
		{
			return new BufferedList<>(propagableDeferredMessagesByRecipientCollection(transaction, recipientUuid));
		}
		finally
		{
			transaction.abort();
		}
	}

	private synchronized void localRouterSetChange()
	{
		synchronized (localRouterSet)
		{
			for (Iterator<Map.Entry<UUID, RecipientData>> iterator = recipientDataMap.entrySet().iterator(); iterator.hasNext();)
			{
				Map.Entry<UUID, RecipientData> e = iterator.next();
				UUID recipientUuid = e.getKey();
				RecipientData recipientData = e.getValue();
				NetworkPhase neighbour = localRouterSet.pathStep(recipientUuid);
				if (neighbour != null && !recipientData.neighbourDistance.containsKey(neighbour))
				{
					Collection<DeferredMessage> deferredMessageCollection = propagableDeferredMessagesByRecipientCollection(recipientUuid);
					if (!deferredMessageCollection.isEmpty())
						neighbour.propagateDeferredMessages(deferredMessageCollection);
				}
				for (Iterator<NetworkPhase> iterator2 = recipientData.neighbourDistance.keySet().iterator(); iterator2.hasNext();)
				{
					NetworkPhase n = iterator2.next();
					if (!localRouterSet.containsNeighbour(n))
						iterator2.remove();
				}
				updateRecipientData(recipientUuid, recipientData);
				if (recipientData.distance > maxDistance)
				{
					iterator.remove();
					clearDeferredMessagesByRecipient(recipientUuid);
				}
			}

		}
	}

	public synchronized void messageQueueNeighbour(UUID recipientUuid, NetworkPhase neighbour, int distance)
	{
		RecipientData recipientData = recipientDataMap.get(recipientUuid);
		if (recipientData == null)
		{
			recipientData = new RecipientData();
			recipientDataMap.put(recipientUuid, recipientData);
		}
		Integer oldDistance = recipientData.neighbourDistance.put(neighbour, distance);
		if (oldDistance == null && distance <= maxDistance)
		{
			Collection<DeferredMessage> deferredMessages = propagableDeferredMessagesByRecipientCollection(recipientUuid);
			if (!deferredMessages.isEmpty())
				neighbour.propagateDeferredMessages(deferredMessages);
		}
		if (oldDistance == null || oldDistance != distance)
		{
			updateRecipientData(recipientUuid, recipientData);
			if (recipientData.distance > maxDistance)
			{
				recipientDataMap.remove(recipientUuid);
				clearDeferredMessagesByRecipient(recipientUuid);
			}
		}

	}

	private synchronized void updateRecipientData(UUID recipientUuid, RecipientData recipientData)
	{
		synchronized (localRouterSet)
		{
			int newDistance;
			if (localRouterSet.pathStep(recipientUuid) == null)
				newDistance = 0;
			else
			{
				newDistance = Integer.MAX_VALUE;
				for (int d : recipientData.neighbourDistance.values())
					if (d < newDistance - 1)
						newDistance = d + 1;
			}
			if (newDistance <= maxDistance)
			{
				for (NetworkPhase neighbour_ : localRouterSet.neighbourCollection())
					if (!recipientData.neighbourDistance.containsKey(neighbour_) || recipientData.distance != newDistance)
						neighbour_.deferredMessageQueue(recipientUuid, newDistance);
			}
			recipientData.distance = newDistance;
		}
	}

	private void asynchronouslyTransmitDeferredMessages(final PrivatePersonResource privatePersonResourceMetadata)
	{
		AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
		{

			@Override
			public void invoke()
			{
				try
				{
					transmitDeferredMessages(privatePersonResourceMetadata);
				}
				catch (IOException | ConnectException | CancelledCommandException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	private synchronized void transmitDeferredMessages(PrivatePersonResource privatePersonResourceMetadata)
			throws IOException, ConnectException, CancelledCommandException
	{
		RecipientData recipientData = recipientDataMap.get(privatePersonResourceMetadata.getUuid());
		if (recipientData != null && recipientData.distance == 0)
		{
			synchronized (resourceTreeNodeSet)
			{
				ResourceTreeNode resourceTreeNode = resourceTreeNodeSet.getResourceTreeNode(privatePersonResourceMetadata);
				if (resourceTreeNode != null)
				{
					Transaction transaction = getPersistenceManager().beginTransaction();
					try
					{
						NodeDeferredMessagesByRecipientCollection nodeDeferredMessagesByRecipientCollection = propagableNodeDeferredMessagesByRecipientCollection(
								transaction, privatePersonResourceMetadata.getUuid());
						List<NodeDeferredMessage> nodeDeferredMessages = new ArrayList<>();
						List<UUID> deferredMessageUuids = new ArrayList<>();
						List<DeferredMessage> deferredMessages = new ArrayList<>();
						for (NodeDeferredMessage nodeDeferredMessage : nodeDeferredMessagesByRecipientCollection)
						{
							nodeDeferredMessages.add(nodeDeferredMessage);
							deferredMessageUuids.add(nodeDeferredMessage.getDeferredMessageUuid());
							deferredMessages.add(nodeDeferredMessage.getDeferredMessage(transaction));
						}
						if (!nodeDeferredMessages.isEmpty())
						{
							try
							{
								boolean transmitted = peerToPeerNode.transmitDeferredMessages(privatePersonResourceMetadata.getUuid(), deferredMessages);
								if (transmitted)
								{
									for (int i = 0; i < nodeDeferredMessages.size(); i++)
										deleteDeferredMessageNode(transaction, deferredMessages.get(i), nodeDeferredMessages.get(i));
									recipientDataMap.remove(privatePersonResourceMetadata.getUuid());
									for (NetworkPhase neighbour : recipientData.neighbourDistance.keySet())
										neighbour.propagateDeferredMessageRemoval(privatePersonResourceMetadata.getUuid(), deferredMessageUuids);
								}
							}
							catch (InterruptedException e)
							{
								logger.error(e.getMessage(), e);
							}
						}
						transaction.commit();
					}
					finally
					{
						transaction.abort();
					}
				}
			}
		}
	}

	public synchronized void removeDeferredMessages(UUID recipientUuid, Collection<UUID> deferredMessageUuids)
	{
		RecipientData recipientData = recipientDataMap.get(recipientUuid);
		if (recipientData != null)
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				Collection<UUID> propagateDeferredMessageUuids = new ArrayList<>();
				for (UUID uuid : deferredMessageUuids)
				{
					DeferredMessage deferredMessage = getPersistenceManager().getDeferredMessage(transaction, uuid);
					if (deferredMessage != null)
					{
						NodeDeferredMessage nodeDeferredMessage = deferredMessage.nodeDeferredMessagesMap(transaction).get(getNodeUuid());
						if (nodeDeferredMessage != null)
						{
							deleteDeferredMessageNode(deferredMessage, nodeDeferredMessage);
							propagateDeferredMessageUuids.add(uuid);
						}
					}
				}
				if (propagableNodeDeferredMessagesByRecipientCollection(transaction, recipientUuid).isEmpty())
					recipientDataMap.remove(recipientUuid);
				if (!propagateDeferredMessageUuids.isEmpty())
				{
					for (NetworkPhase neighbour : recipientData.neighbourDistance.keySet())
						neighbour.propagateDeferredMessageRemoval(recipientUuid, propagateDeferredMessageUuids);
				}
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}
	}
}
