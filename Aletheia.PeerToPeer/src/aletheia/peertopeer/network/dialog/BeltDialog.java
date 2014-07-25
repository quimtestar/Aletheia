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
package aletheia.peertopeer.network.dialog;

import aletheia.peertopeer.NodeAddress;
import aletheia.peertopeer.base.phase.Phase;

public abstract class BeltDialog extends NetworkNonPersistentDialog
{
	private NodeAddress redirectNodeAddress;
	private boolean connected;

	public BeltDialog(Phase phase)
	{
		super(phase);
		this.redirectNodeAddress = null;
		this.connected = false;
	}

	public NodeAddress getRedirectNodeAddress()
	{
		return redirectNodeAddress;
	}

	protected void setRedirectNodeAddress(NodeAddress redirectAddress)
	{
		this.redirectNodeAddress = redirectAddress;
	}

	public boolean isConnected()
	{
		return connected;
	}

	protected void setConnected(boolean connected)
	{
		this.connected = connected;
	}

}
