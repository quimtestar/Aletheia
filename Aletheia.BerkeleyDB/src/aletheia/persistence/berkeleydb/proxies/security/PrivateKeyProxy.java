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
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import com.sleepycat.persist.model.Persistent;

import aletheia.persistence.berkeleydb.entities.PrivateKeyCapsule;
import aletheia.security.utilities.SecurityUtilities;
import aletheia.security.utilities.SecurityUtilities.NoSuchFormatException;

@Persistent(version = 0, proxyFor = PrivateKeyCapsule.class)
public class PrivateKeyProxy extends KeyProxy<PrivateKey>
{

	@Override
	public PrivateKeyCapsule convertProxy()
	{
		try
		{
			return new PrivateKeyCapsule(SecurityUtilities.instance.decodePrivateKey(format, algorithm, encoded));
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchFormatException e)
		{
			throw new RuntimeException(e);
		}
	}

}
