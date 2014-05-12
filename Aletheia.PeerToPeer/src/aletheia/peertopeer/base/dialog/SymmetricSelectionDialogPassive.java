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

import aletheia.peertopeer.base.message.SymmetricSelectionAcknowledgeMessage;
import aletheia.peertopeer.base.message.SymmetricSelectionRequestMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.enumerate.ExportableEnum;

public abstract class SymmetricSelectionDialogPassive<C, E extends ExportableEnum<C, ?>> extends SymmetricSelectionDialog<C, E>
{
	private E receivedSelection;

	public SymmetricSelectionDialogPassive(Phase phase)
	{
		super(phase);
		this.receivedSelection = null;
	}

	protected abstract SymmetricSelectionAcknowledgeMessage<C, ?> makeAcknowledgeMessage(E receivedSelection);

	@SuppressWarnings("unchecked")
	protected SymmetricSelectionRequestMessage<C, E> recvSymmetricSelectionRequestMessage() throws IOException, ProtocolException
	{
		try
		{
			return recvMessage(SymmetricSelectionRequestMessage.class);
		}
		catch (ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		SymmetricSelectionRequestMessage<C, E> m = recvSymmetricSelectionRequestMessage();
		this.receivedSelection = m.getSelection();
		sendMessage(makeAcknowledgeMessage(receivedSelection));
	}

	public E getReceivedSelection()
	{
		return receivedSelection;
	}

}
