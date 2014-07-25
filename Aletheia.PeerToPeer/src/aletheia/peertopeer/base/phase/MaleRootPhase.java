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
import java.util.UUID;

import aletheia.peertopeer.MalePeerToPeerConnection;
import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.dialog.SalutationDialogMale;
import aletheia.peertopeer.base.dialog.SubRootPhaseDialogActive;
import aletheia.protocol.ProtocolException;

public abstract class MaleRootPhase extends RootPhase
{
	private final SubRootPhaseType subRootPhaseType;

	public MaleRootPhase(MalePeerToPeerConnection peerToPeerConnection, SubRootPhaseType subRootPhaseType)
	{
		super(peerToPeerConnection);
		this.subRootPhaseType = subRootPhaseType;
	}

	@Override
	public MalePeerToPeerConnection getPeerToPeerConnection()
	{
		return (MalePeerToPeerConnection) super.getPeerToPeerConnection();
	}

	public UUID getExpectedPeerNodeUuid()
	{
		return getPeerToPeerConnection().getExpectedPeerNodeUuid();
	}

	@Override
	public SubRootPhaseType getSubRootPhaseType()
	{
		return subRootPhaseType;
	}

	private SubRootPhaseDialogActive subRootPhaseDialogActive(SubRootPhaseType subRootPhaseType) throws IOException, ProtocolException, InterruptedException,
	DialogStreamException
	{
		return dialog(SubRootPhaseDialogActive.class, this, subRootPhaseType);
	}

	@Override
	protected SubRootPhaseType subRootPhaseDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		SubRootPhaseDialogActive subRootPhaseDialogActive = subRootPhaseDialogActive(subRootPhaseType);
		if (subRootPhaseDialogActive.isAcknowledged())
			return subRootPhaseType;
		else
			return null;
	}

	@Override
	protected SalutationDialogMale salutationDialog(int localProtocolVersion) throws IOException, ProtocolException, InterruptedException,
	DialogStreamException
	{
		return dialog(SalutationDialogMale.class, this, localProtocolVersion, getPeerToPeerConnection().getExpectedPeerNodeUuid());
	}

}
