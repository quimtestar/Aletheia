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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import aletheia.utilities.collections.ArrayAsList;

/**
 * A protocol is a object with the capability of sending/receiving objects
 * from/to a generic {@link DataInput}/{@link DataOutput} object.
 *
 * @param <T>
 *            The class of objects that this protocol manages.
 */
@ProtocolInfo(availableVersions = 0)
public abstract class Protocol<T>
{

	public static abstract class ProtocolVersionException extends RuntimeException
	{
		private static final long serialVersionUID = 3731997284839064253L;

		@SuppressWarnings("rawtypes")
		private final Class<? extends Protocol> protocolClass;

		protected ProtocolVersionException(String message, @SuppressWarnings("rawtypes") Class<? extends Protocol> protocolClass)
		{
			super(message);
			this.protocolClass = protocolClass;
		}

		@SuppressWarnings("rawtypes")
		public Class<? extends Protocol> getProtocolClass()
		{
			return protocolClass;
		}

	}

	public static class MissingProtocolInfoException extends ProtocolVersionException
	{
		private static final long serialVersionUID = -4759494116030393564L;

		private MissingProtocolInfoException(@SuppressWarnings("rawtypes") Class<? extends Protocol> protocolClass)
		{
			super("Missing protocol info exception for class " + protocolClass.getName(), protocolClass);
		}

	}

	public static class VersionNotAvailableException extends ProtocolVersionException
	{
		private static final long serialVersionUID = -3402300967435176463L;
		private final int requiredVersion;

		private VersionNotAvailableException(@SuppressWarnings("rawtypes") Class<? extends Protocol> protocolClass, int requiredVersion)
		{
			super("Required version " + requiredVersion + " not available for class " + protocolClass.getName(), protocolClass);
			this.requiredVersion = requiredVersion;
		}

		public int getRequiredVersion()
		{
			return requiredVersion;
		}

	}

	public Protocol(int requiredVersion)
	{
		checkVersionAvailability(Protocol.class, requiredVersion);
	}

	protected static void checkVersionAvailability(@SuppressWarnings("rawtypes") Class<? extends Protocol> protocolClass, int requiredVersion)
			throws MissingProtocolInfoException, VersionNotAvailableException
	{
		if (!availableVersionsForClass(protocolClass).contains(requiredVersion))
			throw new VersionNotAvailableException(protocolClass, requiredVersion);
	}

	protected static Collection<Integer> availableVersionsForClass(@SuppressWarnings("rawtypes") Class<? extends Protocol> protocolClass)
			throws MissingProtocolInfoException
			{
		ProtocolInfo protocolInfo = protocolClass.getAnnotation(ProtocolInfo.class);
		if (protocolInfo == null)
			throw new MissingProtocolInfoException(protocolClass);
		return new ArrayAsList<Integer>(protocolInfo.availableVersions());
			}

	/**
	 * Send a object to a {@link DataOutput}. Must be consistent with
	 * {@link #recv(DataInput)}..
	 *
	 * @param out
	 *            The data output to send the data.
	 * @param t
	 *            The object.
	 * @throws IOException
	 */
	public abstract void send(DataOutput out, T t) throws IOException;

	/**
	 * Receives a object from a {@link DataInput}. Must be consistent with
	 * {@link #send(DataOutput, Object)}.
	 *
	 * @param in
	 *            The data input to receive the data from.
	 * @return The object.
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public abstract T recv(DataInput in) throws IOException, ProtocolException;

	/**
	 * Skips the input data corresponding to a object of this protocol type.
	 *
	 * @param in
	 *            The data input to receive the data from.
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public abstract void skip(DataInput in) throws IOException, ProtocolException;

	public final byte[] toByteArray(T t)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try
		{
			try
			{
				send(dos, t);
				dos.close();
				return baos.toByteArray();
			}
			finally
			{
				dos.close();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public final T fromByteArray(byte[] bytes) throws ProtocolException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try
		{
			try
			{
				return recv(dis);
			}
			finally
			{
				dis.close();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
