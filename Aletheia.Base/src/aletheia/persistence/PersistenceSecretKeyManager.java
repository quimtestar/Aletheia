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
package aletheia.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import aletheia.model.authority.Person;
import aletheia.model.authority.Person.PersonCreationException;
import aletheia.model.authority.PrivateSignatory;
import aletheia.model.authority.Signatory;
import aletheia.model.misc.PersistenceSecretKeySingleton;
import aletheia.model.misc.PersistenceSecretKeySingleton.PersistenceSecretKeySingletonCollisionException;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.security.AESCipherProtocol;

public class PersistenceSecretKeyManager
{
	private final static String secretKeyAlgorithm = "PBKDF2WithHmacSHA1";
	private final static String secureRandomAlgorithm = "SHA1PRNG";
	private final static String secretKeySpecAlgorithm = "AES";
	private final static String messageDigestAlgorithm = "SHA1";
	private final static int iterationCount = 1 << 12;
	private final static int keyLength = 128;
	private final static int testLength = 16;
	private final static int generatedVerificationVersion = 0;

	private final PersistenceManager persistenceManager;
	private final SecretKeyFactory secretKeyFactory;
	private final SecureRandom secureRandom;

	public interface Listener
	{
		public void secretSet();

		public void secretUnset();

		public void keySet();

		public void keyUnset();
	}

	private final Collection<Listener> listeners;

	private class PersistenceSecretKeySingletonStateListener implements PersistenceSecretKeySingleton.StateListener
	{

		@Override
		public void persistenceSecretKeySingletonInserted(Transaction transaction, PersistenceSecretKeySingleton persistenceSecretKeySingleton)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					synchronized (PersistenceSecretKeyManager.this)
					{
						for (Listener l : listeners)
							l.secretSet();
					}
				}
			});
		}

		@Override
		public void persistenceSecretKeySingletonDeleted(Transaction transaction, PersistenceSecretKeySingleton persistenceSecretKeySingleton)
		{
			transaction.runWhenCommit(new Transaction.Hook()
			{

				@Override
				public void run(Transaction closedTransaction)
				{
					synchronized (PersistenceSecretKeyManager.this)
					{
						for (Listener l : listeners)
							l.secretUnset();
					}
				}
			});
		}
	}

	private final PersistenceSecretKeySingletonStateListener persistenceSecretKeySingletonStateListener;

	private SecretKey secretKey;

	public PersistenceSecretKeyManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
		try
		{
			this.secretKeyFactory = SecretKeyFactory.getInstance(secretKeyAlgorithm);
			this.secureRandom = SecureRandom.getInstance(secureRandomAlgorithm);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		this.listeners = new LinkedList<>();
		this.persistenceSecretKeySingletonStateListener = new PersistenceSecretKeySingletonStateListener();
		persistenceManager.getListenerManager().getPersistenceSecretKeySingletonStateListeners().add(persistenceSecretKeySingletonStateListener);
		this.secretKey = null;
	}

	public void close()
	{
		persistenceManager.getListenerManager().getPersistenceSecretKeySingletonStateListeners().remove(persistenceSecretKeySingletonStateListener);
	}

	public synchronized void addListener(Listener l)
	{
		listeners.add(l);
	}

	public synchronized void removeListener(Listener l)
	{
		listeners.remove(l);
	}

	public synchronized SecretKey getSecretKey()
	{
		return secretKey;
	}

	private synchronized void setSecretKey(SecretKey secretKey)
	{
		this.secretKey = secretKey;
		if (secretKey != null)
			for (Listener l : listeners)
				l.keySet();
		else
			for (Listener l : listeners)
				l.keyUnset();
	}

	public class PersistenceSecretKeyException extends Exception
	{
		private static final long serialVersionUID = -8780194251208478076L;

		protected PersistenceSecretKeyException()
		{
			super();
		}

		protected PersistenceSecretKeyException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected PersistenceSecretKeyException(String message)
		{
			super(message);
		}

		protected PersistenceSecretKeyException(Throwable cause)
		{
			super(cause);
		}
	}

	private SecretKey generateSecretKey(char[] passphrase, byte[] salt)
	{
		KeySpec spec = new PBEKeySpec(passphrase, salt, iterationCount, keyLength);
		SecretKey tmp;
		try
		{
			tmp = secretKeyFactory.generateSecret(spec);
		}
		catch (InvalidKeySpecException e)
		{
			throw new RuntimeException(e);
		}
		return new SecretKeySpec(tmp.getEncoded(), secretKeySpecAlgorithm);
	}

	public class SecretNotSetPersistenceSecretKeyException extends PersistenceSecretKeyException
	{
		private static final long serialVersionUID = -2103648247116857085L;

		protected SecretNotSetPersistenceSecretKeyException()
		{
			super("Secret not set");
		}
	}

	public class VersionPersistenceSecretKeyException extends PersistenceSecretKeyException
	{
		private static final long serialVersionUID = 2753834274201604044L;

		protected VersionPersistenceSecretKeyException()
		{
			super();
		}
	}

	public class BadPassphrasePersistenceSecretKeyException extends PersistenceSecretKeyException
	{
		private static final long serialVersionUID = 8806130195634941822L;

		protected BadPassphrasePersistenceSecretKeyException()
		{
			super("Bad passphrase");
		}
	}

	public synchronized void enterPassphrase(char[] passphrase) throws PersistenceSecretKeyException
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			PersistenceSecretKeySingleton persistenceSecretKeySingleton = PersistenceSecretKeySingleton.lock(persistenceManager, transaction);
			if (persistenceSecretKeySingleton == null)
				throw new SecretNotSetPersistenceSecretKeyException();
			SecretKey secretKey_ = generateSecretKey(passphrase, persistenceSecretKeySingleton.getSalt());
			if (persistenceSecretKeySingleton.getVerificationVersion() != 0)
				throw new VersionPersistenceSecretKeyException();
			if (!verify(secretKey_, persistenceSecretKeySingleton.getVerification()))
				throw new BadPassphrasePersistenceSecretKeyException();
			setSecretKey(secretKey_);
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}

	}

	public synchronized void clearPassphrase()
	{
		setSecretKey(null);
	}

	private byte[] generateSalt()
	{
		byte[] salt = new byte[8];
		secureRandom.nextBytes(salt);
		return salt;
	}

	private AESCipherProtocol<byte[]> createAESCipherProtocol(SecretKey secretKey)
	{
		return new AESCipherProtocol<>(0, secretKey, new ByteArrayProtocol(0));
	}

	private byte[] generateVerification(SecretKey secretKey)
	{
		try
		{
			AESCipherProtocol<byte[]> aesCipherProtocol = createAESCipherProtocol(secretKey);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			byte[] testBytes = new byte[testLength];
			secureRandom.nextBytes(testBytes);
			aesCipherProtocol.send(dos, testBytes);
			MessageDigest messageDigest;
			try
			{
				messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
			byte[] digest = messageDigest.digest(testBytes);
			aesCipherProtocol.send(dos, digest);
			dos.close();
			baos.close();
			return baos.toByteArray();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private boolean verify(SecretKey secretKey, byte[] verification)
	{
		byte[] testBytes;
		byte[] digest;
		try
		{
			AESCipherProtocol<byte[]> aesCipherProtocol = createAESCipherProtocol(secretKey);
			ByteArrayInputStream bais = new ByteArrayInputStream(verification);
			DataInputStream dis = new DataInputStream(bais);
			testBytes = aesCipherProtocol.recv(dis);
			digest = aesCipherProtocol.recv(dis);
			dis.close();
			bais.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (ProtocolException e)
		{
			return false;
		}
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		byte[] computedDigest = messageDigest.digest(testBytes);
		return Arrays.equals(digest, computedDigest);
	}

	public class KeyNotSetPersistenceSecretKeyException extends PersistenceSecretKeyException
	{

		private static final long serialVersionUID = 5660558353579783001L;

		protected KeyNotSetPersistenceSecretKeyException()
		{
			super("Passphrase not entered.");
		}
	}

	public synchronized void changePassphrase(char[] passphrase) throws PersistenceSecretKeyException
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			PersistenceSecretKeySingleton persistenceSecretKeySingleton = PersistenceSecretKeySingleton.lock(persistenceManager, transaction);
			if (persistenceSecretKeySingleton != null)
			{
				if (getSecretKey() == null)
					throw new KeyNotSetPersistenceSecretKeyException();
				persistenceSecretKeySingleton.delete(transaction);
			}
			byte[] salt = generateSalt();
			SecretKey newSecretKey = generateSecretKey(passphrase, salt);
			byte[] verification = generateVerification(newSecretKey);
			try
			{
				persistenceSecretKeySingleton = PersistenceSecretKeySingleton.create(persistenceManager, transaction, salt, generatedVerificationVersion,
						verification);
			}
			catch (PersistenceSecretKeySingletonCollisionException e)
			{
				throw new RuntimeException(e);
			}
			for (PrivateSignatory privateSignatory : persistenceManager.privateSignatories(transaction).values())
				privateSignatory.encrypt(transaction, newSecretKey);
			transaction.commit();
			setSecretKey(newSecretKey);
		}
		finally
		{
			transaction.abort();
		}
	}

	public synchronized void deletePassphrase() throws PersistenceSecretKeyException
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			PersistenceSecretKeySingleton persistenceSecretKeySingleton = PersistenceSecretKeySingleton.lock(persistenceManager, transaction);
			if (persistenceSecretKeySingleton != null)
			{
				if (getSecretKey() == null)
					throw new KeyNotSetPersistenceSecretKeyException();
				persistenceSecretKeySingleton.delete(transaction);
			}
			for (PrivateSignatory privateSignatory : persistenceManager.privateSignatories(transaction).values())
				privateSignatory.decrypt(transaction);
			transaction.commit();
			setSecretKey(null);
		}
		finally
		{
			transaction.abort();
		}
	}

	public boolean isSecretSet()
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			PersistenceSecretKeySingleton persistenceSecretKeySingleton = persistenceManager.getPersistenceSecretKeySingleton(transaction);
			transaction.commit();
			return persistenceSecretKeySingleton != null;
		}
		finally
		{
			transaction.abort();
		}
	}

	public synchronized void resetPassphrase() throws PersistenceSecretKeyException
	{
		Transaction transaction = persistenceManager.beginTransaction();
		try
		{
			setSecretKey(null);
			PersistenceSecretKeySingleton persistenceSecretKeySingleton = PersistenceSecretKeySingleton.lock(persistenceManager, transaction);
			if (persistenceSecretKeySingleton != null)
				persistenceSecretKeySingleton.delete(transaction);
			for (Signatory signatory : persistenceManager.privateSignatories(transaction).values())
			{
				signatory = Signatory.create(persistenceManager, transaction, signatory.getUuid(), signatory.getPublicKey());
				Person person = persistenceManager.getPrivatePerson(transaction, signatory.getUuid());
				if (person != null)
					try
					{
						person = Person.create(persistenceManager, transaction, signatory, person.getNick(), person.getName(), person.getEmail(),
								person.getSignatureDate(), person.getSignatureVersion(), person.getSignatureData());
					}
					catch (PersonCreationException e)
					{
						throw new PersistenceSecretKeyException(e);
					}
			}
			transaction.commit();
		}
		finally
		{
			transaction.abort();
		}
	}

}
