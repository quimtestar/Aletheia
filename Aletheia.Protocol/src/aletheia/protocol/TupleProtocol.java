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
package aletheia.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@ProtocolInfo(availableVersions = 0)
public class TupleProtocol extends Protocol<Object[]>
{
	private final Protocol<Object>[] protocols;

	public TupleProtocol(int requiredVersion, Protocol<Object>[] protocols)
	{
		super(0);
		checkVersionAvailability(TupleProtocol.class, requiredVersion);
		this.protocols = protocols;
	}

	@Override
	public void send(DataOutput out, Object[] t) throws IOException
	{
		if (t.length != protocols.length)
			throw new RuntimeException();
		for (int i = 0; i < t.length; i++)
			protocols[i].send(out, t[i]);
	}

	@Override
	public Object[] recv(DataInput in) throws IOException, ProtocolException
	{
		Object[] tuple = new Object[protocols.length];
		for (int i = 0; i < protocols.length; i++)
			tuple[i] = protocols[i].recv(in);
		return tuple;
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		for (int i = 0; i < protocols.length; i++)
			protocols[i].skip(in);
	}

}
