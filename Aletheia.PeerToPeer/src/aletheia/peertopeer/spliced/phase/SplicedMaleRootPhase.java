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

import aletheia.peertopeer.MalePeerToPeerConnection;
import aletheia.peertopeer.base.SubRootPhaseType;
import aletheia.peertopeer.base.phase.MaleRootPhase;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public class SplicedMaleRootPhase extends MaleRootPhase
{

	public SplicedMaleRootPhase(MalePeerToPeerConnection peerToPeerConnection)
	{
		super(peerToPeerConnection, SubRootPhaseType.Spliced);
	}

	@Override
	public synchronized SplicedPhase waitForSubRootPhase() throws InterruptedException
	{
		return (SplicedPhase) super.waitForSubRootPhase();
	}

	@Override
	public synchronized SplicedPhase waitForSubRootPhase(ListenableAborter aborter) throws InterruptedException, AbortException
	{
		return (SplicedPhase) super.waitForSubRootPhase(aborter);
	}

}
