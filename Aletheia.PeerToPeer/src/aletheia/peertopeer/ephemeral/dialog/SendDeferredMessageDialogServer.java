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
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.peertopeer.deferredmessagecontent.DeferredMessageContent;
import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageAddressRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageContentMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessagePortRedirectMessage;
import aletheia.peertopeer.ephemeral.message.SendDeferredMessageUuidMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class SendDeferredMessageDialogServer extends SendDeferredMessageDialog
{
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerManager.instance.logger();

	public SendDeferredMessageDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		SendDeferredMessageUuidMessage sendDeferredMessageUuidMessage = recvMessage(SendDeferredMessageUuidMessage.class);
		final UUID recipientUuid = sendDeferredMessageUuidMessage.getRecipientUuid();
		NodeAddress redirectAddress = getPeerToPeerNode().closestNodeAddress(recipientUuid);
		if (redirectAddress.getAddress() != null)
			sendMessage(new SendDeferredMessageAddressRedirectMessage(redirectAddress));
		else
			sendMessage(new SendDeferredMessagePortRedirectMessage(redirectAddress.getUuid(), getPeerToPeerNode().getExternalBindSocketPort()));
		if (redirectAddress.getUuid().equals(getPeerToPeerNode().getNodeUuid()))
		{
			SendDeferredMessageContentMessage sendDeferredMessageContentMessage = recvMessage(SendDeferredMessageContentMessage.class);
			final DeferredMessageContent content = sendDeferredMessageContentMessage.getContent();
			AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
			{

				@Override
				public void invoke()
				{
					getPeerToPeerNode().seedDeferredMessage(recipientUuid, content);
				}

			});

		}
	}

}
