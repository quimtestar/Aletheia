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
import java.util.Collections;
import java.util.Set;

import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.message.SubRootPhaseRequestMessage;
import aletheia.peertopeer.base.message.SubRootPhaseResponseMessage;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class SubRootPhaseDialogPassive extends SubRootPhaseDialog
{
	private final Set<SubRootPhaseType> acceptedTypes;
	private SubRootPhaseType subRootPhaseType;
	private boolean acknowledged;

	public SubRootPhaseDialogPassive(Phase phase, Set<SubRootPhaseType> acceptedTypes)
	{
		super(phase);
		this.acceptedTypes = acceptedTypes;
	}

	public Set<SubRootPhaseType> getAcceptedTypes()
	{
		return Collections.unmodifiableSet(acceptedTypes);
	}

	public SubRootPhaseType getSubRootPhaseType()
	{
		return subRootPhaseType;
	}

	@Override
	public boolean isAcknowledged()
	{
		return acknowledged;
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		SubRootPhaseRequestMessage request = recvMessage(SubRootPhaseRequestMessage.class);
		subRootPhaseType = request.getSubRootPhaseType();
		acknowledged = acceptedTypes.contains(subRootPhaseType);
		sendMessage(new SubRootPhaseResponseMessage(subRootPhaseType, acknowledged));
	}

}
