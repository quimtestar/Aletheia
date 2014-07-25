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
package aletheia.peertopeer.conjugal.dialog;

import java.io.IOException;
import java.util.Arrays;

import aletheia.peertopeer.SplicedConnectionId;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.conjugal.message.OpenConnectionAcceptedMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionErrorMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionSplicedConnectionIdMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class FemaleOpenConnectionDialogFemale extends FemaleOpenConnectionDialog
{
	private final SplicedConnectionId splicedConnectionId;

	private boolean accepted;
	private String errorMessage;

	public FemaleOpenConnectionDialogFemale(Phase phase, SplicedConnectionId splicedConnectionId)
	{
		super(phase);
		this.splicedConnectionId = splicedConnectionId;
		this.accepted = false;
		this.errorMessage = null;
	}

	public boolean isAccepted()
	{
		return accepted;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		sendMessage(new OpenConnectionSplicedConnectionIdMessage(splicedConnectionId));
		NonPersistentMessage response = recvMessage(Arrays.asList(OpenConnectionAcceptedMessage.class, OpenConnectionErrorMessage.class));
		if (response instanceof OpenConnectionAcceptedMessage)
		{
			accepted = true;
		}
		else if (response instanceof OpenConnectionErrorMessage)
		{
			OpenConnectionErrorMessage openConnectionErrorMessage = (OpenConnectionErrorMessage) response;
			errorMessage = openConnectionErrorMessage.getMessage();
		}
		else
			throw new Error();
	}

}
