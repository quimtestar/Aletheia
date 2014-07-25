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
package aletheia.peertopeer.network.dialog;

import java.io.IOException;
import java.util.EnumSet;
import java.util.UUID;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.message.BeltConfirmMessage;
import aletheia.peertopeer.network.message.BeltRequestMessage;
import aletheia.peertopeer.network.message.RedirectAddressMessage;
import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class BeltDialogFemale extends BeltDialog
{
	private final static Logger logger = LoggerManager.logger();

	public BeltDialogFemale(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		BeltRequestMessage beltRequestMessage = recvMessage(BeltRequestMessage.class);
		NodeAddress maleNodeAddress = getNetworkPhase().nodeAddress();
		Belt belt = getBelt();
		boolean redirect = false;
		synchronized (belt)
		{
			{
				UUID peer = getNetworkPhase().getPeerNodeUuid();
				UUID local = getNodeUuid();
				UUID left = belt.getLeft() != null ? belt.getLeft().getPeerNodeUuid() : null;
				UUID right = belt.getRight() != null ? belt.getRight().getPeerNodeUuid() : null;
				logger.debug(peer + " -> " + "( " + left + ", " + local + ", " + right + " )");
			}
			if (belt.getLeft() != null && belt.getRight() != null && belt.getNetworkPhaseComparator().compare(getNetworkPhase(), belt.getLeft()) < 0
					&& belt.getNetworkPhaseComparator().compare(getNetworkPhase(), belt.getRight()) > 0)
				redirect = true;
			else
			{
				EnumSet<Side> confirmSides = EnumSet.noneOf(Side.class);
				if (beltRequestMessage.getSides().contains(Side.Right)
						&& (belt.getLeft() == null || belt.getNetworkPhaseComparator().compare(getNetworkPhase(), belt.getLeft()) > 0))
				{
					NetworkPhase oldLeft = belt.setLeft(getNetworkPhase());
					confirmSides.add(Side.Left);
					logger.debug("oldLeft: " + (oldLeft != null ? (oldLeft.getPeerNodeUuid() + ": " + oldLeft.getPeerToPeerConnection().getName()) : null));
					if (oldLeft != null && !oldLeft.equals(getNetworkPhase()))
					{
						logger.debug("oldLeft beltConnect to " + maleNodeAddress + " " + Side.Right);
						oldLeft.beltConnect(maleNodeAddress, Side.Right);
						if (!oldLeft.useful())
						{
							logger.debug("oldLeft not useful anymore");
							oldLeft.shutdown(false);
						}
					}
					setConnected(true);
				}
				if (beltRequestMessage.getSides().contains(Side.Left)
						&& (belt.getRight() == null || belt.getNetworkPhaseComparator().compare(getNetworkPhase(), belt.getRight()) < 0))
				{
					NetworkPhase oldRight = belt.setRight(getNetworkPhase());
					confirmSides.add(Side.Right);
					logger.debug("oldRight: " + (oldRight != null ? (oldRight.getPeerNodeUuid() + ": " + oldRight.getPeerToPeerConnection().getName()) : null));
					if (oldRight != null && !oldRight.equals(getNetworkPhase()))
					{
						logger.debug("oldRight beltConnect to " + maleNodeAddress + " " + Side.Left);
						oldRight.beltConnect(maleNodeAddress, Side.Left);
						if (!oldRight.useful())
						{
							logger.debug("oldLeft not useful anymore");
							oldRight.shutdown(false);
						}
					}
					setConnected(true);
				}
				sendMessage(new BeltConfirmMessage(confirmSides));
			}
		}
		if (redirect)
		{
			NodeAddress redirectAddress = getPeerToPeerNode().closestNodeAddress(getNetworkPhase().getPeerNodeUuid());
			sendMessage(new RedirectAddressMessage(redirectAddress));
			setRedirectNodeAddress(redirectAddress);
			logger.debug("redirect: " + redirectAddress);
		}
	}

}
