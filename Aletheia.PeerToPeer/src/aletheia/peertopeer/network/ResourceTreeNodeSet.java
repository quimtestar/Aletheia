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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.peertopeer.resource.Resource;
import aletheia.protocol.Exportable;
import aletheia.protocol.ExportableProtocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.enumerate.ByteExportableEnum;
import aletheia.protocol.enumerate.ByteExportableEnumProtocol;
import aletheia.protocol.enumerate.ExportableEnumInfo;
import aletheia.protocol.net.InetSocketAddressProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.primitive.NullableProtocol;
import aletheia.protocol.primitive.UUIDProtocol;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.collections.BufferedList;

public class ResourceTreeNodeSet
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final LocalRouterSet localRouterSet;

	public static class Location implements Exportable
	{
		private final NodeAddress nodeAddress;
		private final Resource.Metadata resourceMetadata;

		private Location(NodeAddress nodeAddress, Resource.Metadata resourceMetadata)
		{
			super();
			this.nodeAddress = nodeAddress;
			this.resourceMetadata = resourceMetadata;
		}

		public NodeAddress getNodeAddress()
		{
			return nodeAddress;
		}

		public Resource.Metadata getResourceMetadata()
		{
			return resourceMetadata;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nodeAddress == null) ? 0 : nodeAddress.hashCode());
			result = prime * result + ((resourceMetadata == null) ? 0 : resourceMetadata.hashCode());
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
			Location other = (Location) obj;
			if (nodeAddress == null)
			{
				if (other.nodeAddress != null)
					return false;
			}
			else if (!nodeAddress.equals(other.nodeAddress))
				return false;
			if (resourceMetadata == null)
			{
				if (other.resourceMetadata != null)
					return false;
			}
			else if (!resourceMetadata.equals(other.resourceMetadata))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "[" + nodeAddress + ", " + resourceMetadata + "]";
		}

	}

	public class ResourceTreeNode
	{
		private final Resource resource;

		private class UpEntry
		{
			private int distance;
			private Location closestLocation;

			private UpEntry()
			{
				this.distance = -1;
				this.closestLocation = null;
			}

			public int getDistance()
			{
				return distance;
			}

			public Location getClosestLocation()
			{
				return closestLocation;
			}

		}

		private final Map<NetworkPhase, UpEntry> upEntries;
		private Resource.Metadata localResourceMetadata;
		private int distance;
		private NetworkPhase closestUp;
		private NetworkPhase down;
		private Location closestLocation;
		private Location nextLocation;

		private ResourceTreeNode(Resource resource)
		{
			this.resource = resource;
			this.upEntries = new HashMap<>();
			this.localResourceMetadata = null;
			this.distance = -1;
			this.closestUp = null;
			this.closestLocation = null;
			this.nextLocation = null;
			updateDown();
		}

		public Resource getResource()
		{
			return resource;
		}

		public Map<NetworkPhase, UpEntry> getUpEntries()
		{
			return Collections.unmodifiableMap(upEntries);
		}

		private void removeUp(NetworkPhase networkPhase)
		{
			UpEntry e = upEntries.remove(networkPhase);
			if (e != null)
			{
				logger.trace("ResourceTreeNodeSet.ResourceTreeNode.removeUp() -> node: " + localRouterSet.getNodeUuid() + "      resource: "
						+ resource.getUuid() + "     removed: " + networkPhase.getPeerNodeUuid());
				updateInfo();
			}
		}

		private void updateUp(NetworkPhase networkPhase, Action.UpdateUp updateUp)
		{
			UpEntry e = upEntries.get(networkPhase);
			if (e == null)
			{
				e = new UpEntry();
				upEntries.put(networkPhase, e);
				logger.trace("ResourceTreeNodeSet.ResourceTreeNode.updateUp() -> node: " + localRouterSet.getNodeUuid() + "     resource: " + resource.getUuid()
						+ "      added: " + networkPhase.getPeerNodeUuid() + "    updateUp: " + updateUp);
				putPendingUpdateNextLocationAction();
			}
			if (updateUp instanceof Action.UpdateUp.Distance)
			{
				Action.UpdateUp.Distance distance = (Action.UpdateUp.Distance) updateUp;
				if (e.distance != distance.distance)
				{
					e.distance = distance.distance;
					updateInfo();
				}
			}
			else if (updateUp instanceof Action.UpdateUp.ClosestResource)
			{
				Action.UpdateUp.ClosestResource closestResource = (Action.UpdateUp.ClosestResource) updateUp;
				if (closestResource.nodeAddress == null)
					closestResource.nodeAddress = networkPhase.bindAddress();
				Location closestLocation = closestResource.location();
				if (!closestLocation.equals(e.closestLocation))
				{
					e.closestLocation = closestLocation;
					updateInfo();
				}
			}
			else
				throw new Error();
		}

		private void updateNextLocation(NetworkPhase networkPhase, Action.UpdateNextLocation updateNextLocation)
		{
			if (updateNextLocation.nodeAddress == null)
				updateNextLocation.nodeAddress = networkPhase.bindAddress();
			Location nextLocation = updateNextLocation.location();
			if (!nextLocation.equals(this.nextLocation) && !nextLocation.getNodeAddress().getUuid().equals(getNodeUuid()))
			{
				this.nextLocation = nextLocation;
				if (closestUp != null)
					putPendingAction(closestUp, resource, new Action.UpdateNextLocation(nextLocation));
				for (Listener listener : listeners)
					listener.updatedNextLocation(nextLocation);
			}
		}

		private void removeNextLocation()
		{
			if (nextLocation != null)
			{
				nextLocation = null;
				if (closestUp != null)
					putPendingAction(closestUp, resource, new Action.RemoveNextLocation());
				for (Listener listener : listeners)
					listener.removedNextLocation(resource);
			}
		}

		public Resource.Metadata getLocalResourceMetadata()
		{
			return localResourceMetadata;
		}

		public boolean isLocal()
		{
			return localResourceMetadata != null;
		}

		private void setLocalMetadata(Resource.Metadata localMetadata)
		{
			Resource.Metadata oldLocalMetadata = this.localResourceMetadata;
			this.localResourceMetadata = localMetadata;
			if ((localMetadata == null && oldLocalMetadata != null) || (localMetadata != null && !localMetadata.equals(oldLocalMetadata)))
			{
				if ((localMetadata == null) != (oldLocalMetadata == null))
					updateInfo();
				if (localMetadata != null)
					updateLocalMetadata();
				for (Listener listener : listeners)
				{
					if (localMetadata != null)
						listener.setLocalMetadata(localMetadata);
					else
						listener.clearLocalMetadata(resource);
				}
			}

		}

		public int getDistance()
		{
			return distance;
		}

		public NetworkPhase getClosestUp()
		{
			return closestUp;
		}

		public NetworkPhase getDown()
		{
			return down;
		}

		public Location getClosestLocation()
		{
			return closestLocation;
		}

		public Location getNextLocation()
		{
			return nextLocation;
		}

		private void putPendingUpdateNextLocationAction()
		{
			for (NetworkPhase up : upEntries.keySet())
			{
				if (isLocal())
					putPendingAction(up, resource, new Action.UpdateNextLocation(localResourceMetadata, getNodeUuid()));
				else if (up == closestUp)
				{
					if (nextLocation != null)
						putPendingAction(up, resource, new Action.UpdateNextLocation(nextLocation));
					else
						putPendingAction(up, resource, new Action.RemoveNextLocation());
				}
				else if (closestLocation != null)
					putPendingAction(up, resource, new Action.UpdateNextLocation(closestLocation));
			}
		}

		private void updateInfo()
		{
			int oldDistance = distance;
			NetworkPhase oldClosestUp = closestUp;
			Location oldClosestLocation = closestLocation;
			if (isLocal())
			{
				distance = 0;
				closestUp = null;
				closestLocation = null;
				if (oldDistance != distance)
					putPendingAction(down, resource, new Action.UpdateUp.Distance(distance));
				if (oldClosestLocation != null)
				{
					putPendingAction(down, resource, new Action.UpdateUp.ClosestResource(localResourceMetadata, getNodeUuid()));
					putPendingUpdateNextLocationAction();
					for (Listener listener : listeners)
						listener.removedClosestLocation(resource);
				}
			}
			else
			{
				if (upEntries.isEmpty())
				{
					removeResourceTreeNode(resource);
					putPendingAction(down, resource, new Action.RemoveUp());
				}
				else
				{
					UpEntry closestUpEntry = null;
					for (Map.Entry<NetworkPhase, UpEntry> e : upEntries.entrySet())
					{
						if (e.getValue().getDistance() >= 0 && (closestUpEntry == null || e.getValue().getDistance() < closestUpEntry.getDistance()))
						{
							closestUpEntry = e.getValue();
							closestUp = e.getKey();
						}
					}
					if (closestUpEntry != null)
					{
						distance = closestUpEntry.distance + 1;
						closestLocation = closestUpEntry.getClosestLocation();
						if (distance != oldDistance)
							putPendingAction(down, resource, new Action.UpdateUp.Distance(distance));
						if (closestUp != oldClosestUp || (closestLocation != null && !closestLocation.equals(oldClosestLocation)))
							putPendingUpdateNextLocationAction();
						if (closestLocation != null && !closestLocation.equals(oldClosestLocation))
						{
							putPendingAction(down, resource, new Action.UpdateUp.ClosestResource(closestLocation));
							for (Listener listener : listeners)
								listener.updatedClosestLocation(closestLocation);
						}
					}
				}
			}
		}

		private void updateLocalMetadata()
		{
			putPendingAction(down, resource, new Action.UpdateUp.ClosestResource(localResourceMetadata, getNodeUuid()));
			putPendingUpdateNextLocationAction();
		}

		private void updateDown()
		{
			synchronized (localRouterSet)
			{
				Set<NetworkPhase> pathStepMultiple = localRouterSet.pathStepMultiple(resource.getUuid());
				if ((down == null && !pathStepMultiple.isEmpty()) || (down != null && !pathStepMultiple.contains(down)))
				{
					NetworkPhase oldDown = down;
					down = localRouterSet.pathStep(resource.getUuid());
					logger.trace("ResourceTreeNodeSet.ResourceTreeNode.updateDown() -> node: " + localRouterSet.getNodeUuid() + "      resource: "
							+ resource.getUuid() + "       oldDown: " + (oldDown == null ? null : oldDown.getPeerNodeUuid()) + "       down: "
							+ (down == null ? null : down.getPeerNodeUuid()));
					if (down != oldDown)
					{
						putPendingAction(oldDown, resource, new Action.RemoveUp());
						putPendingAction(down, resource, new Action.UpdateUp.Distance(distance));
						Action.UpdateUp u = null;
						if (isLocal())
						{
							if (localResourceMetadata != null)
								u = new Action.UpdateUp.ClosestResource(localResourceMetadata, getNodeUuid());
						}
						else
						{
							if (closestLocation != null)
								u = new Action.UpdateUp.ClosestResource(closestLocation);
						}
						if (u != null)
							putPendingAction(down, resource, u);
					}
					if (down == null)
						removeNextLocation();
				}
			}
		}

	}

	private final Map<Resource, ResourceTreeNode> resourceTreeNodeMap;

	private final Set<Resource> localResourceSet;

	public static abstract class Action implements Exportable
	{
		private Action()
		{
		}

		public static abstract class UpdateUp extends Action
		{
			private UpdateUp()
			{
			}

			public static class Distance extends UpdateUp
			{
				protected final int distance;

				private Distance(int distance)
				{
					this.distance = distance;
				}

				@Override
				public String toString()
				{
					return "Action.UpdateUp.Distance [distance=" + distance + "]";
				}

			}

			public static class ClosestResource extends UpdateUp
			{
				protected final Resource.Metadata metadata;
				protected final UUID nodeUuid;
				protected InetSocketAddress nodeAddress;

				private ClosestResource(Resource.Metadata metadata, UUID nodeUuid, InetSocketAddress nodeAddress)
				{
					this.metadata = metadata;
					this.nodeUuid = nodeUuid;
					this.nodeAddress = nodeAddress;
				}

				private ClosestResource(Resource.Metadata metadata, UUID nodeUuid)
				{
					this(metadata, nodeUuid, null);
				}

				private ClosestResource(Resource.Metadata metadata, NodeAddress nodeAddress)
				{
					this(metadata, nodeAddress.getUuid(), nodeAddress.getAddress());
				}

				private ClosestResource(Location location)
				{
					this(location.getResourceMetadata(), location.getNodeAddress());
				}

				protected NodeAddress nodeAddress()
				{
					return new NodeAddress(nodeUuid, nodeAddress);
				}

				protected Location location()
				{
					return new Location(nodeAddress(), metadata);
				}

				@Override
				public String toString()
				{
					return "ClosestResource [metadata=" + metadata + ", nodeUuid=" + nodeUuid + ", nodeAddress=" + nodeAddress + "]";
				}

			}

		}

		public static class RemoveUp extends Action
		{
			private RemoveUp()
			{
			}

			@Override
			public String toString()
			{
				return "Action.RemoveUp []";
			}

		}

		public static class UpdateNextLocation extends Action
		{
			protected final Resource.Metadata metadata;
			protected final UUID nodeUuid;
			protected InetSocketAddress nodeAddress;

			private UpdateNextLocation(Resource.Metadata metadata, UUID nodeUuid, InetSocketAddress nodeAddress)
			{
				this.metadata = metadata;
				this.nodeUuid = nodeUuid;
				this.nodeAddress = nodeAddress;
			}

			private UpdateNextLocation(Resource.Metadata metadata, UUID nodeUuid)
			{
				this(metadata, nodeUuid, null);
			}

			private UpdateNextLocation(Resource.Metadata metadata, NodeAddress nodeAddress)
			{
				this(metadata, nodeAddress.getUuid(), nodeAddress.getAddress());
			}

			private UpdateNextLocation(Location location)
			{
				this(location.getResourceMetadata(), location.getNodeAddress());
			}

			protected NodeAddress nodeAddress()
			{
				return new NodeAddress(nodeUuid, nodeAddress);
			}

			protected Location location()
			{
				return new Location(nodeAddress(), metadata);
			}

			@Override
			public String toString()
			{
				return "UpdateNextLocation [metadata=" + metadata + ", nodeUuid=" + nodeUuid + ", nodeAddress=" + nodeAddress + "]";
			}

		}

		public static class RemoveNextLocation extends Action
		{
			private RemoveNextLocation()
			{
			}

			@Override
			public String toString()
			{
				return "Action.RemoveNextLocation []";
			}

		}

		@ProtocolInfo(availableVersions = 0)
		public static class Protocol extends ExportableProtocol<Action>
		{
			@ExportableEnumInfo(availableVersions = 0)
			private enum ActionType implements ByteExportableEnum<ActionType>
			{
				// @formatter:off
				UpdateUp((byte) 0),
				RemoveUp((byte) 1),
				UpdateNextLocation((byte) 2),
				RemoveNextLocation((byte) 3),
				;
				// @formatter:on

				private final byte code;

				private ActionType(byte code)
				{
					this.code = code;
				}

				@Override
				public Byte getCode(int version)
				{
					return code;
				}

			}

			@ExportableEnumInfo(availableVersions = 0)
			private enum UpdateUpActionType implements ByteExportableEnum<UpdateUpActionType>
			{
				// @formatter:off
				Distance((byte) 0),
				ClosestResource((byte) 1),
				;
				// @formatter:on

				private final byte code;

				private UpdateUpActionType(byte code)
				{
					this.code = code;
				}

				@Override
				public Byte getCode(int version)
				{
					return code;
				}
			}

			private final ByteExportableEnumProtocol<ActionType> actionTypeProtocol = new ByteExportableEnumProtocol<>(0, ActionType.class, 0);
			private final ByteExportableEnumProtocol<UpdateUpActionType> updateUpActionTypeProtocol = new ByteExportableEnumProtocol<>(0,
					UpdateUpActionType.class, 0);
			private final IntegerProtocol integerProtocol = new IntegerProtocol(0);
			private final Resource.Metadata.Protocol resourceMetadataProtocol = new Resource.Metadata.Protocol(0);
			private final UUIDProtocol uuidProtocol = new UUIDProtocol(0);
			private final InetSocketAddressProtocol inetSocketAddressProtocol = new InetSocketAddressProtocol(0);
			private final NullableProtocol<InetSocketAddress> nullableInetSocketAddressProtocol = new NullableProtocol<>(0, inetSocketAddressProtocol);

			public Protocol(int requiredVersion)
			{
				super(0);
				checkVersionAvailability(Protocol.class, requiredVersion);
			}

			@Override
			public void send(DataOutput out, Action a) throws IOException
			{
				if (a instanceof UpdateUp)
				{
					actionTypeProtocol.send(out, ActionType.UpdateUp);
					UpdateUp u = (UpdateUp) a;
					if (u instanceof UpdateUp.Distance)
					{
						updateUpActionTypeProtocol.send(out, UpdateUpActionType.Distance);
						UpdateUp.Distance d = (UpdateUp.Distance) u;
						integerProtocol.send(out, d.distance);
					}
					else if (u instanceof UpdateUp.ClosestResource)
					{
						updateUpActionTypeProtocol.send(out, UpdateUpActionType.ClosestResource);
						UpdateUp.ClosestResource c = (UpdateUp.ClosestResource) u;
						resourceMetadataProtocol.send(out, c.metadata);
						uuidProtocol.send(out, c.nodeUuid);
						nullableInetSocketAddressProtocol.send(out, c.nodeAddress);
					}
					else
						throw new Error();
				}
				else if (a instanceof RemoveUp)
					actionTypeProtocol.send(out, ActionType.RemoveUp);
				else if (a instanceof UpdateNextLocation)
				{
					actionTypeProtocol.send(out, ActionType.UpdateNextLocation);
					UpdateNextLocation u = (UpdateNextLocation) a;
					resourceMetadataProtocol.send(out, u.metadata);
					uuidProtocol.send(out, u.nodeUuid);
					nullableInetSocketAddressProtocol.send(out, u.nodeAddress);
				}
				else if (a instanceof RemoveNextLocation)
					actionTypeProtocol.send(out, ActionType.RemoveNextLocation);
				else
					throw new Error();
			}

			@Override
			public Action recv(DataInput in) throws IOException, ProtocolException
			{
				ActionType actionType = actionTypeProtocol.recv(in);
				switch (actionType)
				{
				case UpdateUp:
				{
					UpdateUpActionType updateUpActionType = updateUpActionTypeProtocol.recv(in);
					switch (updateUpActionType)
					{
					case Distance:
					{
						int distance = integerProtocol.recv(in);
						return new UpdateUp.Distance(distance);
					}
					case ClosestResource:
					{
						Resource.Metadata metadata = resourceMetadataProtocol.recv(in);
						UUID nodeUuid = uuidProtocol.recv(in);
						InetSocketAddress nodeAddress = nullableInetSocketAddressProtocol.recv(in);
						return new UpdateUp.ClosestResource(metadata, nodeUuid, nodeAddress);
					}
					default:
						throw new Error();
					}
				}
				case RemoveUp:
					return new RemoveUp();
				case UpdateNextLocation:
				{
					Resource.Metadata metadata = resourceMetadataProtocol.recv(in);
					UUID nodeUuid = uuidProtocol.recv(in);
					InetSocketAddress nodeAddress = nullableInetSocketAddressProtocol.recv(in);
					return new UpdateNextLocation(metadata, nodeUuid, nodeAddress);
				}
				case RemoveNextLocation:
					return new RemoveNextLocation();
				default:
					throw new Error();
				}
			}

			@Override
			public void skip(DataInput in) throws IOException, ProtocolException
			{
				ActionType actionType = actionTypeProtocol.recv(in);
				switch (actionType)
				{
				case UpdateUp:
				{
					UpdateUpActionType updateUpActionType = updateUpActionTypeProtocol.recv(in);
					switch (updateUpActionType)
					{
					case Distance:
					{
						integerProtocol.skip(in);
						break;
					}
					case ClosestResource:
					{
						resourceMetadataProtocol.skip(in);
						uuidProtocol.skip(in);
						nullableInetSocketAddressProtocol.skip(in);
					}
					default:
						throw new Error();
					}
				}
				case RemoveUp:
					break;
				case UpdateNextLocation:
				{
					resourceMetadataProtocol.skip(in);
					uuidProtocol.skip(in);
					nullableInetSocketAddressProtocol.skip(in);
				}
				case RemoveNextLocation:
					break;
				default:
					throw new Error();
				}
			}
		}
	}

	private final Map<NetworkPhase, Map<Resource, List<Action>>> pendingActionsMap;

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
					updateLocalRouterSet();

				}
			});
		}
	}

	public interface Listener
	{
		void setLocalMetadata(Resource.Metadata metadata);

		void clearLocalMetadata(Resource resource);

		void updatedClosestLocation(Location nextLocation);

		void removedClosestLocation(Resource resource);

		void updatedNextLocation(Location nextLocation);

		void removedNextLocation(Resource resource);

	}

	private final Set<Listener> listeners;

	public ResourceTreeNodeSet(LocalRouterSet localRouterSet)
	{
		this.localRouterSet = localRouterSet;
		this.resourceTreeNodeMap = new HashMap<>();
		this.localResourceSet = new HashSet<>();
		this.pendingActionsMap = new HashMap<>();
		this.listeners = new HashSet<>();
		localRouterSet.addListener(new LocalRouterSetListener());
	}

	public LocalRouterSet getLocalRouterSet()
	{
		return localRouterSet;
	}

	public UUID getNodeUuid()
	{
		return localRouterSet.getNodeUuid();
	}

	public synchronized Map<Resource, ResourceTreeNode> getResourceTreeNodeMap()
	{
		return Collections.unmodifiableMap(resourceTreeNodeMap);
	}

	public synchronized ResourceTreeNode getResourceTreeNode(Resource resource)
	{
		return resourceTreeNodeMap.get(resource);
	}

	private synchronized ResourceTreeNode getOrCreateResourceTreeNode(Resource resource)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resource);
		if (resourceTreeNode == null)
		{
			resourceTreeNode = new ResourceTreeNode(resource);
			resourceTreeNodeMap.put(resource, resourceTreeNode);
		}
		return resourceTreeNode;
	}

	private synchronized void removeResourceTreeNode(Resource resource)
	{
		resourceTreeNodeMap.remove(resource);
	}

	public synchronized void putLocalResource(Resource.Metadata resourceMetadata)
	{
		localResourceSet.add(resourceMetadata.getResource());
		ResourceTreeNode resourceTreeNode = getOrCreateResourceTreeNode(resourceMetadata.getResource());
		resourceTreeNode.setLocalMetadata(resourceMetadata);
		actions();
	}

	public synchronized boolean removeLocalResource(Resource resource)
	{
		boolean removed = localResourceSet.remove(resource);
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resource);
		if (resourceTreeNode == null)
			return removed;
		resourceTreeNode.setLocalMetadata(null);
		actions();
		return removed;
	}

	public synchronized Set<Resource> getLocalResourceSet()
	{
		return Collections.unmodifiableSet(localResourceSet);
	}

	public synchronized void clearLocalResources()
	{
		for (Resource resourceMetadata : new BufferedList<>(localResourceSet))
			removeLocalResource(resourceMetadata);
	}

	public synchronized boolean isLocalResource(Resource resource)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resource);
		if (resourceTreeNode == null)
			return false;
		return resourceTreeNode.isLocal();
	}

	public synchronized Resource.Metadata getLocalResourceMetadata(Resource resource)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resource);
		if (resourceTreeNode == null)
			return null;
		return resourceTreeNode.getLocalResourceMetadata();
	}

	public synchronized Location getNextLocation(Resource resource)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resource);
		if (resourceTreeNode == null)
			return null;
		return resourceTreeNode.getNextLocation();
	}

	public synchronized void addListener(Listener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeListener(Listener listener)
	{
		listeners.remove(listener);
	}

	private synchronized void removeUp(Resource resourceMetadata, NetworkPhase networkPhase, Action.RemoveUp removeUp)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resourceMetadata);
		if (resourceTreeNode != null)
			resourceTreeNode.removeUp(networkPhase);
	}

	private synchronized void updateUp(Resource resource, NetworkPhase networkPhase, Action.UpdateUp updateUp)
	{
		ResourceTreeNode resourceTreeNode = getOrCreateResourceTreeNode(resource);
		resourceTreeNode.updateUp(networkPhase, updateUp);
	}

	private synchronized void updateNextLocation(Resource resource, NetworkPhase networkPhase, Action.UpdateNextLocation updateNextLocation)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resource);
		if (resourceTreeNode != null)
			resourceTreeNode.updateNextLocation(networkPhase, updateNextLocation);
	}

	private synchronized void removeNextLocation(Resource resourceMetadata, NetworkPhase networkPhase, Action.RemoveNextLocation removeNextLocation)
	{
		ResourceTreeNode resourceTreeNode = getResourceTreeNode(resourceMetadata);
		if (resourceTreeNode != null)
			resourceTreeNode.removeNextLocation();
	}

	public synchronized void runActionMap(NetworkPhase networkPhase, Map<Resource, List<Action>> actionMap)
	{
		for (Map.Entry<Resource, List<Action>> e : actionMap.entrySet())
		{
			Resource resource = e.getKey();
			for (Action action : e.getValue())
			{
				logger.trace("ResourceTreeNodeSet.runActionMap() -> " + "   node: " + localRouterSet.getNodeUuid() + "   neighbour: "
						+ networkPhase.getPeerNodeUuid() + "   action: " + action);
				if (action instanceof Action.UpdateUp)
					updateUp(resource, networkPhase, (Action.UpdateUp) action);
				else if (action instanceof Action.RemoveUp)
					removeUp(resource, networkPhase, (Action.RemoveUp) action);
				else if (action instanceof Action.UpdateNextLocation)
					updateNextLocation(resource, networkPhase, (Action.UpdateNextLocation) action);
				else if (action instanceof Action.RemoveNextLocation)
					removeNextLocation(resource, networkPhase, (Action.RemoveNextLocation) action);
				else
					throw new Error();
			}
		}
		actions();
	}

	private synchronized void putPendingAction(NetworkPhase networkPhase, Resource resource, Action action)
	{
		if (networkPhase != null)
		{
			Map<Resource, List<Action>> resourceMap = pendingActionsMap.get(networkPhase);
			if (resourceMap == null)
			{
				resourceMap = new HashMap<>();
				pendingActionsMap.put(networkPhase, resourceMap);
			}
			List<Action> list = resourceMap.get(resource);
			if (list == null)
			{
				list = new ArrayList<>();
				resourceMap.put(resource, list);
			}
			list.add(action);
		}
	}

	private synchronized void updateLocalRouterSet()
	{
		for (ResourceTreeNode resourceTreeNode : resourceTreeNodeMap.values())
			resourceTreeNode.updateDown();
		actions();
	}

	private synchronized void actions()
	{
		for (Map.Entry<NetworkPhase, Map<Resource, List<Action>>> e : pendingActionsMap.entrySet())
		{
			NetworkPhase networkPhase = e.getKey();
			networkPhase.resourceTreeNode(e.getValue());
		}
		pendingActionsMap.clear();
	}

	public synchronized void neighbourRemoved(NetworkPhase networkPhase)
	{
		for (ResourceTreeNode resourceTreeNode : new BufferedList<>(resourceTreeNodeMap.values()))
			resourceTreeNode.removeUp(networkPhase);
		actions();
	}

	public synchronized void updateLocalResourcesMetadata()
	{
		for (Resource resource : localResourceSet)
			resourceTreeNodeMap.get(resource).updateLocalMetadata();
		actions();
	}
}
