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
import java.util.List;
import java.util.Map;

import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.ResourceTreeNodeSet.Action;
import aletheia.peertopeer.network.message.ResourceTreeNodeMessage;
import aletheia.peertopeer.resource.Resource;
import aletheia.protocol.ProtocolException;

public abstract class ResourceTreeNodeDialog extends NetworkNonPersistentDialog
{

	public ResourceTreeNodeDialog(Phase phase)
	{
		super(phase);
	}

	protected void dialogateResourceTreeNodeActionMapSend() throws IOException, InterruptedException
	{
		Map<Resource, List<Action>> resourceTreeNodeActionMap = getNetworkPhase().dumpResourceTreeNodeActionMap();
		sendMessage(new ResourceTreeNodeMessage(resourceTreeNodeActionMap));
	}

	protected void dialogateResourceTreeNodeActionMapRecv() throws IOException, ProtocolException
	{
		ResourceTreeNodeMessage message = recvMessage(ResourceTreeNodeMessage.class);
		getResourceTreeNodeSet().runActionMap(getNetworkPhase(), message.getActionMap());
	}

}
