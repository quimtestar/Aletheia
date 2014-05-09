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

import java.util.Collection;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;
import aletheia.utilities.collections.EmptyIgnoringCollection;

@ProtocolInfo(availableVersions = 0)
public class NonReturningCollectionProtocol<E> extends AbstractCollectionProtocol<E, Collection<E>>
{
	public NonReturningCollectionProtocol(int requiredVersion, Protocol<E> elementProtocol)
	{
		super(0, elementProtocol);
		checkVersionAvailability(NonReturningCollectionProtocol.class, requiredVersion);
	}

	@Override
	protected EmptyIgnoringCollection<E> makeCollection(int n)
	{
		return new EmptyIgnoringCollection<>();
	}

}
