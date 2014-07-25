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
package aletheia.peertopeer.base.dialog;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.base.message.SymmetricSelectionAcknowledgeMessage;
import aletheia.peertopeer.base.message.SymmetricSelectionMessage;
import aletheia.peertopeer.base.message.SymmetricSelectionRequestMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.enumerate.ExportableEnum;

public abstract class SymmetricSelectionDialogActive<C, E extends ExportableEnum<C, ?>> extends SymmetricSelectionDialog<C, E>
{
	private final E sentSelection;
	private E receivedSelection = null;
	private boolean acknowledged;
	private PeerToPeerConnection.Gender prevails;

	public SymmetricSelectionDialogActive(Phase phase, E sentSelection)
	{
		super(phase);
		this.sentSelection = sentSelection;
		this.receivedSelection = null;
		this.acknowledged = false;
		this.prevails = null;
	}

	protected abstract SymmetricSelectionRequestMessage<C, ?> makeRequestMessage(E sentSelection, boolean resolver);

	@SuppressWarnings("unchecked")
	protected SymmetricSelectionMessage<C, E> recvSymmetricSelectionMessage() throws IOException, ProtocolException
	{
		try
		{
			return recvMessage(SymmetricSelectionMessage.class);
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		boolean resolver = ThreadLocalRandom.current().nextBoolean();
		sendMessage(makeRequestMessage(sentSelection, resolver));
		SymmetricSelectionMessage<C, E> m = recvSymmetricSelectionMessage();
		receivedSelection = m.getSelection();
		if (m instanceof SymmetricSelectionAcknowledgeMessage)
		{
			if (sentSelection != receivedSelection)
				throw new ProtocolException();
			acknowledged = true;
		}
		else if (m instanceof SymmetricSelectionRequestMessage)
		{
			SymmetricSelectionRequestMessage<C, E> reqm = (SymmetricSelectionRequestMessage<C, E>) m;
			prevails = resolver == reqm.isResolver() ? PeerToPeerConnection.Gender.MALE : PeerToPeerConnection.Gender.FEMALE;
		}
		else
			throw new Error();
	}

	public E getSentSelection()
	{
		return sentSelection;
	}

	public boolean isAcknowledged()
	{
		return acknowledged;
	}

	public E getReceivedSelection()
	{
		return receivedSelection;
	}

	public PeerToPeerConnection.Gender getPrevails()
	{
		return prevails;
	}

}
