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

import java.util.UUID;

import aletheia.peertopeer.base.dialog.NonPersistentDialog;
import aletheia.peertopeer.base.phase.Phase;
import aletheia.peertopeer.network.Belt;
import aletheia.peertopeer.network.CumulationSet;
import aletheia.peertopeer.network.DeferredMessageSet;
import aletheia.peertopeer.network.LocalRouterSet;
import aletheia.peertopeer.network.ResourceTreeNodeSet;
import aletheia.peertopeer.network.phase.NetworkPhase;

public abstract class NetworkNonPersistentDialog extends NonPersistentDialog
{

	public NetworkNonPersistentDialog(Phase phase)
	{
		super(phase);
	}

	protected NetworkPhase getNetworkPhase()
	{
		return ancestor(NetworkPhase.class);
	}

	protected LocalRouterSet getLocalRouterSet()
	{
		return getNetworkPhase().getLocalRouterSet();
	}

	protected Belt getBelt()
	{
		return getNetworkPhase().getBelt();
	}

	protected ResourceTreeNodeSet getResourceTreeNodeSet()
	{
		return getNetworkPhase().getResourceTreeNodeSet();
	}

	protected DeferredMessageSet getDeferredMessageSet()
	{
		return getNetworkPhase().getDeferredMessageSet();
	}

	protected CumulationSet getCumulationSet()
	{
		return getNetworkPhase().getCumulationSet();
	}

	@Override
	protected UUID getNodeUuid()
	{
		return getNetworkPhase().getNodeUuid();
	}

}
