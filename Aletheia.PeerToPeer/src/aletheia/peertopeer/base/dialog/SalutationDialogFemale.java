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
package aletheia.peertopeer.base.dialog;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.PeerToPeerNode.ConnectException;
import aletheia.peertopeer.base.message.MaleSalutationMessage;
import aletheia.peertopeer.base.message.SalutationMessage;
import aletheia.peertopeer.base.message.ValidPeerNodeUuidMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.protocol.ProtocolException;

public class SalutationDialogFemale extends SalutationDialog
{
	private final static Logger logger = LoggerManager.instance.logger();

	public SalutationDialogFemale(Phase phase, int localProtocolVersion)
	{
		super(phase, localProtocolVersion);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		MaleSalutationMessage maleSalutationMessage = recvMessage(MaleSalutationMessage.class);
		setPeerProtocolVersion(maleSalutationMessage.getProtocolVersion());
		if (maleSalutationMessage.getGender() == getGender())
			throw new ProtocolException();
		if (maleSalutationMessage.getExpectedPeerNodeUuid() == null || getNodeUuid().equals(maleSalutationMessage.getExpectedPeerNodeUuid()))
		{
			sendMessage(new SalutationMessage(getLocalProtocolVersion(), getGender(), getNodeUuid()));
			setPeerProtocolVersion(maleSalutationMessage.getProtocolVersion());
			if (maleSalutationMessage.getGender() == getGender())
				throw new ProtocolException();
			setPeerNodeUuid(maleSalutationMessage.getNodeUuid());
			ValidPeerNodeUuidMessage validPeerNodeUuidMessage = recvMessage(ValidPeerNodeUuidMessage.class);
			setPeerNodeUuidValid(validPeerNodeUuidMessage.isValid());
		}
		else
		{
			setPeerNodeUuidValid(false);
			try
			{
				getPeerToPeerNode().newPendingSplicedSockedChannelFemale(getPeerToPeerConnection().getSocketChannel(),
						maleSalutationMessage.getExpectedPeerNodeUuid(), getRemoteAddress(), getMessageProtocol().toByteArray(maleSalutationMessage));
				getPeerToPeerConnection().setShutdownSocketWhenFinish(false);
			}
			catch (ConnectException e)
			{
				logger.warn("Exception caught", e);
			}
		}
	}
}
