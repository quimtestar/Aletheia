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

import java.util.EnumMap;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class EnumMapProtocol<K extends Enum<K>, V> extends AbstractMapProtocol<K, V, EnumMap<K, V>>
{
	private final Class<K> keyType;

	public EnumMapProtocol(int requiredVersion, Protocol<K> keyProtocol, Class<K> keyType, Protocol<V> valueProtocol)
	{
		super(0, keyProtocol, valueProtocol);
		checkVersionAvailability(EnumMapProtocol.class, requiredVersion);
		this.keyType = keyType;
	}

	@Override
	protected EnumMap<K, V> makeMap(int n)
	{
		return new EnumMap<>(keyType);
	}

}
