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
package aletheia.peertopeer.network.phase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.Hook;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.base.dialog.Dialog;
import aletheia.peertopeer.base.phase.LoopSubPhase;
import aletheia.peertopeer.base.phase.RootPhase;
import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.BeltNetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.CumulationSet;
import aletheia.peertopeer.network.CumulationSet.Cumulation;
import aletheia.peertopeer.network.DeferredMessageSet;
import aletheia.peertopeer.network.InitialNetworkPhaseType;
import aletheia.peertopeer.network.LocalRouterSet;
import aletheia.peertopeer.network.NetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.RemoteRouterSet;
import aletheia.peertopeer.network.ResourceTreeNodeSet;
import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.peertopeer.resource.Resource;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.CombinedList;

public class NetworkPhase extends SubRootPhase
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final InitialNetworkPhase initialNetworkPhase;
	private final LoopNetworkPhase loopNetworkPhase;

	private RemoteRouterSet remoteRouterSet;
	private Map<Resource, List<ResourceTreeNodeSet.Action>> resourceTreeNodeSetActionMap;
	private int bindPort;

	public NetworkPhase(RootPhase rootPhase, UUID peerNodeUuid) throws IOException
	{
		super(rootPhase, peerNodeUuid);
		this.initialNetworkPhase = new InitialNetworkPhase(this);
		this.loopNetworkPhase = new LoopNetworkPhase(this);

		this.remoteRouterSet = null;
		this.resourceTreeNodeSetActionMap = null;
		this.bindPort = -1;
	}

	public RemoteRouterSet getRemoteRouterSet()
	{
		return remoteRouterSet;
	}

	public void setRemoteRouterSet(RemoteRouterSet routerSet)
	{
		setRemoteRouterSet(routerSet, Collections.<UUID> emptySet());
	}

	public void setRemoteRouterSet(RemoteRouterSet routerSet, Set<UUID> clearing)
	{
		this.remoteRouterSet = routerSet;
		getLocalRouterSet().updateRoutes(clearing);
	}

	@Override
	public UUID getNodeUuid()
	{
		return getPeerToPeerNode().getNodeUuid();
	}

	public LocalRouterSet getLocalRouterSet()
	{
		return getPeerToPeerNode().getLocalRouterSet();
	}

	public Belt getBelt()
	{
		return getPeerToPeerNode().getBelt();
	}

	public ResourceTreeNodeSet getResourceTreeNodeSet()
	{
		return getPeerToPeerNode().getResourceTreeNodeSet();
	}

	public DeferredMessageSet getDeferredMessageSet()
	{
		return getPeerToPeerNode().getDeferredMessageSet();
	}

	public CumulationSet getCumulationSet()
	{
		return getPeerToPeerNode().getCumulationSet();
	}

	public int getBindPort()
	{
		return bindPort;
	}

	protected void setBindPort(int bindPort) throws CheckConnectSocketAddressProtocolException
	{
		this.bindPort = bindPort;
		checkConnectSocketAddress();
		updateHookList();
	}

	public class CheckConnectSocketAddressProtocolException extends ProtocolException
	{
		private static final long serialVersionUID = 375689304586223816L;

		private CheckConnectSocketAddressProtocolException()
		{
			super();
		}

		private CheckConnectSocketAddressProtocolException(Throwable cause)
		{
			super(cause);
		}

	}

	private void checkConnectSocketAddress() throws CheckConnectSocketAddressProtocolException
	{
		try
		{
			if (!getPeerToPeerNode().checkConnectSocketAddress(nodeAddress()))
				throw new CheckConnectSocketAddressProtocolException();
		}
		catch (IOException | ConnectException | InterruptedException e)
		{
			throw new CheckConnectSocketAddressProtocolException(e);
		}
	}

	private void updateHookList()
	{
		InetSocketAddress bindAddress = bindAddress();
		if (bindAddress != null)
		{
			Transaction transaction = getPersistenceManager().beginTransaction();
			try
			{
				Hook.create(getPersistenceManager(), transaction, bindAddress);
				transaction.commit();
			}
			finally
			{
				transaction.abort();
			}
		}
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, Dialog.DialogStreamException
	{
		logger.debug(": starting");
		boolean redirected = false;
		try
		{
			initialNetworkPhase.run();
			if (initialNetworkPhase.isLoop())
			{
				if (initialNetworkPhase.isJoinedToNetwork())
					getPeerToPeerNode().setJoinedToNetwork();
				logger.debug("loop");
				loopNetworkPhase.run();
			}
			else
			{
				logger.debug("not loop");
				loopNetworkPhase.close();
				valedictionDialog();
				if (getGender() == PeerToPeerConnection.Gender.MALE)
				{
					NodeAddress redirect = translateRemoteNodeAddress(initialNetworkPhase.getRedirectNodeAddress());
					if (redirectedNodeAddress(redirect))
					{
						logger.debug("redirect: " + redirect.toString());
						if (!redirect.getUuid().equals(getNodeUuid()))
						{
							try
							{
								switch (initialNetworkPhase.getType())
								{
								case Joining:
									getPeerToPeerNode().joiningNetworkConnect(redirect);
									break;
								case Complementing:
									getPeerToPeerNode().complementingNetworkConnect(redirect);
									break;
								case Belt:
									getPeerToPeerNode().beltNetworkConnect(redirect,
											((BeltNetworkMalePeerToPeerConnection) getPeerToPeerConnection()).getSides());
									break;
								default:
									throw new Error();
								}
								redirected = true;
							}
							catch (ConnectException e)
							{
								logger.error("Can't connect", e);
							}
						}
					}
				}
			}
		}
		finally
		{
			if (getGender() == PeerToPeerConnection.Gender.MALE)
				if (((NetworkMalePeerToPeerConnection) getPeerToPeerConnection()).getInitialNetworkPhaseType() == InitialNetworkPhaseType.Joining)
					if (!initialNetworkPhase.isJoinedToNetwork())
						if (!redirected)
							getPeerToPeerNode().setNotJoiningToNetworkIfPending();
			disconnectFromRouteableSubMessageProcessor();
			loopNetworkPhase.close();
			logger.debug(": ending");
		}

	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		initialNetworkPhase.shutdown(fast);
		loopNetworkPhase.shutdown(fast);
	}

	public void routerSet(Collection<UUID> clearing)
	{
		loopNetworkPhase.routerSet(clearing);
	}

	public void routeableSubMessage(RouteableSubMessage routeableSubMessage)
	{
		loopNetworkPhase.routeableSubMessage(routeableSubMessage);
	}

	public void updateBindPort()
	{
		loopNetworkPhase.updateBindPort();
	}

	public InetSocketAddress bindAddress()
	{
		if (getRemoteAddress() == null)
			return null;
		else
			return new InetSocketAddress(getRemoteAddress(), getBindPort());
	}

	public NodeAddress nodeAddress()
	{
		return new NodeAddress(getPeerNodeUuid(), bindAddress());
	}

	public boolean isLastNeighbour()
	{
		return equals(getLocalRouterSet().lastNeighbour());
	}

	public void complementingInvitation(NodeAddress nodeAddress)
	{
		loopNetworkPhase.complementingInvitation(nodeAddress);
	}

	public void beltConnect(NodeAddress nodeAddress, Collection<Side> sides)
	{
		loopNetworkPhase.beltConnect(nodeAddress, sides);
	}

	public void beltConnect(NodeAddress nodeAddress, Side side)
	{
		loopNetworkPhase.beltConnect(nodeAddress, EnumSet.of(side));
	}

	public boolean routerSetNeighbour() throws InterruptedException, LoopSubPhase.CancelledCommandException
	{
		return loopNetworkPhase.routerSetNeighbour();
	}

	public boolean useful()
	{
		return getBelt().containsNeighbour(this) || getLocalRouterSet().containsNeighbour(this);
	}

	public boolean isOpen()
	{
		return loopNetworkPhase.isOpen();
	}

	private synchronized void putResourceTreeNodeActionMap(Map<Resource, List<ResourceTreeNodeSet.Action>> actionMap)
	{
		if (resourceTreeNodeSetActionMap == null)
			resourceTreeNodeSetActionMap = new HashMap<>();
		for (Map.Entry<Resource, List<ResourceTreeNodeSet.Action>> e : actionMap.entrySet())
		{
			Resource resource = e.getKey();
			List<ResourceTreeNodeSet.Action> list = resourceTreeNodeSetActionMap.get(resource);
			if (list == null)
				resourceTreeNodeSetActionMap.put(resource, e.getValue());
			else
				resourceTreeNodeSetActionMap.put(resource, new CombinedList<>(list, e.getValue()));
		}
	}

	public synchronized Map<Resource, List<ResourceTreeNodeSet.Action>> dumpResourceTreeNodeActionMap()
	{
		Map<Resource, List<ResourceTreeNodeSet.Action>> dumped = resourceTreeNodeSetActionMap;
		resourceTreeNodeSetActionMap = null;
		if (dumped == null)
			return dumped = Collections.emptyMap();
		return dumped;
	}

	public void resourceTreeNode(Map<Resource, List<ResourceTreeNodeSet.Action>> actionMap)
	{
		putResourceTreeNodeActionMap(actionMap);
		loopNetworkPhase.resourceTreeNode();
	}

	public void beltDisconnect(Collection<Side> sides)
	{
		loopNetworkPhase.beltDisconnect(sides);
	}

	public void beltDisconnect(Side side)
	{
		beltDisconnect(EnumSet.of(side));
	}

	public void propagateDeferredMessages(DeferredMessage deferredMessage)
	{
		propagateDeferredMessages(Collections.singleton(deferredMessage));
	}

	public void propagateDeferredMessages(Collection<DeferredMessage> deferredMessages)
	{
		loopNetworkPhase.propagateDeferredMessages(deferredMessages);
	}

	public void deferredMessageQueue(UUID recipientUuid, int distance)
	{
		loopNetworkPhase.deferredMessageQueue(recipientUuid, distance);
	}

	public void propagateDeferredMessageRemoval(UUID recipientUuid, Collection<UUID> deferredMessageUuids)
	{
		loopNetworkPhase.propagateDeferredMessageRemoval(recipientUuid, deferredMessageUuids);
	}

	private final static int beltCompletionTimeout = 1000;

	protected void disconnectFromRouteableSubMessageProcessor() throws InterruptedException
	{
		logger.debug("disconnectFromRouteableSubMessageProcessor");
		PeerToPeerNode peerToPeerNode = getPeerToPeerNode();
		LocalRouterSet localRouterSet = getLocalRouterSet();
		synchronized (localRouterSet)
		{
			int i = localRouterSet.dropNeighbour(this);
			if (i >= 0)
				peerToPeerNode.sendComplementingInvitation(i);
		}
		Belt belt = getBelt();
		synchronized (belt)
		{
			if (belt.dropNeighbour(this))
			{
				logger.debug("sendBeltConnect");
				peerToPeerNode.sendBeltConnect();
				belt.waitForCompletionStatus(true, beltCompletionTimeout);
			}
		}
		ResourceTreeNodeSet resourceTreeNodeSet = getResourceTreeNodeSet();
		resourceTreeNodeSet.neighbourRemoved(this);
	}

	public void updateRouterCumulationValue(int i, Cumulation.Value<?> cumulationValue)
	{
		loopNetworkPhase.updateRouterCumulationValue(i, cumulationValue);
	}

	public void removeRouterCumulationValue(int i, CumulationSet.Cumulation<?> cumulation)
	{
		loopNetworkPhase.removeRouterCumulationValue(i, cumulation);
	}

	public void requestRouterCumulationValue(int i)
	{
		loopNetworkPhase.requestRouterCumulationValue(i);
	}

	public void updateNeighbourCumulationValue(Cumulation.Value<?> cumulationValue)
	{
		loopNetworkPhase.updateNeighbourCumulationValue(cumulationValue);
	}

	protected boolean redirectedNodeAddress(NodeAddress redirectNodeAddress)
	{
		if (redirectNodeAddress == null)
			return false;
		switch (getGender())
		{
		case FEMALE:
			return !redirectNodeAddress.getUuid().equals(getNodeUuid());
		case MALE:
			return !redirectNodeAddress.getUuid().equals(getPeerNodeUuid());
		default:
			throw new Error();
		}
	}

	public NodeAddress translateRemoteNodeAddress(NodeAddress nodeAddress)
	{
		if (nodeAddress == null)
			return null;
		if (nodeAddress.getAddress() == null)
			return new NodeAddress(nodeAddress.getUuid(), bindAddress());
		else
			return nodeAddress;
	}

}
