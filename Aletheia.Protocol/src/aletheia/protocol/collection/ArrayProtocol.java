/*******************************************************************************
 * Copyright (c) 2014, 2016 Quim Testar.
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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class ArrayProtocol<T> extends Protocol<T[]>
{
	private final Class<? extends T> elementClass;
	private final ListProtocol<T> listProtocol;

	public ArrayProtocol(int requiredVersion, Class<? extends T> elementClass, Protocol<T> elementProtocol)
	{
		super(0);
		checkVersionAvailability(ArrayProtocol.class, requiredVersion);
		this.elementClass = elementClass;
		this.listProtocol = new ListProtocol<>(0, elementProtocol);
	}

	@Override
	public void send(DataOutput out, T[] a) throws IOException
	{
		listProtocol.send(out, Arrays.asList(a));
	}

	@Override
	public T[] recv(DataInput in) throws IOException, ProtocolException
	{
		List<T> list = listProtocol.recv(in);
		@SuppressWarnings("unchecked")
		T[] a = (T[]) Array.newInstance(elementClass, list.size());
		list.toArray(a);
		return a;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		listProtocol.skip(in);
	}

}
