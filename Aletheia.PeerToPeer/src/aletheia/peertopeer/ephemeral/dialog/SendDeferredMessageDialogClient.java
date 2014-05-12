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
package aletheia.peertopeer.ephemeral.dialog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageAddressRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageContentMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessagePortRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageUuidMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class SendDeferredMessageDialogClient extends SendDeferredMessageDialog
{
	private final UUID recipientUuid;
	private final DeferredMessageContent content;

	private NodeAddress redirectAddress;

	public SendDeferredMessageDialogClient(Phase phase, UUID recipientUuid, DeferredMessageContent content)
	{
		super(phase);
		this.recipientUuid = recipientUuid;
		this.content = content;
	}

	public UUID getRecipientUuid()
	{
		return recipientUuid;
	}

	public DeferredMessageContent getContent()
	{
		return content;
	}

	public NodeAddress getRedirectAddress()
	{
		return redirectAddress;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		sendMessage(new SendDeferredMessageUuidMessage(recipientUuid));
		SendDeferredMessageRedirectMessage redirectMessage = recvMessage(SendDeferredMessageRedirectMessage.class);
		if (redirectMessage.getUuid().equals(getEphemeralPhase().getPeerNodeUuid()))
		{
			sendMessage(new SendDeferredMessageContentMessage(content));
			redirectAddress = null;
		}
		else if (redirectMessage instanceof SendDeferredMessageAddressRedirectMessage)
			redirectAddress = ((SendDeferredMessageAddressRedirectMessage) redirectMessage).nodeAddress();
		else if (redirectMessage instanceof SendDeferredMessagePortRedirectMessage)
		{
			InetSocketAddress address = null;
			if (getPeerToPeerConnection().getRemoteAddress() != null)
				address = new InetSocketAddress(getPeerToPeerConnection().getRemoteAddress(),
						((SendDeferredMessagePortRedirectMessage) redirectMessage).getPort());
			redirectAddress = new NodeAddress(redirectMessage.getUuid(), address);
		}
	}

}
