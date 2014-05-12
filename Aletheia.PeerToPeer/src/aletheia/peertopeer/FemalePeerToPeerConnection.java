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
import java.util.Set;

import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.phase.FemaleRootPhase;

public class FemalePeerToPeerConnection extends PeerToPeerConnection
{
	private final FemaleRootPhase rootPhase;

	public FemalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress,
			Set<SubRootPhaseType> acceptedSubRootPhaseTypes) throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress);
		this.rootPhase = new FemaleRootPhase(this, acceptedSubRootPhaseTypes);
	}

	public FemalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, Set<SubRootPhaseType> acceptedSubRootPhaseTypes)
			throws IOException
	{
		this(peerToPeerNode, socketChannel, null, acceptedSubRootPhaseTypes);
	}

	@Override
	public Gender getGender()
	{
		return Gender.FEMALE;
	}

	@Override
	public FemaleRootPhase getRootPhase()
	{
		return rootPhase;
	}

}
