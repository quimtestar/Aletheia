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
import java.util.ArrayList;
import java.util.List;

import aletheia.protocol.AllocateProtocolException;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.utilities.collections.BufferedList;

@ProtocolInfo(availableVersions = 0)
public abstract class AbstractIterableProtocol<E, I extends Iterable<E>> extends Protocol<I>
{
	private final IntegerProtocol integerProtocol;
	private final Protocol<E> elementProtocol;

	public AbstractIterableProtocol(int requiredVersion, Protocol<E> elementProtocol)
	{
		super(0);
		checkVersionAvailability(AbstractIterableProtocol.class, requiredVersion);
		this.integerProtocol = new IntegerProtocol(0);
		this.elementProtocol = elementProtocol;
	}

	@Override
	public void send(DataOutput out, I iterable) throws IOException
	{
		List<E> list = new BufferedList<E>(iterable);
		integerProtocol.send(out, list.size());
		for (E e : list)
			elementProtocol.send(out, e);
	}

	protected abstract I makeIterable(ArrayList<E> list);

	@Override
	public I recv(DataInput in) throws IOException, ProtocolException
	{
		int n = integerProtocol.recv(in);
		if (n < 0)
			throw new ProtocolException();
		try
		{
			ArrayList<E> list = new ArrayList<E>(n);
			for (int i = 0; i < n; i++)
				list.add(elementProtocol.recv(in));
			return makeIterable(list);
		}
		catch (OutOfMemoryError e)
		{
			throw new AllocateProtocolException(n, e);
		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		int n = integerProtocol.recv(in);
		if (n < 0)
			throw new ProtocolException();
		for (int i = 0; i < n; i++)
			elementProtocol.skip(in);
	}

}
