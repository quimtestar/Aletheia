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
package aletheia.security.messagedigester;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import aletheia.security.model.MessageDigestData;

public abstract class AbstractMessageDigester implements MessageDigester
{
	private final String algorithm;
	private final MessageDigest messageDigest;

	public AbstractMessageDigester(String algorithm) throws NoSuchAlgorithmException
	{
		this.algorithm = algorithm;
		this.messageDigest = MessageDigest.getInstance(algorithm);
	}

	protected MessageDigest getMessageDigest()
	{
		return messageDigest;
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

	@Override
	public abstract MessageDigestData digest();

}
