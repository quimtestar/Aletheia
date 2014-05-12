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
import java.util.UUID;

import aletheia.peertopeer.MalePeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.network.phase.NetworkMaleRootPhase;
import aletheia.peertopeer.network.phase.NetworkPhase;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public abstract class NetworkMalePeerToPeerConnection extends MalePeerToPeerConnection
{

	public NetworkMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress, UUID expectedPeerNodeUuid)
			throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress, expectedPeerNodeUuid);
	}

	public NetworkMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress) throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress);
	}

	public abstract InitialNetworkPhaseType getInitialNetworkPhaseType();

	@Override
	public NetworkMaleRootPhase makeMaleRootPhase()
	{
		return new NetworkMaleRootPhase(this);
	}

	@Override
	protected NetworkMaleRootPhase getMaleRootPhase()
	{
		return (NetworkMaleRootPhase) super.getMaleRootPhase();
	}

	@Override
	public NetworkMaleRootPhase getRootPhase()
	{
		return (NetworkMaleRootPhase) super.getRootPhase();
	}

	@Override
	public NetworkPhase waitForSubRootPhase() throws InterruptedException
	{
		return (NetworkPhase) super.waitForSubRootPhase();
	}

	@Override
	public NetworkPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return (NetworkPhase) super.waitForSubRootPhase(aborter);
	}

}
