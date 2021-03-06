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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

public abstract class AbstractSignerOrVerifier implements SignerOrVerifier
{
	private final String algorithm;
	private final Signature signature;

	public AbstractSignerOrVerifier(String algorithm) throws NoSuchAlgorithmException
	{
		this.algorithm = algorithm;
		this.signature = Signature.getInstance(algorithm);
	}

	protected Signature getSignature()
	{
		return signature;
	}

	@Override
	public String getAlgorithm()
	{
		return algorithm;
	}

	protected abstract OutputStream outputStream();

	@Override
	public DataOutput dataOutput()
	{
		return new DataOutputStream(outputStream());
	}

}
