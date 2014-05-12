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

import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.TransmitDeferredMessagesConfirmMessage;
import aletheia.peertopeer.ephemeral.message.TransmitDeferredMessagesMessage;
import aletheia.peertopeer.ephemeral.message.TransmitDeferredMessagesMessage.Entry;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class TransmitDeferredMessagesDialogServer extends TransmitDeferredMessagesDialog
{

	public TransmitDeferredMessagesDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		TransmitDeferredMessagesMessage transmitDeferredMessagesMessage = recvMessage(TransmitDeferredMessagesMessage.class);
		for (Entry e : transmitDeferredMessagesMessage.getEntries())
			getPeerToPeerNode().receiveDeferredMessageContent(e.getRecipientUuid(), e.getDate(), e.getContent());
		sendMessage(new TransmitDeferredMessagesConfirmMessage());
	}

}
