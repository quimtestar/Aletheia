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
package aletheia.protocol.collection;

import java.util.HashSet;
import java.util.Set;

import aletheia.protocol.AllocateProtocolException;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class SetProtocol<E> extends AbstractSetProtocol<E, Set<E>>
{

	public SetProtocol(int requiredVersion, Protocol<E> elementProtocol)
	{
		super(0, elementProtocol);
		checkVersionAvailability(SetProtocol.class, requiredVersion);
	}

	@Override
	protected Set<E> makeCollection(int n) throws AllocateProtocolException
	{
		try
		{
			return new HashSet<E>(n);
		}
		catch (OutOfMemoryError e)
		{
			throw new AllocateProtocolException(n, e);
		}
	}

}
