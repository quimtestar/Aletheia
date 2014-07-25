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
package aletheia.peertopeer;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import aletheia.peertopeer.base.phase.MaleRootPhase;

public abstract class MalePeerToPeerConnection extends PeerToPeerConnection
{
	private final UUID expectedPeerNodeUuid;
	private final MaleRootPhase maleRootPhase;

	public MalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress, UUID expectedPeerNodeUuid)
			throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress);
		this.expectedPeerNodeUuid = expectedPeerNodeUuid;
		this.maleRootPhase = makeMaleRootPhase();
	}

	public MalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress) throws IOException
	{
		this(peerToPeerNode, socketChannel, remoteAddress, null);
	}

	public UUID getExpectedPeerNodeUuid()
	{
		return expectedPeerNodeUuid;
	}

	public abstract MaleRootPhase makeMaleRootPhase();

	protected MaleRootPhase getMaleRootPhase()
	{
		return maleRootPhase;
	}

	@Override
	public Gender getGender()
	{
		return Gender.MALE;
	}

	@Override
	public MaleRootPhase getRootPhase()
	{
		return getMaleRootPhase();
	}

}
