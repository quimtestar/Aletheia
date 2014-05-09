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

import java.util.EnumSet;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class EnumSetProtocol<E extends Enum<E>> extends AbstractSetProtocol<E, EnumSet<E>>
{
	private final Class<E> elementType;

	public EnumSetProtocol(int requiredVersion, Protocol<E> elementProtocol, Class<E> elementType)
	{
		super(0, elementProtocol);
		checkVersionAvailability(EnumSetProtocol.class, requiredVersion);
		this.elementType = elementType;
	}

	@Override
	protected EnumSet<E> makeCollection(int n)
	{
		return EnumSet.noneOf(elementType);
	}
}
