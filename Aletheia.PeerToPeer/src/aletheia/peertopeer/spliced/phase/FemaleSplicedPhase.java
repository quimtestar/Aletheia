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

import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.spliced.dialog.ConnectionIdDialogFemale;
import aletheia.protocol.ProtocolException;

public class FemaleSplicedPhase extends GenderedSplicedPhase
{
	private int connectionId;

	public FemaleSplicedPhase(SplicedPhase conjugalPhase) throws IOException
	{
		super(conjugalPhase);
		this.connectionId = -1;
	}

	@Override
	protected void connectionIdDialog() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		ConnectionIdDialogFemale connectionIdDialogFemale = dialog(ConnectionIdDialogFemale.class, this);
		connectionId = connectionIdDialogFemale.getConnectionId();
	}

	@Override
	public int getConnectionId()
	{
		return connectionId;
	}

}
