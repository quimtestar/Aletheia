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
package aletheia.peertopeer.conjugal.dialog;

import java.io.IOException;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.SplicedConnectionId;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.conjugal.message.OpenConnectionSocketAddressMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionErrorMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionExpectedPeerNodeUuidMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionSplicedConnectionIdMessage;
import aletheia.peertopeer.conjugal.phase.FemaleConjugalPhase;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class MaleOpenConnectionDialogFemale extends MaleOpenConnectionDialog
{
	private static final Logger logger = LoggerManager.logger();

	public MaleOpenConnectionDialogFemale(Phase phase)
	{
		super(phase);
	}

	protected FemaleConjugalPhase getFemaleConjugalPhase()
	{
		return ancestor(FemaleConjugalPhase.class);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		OpenConnectionSocketAddressMessage openConnectionAddressMessage = recvMessage(OpenConnectionSocketAddressMessage.class);
		OpenConnectionExpectedPeerNodeUuidMessage openConnectionExpectedPeerNodeUuidMessage = recvMessage(OpenConnectionExpectedPeerNodeUuidMessage.class);
		try
		{
			SplicedConnectionId splicedConnectionId = getPeerToPeerNode().newPendingSplicedSockedChannelMale(getFemaleConjugalPhase(),
					openConnectionExpectedPeerNodeUuidMessage.getExpectedPeerNodeUuid(), openConnectionAddressMessage.getSocketAddress());
			sendMessage(new OpenConnectionSplicedConnectionIdMessage(splicedConnectionId));
		}
		catch (ConnectException e)
		{
			logger.warn("Exception message to send", e);
			sendMessage(new OpenConnectionErrorMessage(e.getMessage()));
		}
	}

}
