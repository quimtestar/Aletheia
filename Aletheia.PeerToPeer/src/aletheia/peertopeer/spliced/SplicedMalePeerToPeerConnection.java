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
package aletheia.peertopeer.spliced;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import aletheia.peertopeer.DirectMalePeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.spliced.phase.SplicedMaleRootPhase;
import aletheia.peertopeer.spliced.phase.SplicedPhase;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.aborter.Aborter.AbortException;

public class SplicedMalePeerToPeerConnection extends DirectMalePeerToPeerConnection
{
	private final int connectionId;

	public SplicedMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress, UUID expectedPeerNodeUuid,
			int connectionId) throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress, expectedPeerNodeUuid);
		this.connectionId = connectionId;
	}

	public int getConnectionId()
	{
		return connectionId;
	}

	@Override
	public SplicedMaleRootPhase makeMaleRootPhase()
	{
		return new SplicedMaleRootPhase(this);
	}

	@Override
	public SplicedPhase waitForSubRootPhase() throws InterruptedException
	{
		return (SplicedPhase) super.waitForSubRootPhase();
	}

	@Override
	public SplicedPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return (SplicedPhase) super.waitForSubRootPhase(aborter);
	}

}
