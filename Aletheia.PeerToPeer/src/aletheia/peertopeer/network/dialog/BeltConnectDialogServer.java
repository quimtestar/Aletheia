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

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.message.BeltConnectMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class BeltConnectDialogServer extends BeltConnectDialog
{
	private final static Logger logger = LoggerManager.instance.logger();

	public BeltConnectDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		BeltConnectMessage beltConnectMessage = recvMessage(BeltConnectMessage.class);
		logger.debug("beltConnectMessage: " + beltConnectMessage.getNodeAddress() + " " + beltConnectMessage.getSides());
		Belt belt = getBelt();
		synchronized (belt)
		{
			belt.dropNeighbour(getNetworkPhase(), beltConnectMessage.getSides());
			try
			{
				logger.debug("beltNetworkConnect");
				getPeerToPeerNode().beltNetworkConnect(getNetworkPhase().translateRemoteNodeAddress(beltConnectMessage.getNodeAddress()),
						beltConnectMessage.getSides());
			}
			catch (ConnectException e)
			{
				logger.error("Can't connect", e);
			}
			if (!getNetworkPhase().useful())
				getNetworkPhase().shutdown(false);
		}
	}
}
