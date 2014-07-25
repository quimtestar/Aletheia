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
import java.util.UUID;

import aletheia.peertopeer.base.message.MaleSalutationMessage;
import aletheia.peertopeer.base.message.SalutationMessage;
import aletheia.peertopeer.base.message.ValidPeerNodeUuidMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.protocol.ProtocolException;

public class SalutationDialogMale extends SalutationDialog
{
	private final UUID expectedPeerNodeUuid;

	public SalutationDialogMale(Phase phase, int localProtocolVersion, UUID expectedPeerNodeUuid)
	{
		super(phase, localProtocolVersion);
		this.expectedPeerNodeUuid = expectedPeerNodeUuid;
	}

	protected UUID getExpectedPeerNodeUuid()
	{
		return expectedPeerNodeUuid;
	}

	private void setAndSendValid(boolean valid) throws IOException, InterruptedException
	{
		setPeerNodeUuidValid(valid);
		sendMessage(new ValidPeerNodeUuidMessage(valid));
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException
	{
		sendMessage(new MaleSalutationMessage(getLocalProtocolVersion(), getGender(), getNodeUuid(), getExpectedPeerNodeUuid()));
		SalutationMessage m = recvMessage(SalutationMessage.class);
		setPeerProtocolVersion(m.getProtocolVersion());
		if (m.getGender() == getGender())
			throw new ProtocolException();
		setPeerNodeUuid(m.getNodeUuid());
		if (getNodeUuid() != null && getNodeUuid().equals(getPeerNodeUuid()))
			setPeerNodeUuidValid(false);

		if (isPeerNodeUuidValid())
		{
			if (expectedPeerNodeUuid != null && !expectedPeerNodeUuid.equals(getPeerNodeUuid()))
				setAndSendValid(false);
			else
				setAndSendValid(true);
		}
	}

}
