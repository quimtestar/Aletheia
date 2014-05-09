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
package aletheia.security.utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import aletheia.protocol.ProtocolException;
import aletheia.protocol.primitive.ByteArrayProtocol;

public class PassphraseEncryptedStreamer
{
	private final static String secretKeyAlgorithm = "PBKDF2WithHmacSHA1";
	private final static String secureRandomAlgorithm = "SHA1PRNG";
	private final static String cipherAlgorithm = "AES/CFB8/NoPadding";
	private final static String secretKeySpecAlgorithm = "AES";
	private final static String messageDigestAlgorithm = "SHA1";
	private final static int iterationCount = 1 << 12;
	private final static int keyLength = 256;
	private final static int testLength = 16;

	public class BadPassphraseException extends Exception
	{
		private static final long serialVersionUID = 1443770094738528868L;

		public BadPassphraseException()
		{
			super("Wrong passphrase");
		}

		public BadPassphraseException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public BadPassphraseException(String message)
		{
			super(message);
		}

		public BadPassphraseException(Throwable cause)
		{
			super(cause);
		}

	}

	private final ByteArrayProtocol byteArrayProtocol;
	private final SecretKeyFactory secretKeyFactory;
	private final SecureRandom secureRandom;

	public abstract class PassphraseEncryptedStreamerException extends Exception
	{
		private static final long serialVersionUID = 8262606775386595322L;

		protected PassphraseEncryptedStreamerException()
		{
			super();
		}

		protected PassphraseEncryptedStreamerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		protected PassphraseEncryptedStreamerException(String message)
		{
			super(message);
		}

		protected PassphraseEncryptedStreamerException(Throwable cause)
		{
			super(cause);
		}

	}

	public class VersionException extends PassphraseEncryptedStreamerException
	{
		private static final long serialVersionUID = -4815096882622954150L;

		protected VersionException(int version)
		{
			super("Passphrase encrypted streamer version " + version + " not supported :(");
		}

	}

	public PassphraseEncryptedStreamer(int version) throws PassphraseEncryptedStreamerException
	{
		if (version != 0)
			throw new VersionException(version);
		try
		{
			this.byteArrayProtocol = new ByteArrayProtocol(0);
			this.secretKeyFactory = SecretKeyFactory.getInstance(secretKeyAlgorithm);
			this.secureRandom = SecureRandom.getInstance(secureRandomAlgorithm);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	private SecretKey generateSecretKey(char[] passphrase, byte[] salt) throws InvalidKeySpecException
	{
		KeySpec spec = new PBEKeySpec(passphrase, salt, iterationCount, keyLength);
		SecretKey tmp = secretKeyFactory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), secretKeySpecAlgorithm);
	}

	public CipherOutputStream outputStream(char[] passphrase, OutputStream out) throws IOException
	{
		try
		{
			DataOutputStream dout = new DataOutputStream(out);
			byte[] salt = new byte[8];
			secureRandom.nextBytes(salt);
			byteArrayProtocol.send(dout, salt);
			SecretKey secretKey = generateSecretKey(passphrase, salt);
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			AlgorithmParameters params = cipher.getParameters();
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			byteArrayProtocol.send(dout, iv);
			CipherOutputStream cout = new CipherOutputStream(out, cipher);
			byte[] testBytes = new byte[testLength];
			secureRandom.nextBytes(testBytes);
			cout.write(testBytes);
			MessageDigest messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
			byte[] digest = messageDigest.digest(testBytes);
			cout.write(digest);
			return cout;
		}
		catch (InvalidKeyException | InvalidParameterSpecException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}
	}

	public InputStream inputStream(char[] passphrase, InputStream in) throws IOException, ProtocolException, BadPassphraseException
	{
		try
		{
			DataInputStream din = new DataInputStream(in);
			byte[] salt = byteArrayProtocol.recv(din);
			SecretKey secretKey = generateSecretKey(passphrase, salt);
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			byte[] iv = byteArrayProtocol.recv(din);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			CipherInputStream cin = new CipherInputStream(in, cipher);
			byte[] testBytes = new byte[testLength];
			cin.read(testBytes);
			MessageDigest messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
			byte[] digest = messageDigest.digest(testBytes);
			byte[] digest_ = new byte[digest.length];
			cin.read(digest_);
			if (!Arrays.equals(digest, digest_))
				throw new BadPassphraseException();
			return cin;
		}
		catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}

	}

}
