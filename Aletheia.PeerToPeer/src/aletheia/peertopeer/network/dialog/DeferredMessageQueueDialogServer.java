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

import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.DeferredMessageSet;
import aletheia.peertopeer.network.message.DeferredMessageQueueMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.AsynchronousInvoker;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class DeferredMessageQueueDialogServer extends DeferredMessageQueueDialog
{

	public DeferredMessageQueueDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		final DeferredMessageQueueMessage deferredMessageQueueMessage = recvMessage(DeferredMessageQueueMessage.class);
		AsynchronousInvoker.instance.invoke(new AsynchronousInvoker.Invokable()
		{

			@Override
			public void invoke()
			{
				DeferredMessageSet deferredMessageSet = getDeferredMessageSet();
				if (deferredMessageSet != null)
					deferredMessageSet.messageQueueNeighbour(deferredMessageQueueMessage.getRecipientUuid(), getNetworkPhase(),
							deferredMessageQueueMessage.getDistance());
			}
		});
	}

}
