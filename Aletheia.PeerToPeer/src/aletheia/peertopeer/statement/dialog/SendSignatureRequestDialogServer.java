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
package aletheia.peertopeer.statement.dialog;

import java.io.IOException;

import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.ephemeral.message.SignatureRequestConfirmMessage;
import aletheia.peertopeer.ephemeral.message.SignatureRequestMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

@Deprecated
public class SendSignatureRequestDialogServer extends SendSignatureRequestDialog
{

	public SendSignatureRequestDialogServer(Phase phase)
	{
		super(phase);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		boolean received;
		try
		{
			recvMessage(SignatureRequestMessage.class);
			received = true;
		}
		catch (SignatureRequestMessage.SubProtocol.CollisionPackedSignatureRequestProtocolException e)
		{
			received = false;
		}
		sendMessage(new SignatureRequestConfirmMessage(received));
	}

}
