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
package aletheia.security.model;

import java.util.Arrays;
import java.util.UUID;

import aletheia.security.protocol.SignatureDataProtocol;
import aletheia.security.utilities.SecurityUtilities;
import aletheia.utilities.MiscUtilities;

public class SignatureData
{
	private final String algorithm;
	private final byte[] encoded;

	public SignatureData(String algorithm, byte[] encoded)
	{
		super();
		this.algorithm = algorithm;
		this.encoded = encoded.clone();
	}

	public String getAlgorithm()
	{
		return algorithm;
	}

	public byte[] getEncoded()
	{
		return encoded.clone();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + Arrays.hashCode(encoded);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignatureData other = (SignatureData) obj;
		if (algorithm == null)
		{
			if (other.algorithm != null)
				return false;
		}
		else if (!algorithm.equals(other.algorithm))
			return false;
		if (!Arrays.equals(encoded, other.encoded))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "SignatureData [" + algorithm + "]: "
				+ (encoded.length <= 20 ? MiscUtilities.toHexString(encoded) : MiscUtilities.toHexString(Arrays.copyOf(encoded, 20)) + "...");
	}

	public UUID uuid()
	{
		return SecurityUtilities.instance.objectToUUID(this, new SignatureDataProtocol(0));
	}
}