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
package aletheia.peertopeer.ephemeral;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import aletheia.peertopeer.MalePeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.ephemeral.phase.EphemeralMaleRootPhase;
import aletheia.peertopeer.ephemeral.phase.EphemeralPhase;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public class EphemeralMalePeerToPeerConnection extends MalePeerToPeerConnection
{

	public EphemeralMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress, UUID expectedPeerNodeUuid)
			throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress, expectedPeerNodeUuid);
	}

	@Override
	public EphemeralMaleRootPhase makeMaleRootPhase()
	{
		return new EphemeralMaleRootPhase(this);
	}

	@Override
	protected EphemeralMaleRootPhase getMaleRootPhase()
	{
		return (EphemeralMaleRootPhase) super.getMaleRootPhase();
	}

	@Override
	public EphemeralMaleRootPhase getRootPhase()
	{
		return (EphemeralMaleRootPhase) super.getRootPhase();
	}

	@Override
	public EphemeralPhase waitForSubRootPhase() throws InterruptedException
	{
		return (EphemeralPhase) super.waitForSubRootPhase();
	}

	@Override
	public EphemeralPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return (EphemeralPhase) super.waitForSubRootPhase(aborter);
	}

}
