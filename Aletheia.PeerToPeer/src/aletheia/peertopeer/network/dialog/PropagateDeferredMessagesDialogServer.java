/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import aletheia.model.peertopeer.DeferredMessage;
import aletheia.model.peertopeer.DeferredMessage.DeletedDeferredMessageException;
import aletheia.model.peertopeer.NodeDeferredMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.DeferredMessageSet;
import aletheia.peertopeer.network.message.DeferredMessageInfoMessage;
import aletheia.peertopeer.network.message.DeferredMessageRequestMessage;
import aletheia.peertopeer.network.message.DeferredMessageResponseMessage;
import aletheia.persistence.Transaction;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.CombinedCollection;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class PropagateDeferredMessagesDialogServer extends PropagateDeferredMessagesDialog
{

	public PropagateDeferredMessagesDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		DeferredMessageInfoMessage deferredMessageInfoMessage = recvMessage(DeferredMessageInfoMessage.class);
		Map<UUID, DeferredMessage> oldDeferredMessages = new HashMap<>();
		Collection<UUID> missingUuids = new ArrayList<>();
		for (UUID uuid : deferredMessageInfoMessage.getUuids())
		{
			DeferredMessage deferredMessage = getPersistenceManager().getDeferredMessage(getTransaction(), uuid);
			if (deferredMessage == null)
				missingUuids.add(uuid);
			else
				oldDeferredMessages.put(uuid, deferredMessage);
		}
		sendMessage(new DeferredMessageRequestMessage(missingUuids));
		DeferredMessageResponseMessage deferredMessageResponseMessage = recvMessage(DeferredMessageResponseMessage.class);
		Map<UUID, DeferredMessage> newDeferredMessages = deferredMessageResponseMessage.getMap();
		UUID nodeUuid = getNodeUuid();
		class Added
		{
			final DeferredMessage deferredMessage;
			final NodeDeferredMessage nodeDeferredMessage;

			Added(DeferredMessage deferredMessage, NodeDeferredMessage nodeDeferredMessage)
			{
				super();
				this.deferredMessage = deferredMessage;
				this.nodeDeferredMessage = nodeDeferredMessage;
			}

		}
		final Collection<Added> added = new ArrayList<>();
		for (DeferredMessage deferredMessage : new CombinedCollection<>(oldDeferredMessages.values(), newDeferredMessages.values()))
		{
			if (!deferredMessage.nodeDeferredMessagesMap(getTransaction()).containsKey(nodeUuid))
			{
				try
				{
					NodeDeferredMessage nodeDeferredMessage = deferredMessage.createNodeDeferredMessage(getTransaction(), nodeUuid);
					added.add(new Added(deferredMessage, nodeDeferredMessage));
				}
				catch (DeletedDeferredMessageException e)
				{
				}
			}
		}
		getTransaction().runWhenCommit(new Transaction.Hook()
		{

			@Override
			public void run(Transaction closedTransaction)
			{
				DeferredMessageSet deferredMessageSet = getDeferredMessageSet();
				if (deferredMessageSet != null)
					for (Added a : added)
						getDeferredMessageSet().addDeferredMessage(a.deferredMessage, a.nodeDeferredMessage);
			}
		});
	}

}
