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
import java.util.EnumSet;

import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.message.BeltConnectMessage;
import aletheia.peertopeer.network.message.Side;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class BeltConnectDialogClient extends BeltConnectDialog
{
	private final NodeAddress nodeAddress;
	private final EnumSet<Side> sides;

	public BeltConnectDialogClient(Phase phase, NodeAddress nodeAddress, Collection<Side> sides)
	{
		super(phase);
		this.nodeAddress = nodeAddress;
		this.sides = EnumSet.copyOf(sides);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		sendMessage(new BeltConnectMessage(nodeAddress, sides));
	}

}
