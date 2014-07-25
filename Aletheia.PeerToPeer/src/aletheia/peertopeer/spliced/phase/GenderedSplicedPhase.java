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
package aletheia.peertopeer.spliced.phase;

import java.io.IOException;
import java.util.UUID;

import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.phase.SubPhase;
import aletheia.protocol.ProtocolException;

public abstract class GenderedSplicedPhase extends SubPhase
{
	public GenderedSplicedPhase(SplicedPhase splicedPhase)
	{
		super(splicedPhase);
	}

	@Override
	protected SplicedPhase getParentPhase()
	{
		return (SplicedPhase) super.getParentPhase();
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		connectionIdDialog();
		getPeerToPeerConnection().setShutdownSocketWhenFinish(false);
	}

	protected abstract void connectionIdDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException;

	public abstract int getConnectionId();

	public UUID getPeerNodeUuid()
	{
		return getParentPhase().getPeerNodeUuid();
	}

}
