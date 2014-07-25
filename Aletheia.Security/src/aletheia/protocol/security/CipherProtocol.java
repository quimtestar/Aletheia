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
package aletheia.protocol.security;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.StringProtocol;

@ProtocolInfo(availableVersions = 0)
public abstract class CipherProtocol<T> extends Protocol<T>
{
	private final String algorithm;
	private final Key key;
	private final Protocol<T> inner;
	private final StringProtocol stringProtocol;

	public CipherProtocol(int requiredVersion, String algorithm, Key key, Protocol<T> inner)
	{
		super(0);
		checkVersionAvailability(CipherProtocol.class, requiredVersion);
		this.algorithm = algorithm;
		this.key = key;
		this.inner = inner;
		this.stringProtocol = new StringProtocol(0);
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public Key getKey()
	{
		return key;
	}

	protected Protocol<T> getInner()
	{
		return inner;
	}
	
	public static class CipherException extends RuntimeException
	{
		private static final long serialVersionUID = 6185585348650786411L;

		CipherException(GeneralSecurityException cause)
		{
			super(cause);
		}

		@Override
		public synchronized GeneralSecurityException getCause()
		{
			return (GeneralSecurityException) super.getCause();
		}
		
	}

	@Override
	public void send(DataOutput out, T t) throws IOException, CipherException
	{
		stringProtocol.send(out, algorithm);
	}

	protected abstract T recv(String algorithm, DataInput in) throws IOException, ProtocolException, CipherException;

	
	@Override
	public final T recv(DataInput in) throws IOException, ProtocolException, CipherException
	{
		String algorithm = stringProtocol.recv(in);
		return recv(algorithm, in);
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		stringProtocol.skip(in);
	}

}
