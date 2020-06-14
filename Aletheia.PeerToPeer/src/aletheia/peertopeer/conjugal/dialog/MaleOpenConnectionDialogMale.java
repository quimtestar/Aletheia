/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;

import aletheia.peertopeer.SplicedConnectionId;
import aletheia.peertopeer.base.message.NonPersistentMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.conjugal.message.OpenConnectionErrorMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionExpectedPeerNodeUuidMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionSocketAddressMessage;
import aletheia.peertopeer.conjugal.message.OpenConnectionSplicedConnectionIdMessage;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class MaleOpenConnectionDialogMale extends MaleOpenConnectionDialog
{
	private final InetSocketAddress socketAddress;
	private final UUID expectedPeerNodeUuid;

	private SplicedConnectionId splicedConnectionId;
	private String errorMessage;

	public MaleOpenConnectionDialogMale(Phase phase, InetSocketAddress socketAddress, UUID expectedPeerNodeUuid)
	{
		super(phase);
		this.socketAddress = socketAddress;
		this.expectedPeerNodeUuid = expectedPeerNodeUuid;

		this.splicedConnectionId = null;
		this.errorMessage = null;
	}

	public SplicedConnectionId getSplicedConnectionId()
	{
		return splicedConnectionId;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		sendMessage(new OpenConnectionSocketAddressMessage(socketAddress));
		sendMessage(new OpenConnectionExpectedPeerNodeUuidMessage(expectedPeerNodeUuid));
		NonPersistentMessage response = recvMessage(Arrays.asList(OpenConnectionSplicedConnectionIdMessage.class, OpenConnectionErrorMessage.class));
		if (response instanceof OpenConnectionSplicedConnectionIdMessage)
		{
			OpenConnectionSplicedConnectionIdMessage openConnectionSplicedConnectionIdMessage = (OpenConnectionSplicedConnectionIdMessage) response;
			splicedConnectionId = openConnectionSplicedConnectionIdMessage.getSplicedConnectionId();
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
