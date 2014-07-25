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
package aletheia.protocol.primitive;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class NullableProtocol<T> extends Protocol<T>
{
	private final Protocol<T> protocol;
	private final BooleanProtocol booleanProtocol;

	public NullableProtocol(int requiredVersion, Protocol<T> protocol)
	{
		super(0);
		checkVersionAvailability(NullableProtocol.class, requiredVersion);
		this.protocol = protocol;
		this.booleanProtocol = new BooleanProtocol(0);
	}

	@Override
	public void send(DataOutput out, T t) throws IOException
	{
		booleanProtocol.send(out, t == null);
		if (t != null)
			protocol.send(out, t);
	}

	@Override
	public T recv(DataInput in) throws IOException, ProtocolException
	{
		if (booleanProtocol.recv(in))
			return null;
		else
			return protocol.recv(in);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		if (!booleanProtocol.recv(in))
			protocol.skip(in);
	}

}
