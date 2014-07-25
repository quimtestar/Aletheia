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
package aletheia.peertopeer.statement;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import aletheia.peertopeer.MalePeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.statement.phase.StatementMaleRootPhase;
import aletheia.peertopeer.statement.phase.StatementPhase;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public class StatementMalePeerToPeerConnection extends MalePeerToPeerConnection
{

	public StatementMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress, UUID expectedPeerNodeUuid)
			throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress, expectedPeerNodeUuid);
	}

	@Override
	public StatementMaleRootPhase makeMaleRootPhase()
	{
		return new StatementMaleRootPhase(this);
	}

	@Override
	protected StatementMaleRootPhase getMaleRootPhase()
	{
		return (StatementMaleRootPhase) super.getMaleRootPhase();
	}

	@Override
	public StatementMaleRootPhase getRootPhase()
	{
		return (StatementMaleRootPhase) super.getRootPhase();
	}

	@Override
	public StatementPhase waitForSubRootPhase() throws InterruptedException
	{
		return (StatementPhase) super.waitForSubRootPhase();
	}

	@Override
	public StatementPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return (StatementPhase) super.waitForSubRootPhase(aborter);
	}

}
