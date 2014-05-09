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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import aletheia.protocol.AllocateProtocolException;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.utilities.collections.BufferedList;

@ProtocolInfo(availableVersions = 0)
public abstract class AbstractMapProtocol<K, V, M extends Map<K, V>> extends Protocol<M>
{
	private final IntegerProtocol integerProtocol;
	private final Protocol<K> keyProtocol;
	private final Protocol<V> valueProtocol;

	public AbstractMapProtocol(int requiredVersion, Protocol<K> keyProtocol, Protocol<V> valueProtocol)
	{
		super(0);
		checkVersionAvailability(AbstractMapProtocol.class, requiredVersion);
		this.integerProtocol = new IntegerProtocol(0);
		this.keyProtocol = keyProtocol;
		this.valueProtocol = valueProtocol;
	}

	@Override
	public void send(DataOutput out, M map) throws IOException
	{
		List<Map.Entry<K, V>> list = new BufferedList<>(map.entrySet());
		integerProtocol.send(out, list.size());
		for (Map.Entry<K, V> e : list)
		{
			keyProtocol.send(out, e.getKey());
			valueProtocol.send(out, e.getValue());
		}
	}

	protected abstract M makeMap(int n) throws AllocateProtocolException;

	@Override
	public M recv(DataInput in) throws IOException, ProtocolException
	{
		int n = integerProtocol.recv(in);
		if (n < 0)
			throw new ProtocolException();
		M map = makeMap(n);
		for (int i = 0; i < n; i++)
		{
			K key = keyProtocol.recv(in);
			V value = valueProtocol.recv(in);
			map.put(key, value);
		}
		return map;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		int n = integerProtocol.recv(in);
		if (n < 0)
			throw new ProtocolException();
		for (int i = 0; i < n; i++)
		{
			keyProtocol.skip(in);
			valueProtocol.skip(in);
		}
	}

}
