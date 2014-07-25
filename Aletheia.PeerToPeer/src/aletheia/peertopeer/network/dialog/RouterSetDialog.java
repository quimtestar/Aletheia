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
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.RemoteRouterSet;
import aletheia.peertopeer.network.message.RouterSetMessage;
import aletheia.protocol.ProtocolException;

public abstract class RouterSetDialog extends NetworkNonPersistentDialog
{

	public RouterSetDialog(Phase phase)
	{
		super(phase);
	}

	protected void dialogateRouterSetSend(Set<UUID> clearing) throws IOException, InterruptedException
	{
		sendMessage(new RouterSetMessage(getLocalRouterSet(), clearing));
	}

	protected void dialogateRouterSetSend() throws IOException, InterruptedException
	{
		sendMessage(new RouterSetMessage(getLocalRouterSet(), Collections.<UUID> emptySet()));
	}

	protected void dialogateRouterSetRecv() throws IOException, ProtocolException
	{
		RouterSetMessage routerSetMessage = recvMessage(RouterSetMessage.class);
		RemoteRouterSet remoteRouterSet = (RemoteRouterSet) routerSetMessage.getRouterSet();
		Set<UUID> clearing = routerSetMessage.getClearing();
		getNetworkPhase().setRemoteRouterSet(remoteRouterSet, clearing);
	}

}
