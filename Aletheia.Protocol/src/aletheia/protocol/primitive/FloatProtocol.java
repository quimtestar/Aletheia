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
public class FloatProtocol extends Protocol<Float>
{
	private final IntegerProtocol integerProtocol = new IntegerProtocol(0);

	public FloatProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(FloatProtocol.class, requiredVersion);
	}

	@Override
	public void send(DataOutput out, Float f) throws IOException
	{
		integerProtocol.send(out, Float.floatToIntBits(f));
	}

	@Override
	public Float recv(DataInput in) throws IOException, ProtocolException
	{
		int bits = integerProtocol.recv(in);
		return Float.intBitsToFloat(bits);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		integerProtocol.skip(in);
	}

}
