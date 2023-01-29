/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.peertopeer.DeferredMessage;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.LoopSubPhase;
import aletheia.peertopeer.base.phase.LoopSubPhase.CancelledCommandException;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.CumulationSet;
import aletheia.peertopeer.network.CumulationSet.Cumulation;
import aletheia.peertopeer.network.LocalRouterSet;
import aletheia.peertopeer.network.RouteableSubMessageProcessor;
import aletheia.peertopeer.network.dialog.BeltConnectDialogClient;
import aletheia.peertopeer.network.dialog.BeltConnectDialogServer;
import aletheia.peertopeer.network.dialog.BeltDisconnectDialogClient;
import aletheia.peertopeer.network.dialog.BeltDisconnectDialogServer;
import aletheia.peertopeer.network.dialog.ComplementingInvitationDialogClient;
import aletheia.peertopeer.network.dialog.ComplementingInvitationDialogServer;
import aletheia.peertopeer.network.dialog.DeferredMessageQueueDialogClient;
import aletheia.peertopeer.network.dialog.DeferredMessageQueueDialogServer;
import aletheia.peertopeer.network.dialog.LoopNetworkDialogType;
import aletheia.peertopeer.network.dialog.LoopNetworkDialogTypeDialogActive;
import aletheia.peertopeer.network.dialog.LoopNetworkDialogTypeDialogPassive;
import aletheia.peertopeer.network.dialog.PropagateDeferredMessageRemovalDialogClient;
import aletheia.peertopeer.network.dialog.PropagateDeferredMessageRemovalDialogServer;
import aletheia.peertopeer.network.dialog.PropagateDeferredMessagesDialogClient;
import aletheia.peertopeer.network.dialog.PropagateDeferredMessagesDialogServer;
import aletheia.peertopeer.network.dialog.RemoveRouterCumulationValueDialogClient;
import aletheia.peertopeer.network.dialog.RemoveRouterCumulationValueDialogServer;
import aletheia.peertopeer.network.dialog.RequestRouterCumulationValueDialogClient;
import aletheia.peertopeer.network.dialog.RequestRouterCumulationValueDialogServer;
import aletheia.peertopeer.network.dialog.ResourceTreeNodeLoopDialogClient;
import aletheia.peertopeer.network.dialog.ResourceTreeNodeLoopDialogServer;
import aletheia.peertopeer.network.dialog.RouteableMessageDialogClient;
import aletheia.peertopeer.network.dialog.RouteableMessageDialogServer;
import aletheia.peertopeer.network.dialog.RouterSetLoopDialogClient;
import aletheia.peertopeer.network.dialog.RouterSetLoopDialogServer;
import aletheia.peertopeer.network.dialog.RouterSetNeighbourDialog;
import aletheia.peertopeer.network.dialog.UpdateBindPortLoopDialogClient;
import aletheia.peertopeer.network.dialog.UpdateBindPortLoopDialogServer;
import aletheia.peertopeer.network.dialog.UpdateNeighbourCumulationValueDialogClient;
import aletheia.peertopeer.network.dialog.UpdateNeighbourCumulationValueDialogServer;
import aletheia.peertopeer.network.dialog.UpdateRouterCumulationValueDialogClient;
import aletheia.peertopeer.network.dialog.UpdateRouterCumulationValueDialogServer;
import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.message.routeablesubmessage.RouteableSubMessage;
import aletheia.protocol.ProtocolException;

public class LoopNetworkPhase extends NetworkSubPhase
{
	private final static Logger logger = LoggerManager.instance.logger();
	private final static int closestConnectRetryTime = 500;

	private class LoopNetworkSubPhase extends LoopSubPhase<LoopNetworkDialogType>
	{

		protected abstract class Command<C extends Command<C>> extends LoopSubPhase<LoopNetworkDialogType>.Command<C>
		{
			public Command(LoopNetworkDialogType loopDialogType)
			{
				super(loopDialogType);
			}

		}

		protected class BeltConnectCommand extends Command<BeltConnectCommand>
		{
			private final NodeAddress nodeAddress;
			private final Collection<Side> sides;

			public BeltConnectCommand(NodeAddress nodeAddress, Collection<Side> sides)
			{
				super(LoopNetworkDialogType.BeltConnect);
				this.nodeAddress = nodeAddress;
				this.sides = sides;
			}

			public NodeAddress getNodeAddress()
			{
				return nodeAddress;
			}

			public Collection<Side> getSides()
			{
				return sides;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((nodeAddress == null) ? 0 : nodeAddress.hashCode());
				result = prime * result + ((sides == null) ? 0 : sides.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				BeltConnectCommand other = (BeltConnectCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (nodeAddress == null)
				{
					if (other.nodeAddress != null)
						return false;
				}
				else if (!nodeAddress.equals(other.nodeAddress))
					return false;
				if (sides == null)
				{
					if (other.sides != null)
						return false;
				}
				else if (!sides.equals(other.sides))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class BeltDisconnectCommand extends Command<BeltDisconnectCommand>
		{
			private final Collection<Side> sides;

			public BeltDisconnectCommand(Collection<Side> sides)
			{
				super(LoopNetworkDialogType.BeltDisconnect);
				this.sides = sides;
			}

			protected Collection<Side> getSides()
			{
				return sides;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((sides == null) ? 0 : sides.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				BeltDisconnectCommand other = (BeltDisconnectCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (sides == null)
				{
					if (other.sides != null)
						return false;
				}
				else if (!sides.equals(other.sides))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class UpdateBindPortCommand extends Command<UpdateBindPortCommand>
		{

			public UpdateBindPortCommand()
			{
				super(LoopNetworkDialogType.UpdateBindPort);
			}

		}

		protected class ComplementingInvitationCommand extends Command<ComplementingInvitationCommand>
		{
			private final NodeAddress nodeAddress;

			public ComplementingInvitationCommand(NodeAddress nodeAddress)
			{
				super(LoopNetworkDialogType.ComplementingInvitation);
				this.nodeAddress = nodeAddress;
			}

			public NodeAddress getNodeAddress()
			{
				return nodeAddress;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((nodeAddress == null) ? 0 : nodeAddress.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				ComplementingInvitationCommand other = (ComplementingInvitationCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (nodeAddress == null)
				{
					if (other.nodeAddress != null)
						return false;
				}
				else if (!nodeAddress.equals(other.nodeAddress))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class RouteableMessageCommand extends Command<RouteableMessageCommand>
		{

			public RouteableMessageCommand()
			{
				super(LoopNetworkDialogType.RouteableMessage);
			}

			@Override
			public synchronized void cancel(Throwable cancelCause)
			{
				super.cancel(cancelCause);
				resendSendingRouteableSubMessages();
			}

		}

		protected class RouterSetCommand extends Command<RouterSetCommand>
		{
			private final Set<UUID> clearing;

			public RouterSetCommand(Collection<UUID> clearing)
			{
				super(LoopNetworkDialogType.RouterSet);
				this.clearing = new HashSet<>(clearing);
			}

			public Set<UUID> getClearing()
			{
				return Collections.unmodifiableSet(clearing);
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((clearing == null) ? 0 : clearing.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				RouterSetCommand other = (RouterSetCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (clearing == null)
				{
					if (other.clearing != null)
						return false;
				}
				else if (!clearing.equals(other.clearing))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class RouterSetNeighbourCommand extends Command<RouterSetNeighbourCommand>
		{
			public class Result extends Command<RouterSetNeighbourCommand>.Result
			{
				private final boolean put;

				private Result(boolean put)
				{
					super();
					this.put = put;
				}

				public boolean isPut()
				{
					return put;
				}

			}

			public RouterSetNeighbourCommand()
			{
				super(LoopNetworkDialogType.RouterSetNeighbour);
			}
		}

		protected class PropagateDeferredMessagesCommand extends Command<PropagateDeferredMessagesCommand>
		{
			private final Collection<DeferredMessage> deferredMessages;

			public PropagateDeferredMessagesCommand(Collection<DeferredMessage> deferredMessages)
			{
				super(LoopNetworkDialogType.PropagateDeferredMessages);
				this.deferredMessages = deferredMessages;
			}

			public Collection<DeferredMessage> getDeferredMessages()
			{
				return deferredMessages;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((deferredMessages == null) ? 0 : deferredMessages.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				PropagateDeferredMessagesCommand other = (PropagateDeferredMessagesCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (deferredMessages == null)
				{
					if (other.deferredMessages != null)
						return false;
				}
				else if (!deferredMessages.equals(other.deferredMessages))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class DeferredMessageQueueCommand extends Command<DeferredMessageQueueCommand>
		{
			private final UUID recipientUuid;
			private final int distance;

			public DeferredMessageQueueCommand(UUID recipientUuid, int distance)
			{
				super(LoopNetworkDialogType.DeferredMessageQueue);
				this.recipientUuid = recipientUuid;
				this.distance = distance;
			}

			public UUID getRecipientUuid()
			{
				return recipientUuid;
			}

			public int getDistance()
			{
				return distance;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + distance;
				result = prime * result + ((recipientUuid == null) ? 0 : recipientUuid.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				DeferredMessageQueueCommand other = (DeferredMessageQueueCommand) obj;
				if (!getOuterType().equals(other.getOuterType()) || (distance != other.distance))
					return false;
				if (recipientUuid == null)
				{
					if (other.recipientUuid != null)
						return false;
				}
				else if (!recipientUuid.equals(other.recipientUuid))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class PropagateDeferredMessageRemovalCommand extends Command<PropagateDeferredMessageRemovalCommand>
		{
			private final UUID recipientUuid;
			private final Collection<UUID> deferredMessageUuids;

			public PropagateDeferredMessageRemovalCommand(UUID recipientUuid, Collection<UUID> deferredMessageUuids)
			{
				super(LoopNetworkDialogType.PropagateDeferredMessageRemoval);
				this.recipientUuid = recipientUuid;
				this.deferredMessageUuids = deferredMessageUuids;
			}

			public UUID getRecipientUuid()
			{
				return recipientUuid;
			}

			public Collection<UUID> getDeferredMessageUuids()
			{
				return deferredMessageUuids;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((deferredMessageUuids == null) ? 0 : deferredMessageUuids.hashCode());
				result = prime * result + ((recipientUuid == null) ? 0 : recipientUuid.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				PropagateDeferredMessageRemovalCommand other = (PropagateDeferredMessageRemovalCommand) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (deferredMessageUuids == null)
				{
					if (other.deferredMessageUuids != null)
						return false;
				}
				else if (!deferredMessageUuids.equals(other.deferredMessageUuids))
					return false;
				if (recipientUuid == null)
				{
					if (other.recipientUuid != null)
						return false;
				}
				else if (!recipientUuid.equals(other.recipientUuid))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class ValedictionCommand extends Command<ValedictionCommand>
		{
			public ValedictionCommand()
			{
				super(LoopNetworkDialogType.Valediction);
			}

		}

		protected class ResourceTreeNodeCommand extends Command<ResourceTreeNodeCommand>
		{
			public ResourceTreeNodeCommand()
			{
				super(LoopNetworkDialogType.ResourceTreeNode);
			}
		}

		protected abstract class UpdateCumulationValueCommand<C extends UpdateCumulationValueCommand<C>> extends Command<C>
		{
			private final Cumulation.Value<?> cumulationValue;

			public UpdateCumulationValueCommand(LoopNetworkDialogType loopDialogType, Cumulation.Value<?> cumulationValue)
			{
				super(loopDialogType);
				this.cumulationValue = cumulationValue;
			}

			public Cumulation.Value<?> getCumulationValue()
			{
				return cumulationValue;
			}

			@Override
			public int hashCode()
			{
				return System.identityHashCode(this);
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				return false;
			}

		}

		protected class UpdateRouterCumulationValueCommand extends UpdateCumulationValueCommand<UpdateRouterCumulationValueCommand>
		{
			private final int index;

			public UpdateRouterCumulationValueCommand(int index, Cumulation.Value<?> cumulationValue)
			{
				super(LoopNetworkDialogType.UpdateRouterCumulationValue, cumulationValue);
				this.index = index;
			}

			public int getIndex()
			{
				return index;
			}

		}

		protected class UpdateNeighbourCumulationValueCommand extends UpdateCumulationValueCommand<UpdateNeighbourCumulationValueCommand>
		{

			public UpdateNeighbourCumulationValueCommand(Cumulation.Value<?> cumulationValue)
			{
				super(LoopNetworkDialogType.UpdateNeighbourCumulationValue, cumulationValue);
			}

		}

		protected abstract class RemoveCumulationValueCommand<C extends RemoveCumulationValueCommand<C>> extends Command<C>
		{
			private final CumulationSet.Cumulation<?> cumulation;

			public RemoveCumulationValueCommand(LoopNetworkDialogType loopDialogType, CumulationSet.Cumulation<?> cumulation)
			{
				super(loopDialogType);
				this.cumulation = cumulation;
			}

			public CumulationSet.Cumulation<?> getCumulation()
			{
				return cumulation;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((cumulation == null) ? 0 : cumulation.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				RemoveCumulationValueCommand<?> other = (RemoveCumulationValueCommand<?>) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (cumulation == null)
				{
					if (other.cumulation != null)
						return false;
				}
				else if (!cumulation.equals(other.cumulation))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected class RemoveRouterCumulationValueCommand extends RemoveCumulationValueCommand<RemoveRouterCumulationValueCommand>
		{
			private final int index;

			public RemoveRouterCumulationValueCommand(int index, CumulationSet.Cumulation<?> cumulation)
			{
				super(LoopNetworkDialogType.RemoveRouterCumulationValue, cumulation);
				this.index = index;
			}

			public int getIndex()
			{
				return index;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + index;
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				RemoveRouterCumulationValueCommand other = (RemoveRouterCumulationValueCommand) obj;
				if (!getOuterType().equals(other.getOuterType()) || (index != other.index))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		protected abstract class RequestCumulationValueCommand<C extends RequestCumulationValueCommand<C>> extends Command<C>
		{

			public RequestCumulationValueCommand(LoopNetworkDialogType loopDialogType)
			{
				super(loopDialogType);
			}

		}

		protected class RequestRouterCumulationValueCommand extends RequestCumulationValueCommand<RequestRouterCumulationValueCommand>
		{
			private final int index;

			public RequestRouterCumulationValueCommand(int index)
			{
				super(LoopNetworkDialogType.RequestRouterCumulationValue);
				this.index = index;
			}

			public int getIndex()
			{
				return index;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = super.hashCode();
				result = prime * result + getOuterType().hashCode();
				result = prime * result + index;
				return result;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (this == obj)
					return true;
				if (!super.equals(obj) || (getClass() != obj.getClass()))
					return false;
				RequestRouterCumulationValueCommand other = (RequestRouterCumulationValueCommand) obj;
				if (!getOuterType().equals(other.getOuterType()) || (index != other.index))
					return false;
				return true;
			}

			private LoopNetworkSubPhase getOuterType()
			{
				return LoopNetworkSubPhase.this;
			}

		}

		private Collection<RouteableSubMessage> sendingRouteableSubMessages;

		public LoopNetworkSubPhase() throws IOException
		{
			super(LoopNetworkPhase.this, LoopNetworkDialogTypeDialogActive.class, LoopNetworkDialogTypeDialogPassive.class);
			this.sendingRouteableSubMessages = null;
		}

		@Override
		protected boolean serverPhase(LoopNetworkDialogType loopDialogType) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (loopDialogType)
			{
			case Valediction:
				valedictionDialog();
				return false;
			case RouterSet:
				routerSetLoopDialogServer();
				return true;
			case RouteableMessage:
				routeableMessageDialogServer();
				return true;
			case UpdateBindPort:
				updateBindPortDialogServer();
				return true;
			case ComplementingInvitation:
				complementingInvitationDialogServer();
				return true;
			case BeltConnect:
				beltConnectDialogServer();
				return true;
			case RouterSetNeighbour:
				routerSetNeighbourDialog();
				return true;
			case ResourceTreeNode:
				resourceTreeNodeDialogServer();
				return true;
			case BeltDisconnect:
				beltDisconnectDialogServer();
				return true;
			case PropagateDeferredMessages:
				propagateDeferredMessagesDialogServer();
				return true;
			case DeferredMessageQueue:
				deferredMessageQueueDialogServer();
				return true;
			case PropagateDeferredMessageRemoval:
				propagateDeferredMessageRemovalDialogServer();
				return true;
			case UpdateNeighbourCumulationValue:
				updateNeighbourCumulationValueDialogServer();
				return true;
			case UpdateRouterCumulationValue:
				updateRouterCumulationValueDialogServer();
				return true;
			case RemoveRouterCumulationValue:
				removeRouterCumulationValueDialogServer();
				return true;
			case RequestRouterCumulationValue:
				requestRouterCumulationValueDialogServer();
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected boolean clientPhase(LoopSubPhase<LoopNetworkDialogType>.Command<?> command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			switch (command.getLoopDialogType())
			{
			case Valediction:
				valedictionDialog();
				return false;
			case RouterSet:
				routerSetLoopDialogClient(((RouterSetCommand) command).getClearing());
				return true;
			case RouteableMessage:
				routeableMessageDialogClient((RouteableMessageCommand) command);
				return true;
			case UpdateBindPort:
				updateBindPortDialogClient();
				return true;
			case ComplementingInvitation:
			{
				ComplementingInvitationCommand complementingInvitationCommand = (ComplementingInvitationCommand) command;
				complementingInvitationDialogClient(complementingInvitationCommand.getNodeAddress());
				return true;
			}
			case BeltConnect:
			{
				BeltConnectCommand beltConnectCommand = (BeltConnectCommand) command;
				beltConnectDialogClient(beltConnectCommand.getNodeAddress(), beltConnectCommand.getSides());
				return true;
			}
			case RouterSetNeighbour:
				routerSetNeighbourDialog((RouterSetNeighbourCommand) command);
				return true;
			case ResourceTreeNode:
				resourceTreeNodeDialogClient();
				return true;
			case BeltDisconnect:
			{
				BeltDisconnectCommand beltDisconnectCommand = (BeltDisconnectCommand) command;
				beltDisconnectDialogClient(beltDisconnectCommand.getSides());
				return true;
			}
			case PropagateDeferredMessages:
				propagateDeferredMessagesDialogClient((PropagateDeferredMessagesCommand) command);
				return true;
			case DeferredMessageQueue:
				deferredMessageQueueDialogClient((DeferredMessageQueueCommand) command);
				return true;
			case PropagateDeferredMessageRemoval:
				propagateDeferredMessageRemovalDialogClient((PropagateDeferredMessageRemovalCommand) command);
				return true;
			case UpdateNeighbourCumulationValue:
				updateNeighbourCumulationValueDialogClient((UpdateNeighbourCumulationValueCommand) command);
				return true;
			case UpdateRouterCumulationValue:
				updateRouterCumulationValueDialogClient((UpdateRouterCumulationValueCommand) command);
				return true;
			case RemoveRouterCumulationValue:
				removeRouterCumulationValueDialogClient((RemoveRouterCumulationValueCommand) command);
				return true;
			case RequestRouterCumulationValue:
				requestRouterCumulationValueDialogClient((RequestRouterCumulationValueCommand) command);
				return true;
			default:
				throw new Error();
			}
		}

		@Override
		protected void loopPhaseTerminate() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			super.loopPhaseTerminate();
			getNetworkPhase().disconnectFromRouteableSubMessageProcessor();
			LocalRouterSet localRouterSet = getLocalRouterSet();
			Belt belt = getBelt();
			while (!getPeerToPeerNode().isDisconnectingAll())
			{
				NetworkPhase closest = belt.closestNeighbour();
				if (closest == null)
					break;
				synchronized (localRouterSet)
				{
					int i = localRouterSet.neighbourPosition(closest);
					int l = localRouterSet.lastNeighbourIndex();
					if (i <= l)
						break;
				}
				logger.debug("loopPhaseTerminate() -> routerSetNeighbour()");
				try
				{
					if (closest.routerSetNeighbour())
						break;
				}
				catch (LoopSubPhase.CancelledCommandException e)
				{
				}
				Thread.sleep(closestConnectRetryTime);
			}
		}

		@Override
		protected ValedictionCommand makeValedictionCommand()
		{
			return new ValedictionCommand();
		}

		private void routerSetLoopDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RouterSetLoopDialogServer.class, this);
		}

		private void routerSetLoopDialogClient(Set<UUID> clearing) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RouterSetLoopDialogClient.class, this, clearing);
		}

		private void routeableMessageDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RouteableMessageDialogServer.class, this);
		}

		private void routeableMessageDialogClient(RouteableMessageCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RouteableMessageDialogClient.class, this);
		}

		private void updateBindPortDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateBindPortLoopDialogClient.class, this);
		}

		private void updateBindPortDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			UpdateBindPortLoopDialogServer dialog = dialog(UpdateBindPortLoopDialogServer.class, this);
			getNetworkPhase().setBindPort(dialog.getBindPort());
		}

		private void complementingInvitationDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(ComplementingInvitationDialogServer.class, this);
		}

		private void complementingInvitationDialogClient(NodeAddress nodeAddress)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(ComplementingInvitationDialogClient.class, this, nodeAddress);
		}

		private void beltConnectDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(BeltConnectDialogServer.class, this);
		}

		private void beltConnectDialogClient(NodeAddress nodeAddress, Collection<Side> sides)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(BeltConnectDialogClient.class, this, nodeAddress, sides);
		}

		private void resourceTreeNodeDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(ResourceTreeNodeLoopDialogServer.class, this);
		}

		private void resourceTreeNodeDialogClient() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(ResourceTreeNodeLoopDialogClient.class, this);
		}

		private void beltDisconnectDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(BeltDisconnectDialogServer.class, this);
		}

		private void beltDisconnectDialogClient(Collection<Side> sides) throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(BeltDisconnectDialogClient.class, this, sides);
		}

		private RouterSetNeighbourDialog routerSetNeighbourDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			logger.debug("routerSetNeighbourDialog()");
			return dialog(RouterSetNeighbourDialog.class, this);
		}

		private RouterSetNeighbourDialog routerSetNeighbourDialog(RouterSetNeighbourCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			try
			{
				logger.debug("routerSetNeighbourDialog(RouterSetNeighbourCommand command)");
				RouterSetNeighbourDialog dialog = dialog(RouterSetNeighbourDialog.class, this);
				command.setResult(command.new Result(dialog.isPut()));
				return dialog;
			}
			catch (Throwable t)
			{
				command.cancel(t);
				throw t;
			}
		}

		private void propagateDeferredMessagesDialogClient(PropagateDeferredMessagesCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(PropagateDeferredMessagesDialogClient.class, this, command.getDeferredMessages());
		}

		private void propagateDeferredMessagesDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(PropagateDeferredMessagesDialogServer.class, this);
		}

		private void deferredMessageQueueDialogClient(DeferredMessageQueueCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(DeferredMessageQueueDialogClient.class, this, command.getRecipientUuid(), command.getDistance());
		}

		private void deferredMessageQueueDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(DeferredMessageQueueDialogServer.class, this);
		}

		private void propagateDeferredMessageRemovalDialogClient(PropagateDeferredMessageRemovalCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(PropagateDeferredMessageRemovalDialogClient.class, this, command.getRecipientUuid(), command.getDeferredMessageUuids());
		}

		private void propagateDeferredMessageRemovalDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(PropagateDeferredMessageRemovalDialogServer.class, this);
		}

		private void updateNeighbourCumulationValueDialogClient(UpdateNeighbourCumulationValueCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateNeighbourCumulationValueDialogClient.class, this, command.getCumulationValue());
		}

		private void updateNeighbourCumulationValueDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateNeighbourCumulationValueDialogServer.class, this);
		}

		private void updateRouterCumulationValueDialogClient(UpdateRouterCumulationValueCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateRouterCumulationValueDialogClient.class, this, command.getIndex(), command.getCumulationValue());
		}

		private void updateRouterCumulationValueDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(UpdateRouterCumulationValueDialogServer.class, this);
		}

		private void removeRouterCumulationValueDialogClient(RemoveRouterCumulationValueCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RemoveRouterCumulationValueDialogClient.class, this, command.getIndex(), command.getCumulation());
		}

		private void removeRouterCumulationValueDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RemoveRouterCumulationValueDialogServer.class, this);
		}

		private void requestRouterCumulationValueDialogClient(RequestRouterCumulationValueCommand command)
				throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RequestRouterCumulationValueDialogClient.class, this, command.getIndex());
		}

		private void requestRouterCumulationValueDialogServer() throws IOException, ProtocolException, InterruptedException, DialogStreamException
		{
			dialog(RequestRouterCumulationValueDialogServer.class, this);
		}

		public void routerSet(Collection<UUID> clearing)
		{
			command(new RouterSetCommand(clearing));
		}

		private synchronized void addSendingRouteableSubMessage(RouteableSubMessage routeableSubMessage)
		{
			if (sendingRouteableSubMessages == null)
				sendingRouteableSubMessages = new ArrayList<>();
			sendingRouteableSubMessages.add(routeableSubMessage);
		}

		public void routeableSubMessage(RouteableSubMessage routeableSubMessage)
		{
			addSendingRouteableSubMessage(routeableSubMessage);
			command(new RouteableMessageCommand());
		}

		public synchronized Collection<RouteableSubMessage> dumpSendingRouteableSubMessages()
		{
			Collection<RouteableSubMessage> dumped = sendingRouteableSubMessages;
			sendingRouteableSubMessages = null;
			if (dumped == null)
				dumped = Collections.emptyList();
			return dumped;
		}

		private void resendSendingRouteableSubMessages()
		{
			RouteableSubMessageProcessor processor = getPeerToPeerNode().getRouteableSubMessageProcessor();
			for (RouteableSubMessage subMessage : dumpSendingRouteableSubMessages())
				processor.process(subMessage, getNetworkPhase());
		}

		public void updateBindPort()
		{
			command(new UpdateBindPortCommand());
		}

		public void complementingInvitation(NodeAddress nodeAddress)
		{
			command(new ComplementingInvitationCommand(nodeAddress));
		}

		public void beltConnect(NodeAddress nodeAddress, Collection<Side> sides)
		{
			command(new BeltConnectCommand(nodeAddress, sides));
		}

		public boolean routerSetNeighbour() throws InterruptedException, CancelledCommandException
		{
			return ((RouterSetNeighbourCommand.Result) commandResult(new RouterSetNeighbourCommand())).isPut();
		}

		public void resourceTreeNode()
		{
			command(new ResourceTreeNodeCommand());
		}

		public void beltDisconnect(Collection<Side> sides)
		{
			command(new BeltDisconnectCommand(sides));
		}

		public void propagateDeferredMessages(Collection<DeferredMessage> deferredMessages)
		{
			command(new PropagateDeferredMessagesCommand(deferredMessages));
		}

		public void deferredMessageQueue(UUID recipientUuid, int distance)
		{
			command(new DeferredMessageQueueCommand(recipientUuid, distance));
		}

		public void propagateDeferredMessageRemoval(UUID recipientUuid, Collection<UUID> deferredMessageUuids)
		{
			command(new PropagateDeferredMessageRemovalCommand(recipientUuid, deferredMessageUuids));
		}

		public void updateRouterCumulationValue(int i, Cumulation.Value<?> cumulationValue)
		{
			command(new UpdateRouterCumulationValueCommand(i, cumulationValue));
		}

		public void removeRouterCumulationValue(int i, CumulationSet.Cumulation<?> cumulation)
		{
			command(new RemoveRouterCumulationValueCommand(i, cumulation));
		}

		public void requestRouterCumulationValue(int i)
		{
			command(new RequestRouterCumulationValueCommand(i));
		}

		public void updateNeighbourCumulationValue(Cumulation.Value<?> cumulationValue)
		{
			command(new UpdateNeighbourCumulationValueCommand(cumulationValue));
		}

	}

	private final LoopNetworkSubPhase loopNetworkSubPhase;

	public LoopNetworkPhase(NetworkPhase networkPhase) throws IOException
	{
		super(networkPhase);
		this.loopNetworkSubPhase = new LoopNetworkSubPhase();
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		loopNetworkSubPhase.run();
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		loopNetworkSubPhase.shutdown(fast);
	}

	public void routerSet(Collection<UUID> clearing)
	{
		loopNetworkSubPhase.routerSet(clearing);
	}

	public void routeableSubMessage(RouteableSubMessage routeableSubMessage)
	{
		loopNetworkSubPhase.routeableSubMessage(routeableSubMessage);
	}

	public Collection<RouteableSubMessage> dumpSendingRouteableSubMessages()
	{
		return loopNetworkSubPhase.dumpSendingRouteableSubMessages();
	}

	public void updateBindPort()
	{
		loopNetworkSubPhase.updateBindPort();
	}

	public void complementingInvitation(NodeAddress nodeAddress)
	{
		loopNetworkSubPhase.complementingInvitation(nodeAddress);
	}

	public void beltConnect(NodeAddress nodeAddress, Collection<Side> sides)
	{
		loopNetworkSubPhase.beltConnect(nodeAddress, sides);
	}

	public boolean routerSetNeighbour() throws InterruptedException, CancelledCommandException
	{
		return loopNetworkSubPhase.routerSetNeighbour();
	}

	public boolean isOpen()
	{
		return loopNetworkSubPhase.isOpen();
	}

	public void close() throws IOException
	{
		loopNetworkSubPhase.close();
	}

	public void resourceTreeNode()
	{
		loopNetworkSubPhase.resourceTreeNode();
	}

	public void beltDisconnect(Collection<Side> sides)
	{
		loopNetworkSubPhase.beltDisconnect(sides);
	}

	public void propagateDeferredMessages(Collection<DeferredMessage> deferredMessages)
	{
		loopNetworkSubPhase.propagateDeferredMessages(deferredMessages);
	}

	public void deferredMessageQueue(UUID recipientUuid, int distance)
	{
		loopNetworkSubPhase.deferredMessageQueue(recipientUuid, distance);
	}

	public void propagateDeferredMessageRemoval(UUID recipientUuid, Collection<UUID> deferredMessageUuids)
	{
		loopNetworkSubPhase.propagateDeferredMessageRemoval(recipientUuid, deferredMessageUuids);
	}

	public void updateRouterCumulationValue(int i, Cumulation.Value<?> cumulationValue)
	{
		loopNetworkSubPhase.updateRouterCumulationValue(i, cumulationValue);
	}

	public void removeRouterCumulationValue(int i, CumulationSet.Cumulation<?> cumulation)
	{
		loopNetworkSubPhase.removeRouterCumulationValue(i, cumulation);
	}

	public void requestRouterCumulationValue(int i)
	{
		loopNetworkSubPhase.requestRouterCumulationValue(i);
	}

	public void updateNeighbourCumulationValue(Cumulation.Value<?> cumulationValue)
	{
		loopNetworkSubPhase.updateNeighbourCumulationValue(cumulationValue);
	}

}
