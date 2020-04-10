/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.model.peertopeer.deferredmessagecontent;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import aletheia.model.peertopeer.deferredmessagecontent.protocol.DeferredMessageContentCode;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.IntegerProtocol;
import aletheia.protocol.security.ReceivingRSACipherProtocol;
import aletheia.protocol.security.SendingRSACipherProtocol;

public abstract class CipheredDeferredMessageContent<T> extends DeferredMessageContent
{
	private static final long serialVersionUID = 6486155372952081238L;

	private final int version;
	private final byte[] cipher;

	public CipheredDeferredMessageContent(int version, Protocol<T> protocol, PublicKey publicKey, T plainContent)
	{
		this.version = version;
		SendingRSACipherProtocol<T> cipherProtocol = new SendingRSACipherProtocol<>(0, publicKey, protocol);
		this.cipher = cipherProtocol.toByteArray(plainContent);
	}

	public CipheredDeferredMessageContent(int version, byte[] data)
	{
		this.version = version;
		this.cipher = data.clone();
	}

	public int getVersion()
	{
		return version;
	}

	public byte[] getCipher()
	{
		return cipher.clone();
	}

	public static abstract class DecipherException extends Exception
	{

		private static final long serialVersionUID = -7263658517314348058L;

		protected DecipherException()
		{
			super();
		}

		protected DecipherException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected DecipherException(String message)
		{
			super(message);
		}

		protected DecipherException(Throwable cause)
		{
			super(cause);
		}

	}

	public static class VersionDecipherException extends DecipherException
	{
		private static final long serialVersionUID = 7589892251611355313L;

		protected VersionDecipherException()
		{
			super();
		}
	}

	public static class ProtocolDecipherException extends DecipherException
	{
		private static final long serialVersionUID = -1685909235725082078L;

		protected ProtocolDecipherException(ProtocolException cause)
		{
			super(cause);
		}
	}

	protected T decipher(Protocol<T> protocol, PrivateKey privateKey) throws DecipherException
	{
		if (version != 0)
			throw new VersionDecipherException();
		ReceivingRSACipherProtocol<T> cipherProtocol = new ReceivingRSACipherProtocol<>(0, privateKey, protocol);
		try
		{
			return cipherProtocol.fromByteArray(cipher);
		}
		catch (ProtocolException e)
		{
			throw new ProtocolDecipherException(e);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(cipher);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CipheredDeferredMessageContent<?> other = (CipheredDeferredMessageContent<?>) obj;
		if (!Arrays.equals(cipher, other.cipher))
			return false;
		return true;
	}

	@ProtocolInfo(availableVersions = 0)
	public static abstract class SubProtocol<T> extends DeferredMessageContent.SubProtocol<CipheredDeferredMessageContent<T>>
	{
		private final IntegerProtocol integerProtocol = new IntegerProtocol(0);
		private final ByteArrayProtocol byteArrayProtocol = new ByteArrayProtocol(0);

		public SubProtocol(int requiredVersion, DeferredMessageContentCode code)
		{
			super(0, code);
			checkVersionAvailability(SubProtocol.class, requiredVersion);
		}

		@Override
		public void send(DataOutput out, CipheredDeferredMessageContent<T> c) throws IOException
		{
			integerProtocol.send(out, c.getVersion());
			byteArrayProtocol.send(out, c.getCipher());
		}

		protected abstract CipheredDeferredMessageContent<T> recv(int version, byte[] cipher, DataInput in);

		@Override
		public CipheredDeferredMessageContent<T> recv(DataInput in) throws IOException, ProtocolException
		{
			int version = integerProtocol.recv(in);
			byte[] cipher = byteArrayProtocol.recv(in);
			return recv(version, cipher, in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			byteArrayProtocol.skip(in);
		}

	}

}
