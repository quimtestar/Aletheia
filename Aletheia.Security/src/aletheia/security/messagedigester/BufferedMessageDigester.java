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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import aletheia.security.model.MessageDigestData;

public class BufferedMessageDigester extends AbstractMessageDigester
{
	private final ByteArrayOutputStream byteArrayOutputStream;

	public BufferedMessageDigester(String algorithm) throws NoSuchAlgorithmException
	{
		super(algorithm);
		this.byteArrayOutputStream = new ByteArrayOutputStream();
	}

	@Override
	protected ByteArrayOutputStream outputStream()
	{
		return byteArrayOutputStream;
	}

	@Override
	public MessageDigestData digest()
	{
		try
		{
			MessageDigest messageDigest = getMessageDigest();
			outputStream().close();
			messageDigest.update(outputStream().toByteArray());
			return new MessageDigestData(getAlgorithm(), messageDigest.digest());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
