/*******************************************************************************
 * Copyright (c) 2014, 2015 Quim Testar.
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.UUID;

import aletheia.protocol.Protocol;
import aletheia.security.messagedigester.BufferedMessageDigester;
import aletheia.security.model.MessageDigestData;

public class SecurityUtilities
{
	public final static SecurityUtilities instance = new SecurityUtilities();

	public class NoSuchFormatException extends GeneralSecurityException
	{
		private static final long serialVersionUID = 3745521857620921837L;

		public NoSuchFormatException()
		{
			super();
		}

		public NoSuchFormatException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public NoSuchFormatException(String msg)
		{
			super(msg);
		}

		public NoSuchFormatException(Throwable cause)
		{
			super(cause);
		}

	}

	private SecurityUtilities()
	{
	}

	public UUID messageDigestDataToUUID(MessageDigestData messageDigestData)
	{
		try
		{
			byte[] dig = messageDigestData.getEncoded();
			dig[6] = (byte) ((dig[6] & 0x0f) | 0x50);
			dig[8] = (byte) ((dig[8] & 0x3f) | 0x80);
			DataInputStream dais = new DataInputStream(new ByteArrayInputStream(dig));
			try
			{
				long lh = dais.readLong();
				long ll = dais.readLong();
				return new UUID(lh, ll);
			}
			finally
			{
				dais.close();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public <T> UUID objectToUUID(T object, Protocol<T> protocol)
	{
		try
		{
			BufferedMessageDigester digester = new BufferedMessageDigester("SHA-1");
			protocol.send(digester.dataOutput(), object);
			return messageDigestDataToUUID(digester.digest());
		}
		catch (NoSuchAlgorithmException | IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}
	}

	private Class<? extends EncodedKeySpec> formatToEncodedKeySpecClass(String format) throws NoSuchFormatException
	{
		switch (format)
		{
		case "X.509":
			return X509EncodedKeySpec.class;
		case "PKCS#8":
			return PKCS8EncodedKeySpec.class;
		default:
			throw new NoSuchFormatException("No such format:" + format);
		}
	}

	private EncodedKeySpec instantiateEncodedKeySpec(String format, byte[] encoded) throws NoSuchFormatException
	{
		try
		{
			return formatToEncodedKeySpecClass(format).getConstructor(byte[].class).newInstance(encoded);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e)
		{
			throw new Error(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getCause());
		}
	}

	public PublicKey decodePublicKey(String format, String algorithm, byte[] publicKeyEncoded)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFormatException
	{
		EncodedKeySpec ekeyspec = instantiateEncodedKeySpec(format, publicKeyEncoded);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePublic(ekeyspec);

	}

	public PrivateKey decodePrivateKey(String format, String algorithm, byte[] privateKeyEncoded)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFormatException
	{
		EncodedKeySpec ekeyspec = instantiateEncodedKeySpec(format, privateKeyEncoded);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePrivate(ekeyspec);
	}

	public boolean checkKeyPair(String signatureAlgorithm, PrivateKey privateKey, PublicKey publicKey)
	{
		try
		{
			byte[] bytes = new byte[128];
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.nextBytes(bytes);
			Signature signature = Signature.getInstance(signatureAlgorithm);
			signature.initSign(privateKey);
			signature.update(bytes);
			byte[] sign = signature.sign();
			signature.initVerify(publicKey);
			signature.update(bytes);
			return signature.verify(sign);
		}
		catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}
	}

	/*
	 * Copied from http://stackoverflow.com/questions/1179672/unlimited-strength-jce-policy-files
	 */
	public void removeCryptographyRestrictions()
	{
		if (!isRestrictedCryptography())
		{
			return;
		}
		try
		{
			/*
			 * Do the following, but with reflection to bypass access checks:
			 *
			 * JceSecurity.isRestricted = false;
			 * JceSecurity.defaultPolicy.perms.clear();
			 * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
			 */
			final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
			final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
			final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

			final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
			isRestrictedField.setAccessible(true);
			isRestrictedField.set(null, false);

			final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
			defaultPolicyField.setAccessible(true);
			final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

			final Field perms = cryptoPermissions.getDeclaredField("perms");
			perms.setAccessible(true);
			((Map<?, ?>) perms.get(defaultPolicy)).clear();

			final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
			instance.setAccessible(true);
			defaultPolicy.add((Permission) instance.get(null));

		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static boolean isRestrictedCryptography()
	{
		// This simply matches the Oracle JRE, but not OpenJDK.
		return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}

}
