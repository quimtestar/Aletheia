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
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.network.ResourceTreeNodeSet.ResourceTreeNode;
import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.message.routeablesubmessage.AdjacentRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.BeltConnectRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.BitSlotRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ClosestNodeResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ClosestNodeRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ComplementingInvitationRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.FoundLocateResourceResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.LocateResourceResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.LocateResourceRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.NotFoundLocateResourceResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ResourceMetadataRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ResourceResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ResourceRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.ResponseRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.SlotRouteableSubMessage;
import aletheia.peertopeer.network.message.routeablesubmessage.TargetRouteableSubMessage;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.peertopeer.resource.Resource;
import aletheia.utilities.MiscUtilities;

public class RouteableSubMessageProcessor
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.instance.logger();

	private final PeerToPeerNode peerToPeerNode;
	private final LocalRouterSet localRouterSet;
	private final Belt belt;
	private final ResourceTreeNodeSet resourceTreeNodeSet;
	private final DeferredMessageSet deferredMessageSet;
	private final CumulationSet cumulationSet;

	public RouteableSubMessageProcessor(PeerToPeerNode peerToPeerNode)
	{
		this.peerToPeerNode = peerToPeerNode;
		this.localRouterSet = new LocalRouterSet(peerToPeerNode.getNodeUuid());
		this.belt = new Belt(peerToPeerNode.getNodeUuid());
		this.resourceTreeNodeSet = new ResourceTreeNodeSet(localRouterSet);
		this.deferredMessageSet = new DeferredMessageSet(peerToPeerNode, localRouterSet, resourceTreeNodeSet);
		this.cumulationSet = null; // DISABLED FEATURE new CumulationSet(peerToPeerNode, localRouterSet);
	}

	public PeerToPeerNode getPeerToPeerNode()
	{
		return peerToPeerNode;
	}

	public LocalRouterSet getLocalRouterSet()
	{
		return localRouterSet;
	}

	public Belt getBelt()
	{
		return belt;
	}

	public ResourceTreeNodeSet getResourceTreeNodeSet()
	{
		return resourceTreeNodeSet;
	}

	public DeferredMessageSet getDeferredMessageSet()
	{
		return deferredMessageSet;
	}

	public CumulationSet getCumulationSet()
	{
		return cumulationSet;
	}

	private UUID getNodeUuid()
	{
		return peerToPeerNode.getNodeUuid();
	}

	public void putLocalResource(Resource.Metadata resourceMetadata)
	{
		resourceTreeNodeSet.putLocalResource(resourceMetadata);
	}

	public void removeLocalResource(Resource resource)
	{
		resourceTreeNodeSet.removeLocalResource(resource);
	}

	public boolean isLocalResource(Resource resource)
	{
		return resourceTreeNodeSet.isLocalResource(resource);
	}

	public Resource.Metadata getLocalResourceMetadata(Resource resource)
	{
		return resourceTreeNodeSet.getLocalResourceMetadata(resource);
	}

	public ResourceTreeNodeSet.Location getNextLocation(Resource resource)
	{
		return resourceTreeNodeSet.getNextLocation(resource);
	}

	public void clearLocalResources()
	{
		resourceTreeNodeSet.clearLocalResources();
	}

	private boolean processClosestNodeResponse(ClosestNodeResponseRouteableSubMessage closestNodeResponseRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (fromNetworkPhase != null && closestNodeResponseRouteableSubMessage.getAddress() == null)
			closestNodeResponseRouteableSubMessage.setAddress(fromNetworkPhase.bindAddress());
		if (closestNodeResponseRouteableSubMessage.getTarget().equals(getNodeUuid()))
		{
			peerToPeerNode.pendingResponse(closestNodeResponseRouteableSubMessage);
			return true;
		}
		else
		{
			NetworkPhase toNetworkPhase = localRouterSet.pathStep(closestNodeResponseRouteableSubMessage);
			if (toNetworkPhase == null)
				return false;
			toNetworkPhase.routeableSubMessage(closestNodeResponseRouteableSubMessage);
			return true;
		}
	}

	private boolean processResourceResponse(ResourceResponseRouteableSubMessage resourceResponseRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (resourceResponseRouteableSubMessage instanceof LocateResourceResponseRouteableSubMessage)
			return processLocateResourceResponse((LocateResourceResponseRouteableSubMessage) resourceResponseRouteableSubMessage, fromNetworkPhase);
		else
			throw new Error();
	}

	private boolean processLocateResourceResponse(LocateResourceResponseRouteableSubMessage locateResourceResponseRouteableSubMessage,
			NetworkPhase fromNetworkPhase)
	{
		if (locateResourceResponseRouteableSubMessage instanceof FoundLocateResourceResponseRouteableSubMessage)
		{
			FoundLocateResourceResponseRouteableSubMessage foundLocateResourceResponseRouteableSubMessage = (FoundLocateResourceResponseRouteableSubMessage) locateResourceResponseRouteableSubMessage;
			if (foundLocateResourceResponseRouteableSubMessage.getAddress() == null)
				foundLocateResourceResponseRouteableSubMessage.setAddress(fromNetworkPhase.bindAddress());
		}
		if (locateResourceResponseRouteableSubMessage.getTarget().equals(getNodeUuid()))
		{
			peerToPeerNode.pendingResponse(locateResourceResponseRouteableSubMessage);
			return true;
		}
		else
		{
			NetworkPhase toNetworkPhase = localRouterSet.pathStep(locateResourceResponseRouteableSubMessage);
			if (toNetworkPhase == null)
				return false;
			toNetworkPhase.routeableSubMessage(locateResourceResponseRouteableSubMessage);
			return true;
		}
	}

	private boolean processResponse(ResponseRouteableSubMessage responseRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (responseRouteableSubMessage instanceof ClosestNodeResponseRouteableSubMessage)
			return processClosestNodeResponse((ClosestNodeResponseRouteableSubMessage) responseRouteableSubMessage, fromNetworkPhase);
		else if (responseRouteableSubMessage instanceof ResourceResponseRouteableSubMessage)
			return processResourceResponse((ResourceResponseRouteableSubMessage) responseRouteableSubMessage, fromNetworkPhase);
		else
		{
			if (responseRouteableSubMessage.getTarget().equals(getNodeUuid()))
			{
				peerToPeerNode.pendingResponse(responseRouteableSubMessage);
				return true;
			}
			else
			{
				NetworkPhase toNetworkPhase = localRouterSet.pathStep(responseRouteableSubMessage);
				if (toNetworkPhase == null)
					return false;
				toNetworkPhase.routeableSubMessage(responseRouteableSubMessage);
				return true;
			}
		}
	}

	private boolean terminalProcessTargetRouteable(TargetRouteableSubMessage targetRouteableSubMessage)
	{
		if (targetRouteableSubMessage instanceof ClosestNodeRouteableSubMessage)
			return terminalProcessClosestNode((ClosestNodeRouteableSubMessage) targetRouteableSubMessage);
		else
			throw new Error();
	}

	private boolean terminalProcessClosestNode(ClosestNodeRouteableSubMessage closestNodeRouteableSubMessage)
	{
		ClosestNodeResponseRouteableSubMessage closestNodeResponseRouteableSubMessage = new ClosestNodeResponseRouteableSubMessage(getNodeUuid(),
				peerToPeerNode.sequence(), closestNodeRouteableSubMessage.getOrigin(), closestNodeRouteableSubMessage.getSequence(), getNodeUuid());
		if (closestNodeResponseRouteableSubMessage.getTarget().equals(getNodeUuid()))
		{
			peerToPeerNode.pendingResponse(closestNodeResponseRouteableSubMessage);
			return true;
		}
		else
		{
			NetworkPhase toNetworkPhase = localRouterSet.pathStep(closestNodeResponseRouteableSubMessage);
			if (toNetworkPhase == null)
				return false;
			toNetworkPhase.routeableSubMessage(closestNodeResponseRouteableSubMessage);
			return true;
		}
	}

	private boolean processTargetRouteable(TargetRouteableSubMessage targetRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (targetRouteableSubMessage instanceof ClosestNodeRouteableSubMessage)
			return processClosestNodeRouteable((ClosestNodeRouteableSubMessage) targetRouteableSubMessage, fromNetworkPhase);
		else if (targetRouteableSubMessage instanceof ResponseRouteableSubMessage)
			return processResponse((ResponseRouteableSubMessage) targetRouteableSubMessage, fromNetworkPhase);
		else if (targetRouteableSubMessage instanceof ResourceMetadataRouteableSubMessage)
			return processResourceMetatadataRouteableSubmessage((ResourceMetadataRouteableSubMessage) targetRouteableSubMessage);
		else
			throw new Error();
	}

	private boolean processResourceMetatadataRouteableSubmessage(ResourceMetadataRouteableSubMessage resourceMetadataRouteableSubMessage)
	{
		if (resourceMetadataRouteableSubMessage.getTarget().equals(getNodeUuid()))
		{
			peerToPeerNode.receiveResourceMetadata(resourceMetadataRouteableSubMessage.getOrigin(), resourceMetadataRouteableSubMessage.getResourceMetadata());
			return true;
		}
		else
		{
			NetworkPhase toNetworkPhase = localRouterSet.pathStep(resourceMetadataRouteableSubMessage);
			if (toNetworkPhase == null)
				return false;
			toNetworkPhase.routeableSubMessage(resourceMetadataRouteableSubMessage);
			return true;
		}
	}

	private boolean processClosestNodeRouteable(ClosestNodeRouteableSubMessage closestNodeRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		NetworkPhase toNetworkPhase = localRouterSet.pathStep(closestNodeRouteableSubMessage);
		if (toNetworkPhase == null)
			return terminalProcessTargetRouteable(closestNodeRouteableSubMessage);
		else
		{
			toNetworkPhase.routeableSubMessage(closestNodeRouteableSubMessage);
			return true;
		}
	}

	private boolean processSlotRouteable(SlotRouteableSubMessage slotRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (slotRouteableSubMessage instanceof BitSlotRouteableSubMessage)
			return processBitSlotRouteable((BitSlotRouteableSubMessage) slotRouteableSubMessage, fromNetworkPhase);
		else
			throw new Error();
	}

	private boolean processBitSlotRouteable(BitSlotRouteableSubMessage bitSlotRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (bitSlotRouteableSubMessage instanceof ComplementingInvitationRouteableSubMessage)
			return processComplementingInvitation((ComplementingInvitationRouteableSubMessage) bitSlotRouteableSubMessage, fromNetworkPhase);
		else
			throw new Error();
	}

	private boolean processComplementingInvitation(ComplementingInvitationRouteableSubMessage complementingInvitationRouteableSubMessage,
			NetworkPhase fromNetworkPhase)
	{
		if (fromNetworkPhase != null && complementingInvitationRouteableSubMessage.getAddress() == null)
			complementingInvitationRouteableSubMessage.setAddress(fromNetworkPhase.bindAddress());
		int slot = complementingInvitationRouteableSubMessage.getSlot();
		boolean bit = complementingInvitationRouteableSubMessage.getBit();
		if (MiscUtilities.bitAt(peerToPeerNode.getNodeUuid(), slot) == bit)
		{
			NodeAddress nodeAddress = complementingInvitationRouteableSubMessage.nodeAddress();
			for (NetworkPhase neighbour : getLocalRouterSet().neighbourCollection(slot + 1))
				neighbour.complementingInvitation(nodeAddress);
			return true;
		}
		else
		{
			Set<NetworkPhase> toNetworkPhases = localRouterSet.pathStepMultiple(complementingInvitationRouteableSubMessage);
			for (NetworkPhase to : toNetworkPhases)
				to.routeableSubMessage(complementingInvitationRouteableSubMessage);
			return true;
		}
	}

	private boolean processBeltConnect(BeltConnectRouteableSubMessage beltConnectRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		NetworkPhase toNetworkPhase = adjacentPathStep(beltConnectRouteableSubMessage, fromNetworkPhase);
		if (fromNetworkPhase == null)
		{
			if (toNetworkPhase != null)
			{
				toNetworkPhase.routeableSubMessage(beltConnectRouteableSubMessage);
				return true;
			}
			else
				return false;
		}
		else
		{
			if (beltConnectRouteableSubMessage.getAddress() == null)
				beltConnectRouteableSubMessage.setAddress(fromNetworkPhase.bindAddress());
			if (toNetworkPhase == null)
			{
				if (!beltConnectRouteableSubMessage.getOrigin().equals(getNodeUuid()))
				{
					try
					{
						peerToPeerNode.beltNetworkConnect(beltConnectRouteableSubMessage.nodeAddress(), beltConnectRouteableSubMessage.getSide());
						return true;
					}
					catch (IOException | ConnectException e)
					{
						return false;
					}
				}
				else
					return false;
			}
			else
			{
				toNetworkPhase.routeableSubMessage(beltConnectRouteableSubMessage);
				return true;
			}
		}
	}

	private boolean processResource(ResourceRouteableSubMessage resourceRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (resourceRouteableSubMessage instanceof LocateResourceRouteableSubMessage)
			return processLocateResource((LocateResourceRouteableSubMessage) resourceRouteableSubMessage, fromNetworkPhase);
		else
			throw new Error();
	}

	private boolean processLocateResource(LocateResourceRouteableSubMessage locateResourceRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		Resource resource = locateResourceRouteableSubMessage.getResource();
		ResourceTreeNode resourceTreeNode = resourceTreeNodeSet.getResourceTreeNode(resource);
		if (resourceTreeNode == null)
		{
			NetworkPhase toNetworkPhase = localRouterSet.pathStep(resource.getUuid());
			if (toNetworkPhase != null)
			{
				toNetworkPhase.routeableSubMessage(locateResourceRouteableSubMessage);
				return true;
			}
			else
			{
				NotFoundLocateResourceResponseRouteableSubMessage notFoundLocateResourceResponseRouteableSubMessage = new NotFoundLocateResourceResponseRouteableSubMessage(
						getNodeUuid(), peerToPeerNode.sequence(), locateResourceRouteableSubMessage.getOrigin(),
						locateResourceRouteableSubMessage.getSequence(), resource);
				NetworkPhase toNetworkPhase_ = localRouterSet.pathStep(locateResourceRouteableSubMessage.getOrigin());
				if (toNetworkPhase_ == null)
				{
					peerToPeerNode.pendingResponse(notFoundLocateResourceResponseRouteableSubMessage);
					return true;
				}
				else
				{
					toNetworkPhase_.routeableSubMessage(notFoundLocateResourceResponseRouteableSubMessage);
					return true;
				}
			}
		}
		else
		{
			Resource.Metadata localMetadata = resourceTreeNode.getLocalResourceMetadata();
			if (localMetadata == null)
			{
				ResourceTreeNodeSet.Location closestLocation = resourceTreeNode.getClosestLocation();
				FoundLocateResourceResponseRouteableSubMessage foundLocateResourceResponseRouteableSubMessage = new FoundLocateResourceResponseRouteableSubMessage(
						getNodeUuid(), peerToPeerNode.sequence(), locateResourceRouteableSubMessage.getOrigin(),
						locateResourceRouteableSubMessage.getSequence(), closestLocation.getResourceMetadata(), closestLocation.getNodeAddress().getUuid());
				foundLocateResourceResponseRouteableSubMessage.setAddress(closestLocation.getNodeAddress().getAddress());
				NetworkPhase toNetworkPhase = localRouterSet.pathStep(locateResourceRouteableSubMessage.getOrigin());
				if (toNetworkPhase == null)
				{
					peerToPeerNode.pendingResponse(foundLocateResourceResponseRouteableSubMessage);
					return true;
				}
				else
				{
					toNetworkPhase.routeableSubMessage(foundLocateResourceResponseRouteableSubMessage);
					return true;
				}
			}
			else
			{
				FoundLocateResourceResponseRouteableSubMessage foundLocateResourceResponseRouteableSubMessage = new FoundLocateResourceResponseRouteableSubMessage(
						getNodeUuid(), peerToPeerNode.sequence(), locateResourceRouteableSubMessage.getOrigin(),
						locateResourceRouteableSubMessage.getSequence(), localMetadata, getNodeUuid());
				NetworkPhase toNetworkPhase = localRouterSet.pathStep(locateResourceRouteableSubMessage.getOrigin());
				if (toNetworkPhase == null)
				{
					peerToPeerNode.pendingResponse(foundLocateResourceResponseRouteableSubMessage);
					return true;
				}
				else
				{
					toNetworkPhase.routeableSubMessage(foundLocateResourceResponseRouteableSubMessage);
					return true;
				}
			}
		}
	}

	public boolean process(RouteableSubMessage routeableSubMessage, NetworkPhase fromNetworkPhase)
	{
		if (routeableSubMessage instanceof TargetRouteableSubMessage)
			return processTargetRouteable((TargetRouteableSubMessage) routeableSubMessage, fromNetworkPhase);
		else if (routeableSubMessage instanceof SlotRouteableSubMessage)
			return processSlotRouteable((SlotRouteableSubMessage) routeableSubMessage, fromNetworkPhase);
		else if (routeableSubMessage instanceof BeltConnectRouteableSubMessage)
			return processBeltConnect((BeltConnectRouteableSubMessage) routeableSubMessage, fromNetworkPhase);
		else if (routeableSubMessage instanceof ResourceRouteableSubMessage)
			return processResource((ResourceRouteableSubMessage) routeableSubMessage, fromNetworkPhase);
		else
			throw new Error();
	}

	public boolean process(RouteableSubMessage routeableSubMessage)
	{
		return process(routeableSubMessage, null);
	}

	private int sideFactor(Side side)
	{
		switch (side)
		{
		case Left:
			return +1;
		case Right:
			return -1;
		default:
			throw new Error();
		}
	}

	private NetworkPhase adjacentPathStep(AdjacentRouteableSubMessage adjacentRouteableSubMessage, NetworkPhase fromNetworkPhase)
	{
		UUID target = adjacentRouteableSubMessage.getTarget();
		RelativeNetworkPhaseComparator relativeNetworkPhaseComparator = new RelativeNetworkPhaseComparator(target);
		int sideFactor = sideFactor(adjacentRouteableSubMessage.getSide());
		NetworkPhase neighbour = null;
		Belt belt = getBelt();
		synchronized (belt)
		{
			for (NetworkPhase n : belt.neighbourCollection())
			{
				if (!n.equals(fromNetworkPhase) && !target.equals(n.getPeerNodeUuid()) && n.isOpen()
						&& (neighbour == null || relativeNetworkPhaseComparator.compare(neighbour, n) * sideFactor > 0))
					neighbour = n;
			}
		}
		LocalRouterSet localRouterSet = getLocalRouterSet();
		synchronized (localRouterSet)
		{
			for (NetworkPhase n : localRouterSet.neighbourCollection())
			{
				if (!n.equals(fromNetworkPhase) && !target.equals(n.getPeerNodeUuid()) && n.isOpen()
						&& (neighbour == null || relativeNetworkPhaseComparator.compare(neighbour, n) * sideFactor > 0))
					neighbour = n;
			}
		}

		if (neighbour != null && !target.equals(getNodeUuid())
				&& relativeNetworkPhaseComparator.getInner().compare(neighbour.getPeerNodeUuid(), getNodeUuid()) * sideFactor >= 0)
			neighbour = null;

		return neighbour;
	}

	public void seedDeferredMessage(UUID recipientUuid, DeferredMessageContent content)
	{
		deferredMessageSet.seedDeferredMessage(recipientUuid, content);
	}

	public void close()
	{
		deferredMessageSet.close();
	}

	public double networkSizeEstimation()
	{
		return localRouterSet.networkSizeEstimation();
	}

}
