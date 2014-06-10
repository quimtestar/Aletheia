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

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.model.authority.Person.PersonCreationException;
import aletheia.persistence.PersistenceManager;
import aletheia.persistence.Transaction;
import aletheia.persistence.entities.authority.PrivateSignatoryEntity;
import aletheia.protocol.security.PublicKeyProtocol;
import aletheia.security.signerverifier.BufferedSigner;
import aletheia.security.signerverifier.Signer;
import aletheia.security.utilities.SecurityUtilities;

public abstract class PrivateSignatory extends Signatory
{
	private static final Logger logger = LoggerManager.logger();

	private static final String creationKeyPairGenerationAlgorithm = "RSA";
	private static final String creationSignatureAlgorithm = "SHA1withRSA";

	private static UUID publicKeyToUUID(PublicKey publicKey)
	{
		return SecurityUtilities.instance.objectToUUID(publicKey, new PublicKeyProtocol(0));
	}

	protected PrivateSignatory(PersistenceManager persistenceManager, Class<? extends PrivateSignatoryEntity> entityClass, UUID uuid, PublicKey publicKey,
			String signatureAlgorithm)
	{
		super(persistenceManager, entityClass, uuid, publicKey);
		getEntity().setSignatureAlgorithm(signatureAlgorithm);
	}

	public PrivateSignatory(PersistenceManager persistenceManager, PrivateSignatoryEntity entity)
	{
		super(persistenceManager, entity);
	}

	public static PrivateSignatory create(PersistenceManager persistenceManager, Transaction transaction)
	{
		KeyPair keyPair = generateKeyPair(creationKeyPairGenerationAlgorithm);
		UUID uuid = publicKeyToUUID(keyPair.getPublic());
		try
		{
			return create(persistenceManager, transaction, uuid, creationSignatureAlgorithm, keyPair.getPublic(), keyPair.getPrivate());
		}
		catch (KeysDontMatchException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static PrivateSignatory create(PersistenceManager persistenceManager, Transaction transaction, UUID uuid, String signatureAlgorithm,
			PublicKey publicKey, PrivateKey privateKey) throws KeysDontMatchException
	{
		SecretKey secretKey = persistenceManager.getSecretKeyManager().getSecretKey();
		PrivateSignatory privateSignatory;
		if (secretKey == null)
			privateSignatory = new PlainPrivateSignatory(persistenceManager, uuid, publicKey, signatureAlgorithm, privateKey);
		else
			privateSignatory = new EncryptedPrivateSignatory(persistenceManager, uuid, publicKey, signatureAlgorithm, secretKey, privateKey);
		privateSignatory.persistenceUpdate(transaction);
		return privateSignatory;
	}

	public static class KeysDontMatchException extends Exception
	{
		private static final long serialVersionUID = 2995690025602840077L;

		public KeysDontMatchException()
		{
			super();
		}

		public KeysDontMatchException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public KeysDontMatchException(String message)
		{
			super(message);
		}

		public KeysDontMatchException(Throwable cause)
		{
			super(cause);
		}

	}

	@Override
	public PrivateSignatoryEntity getEntity()
	{
		return (PrivateSignatoryEntity) super.getEntity();
	}

	public String getSignatureAlgorithm()
	{
		return getEntity().getSignatureAlgorithm();
	}

	public abstract PrivateKey getPrivateKey();

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getSignatureAlgorithm().hashCode();
		result = prime * result + getPrivateKey().hashCode();
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
		PrivateSignatory other = (PrivateSignatory) obj;
		if (getSignatureAlgorithm() != other.getSignatureAlgorithm())
			return false;
		if (getPrivateKey() != other.getPrivateKey())
			return false;
		return true;
	}

	private static KeyPair generateKeyPair(String keyGenerationAlgorithm)
	{
		try
		{
			return KeyPairGenerator.getInstance(keyGenerationAlgorithm).generateKeyPair();
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Signer signer()
	{
		try
		{
			return new BufferedSigner(getSignatureAlgorithm(), getPrivateKey());
		}
		catch (InvalidKeyException | NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + "*";
	}

	@Override
	protected void persistenceUpdate(Transaction transaction)
	{
		super.persistenceUpdate(transaction);
		Person person = getPersistenceManager().getPerson(transaction, getUuid());
		if (person != null)
			try
			{
				person.toPrivatePerson(transaction, this);
			}
			catch (PersonCreationException e)
			{
				logger.warn("Updating private signatory", e);
			}
	}

	public EncryptedPrivateSignatory encrypt(Transaction transaction, SecretKey secretKey)
	{
		EncryptedPrivateSignatory encryptedPrivateSignatory;
		try
		{
			encryptedPrivateSignatory = new EncryptedPrivateSignatory(getPersistenceManager(), getUuid(), getPublicKey(), getSignatureAlgorithm(), secretKey,
					getPrivateKey());
		}
		catch (KeysDontMatchException e)
		{
			throw new RuntimeException(e);
		}
		encryptedPrivateSignatory.persistenceUpdate(transaction);
		return encryptedPrivateSignatory;
	}

	public PlainPrivateSignatory decrypt(Transaction transaction)
	{
		PlainPrivateSignatory plainPrivateSignatory;
		try
		{
			plainPrivateSignatory = new PlainPrivateSignatory(getPersistenceManager(), getUuid(), getPublicKey(), getSignatureAlgorithm(), getPrivateKey());
		}
		catch (KeysDontMatchException e)
		{
			throw new RuntimeException(e);
		}
		plainPrivateSignatory.persistenceUpdate(transaction);
		return plainPrivateSignatory;
	}

}
