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
package aletheia.security.signerverifier;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import aletheia.model.security.SignatureData;

public class BufferedSigner extends BufferedSignerOrVerifier implements Signer
{
	public BufferedSigner(String algorithm, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException
	{
		super(algorithm);
		getSignature().initSign(privateKey);
	}

	@Override
	public SignatureData sign() throws InvalidKeyException
	{
		try
		{
			Signature signature = getSignature();
			outputStream().close();
			signature.update(outputStream().toByteArray());
			return new SignatureData(getAlgorithm(), signature.sign());
		}
		catch (IOException | SignatureException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{

		}
	}

}
