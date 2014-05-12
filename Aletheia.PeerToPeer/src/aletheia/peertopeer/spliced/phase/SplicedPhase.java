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
import aletheia.peertopeer.base.phase.RootPhase;
import aletheia.peertopeer.base.phase.SubRootPhase;
import aletheia.protocol.ProtocolException;

public class SplicedPhase extends SubRootPhase
{

	private final GenderedSplicedPhase genderedConjugalPhase;

	public SplicedPhase(RootPhase rootPhase, UUID peerNodeUuid) throws IOException
	{
		super(rootPhase, peerNodeUuid);
		switch (getGender())
		{
		case FEMALE:
			this.genderedConjugalPhase = new FemaleSplicedPhase(this);
			break;
		case MALE:
			this.genderedConjugalPhase = new MaleSplicedPhase(this);
			break;
		default:
			throw new Error();
		}
	}

	public GenderedSplicedPhase getGenderedConjugalPhase()
	{
		return genderedConjugalPhase;
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		genderedConjugalPhase.run();
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		genderedConjugalPhase.shutdown(fast);
	}

}
