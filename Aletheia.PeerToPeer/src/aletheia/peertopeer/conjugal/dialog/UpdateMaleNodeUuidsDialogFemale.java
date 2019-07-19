/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.conjugal.message.UpdateMaleNodeUuidsMessage;
import aletheia.peertopeer.conjugal.phase.FemaleConjugalPhase;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.io.NonBlockingSocketChannelStream.TimeoutException;

public class UpdateMaleNodeUuidsDialogFemale extends UpdateMaleNodeUuidsDialog
{

	public UpdateMaleNodeUuidsDialogFemale(Phase phase)
	{
		super(phase);
	}

	protected FemaleConjugalPhase getFemaleConjugalPhase()
	{
		return ancestor(FemaleConjugalPhase.class);
	}

	@Override
	protected void dialogate() throws IOException, ProtocolException, InterruptedException, TimeoutException
	{
		UpdateMaleNodeUuidsMessage updateMaleNodeUuidsMessage = recvMessage(UpdateMaleNodeUuidsMessage.class);
		getPeerToPeerNode().updateMaleNodeUuids(getFemaleConjugalPhase(), updateMaleNodeUuidsMessage.getAddUuids(),
				updateMaleNodeUuidsMessage.getRemoveUuids());
	}

}
