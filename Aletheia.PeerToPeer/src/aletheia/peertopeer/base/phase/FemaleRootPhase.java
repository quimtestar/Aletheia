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
package aletheia.peertopeer.base.phase;

import java.io.IOException;
import java.util.Set;

import aletheia.peertopeer.PeerToPeerConnection;
import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.dialog.SalutationDialogFemale;
import aletheia.peertopeer.base.dialog.SubRootPhaseDialogPassive;
import aletheia.protocol.ProtocolException;

public class FemaleRootPhase extends RootPhase
{
	private final Set<SubRootPhaseType> acceptedSubRootPhaseTypes;

	public FemaleRootPhase(PeerToPeerConnection peerToPeerConnection, Set<SubRootPhaseType> acceptedSubRootPhaseTypes)
	{
		super(peerToPeerConnection);
		this.acceptedSubRootPhaseTypes = acceptedSubRootPhaseTypes;
	}

	private SubRootPhaseDialogPassive subRootPhaseDialogPassive(Set<SubRootPhaseType> acceptedTypes) throws IOException, ProtocolException,
			InterruptedException, DialogStreamException
	{
		return dialog(SubRootPhaseDialogPassive.class, this, acceptedTypes);
	}

	@Override
	protected SubRootPhaseType subRootPhaseDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		SubRootPhaseDialogPassive subRootPhaseDialogPassive = subRootPhaseDialogPassive(acceptedSubRootPhaseTypes);
		if (subRootPhaseDialogPassive.isAcknowledged())
			return subRootPhaseDialogPassive.getSubRootPhaseType();
		else
			return null;
	}

	@Override
	protected SalutationDialogFemale salutationDialog(int localProtocolVersion) throws IOException, ProtocolException, InterruptedException,
			DialogStreamException
	{
		return dialog(SalutationDialogFemale.class, this, localProtocolVersion);
	}

}
