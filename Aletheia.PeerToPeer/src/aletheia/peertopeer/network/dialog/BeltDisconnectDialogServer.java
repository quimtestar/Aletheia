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

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.message.BeltDisconnectMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class BeltDisconnectDialogServer extends BeltDisconnectDialog
{
	private final static Logger logger = LoggerManager.logger();

	public BeltDisconnectDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		BeltDisconnectMessage beltDisconnectMessage = recvMessage(BeltDisconnectMessage.class);
		logger.debug("beltDisconnectMessage: " + beltDisconnectMessage.getSides());
		Belt belt = getBelt();
		synchronized (belt)
		{
			belt.dropNeighbour(getNetworkPhase(), beltDisconnectMessage.getSides());
			logger.debug("sendBeltConnect");
			getPeerToPeerNode().sendBeltConnect();
			if (!getNetworkPhase().useful())
				getNetworkPhase().shutdown(false);
		}
	}

}