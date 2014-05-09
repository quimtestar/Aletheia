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
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;
import aletheia.protocol.primitive.ByteArrayProtocol;
import aletheia.protocol.primitive.StringProtocol;

@ProtocolInfo(availableVersions = 0)
public abstract class AsymmetricKeyCipherProtocol<T> extends CipherProtocol<T>
{
	private final static String secureRandomAlgorithm = "SHA1PRNG";

	private final String secretKeyAlgorithm;
	private final String symmetricAlgorithm;
	private final StringProtocol stringProtocol;
	private final ByteArrayProtocol byteArrayProtocol;

	public AsymmetricKeyCipherProtocol(int requiredVersion, String algorithm, String secretKeyAlgorithm, String symmetricAlgorithm, Key key, Protocol<T> inner)
	{
		super(0, algorithm, key, inner);
		checkVersionAvailability(AsymmetricKeyCipherProtocol.class, requiredVersion);
		this.secretKeyAlgorithm = secretKeyAlgorithm;
		this.symmetricAlgorithm = symmetricAlgorithm;
		this.stringProtocol = new StringProtocol(0);
		this.byteArrayProtocol = new ByteArrayProtocol(0);
	}

	public String getSecretKeyAlgorithm()
	{
		return secretKeyAlgorithm;
	}

	public String getSymmetricAlgorithm()
	{
		return symmetricAlgorithm;
	}

	@Override
	public void send(DataOutput out, T t) throws IOException
	{
		super.send(out, t);
		try
		{
			SecureRandom secureRandom = SecureRandom.getInstance(secureRandomAlgorithm);
			stringProtocol.send(out, secretKeyAlgorithm);
			KeyGenerator keyGenerator = KeyGenerator.getInstance(secretKeyAlgorithm);
			keyGenerator.init(secureRandom);
			SecretKey secretKey = keyGenerator.generateKey();
			Cipher rsaCipher = Cipher.getInstance(getAlgorithm());
			rsaCipher.init(Cipher.WRAP_MODE, getKey());
			byte[] wrappedKey = rsaCipher.wrap(secretKey);
			byteArrayProtocol.send(out, wrappedKey);
			SymmetricKeyCipherProtocol<T> symmetricKeyCipherProtocol = new SymmetricKeyCipherProtocol<>(0, symmetricAlgorithm, secretKey, getInner());
			symmetricKeyCipherProtocol.send(out, t);
		}
		catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected T recv(String algorithm, DataInput in) throws IOException, ProtocolException
	{
		try
		{
			String secretKeyAlgorithm = stringProtocol.recv(in);
			Cipher rsaCipher = Cipher.getInstance(algorithm);
			rsaCipher.init(Cipher.UNWRAP_MODE, getKey());
			byte[] wrappedKey = byteArrayProtocol.recv(in);
			SecretKey secretKey = (SecretKey) rsaCipher.unwrap(wrappedKey, secretKeyAlgorithm, Cipher.SECRET_KEY);
			SymmetricKeyCipherProtocol<T> symmetricKeyCipherProtocol = new SymmetricKeyCipherProtocol<>(0, null, secretKey, getInner());
			return symmetricKeyCipherProtocol.recv(in);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e)
		{
			throw new ProtocolException(e);
		}
		finally
		{

		}

	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		super.skip(in);
		stringProtocol.skip(in);
		byteArrayProtocol.skip(in);
		SymmetricKeyCipherProtocol<T> symmetricKeyCipherProtocol = new SymmetricKeyCipherProtocol<>(0, null, null, getInner());
		symmetricKeyCipherProtocol.skip(in);
	}

}
