/*******************************************************************************
 * Copyright (c) 2014, 2020 Quim Testar.
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
package aletheia.persistence.berkeleydb.proxies.security;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import com.sleepycat.persist.model.Persistent;

import aletheia.persistence.berkeleydb.entities.PublicKeyCapsule;
import aletheia.security.utilities.SecurityUtilities;
import aletheia.security.utilities.SecurityUtilities.NoSuchFormatException;

@Persistent(version = 0, proxyFor = PublicKeyCapsule.class)
public class PublicKeyProxy extends KeyProxy<PublicKey>
{

	@Override
	public PublicKeyCapsule convertProxy()
	{
		try
		{
			return new PublicKeyCapsule(SecurityUtilities.instance.decodePublicKey(format, algorithm, encoded));
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchFormatException e)
		{
			throw new RuntimeException(e);
		}
	}

}
