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
package aletheia.security.protocol;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import aletheia.protocol.ProtocolInfo;
import aletheia.security.utilities.SecurityUtilities;
import aletheia.security.utilities.SecurityUtilities.NoSuchFormatException;

@ProtocolInfo(availableVersions = 0)
public class PrivateKeyProtocol extends KeyProtocol<PrivateKey>
{
	public PrivateKeyProtocol(int requiredVersion)
	{
		super(0);
		checkVersionAvailability(PrivateKeyProtocol.class, requiredVersion);
	}

	@Override
	protected PrivateKey decode(String format, String algorithm, byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFormatException
	{
		return SecurityUtilities.instance.decodePrivateKey(format, algorithm, encoded);
	}

}
