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
package aletheia.peertopeer.conjugal;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import aletheia.peertopeer.DirectMalePeerToPeerConnection;
import aletheia.peertopeer.PeerToPeerNode;
import aletheia.peertopeer.conjugal.phase.ConjugalMaleRootPhase;
import aletheia.peertopeer.conjugal.phase.ConjugalPhase;
import aletheia.peertopeer.conjugal.phase.MaleConjugalPhase;
import aletheia.utilities.aborter.ListenableAborter;
import aletheia.utilities.aborter.Aborter.AbortException;

public class ConjugalMalePeerToPeerConnection extends DirectMalePeerToPeerConnection
{

	public ConjugalMalePeerToPeerConnection(PeerToPeerNode peerToPeerNode, SocketChannel socketChannel, InetAddress remoteAddress) throws IOException
	{
		super(peerToPeerNode, socketChannel, remoteAddress);
	}

	@Override
	public ConjugalMaleRootPhase makeMaleRootPhase()
	{
		return new ConjugalMaleRootPhase(this);
	}

	@Override
	public ConjugalPhase waitForSubRootPhase() throws InterruptedException
	{
		return (ConjugalPhase) super.waitForSubRootPhase();
	}

	@Override
	public ConjugalPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return (ConjugalPhase) super.waitForSubRootPhase(aborter);
	}

	public MaleConjugalPhase waitForMaleConjugalPhase() throws InterruptedException
	{
		ConjugalPhase conjugalPhase = waitForSubRootPhase();
		if (conjugalPhase == null)
			return null;
		return (MaleConjugalPhase) conjugalPhase.getGenderedConjugalPhase();
	}

	public MaleConjugalPhase waitForMaleConjugalPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		ConjugalPhase conjugalPhase = waitForSubRootPhase(aborter);
		if (conjugalPhase == null)
			return null;
		return (MaleConjugalPhase) conjugalPhase.getGenderedConjugalPhase();
	}

}
