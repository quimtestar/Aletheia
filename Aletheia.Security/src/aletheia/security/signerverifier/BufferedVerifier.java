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
package aletheia.security.signerverifier;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import aletheia.security.model.SignatureData;

public class BufferedVerifier extends BufferedSignerOrVerifier implements Verifier
{

	private final SignatureData signatureData;

	public BufferedVerifier(PublicKey publicKey, SignatureData signatureData) throws NoSuchAlgorithmException, InvalidKeyException
	{
		super(signatureData.getAlgorithm());
		getSignature().initVerify(publicKey);
		this.signatureData = signatureData;
	}

	@Override
	public boolean verify()
	{
		try
		{
			Signature signature = getSignature();
			outputStream().close();
			signature.update(outputStream().toByteArray());
			return signature.verify(signatureData.getEncoded());
		}
		catch (IOException | SignatureException e)
		{
			throw new RuntimeException(e);
		}
	}

}
