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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import aletheia.model.peertopeer.DeferredMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.message.DeferredMessageInfoMessage;
import aletheia.peertopeer.network.message.DeferredMessageRequestMessage;
import aletheia.peertopeer.network.message.DeferredMessageResponseMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.collections.Bijection;
import aletheia.utilities.collections.BijectionCollection;
import aletheia.utilities.collections.FilteredCollection;
import aletheia.utilities.collections.NotNullFilter;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class PropagateDeferredMessagesDialogClient extends PropagateDeferredMessagesDialog
{
	private final Collection<DeferredMessage> deferredMessages;

	public PropagateDeferredMessagesDialogClient(Phase phase, Collection<DeferredMessage> deferredMessages)
	{
		super(phase);
		this.deferredMessages = deferredMessages;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		final Map<UUID, DeferredMessage> map = new HashMap<UUID, DeferredMessage>();
		for (DeferredMessage deferredMessage : deferredMessages)
			map.put(deferredMessage.getUuid(), deferredMessage);
		sendMessage(new DeferredMessageInfoMessage(map.keySet()));
		DeferredMessageRequestMessage deferredMessageRequestMessage = recvMessage(DeferredMessageRequestMessage.class);
		Collection<DeferredMessage> deferredMessages = new FilteredCollection<DeferredMessage>(new NotNullFilter<DeferredMessage>(),
				new BijectionCollection<UUID, DeferredMessage>(new Bijection<UUID, DeferredMessage>()
				{
					@Override
					public DeferredMessage forward(UUID uuid)
					{
						return map.get(uuid);
					}

					@Override
					public UUID backward(DeferredMessage deferredMessage)
					{
						return deferredMessage.getUuid();
					}
				}, deferredMessageRequestMessage.getUuids()));
		sendMessage(DeferredMessageResponseMessage.create(deferredMessages));
	}

}
