/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.security.protocol;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.utilities.io.SegmentedInputStream;
import aletheia.utilities.io.SegmentedOutputStream;

@ProtocolInfo(availableVersions = 0)
public class SymmetricKeyCipherProtocol<T> extends CipherProtocol<T>
{
	private final static int segmentSize = 1024;
	private final ByteArrayProtocol byteArrayProtocol;

	public SymmetricKeyCipherProtocol(int requiredVersion, String algorithm, SecretKey secretKey, Protocol<T> inner)
	{
		super(0, algorithm, secretKey, inner);
		checkVersionAvailability(SymmetricKeyCipherProtocol.class, requiredVersion);
		this.byteArrayProtocol = new ByteArrayProtocol(0);
	}

	@Override
	public SecretKey getKey()
	{
		return (SecretKey) super.getKey();
	}

	@Override
	public void send(final DataOutput out, T t) throws IOException
	{
		super.send(out, t);
		try
		{
			final Cipher cipher = Cipher.getInstance(getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, getKey());
			SegmentedOutputStream sos = new SegmentedOutputStream(segmentSize)
			{

				@Override
				protected void segment(byte[] b, int off, int len) throws IOException
				{
					byte[] data = cipher.update(b, off, len);
					if (data.length > 0)
						byteArrayProtocol.send(out, data);
				}
			};

			getInner().send(new DataOutputStream(sos), t);
			sos.close();
			byte data[] = cipher.doFinal();
			if (data.length > 0)
				byteArrayProtocol.send(out, data);
			byteArrayProtocol.send(out, new byte[0]);
		}
		catch (GeneralSecurityException e)
		{
			throw new CipherException(e);
		}
		finally
		{

		}
	}

	private static class ExceptionCapsule extends RuntimeException
	{
		private static final long serialVersionUID = 8080643179801688789L;

		public ExceptionCapsule(ProtocolException cause)
		{
			super(cause);
		}

		@Override
		public synchronized ProtocolException getCause()
		{
			return (ProtocolException) super.getCause();
		}
	}

	@Override
	public T recv(String algorithm, final DataInput in) throws IOException, ProtocolException
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, getKey());
			SegmentedInputStream sis = new SegmentedInputStream()
			{
				private boolean ended = false;

				@Override
				public byte[] segment() throws IOException
				{
					try
					{
						if (ended)
							return null;
						byte[] data = byteArrayProtocol.recv(in);
						if (data.length > 0)
							return cipher.update(data);
						else
						{
							ended = true;
							return cipher.doFinal();
						}
					}
					catch (ProtocolException e)
					{
						throw new ExceptionCapsule(e);
					}
					catch (IllegalBlockSizeException | BadPaddingException e)
					{
						throw new ExceptionCapsule(new ProtocolException(e));
					}
				}
			};
			T t = getInner().recv(new DataInputStream(sis));
			return t;
		}
		catch (GeneralSecurityException e)
		{
			throw new CipherException(e);
		}
		catch (ExceptionCapsule e)
		{
			throw e.getCause();
		}
		finally
		{

		}
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		super.skip(in);
		while (true)
		{
			int n = byteArrayProtocol.skipN(in);
			if (n <= 0)
				break;
		}
	}

}
