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
package aletheia.model.authority;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import javax.crypto.SecretKey;

import aletheia.persistence.PersistenceManager;
import aletheia.persistence.entities.authority.EncryptedPrivateSignatoryEntity;
import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.security.protocol.AESCipherProtocol;
import aletheia.security.protocol.PrivateKeyProtocol;
import aletheia.security.utilities.SecurityUtilities;

public class EncryptedPrivateSignatory extends PrivateSignatory
{
	private final static int encryptingVersion = 0;

	@ProtocolInfo(availableVersions = 0)
	private static class EncryptedPrivateKeyProtocol extends Protocol<PrivateKey>
	{
		private final PrivateKeyProtocol privateKeyProtocol;
		private final AESCipherProtocol<PrivateKey> aesCipherProtocol;

		public EncryptedPrivateKeyProtocol(int requiredVersion, SecretKey secretKey)
		{
			super(0);
			checkVersionAvailability(EncryptedPrivateKeyProtocol.class, requiredVersion);
			this.privateKeyProtocol = new PrivateKeyProtocol(0);
			this.aesCipherProtocol = new AESCipherProtocol<>(0, secretKey, privateKeyProtocol);
		}

		@Override
		public void send(DataOutput out, PrivateKey privateKey) throws IOException
		{
			aesCipherProtocol.send(out, privateKey);
		}

		@Override
		public PrivateKey recv(DataInput in) throws IOException, ProtocolException
		{
			return aesCipherProtocol.recv(in);
		}

		@Override
		public void skip(DataInput in) throws IOException, ProtocolException
		{
			aesCipherProtocol.skip(in);
		}

	}

	public static class EncryptedException extends Exception
	{
		private static final long serialVersionUID = 805354684648375184L;

		protected EncryptedException()
		{
			super();
		}

		protected EncryptedException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected EncryptedException(String message)
		{
			super(message);
		}

		protected EncryptedException(Throwable cause)
		{
			super(cause);
		}

	}

	public static class VersionException extends EncryptedException
	{
		private static final long serialVersionUID = -2533437156435600785L;

		protected VersionException()
		{
			super();
		}

	}

	private static byte[] encryptPrivateKey(int version, SecretKey secretKey, PrivateKey privateKey) throws VersionException
	{
		if (version != 0)
			throw new VersionException();
		EncryptedPrivateKeyProtocol encryptedPrivateKeyProtocol = new EncryptedPrivateKeyProtocol(0, secretKey);
		return encryptedPrivateKeyProtocol.toByteArray(privateKey);
	}

	private static PrivateKey decryptPrivateKey(int version, SecretKey secretKey, byte[] bytes) throws EncryptedException
	{
		if (version != 0)
			throw new VersionException();
		EncryptedPrivateKeyProtocol encryptedPrivateKeyProtocol = new EncryptedPrivateKeyProtocol(0, secretKey);
		try
		{
			return encryptedPrivateKeyProtocol.fromByteArray(bytes);
		}
		catch (ProtocolException e)
		{
			throw new EncryptedException(e);
		}

	}

	private final PrivateKey privateKey;

	protected EncryptedPrivateSignatory(PersistenceManager persistenceManager, UUID uuid, PublicKey publicKey, String signatureAlgorithm, SecretKey secretKey,
			PrivateKey privateKey) throws KeysDontMatchException
	{
		super(persistenceManager, EncryptedPrivateSignatoryEntity.class, uuid, publicKey, signatureAlgorithm);
		if (!SecurityUtilities.instance.checkKeyPair(signatureAlgorithm, privateKey, publicKey))
			throw new KeysDontMatchException();
		getEntity().setVersion(0);
		try
		{
			getEntity().setBytes(encryptPrivateKey(encryptingVersion, secretKey, privateKey));
		}
		catch (VersionException e)
		{
			throw new RuntimeException(e);
		}
		this.privateKey = privateKey;
	}

	public class NoSecretEncryptedException extends EncryptedException
	{
		private static final long serialVersionUID = -7032586252424960493L;

		protected NoSecretEncryptedException()
		{
			super();
		}
	}

	public EncryptedPrivateSignatory(PersistenceManager persistenceManager, EncryptedPrivateSignatoryEntity entity) throws EncryptedException
	{
		super(persistenceManager, entity);
		SecretKey secretKey = persistenceManager.getSecretKeyManager().getSecretKey();
		if (secretKey == null)
			throw new NoSecretEncryptedException();
		this.privateKey = decryptPrivateKey(getVersion(), secretKey, getBytes());
	}

	@Override
	public EncryptedPrivateSignatoryEntity getEntity()
	{
		return (EncryptedPrivateSignatoryEntity) super.getEntity();
	}

	private int getVersion()
	{
		return getEntity().getVersion();
	}

	private byte[] getBytes()
	{
		return getEntity().getBytes();
	}

	@Override
	public PrivateKey getPrivateKey()
	{
		return privateKey;
	}

}
