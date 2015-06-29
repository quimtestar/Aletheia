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
package aletheia.peertopeer.network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.network.message.Side;

public class BeltNetworkMalePeerToPeerConnection extends NetworkMalePeerToPeerConnection
{
	private final EnumSet<Side> sides;

	public BeltNetworkMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress, UUID expectedPeerNodeUuid,
			Collection<Side> sides) throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress, expectedPeerNodeUuid);
		this.sides = EnumSet.copyOf(sides);
	}

	@Override
	public InitialNetworkPhaseType getInitialNetworkPhaseType()
	{
		return InitialNetworkPhaseType.Belt;
	}

	public Set<Side> getSides()
	{
		return Collections.unmodifiableSet(sides);
	}

}
