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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.message.Message;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.message.BeltConfirmMessage;
import aletheia.peertopeer.network.message.BeltRequestMessage;
import aletheia.peertopeer.network.message.RedirectAddressMessage;
import aletheia.peertopeer.network.message.Side;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class BeltDialogMale extends BeltDialog
{
	private final static Logger logger = LoggerManager.logger();

	private final EnumSet<Side> requestSides;

	public BeltDialogMale(Phase phase, Collection<Side> requestSides)
	{
		super(phase);
		this.requestSides = EnumSet.copyOf(requestSides);
	}

	protected Set<Side> getRequestSides()
	{
		return Collections.unmodifiableSet(requestSides);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		logger.debug("requestSides: " + requestSides);
		sendMessage(new BeltRequestMessage(requestSides));
		Message message = recvMessage(Arrays.asList(RedirectAddressMessage.class, BeltConfirmMessage.class));
		if (message instanceof RedirectAddressMessage)
		{
			RedirectAddressMessage redirectAddressMessage = (RedirectAddressMessage) message;
			logger.debug("redirect: " + redirectAddressMessage.getNodeAddress());
			setRedirectNodeAddress(redirectAddressMessage.getNodeAddress());
		}
		else if (message instanceof BeltConfirmMessage)
		{
			BeltConfirmMessage beltConfirmMessage = (BeltConfirmMessage) message;
			logger.debug("Confirm sides: " + beltConfirmMessage.getSides());
			NodeAddress femaleNodeAddress = getNetworkPhase().nodeAddress();
			Belt belt = getBelt();
			synchronized (belt)
			{
				if (beltConfirmMessage.getSides().contains(Side.Left))
				{
					NetworkPhase old = belt.setRight(getNetworkPhase());
					logger.debug("oldRight: " + (old != null ? (old.getPeerNodeUuid() + ": " + old.getPeerToPeerConnection().getName()) : null));
					if (old != null && !old.equals(getNetworkPhase()))
					{
						logger.debug("oldRight beltConnect to " + femaleNodeAddress + " " + Side.Left);
						old.beltConnect(femaleNodeAddress, Side.Left);
						if (!old.useful())
						{
							logger.debug("oldRight not useful anymore");
							old.shutdown(false);
						}
					}
					setConnected(true);
				}
				if (beltConfirmMessage.getSides().contains(Side.Right))
				{
					NetworkPhase old = belt.setLeft(getNetworkPhase());
					logger.debug("oldLeft: " + (old != null ? (old.getPeerNodeUuid() + ": " + old.getPeerToPeerConnection().getName()) : null));
					if (old != null && !old.equals(getNetworkPhase()))
					{
						logger.debug("oldLeft beltConnect to " + femaleNodeAddress + " " + Side.Right);
						old.beltConnect(femaleNodeAddress, Side.Right);
						if (!old.useful())
						{
							logger.debug("oldLeft not useful anymore");
							old.shutdown(false);
						}
					}
					setConnected(true);
				}
			}
		}
		else
			throw new Error();

	}

}
