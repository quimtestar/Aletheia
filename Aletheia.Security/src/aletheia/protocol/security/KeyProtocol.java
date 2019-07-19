/*******************************************************************************
 * Copyright (c) 2014, 2018 Quim Testar.
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
package aletheia.protocol.security;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.StringProtocol;
import aletheia.security.utilities.SecurityUtilities;
import aletheia.security.utilities.SecurityUtilities.NoSuchFormatException;

@ProtocolInfo(availableVersions = 0)
public abstract class KeyProtocol<K extends Key> extends Protocol<K>
{
	private final StringProtocol stringProtocol;
	private final ByteArrayProtocol byteArrayProtocol;

	public KeyProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(KeyProtocol.class, requiredVersion);
		this.stringProtocol = new StringProtocol(0);
		this.byteArrayProtocol = new ByteArrayProtocol(0);
	}

	@Override
	public void send(DataOutput out, K k) throws IOException
	{
		stringProtocol.send(out, k.getFormat());
		stringProtocol.send(out, k.getAlgorithm());
		byteArrayProtocol.send(out, k.getEncoded());
	}

	protected Key recv(DataInput in, Method decoder) throws IOException, ProtocolException
	{
		String format = stringProtocol.recv(in);
		String algorithm = stringProtocol.recv(in);
		byte[] encoded = byteArrayProtocol.recv(in);
		try
		{
			return (Key) decoder.invoke(SecurityUtilities.instance, format, algorithm, encoded);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e)
		{
			throw new ProtocolException(e);
		}
	}

	@Override
	public K recv(DataInput in) throws IOException, ProtocolException
	{
		String format = stringProtocol.recv(in);
		String algorithm = stringProtocol.recv(in);
		byte[] encoded = byteArrayProtocol.recv(in);
		try
		{
			return decode(format, algorithm, encoded);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchFormatException e)
		{
			throw new ProtocolException(e);
		}
	}

	protected abstract K decode(String format, String algorithm, byte[] encoded)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFormatException;

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		stringProtocol.skip(in);
		stringProtocol.skip(in);
		byteArrayProtocol.skip(in);
	}

}
