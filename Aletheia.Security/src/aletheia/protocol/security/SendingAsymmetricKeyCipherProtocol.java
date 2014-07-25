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
package aletheia.protocol.security;

import java.io.DataInput;
import java.io.IOException;
import java.security.PublicKey;

import aletheia.protocol.Protocol;
import aletheia.protocol.ProtocolException;
import aletheia.protocol.ProtocolInfo;

@ProtocolInfo(availableVersions = 0)
public class SendingAsymmetricKeyCipherProtocol<T> extends AsymmetricKeyCipherProtocol<T>
{

	public SendingAsymmetricKeyCipherProtocol(int requiredVersion, String algorithm, String secretKeyAlgorithm, String symmetricAlgorithm, PublicKey publicKey,
			Protocol<T> inner)
	{
		super(0, algorithm, secretKeyAlgorithm, symmetricAlgorithm, publicKey, inner);
		checkVersionAvailability(SendingAsymmetricKeyCipherProtocol.class, requiredVersion);
	}

	@Override
	public PublicKey getKey()
	{
		return (PublicKey) super.getKey();
	}

	@Override
	protected T recv(String algorithm, DataInput in) throws IOException, ProtocolException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void skip(DataInput in) throws IOException, ProtocolException
	{
		throw new UnsupportedOperationException();
	}

}
