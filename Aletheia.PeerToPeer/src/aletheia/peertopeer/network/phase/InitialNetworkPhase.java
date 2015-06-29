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
package aletheia.peertopeer.network.phase;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerConnection.Gender;
import aletheia.peertopeer.PeerToPeerNode.JoinedToNetworkTimeoutException;
import aletheia.peertopeer.PeerToPeerNodeProperties;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.network.BeltNetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.InitialNetworkPhaseType;
import aletheia.peertopeer.network.NetworkMalePeerToPeerConnection;
import aletheia.peertopeer.network.dialog.BeltDialog;
import aletheia.peertopeer.network.dialog.BeltDialogFemale;
import aletheia.peertopeer.network.dialog.BeltDialogMale;
import aletheia.peertopeer.network.dialog.ClosestNodeDialog;
import aletheia.peertopeer.network.dialog.ClosestNodeDialogFemale;
import aletheia.peertopeer.network.dialog.ClosestNodeDialogMale;
import aletheia.peertopeer.network.dialog.InitialNetworkPhaseTypeDialog;
import aletheia.peertopeer.network.dialog.InitialNetworkPhaseTypeDialogFemale;
import aletheia.peertopeer.network.dialog.InitialNetworkPhaseTypeDialogMale;
import aletheia.peertopeer.network.dialog.ResourceTreeNodeInitialDialog;
import aletheia.peertopeer.network.dialog.RouterSetInitialDialog;
import aletheia.peertopeer.network.dialog.RouterSetNeighbourDialog;
import aletheia.peertopeer.network.dialog.UpdateBindPortInitialDialog;
import aletheia.peertopeer.network.message.Side;
import aletheia.protocol.ProtocolException;

public class InitialNetworkPhase extends NetworkSubPhase
{
	private final static Logger logger = LoggerManager.instance.logger();
	private final static float waitForJoinedToNetworkTimeout = PeerToPeerNodeProperties.instance.isDebug() ? 0f : 20f; // in secs;

	private InitialNetworkPhaseType type;
	private boolean loop;
	private NodeAddress redirectNodeAddress;
	private boolean joinedToNetwork;

	public InitialNetworkPhase(NetworkPhase networkPhase)
	{
		super(networkPhase);
		this.type = null;
		this.loop = false;
		this.redirectNodeAddress = null;
		this.joinedToNetwork = false;
	}

	public InitialNetworkPhaseType getType()
	{
		return type;
	}

	private void setType(InitialNetworkPhaseType type)
	{
		this.type = type;
	}

	public boolean isLoop()
	{
		return loop;
	}

	private void setLoop(boolean loop)
	{
		this.loop = loop;
	}

	public NodeAddress getRedirectNodeAddress()
	{
		return redirectNodeAddress;
	}

	private void setRedirectNodeAddress(NodeAddress redirectNodeAddress)
	{
		this.redirectNodeAddress = redirectNodeAddress;
	}

	public boolean isJoinedToNetwork()
	{
		return joinedToNetwork;
	}

	private void setJoinedToNetwork(boolean joinedToNetwork)
	{
		this.joinedToNetwork = joinedToNetwork;
	}

	private void updateBindPortInitialDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		UpdateBindPortInitialDialog dialog = dialog(UpdateBindPortInitialDialog.class, this);
		getNetworkPhase().setBindPort(dialog.getBindPort());
	}

	private void routerSetInitialDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		dialog(RouterSetInitialDialog.class, this);
	}

	private ClosestNodeDialogFemale closestNodeDialogFemale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(ClosestNodeDialogFemale.class, this);
	}

	private ClosestNodeDialogMale closestNodeDialogMale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(ClosestNodeDialogMale.class, this);
	}

	private ClosestNodeDialog closestNodeDialog() throws InterruptedException, IOException, ProtocolException, DialogStreamException
	{
		switch (getGender())
		{
		case FEMALE:
			return closestNodeDialogFemale();
		case MALE:
			return closestNodeDialogMale();
		default:
			throw new Error();
		}
	}

	private void resourceTreeNodeInitialDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		dialog(ResourceTreeNodeInitialDialog.class, this);
	}

	private InitialNetworkPhaseTypeDialogFemale initialNetworkPhaseTypeDialogFemale()
			throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(InitialNetworkPhaseTypeDialogFemale.class, this);
	}

	private InitialNetworkPhaseType maleConnectionInitialPhaseType()
	{
		return ((NetworkMalePeerToPeerConnection) getPeerToPeerConnection()).getInitialNetworkPhaseType();
	}

	private InitialNetworkPhaseTypeDialogMale initialNetworkPhaseTypeDialogMale(InitialNetworkPhaseType type)
			throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(InitialNetworkPhaseTypeDialogMale.class, this, type);
	}

	private InitialNetworkPhaseTypeDialog initialNetworkPhaseTypeDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		switch (getGender())
		{
		case FEMALE:
			return initialNetworkPhaseTypeDialogFemale();
		case MALE:
			return initialNetworkPhaseTypeDialogMale(maleConnectionInitialPhaseType());
		default:
			throw new Error();
		}
	}

	private RouterSetNeighbourDialog routerSetNeighbourDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(RouterSetNeighbourDialog.class, this);
	}

	private BeltDialogFemale beltDialogFemale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(BeltDialogFemale.class, this);
	}

	private BeltDialogMale beltDialogMale() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		PeerToPeerConnection connection = getPeerToPeerConnection();
		Set<Side> sides;
		if (connection instanceof BeltNetworkMalePeerToPeerConnection)
			sides = ((BeltNetworkMalePeerToPeerConnection) connection).getSides();
		else
			sides = EnumSet.allOf(Side.class);
		return dialog(BeltDialogMale.class, this, sides);
	}

	private BeltDialog beltDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		switch (getGender())
		{
		case FEMALE:
			return beltDialogFemale();
		case MALE:
			return beltDialogMale();
		default:
			throw new Error();
		}
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		try
		{
			logger.debug(": starting");
			updateBindPortInitialDialog();
			InitialNetworkPhaseTypeDialog initialNetworkPhaseTypeDialog = initialNetworkPhaseTypeDialog();
			InitialNetworkPhaseType type = initialNetworkPhaseTypeDialog.getType();
			logger.debug("type: " + type);
			setType(type);
			switch (type)
			{
			case Joining:
			{
				logger.debug("Joining closestNode");
				if (getGender() == Gender.FEMALE)
				{
					try
					{
						getPeerToPeerNode().waitForJoinedToNetworkTimeout((int) (1000 * waitForJoinedToNetworkTimeout));
					}
					catch (JoinedToNetworkTimeoutException e)
					{
						throw new ProtocolException(e);
					}
				}
				ClosestNodeDialog closestNodeDialog = closestNodeDialog();
				NodeAddress redirectNodeAddress = closestNodeDialog.getRedirectNodeAddress();
				logger.debug("Redirect: " + redirectNodeAddress);
				if (!redirectedNodeAddress(redirectNodeAddress))
				{
					logger.debug("Joining belt");
					BeltDialog beltDialog = beltDialog();
					redirectNodeAddress = beltDialog.getRedirectNodeAddress();
					if (!redirectedNodeAddress(redirectNodeAddress))
					{
						if (beltDialog.isConnected())
						{
							logger.debug("Joining routerSetNeighbour");
							RouterSetNeighbourDialog routerSetNeighbourDialog = routerSetNeighbourDialog();
							if (routerSetNeighbourDialog.isPut())
							{
								logger.debug("Joining routerSet routes");
								routerSetInitialDialog();
								resourceTreeNodeInitialDialog();
							}
							if (getGender() == Gender.MALE)
							{
								getPeerToPeerNode().sendComplementingInvitations();
								setJoinedToNetwork(true);
							}
							setLoop(true);
						}
						else
						{
							logger.debug("beltDialog not connected");
						}
					}
				}
				setRedirectNodeAddress(redirectNodeAddress);
				break;
			}
			case Complementing:
			{
				RouterSetNeighbourDialog routerSetNeighbourDialog = routerSetNeighbourDialog();
				if (routerSetNeighbourDialog.isPut())
				{
					routerSetInitialDialog();
					setLoop(true);
				}
				break;
			}
			case Belt:
			{
				BeltDialog beltDialog = beltDialog();
				redirectNodeAddress = beltDialog.getRedirectNodeAddress();
				if (!redirectedNodeAddress(redirectNodeAddress))
				{
					if (beltDialog.isConnected())
					{
						RouterSetNeighbourDialog routerSetNeighbourDialog = routerSetNeighbourDialog();
						if (routerSetNeighbourDialog.isPut())
							routerSetInitialDialog();
						setLoop(true);
					}
				}
				setRedirectNodeAddress(redirectNodeAddress);
				break;
			}
			case Void:
				break;
			default:
				throw new Error();
			}

		}
		finally
		{
			logger.debug(": ending");
		}

	}

}
